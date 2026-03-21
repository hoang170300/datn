package cosplayshop.service;

import cosplayshop.dto.request.OrderRequest;
import cosplayshop.service.NotificationService;
import cosplayshop.dto.response.*;
import cosplayshop.entity.*;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.*;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final NotificationService notificationService;

    // ── Tạo đơn hàng từ giỏ hàng ─────────────────────
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        User user = userRepository.findById(userId).orElseThrow();

        // Tính subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            ProductVariant v = item.getProductVariant();
            BigDecimal unitPrice;
            int days = 1;

            if (item.getOrderType() == CartItem.OrderType.RENTAL) {
                unitPrice = v.getProduct().getRentalPricePerDay();
                if (item.getRentalStartDate() != null && item.getRentalEndDate() != null) {
                    days = (int) ChronoUnit.DAYS.between(
                            item.getRentalStartDate(), item.getRentalEndDate());
                    if (days <= 0) days = 1;
                }
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf((long) item.getQuantity() * days)));
            } else {
                unitPrice = v.getPrice() != null ? v.getPrice() : v.getProduct().getSalePrice();
                subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        // Áp dụng voucher
        BigDecimal discount = BigDecimal.ZERO;
        Voucher voucher = null;
        if (req.getVoucherCode() != null && !req.getVoucherCode().isBlank()) {
            voucher = voucherRepository.findValidVoucher(req.getVoucherCode(), subtotal)
                    .orElse(null);
            if (voucher != null) {
                if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                    discount = subtotal.multiply(voucher.getDiscountValue())
                            .divide(BigDecimal.valueOf(100));
                    if (voucher.getMaxDiscountAmount() != null) {
                        discount = discount.min(voucher.getMaxDiscountAmount());
                    }
                } else {
                    discount = voucher.getDiscountValue();
                }
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
            }
        }

        // Tính tổng tiền đặt cọc cho các item thuê
        BigDecimal totalDeposit = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getOrderType() == CartItem.OrderType.RENTAL) {
                BigDecimal dep = cartItem.getProductVariant().getProduct().getDepositAmount();
                if (dep != null) totalDeposit = totalDeposit.add(dep.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }
        }

        BigDecimal shippingFee = BigDecimal.valueOf(35000);
        // COD: thanh toán khi nhận - paymentStatus = PENDING
        // BANK/WALLET: đã TT trước - paymentStatus = PENDING (đợi admin xác nhận)
        // finalPrice = tiền hàng + ship - discount + deposit (đặt cọc thuê)
        BigDecimal finalPrice = subtotal.subtract(discount).add(shippingFee).add(totalDeposit)
                .max(BigDecimal.ZERO);

        // Xác định paymentStatus theo phương thức
        String pm = req.getPaymentMethod() != null ? req.getPaymentMethod() : "COD";
        Order.PaymentStatus initPaymentStatus;
        if ("E_WALLET".equals(pm)) {
            // Sandbox đã confirm phía client → đánh dấu PAID ngay
            initPaymentStatus = Order.PaymentStatus.PAID;
        } else {
            // COD, BANK_TRANSFER, BANK_TRANSFER_PREPAID → chờ thu/xác nhận
            initPaymentStatus = Order.PaymentStatus.PENDING;
        }

        // Tạo Order
        String orderNumber = "CS" + System.currentTimeMillis();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(initPaymentStatus)
                .paymentMethod(Order.PaymentMethod.valueOf(pm))
                .subtotalPrice(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .finalPrice(finalPrice)
                .voucher(voucher)
                .shippingAddressStreet(req.getShippingAddressStreet())
                .shippingAddressWard(req.getShippingAddressWard())
                .shippingAddressDistrict(req.getShippingAddressDistrict())
                .shippingAddressProvince(req.getShippingAddressProvince())
                .phone(req.getPhone())
                .note(req.getNote() != null && !totalDeposit.equals(BigDecimal.ZERO)
                        ? (req.getNote() != null ? req.getNote() + " | " : "") + "Tiền cọc thuê: " + totalDeposit.toPlainString() + "đ"
                        : req.getNote())
                .build();

        order = orderRepository.save(order);

        // Tạo OrderItems
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant v = cartItem.getProductVariant();
            Product product = v.getProduct();

            BigDecimal unitPrice;
            int rentalDays = 1;

            if (cartItem.getOrderType() == CartItem.OrderType.RENTAL) {
                unitPrice = product.getRentalPricePerDay();
                if (cartItem.getRentalStartDate() != null && cartItem.getRentalEndDate() != null) {
                    rentalDays = (int) ChronoUnit.DAYS.between(
                            cartItem.getRentalStartDate(), cartItem.getRentalEndDate()) + 1;
                    if (rentalDays < 1) rentalDays = 1;
                }
            } else {
                unitPrice = v.getPrice() != null ? v.getPrice() : product.getSalePrice();
            }

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(
                    cartItem.getOrderType() == CartItem.OrderType.RENTAL
                            ? (long) cartItem.getQuantity() * rentalDays
                            : cartItem.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productVariant(v)
                    .productName(product.getName())    // ← dùng productName, không có sku
                    .itemType(cartItem.getOrderType() == CartItem.OrderType.RENTAL
                            ? OrderItem.ItemType.RENTAL : OrderItem.ItemType.SALE)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .rentalStartDate(cartItem.getRentalStartDate())
                    .rentalEndDate(cartItem.getRentalEndDate())
                    .rentalDays(cartItem.getOrderType() == CartItem.OrderType.RENTAL ? rentalDays : null)
                    .depositAmount(cartItem.getOrderType() == CartItem.OrderType.RENTAL
                            ? product.getDepositAmount() : null)
                    .build();

            order.getItems().add(item);

            // Kiểm tra và giảm tồn kho
            if (cartItem.getOrderType() == CartItem.OrderType.SALE) {
                int newStock = v.getStockQuantity() - cartItem.getQuantity();
                if (newStock < 0)
                    throw new IllegalArgumentException(
                            "Sản phẩm '" + product.getName() + "' (size " + v.getSize() + ") không đủ hàng. Còn lại: " + v.getStockQuantity());
                v.setStockQuantity(newStock);
            } else {
                int newRental = v.getRentalQuantity() - cartItem.getQuantity();
                if (newRental < 0)
                    throw new IllegalArgumentException(
                            "Sản phẩm '" + product.getName() + "' (size " + v.getSize() + ") không đủ số lượng thuê. Còn lại: " + v.getRentalQuantity());
                v.setRentalQuantity(newRental);
            }
            variantRepository.save(v);
        }

        orderRepository.save(order);

        // Xóa giỏ hàng
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        return toResponse(orderRepository.findById(order.getId()).orElseThrow());
    }

    // ── Lấy đơn hàng của user ─────────────────────────
    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse));
    }

    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền truy cập đơn hàng này");
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse confirmDelivery(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền");
        }
        if (order.getStatus() != Order.OrderStatus.SHIPPING) {
            throw new IllegalArgumentException("Đơn hàng chưa ở trạng thái đang giao");
        }
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        return toResponse(orderRepository.save(order));
    }

    // ── Admin: lấy 1 đơn ────────────────────────────
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        return toResponse(order);
    }

    // ── Admin ──────────────────────────────────────────
    public PageResponse<OrderResponse> getAllOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> result = status != null && !status.isBlank()
                ? orderRepository.findByStatus(Order.OrderStatus.valueOf(status), pageable)
                : orderRepository.findAllWithItems(pageable); // FIX: load items + variant cùng lúc
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
        order.setStatus(newStatus);

        // Cập nhật paymentStatus tự động theo logic thực tế:
        switch (newStatus) {
            case CONFIRMED:
                // Chỉ BANK_TRANSFER_PREPAID mới qua bước CONFIRMED
                // Admin xác nhận đã nhận tiền CK trước → PAID
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                break;
            case PROCESSING:
                // COD/BANK_TRANSFER/E_WALLET bỏ qua CONFIRMED, chuyển thẳng sang PROCESSING
                // E_WALLET đã PAID từ đầu, giữ nguyên
                // COD/BANK_TRANSFER → vẫn PENDING cho đến khi DELIVERED
                break;
            case DELIVERED:
                // Khi giao thành công:
                // - COD: shipper thu tiền mặt → PAID
                // - BANK_TRANSFER: shipper thu hộ → PAID
                // - E_WALLET / BANK_TRANSFER_PREPAID: đã PAID từ trước, set lại cho chắc
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                break;
            default:
                break;
        }

        Order saved = orderRepository.save(order);
        // Gửi thông báo cho user
        notificationService.sendOrderNotification(
                saved.getUser().getId(), saved.getOrderNumber(), status);
        return toResponse(saved);
    }

    // ── Mapper ────────────────────────────────────────
    private OrderResponse toResponse(Order o) {
        // ── FIX: dùng new + setter thay vì stream với @Builder để tránh lỗi inference ──
        List<OrderResponse.OrderItemDto> itemDtos = new ArrayList<>();
        for (OrderItem i : o.getItems()) {
            OrderResponse.OrderItemDto dto = new OrderResponse.OrderItemDto();
            dto.setId(i.getId());
            dto.setProductName(i.getProductName());
            // ── FIX: bỏ getProductSku() — dùng size thay thế ──
            dto.setSize(i.getProductVariant() != null ? i.getProductVariant().getSize() : null);
            dto.setColor(i.getProductVariant() != null ? i.getProductVariant().getColor() : null);
            dto.setItemType(i.getItemType() != null ? i.getItemType().name() : null);
            dto.setQuantity(i.getQuantity());
            dto.setUnitPrice(i.getUnitPrice());
            dto.setTotalPrice(i.getTotalPrice());
            dto.setRentalStartDate(i.getRentalStartDate());
            dto.setRentalEndDate(i.getRentalEndDate());
            dto.setRentalDays(i.getRentalDays());
            itemDtos.add(dto);
        }

        OrderResponse res = new OrderResponse();
        res.setId(o.getId());
        res.setOrderNumber(o.getOrderNumber());
        res.setUserId(o.getUser().getId());
        res.setUserName(o.getUser().getFullName() != null
                ? o.getUser().getFullName() : o.getUser().getUsername());
        res.setStatus(o.getStatus() != null ? o.getStatus().name() : null);
        res.setPaymentStatus(o.getPaymentStatus() != null ? o.getPaymentStatus().name() : null);
        res.setPaymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
        res.setSubtotalPrice(o.getSubtotalPrice());
        res.setDiscountAmount(o.getDiscountAmount());
        res.setShippingFee(o.getShippingFee());
        res.setFinalPrice(o.getFinalPrice());
        res.setShippingAddressStreet(o.getShippingAddressStreet());
        res.setShippingAddressWard(o.getShippingAddressWard());
        res.setShippingAddressDistrict(o.getShippingAddressDistrict());
        res.setShippingAddressProvince(o.getShippingAddressProvince());
        res.setPhone(o.getPhone());
        res.setNote(o.getNote());
        res.setItems(itemDtos);
        res.setCreatedAt(o.getCreatedAt());
        return res;
    }

    // ── User huỷ đơn ────────────────────────────────
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền huỷ đơn này");
        }

        // Chỉ cho huỷ khi PENDING (chưa xác nhận thanh toán)
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Chỉ có thể huỷ đơn hàng ở trạng thái 'Chờ xác nhận'. " +
                            "Đơn đã xác nhận vui lòng liên hệ shop: 0909 123 456");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        // Hoàn lại tồn kho
        for (OrderItem item : order.getItems()) {
            ProductVariant v = item.getProductVariant();
            if (item.getItemType() == OrderItem.ItemType.SALE) {
                v.setStockQuantity(v.getStockQuantity() + item.getQuantity());
            } else {
                v.setRentalQuantity(v.getRentalQuantity() + item.getQuantity());
            }
            variantRepository.save(v);
        }

        // Ghi lý do huỷ vào note
        String note = order.getNote() != null ? order.getNote() + " | " : "";
        order.setNote(note + "Huỷ bởi user" + (reason != null && !reason.isBlank() ? ": " + reason : ""));

        // Nếu đã thanh toán → đánh dấu hoàn tiền
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }

        Order saved = orderRepository.save(order);
        notificationService.sendOrderNotification(
                saved.getUser().getId(), saved.getOrderNumber(), "CANCELLED");
    }

}
