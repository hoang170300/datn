package cosplayshop.service;

import cosplayshop.dto.response.PageResponse;
import cosplayshop.entity.*;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    // ── Search products for POS ──────────────────────
    public List<Map<String, Object>> searchProducts(String keyword) {
        // Nếu keyword trống/null → lấy tất cả (truyền null cho JPQL IS NULL check)
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        var products = productRepository.searchProductsNoType(
                kw, null, null, null, null,
                PageRequest.of(0, 50, Sort.by("name")));

        return products.getContent().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("slug", p.getSlug());
            m.put("salePrice", p.getSalePrice());
            m.put("rentalPricePerDay", p.getRentalPricePerDay());
            m.put("depositAmount", p.getDepositAmount());
            m.put("productType", p.getProductType());
            m.put("primaryImageUrl", p.getPrimaryImageUrl());

            // Variants còn hàng
            var variants = variantRepository.findByProductIdAndIsActiveTrue(p.getId())
                    .stream().filter(v -> v.getStockQuantity() > 0 || v.getRentalQuantity() > 0)
                    .map(v -> {
                        Map<String, Object> vm = new LinkedHashMap<>();
                        vm.put("id", v.getId());
                        vm.put("size", v.getSize());
                        vm.put("color", v.getColor());
                        vm.put("stockQuantity", v.getStockQuantity());
                        vm.put("rentalQuantity", v.getRentalQuantity());
                        vm.put("price", v.getPrice() != null ? v.getPrice() : p.getSalePrice());
                        return vm;
                    }).collect(Collectors.toList());
            m.put("variants", variants);
            return m;
        }).collect(Collectors.toList());
    }

    // ── Create offline order ─────────────────────────
    @Transactional
    public Map<String, Object> createOfflineOrder(PosOrderRequest req) {
        // User offline: dùng admin user hoặc tạo guest
        User user = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;

        String orderNumber = "POS" + System.currentTimeMillis();

        // Tại quầy: thu tiền trực tiếp → PAID ngay, giao hàng luôn → DELIVERED
        Order.PaymentMethod posPayMethod = req.getPaymentMethod() != null
                ? Order.PaymentMethod.valueOf(
                "CASH".equals(req.getPaymentMethod()) ? "COD" : req.getPaymentMethod())
                : Order.PaymentMethod.COD;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(Order.OrderStatus.DELIVERED) // Tại quầy: giao hàng trực tiếp luôn
                .paymentMethod(posPayMethod)
                .paymentStatus(Order.PaymentStatus.PAID) // Tại quầy: thu tiền ngay
                .shippingFee(BigDecimal.ZERO) // Offline: không ship
                .phone(req.getCustomerPhone())
                .note(req.getNote() != null ? req.getNote() : "Đơn offline tại cửa hàng"
                        + (req.getCustomerName() != null ? " - KH: " + req.getCustomerName() : ""))
                .build();

        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (PosOrderRequest.PosItem pi : req.getItems()) {
            ProductVariant v = variantRepository.findById(pi.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant không tồn tại"));
            Product p = v.getProduct();

            BigDecimal unitPrice;
            int rentalDays = 1;
            BigDecimal depositAmt = BigDecimal.ZERO;
            OrderItem.ItemType itemType;

            if ("RENTAL".equals(pi.getOrderType())) {
                itemType = OrderItem.ItemType.RENTAL;
                unitPrice = p.getRentalPricePerDay() != null
                        ? p.getRentalPricePerDay() : BigDecimal.ZERO;
                if (pi.getRentalStartDate() != null && pi.getRentalEndDate() != null) {
                    rentalDays = (int) ChronoUnit.DAYS.between(
                            pi.getRentalStartDate(), pi.getRentalEndDate()) + 1;
                    if (rentalDays < 1) rentalDays = 1;
                }
                depositAmt = p.getDepositAmount() != null
                        ? p.getDepositAmount().multiply(BigDecimal.valueOf(pi.getQuantity()))
                        : BigDecimal.ZERO;
                totalDeposit = totalDeposit.add(depositAmt);

                // Giảm rental_quantity
                v.setRentalQuantity(Math.max(0, v.getRentalQuantity() - pi.getQuantity()));
            } else {
                itemType = OrderItem.ItemType.SALE;
                unitPrice = v.getPrice() != null ? v.getPrice()
                        : (p.getSalePrice() != null ? p.getSalePrice() : BigDecimal.ZERO);

                // Giảm stock_quantity
                v.setStockQuantity(Math.max(0, v.getStockQuantity() - pi.getQuantity()));
            }
            variantRepository.save(v);

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(
                    "RENTAL".equals(pi.getOrderType())
                            ? (long) pi.getQuantity() * rentalDays
                            : pi.getQuantity()));

            subtotal = subtotal.add(totalPrice);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productVariant(v)
                    .productName(p.getName())
                    .itemType(itemType)
                    .quantity(pi.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .depositAmount(depositAmt)
                    .rentalStartDate(pi.getRentalStartDate())
                    .rentalEndDate(pi.getRentalEndDate())
                    .rentalDays("RENTAL".equals(pi.getOrderType()) ? rentalDays : null)
                    .build();
            items.add(item);
        }

        orderItemRepository.saveAll(items);
        order.setItems(items);

        // Cập nhật tổng tiền
        BigDecimal finalPrice = subtotal.add(totalDeposit);
        order.setSubtotalPrice(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalPrice(finalPrice);

        // Nếu thanh toán ngay (CASH)
        if (Order.PaymentMethod.COD.name().equals(req.getPaymentMethod())) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.DELIVERED);
        }

        orderRepository.save(order);

        return buildOrderMap(order, items, totalDeposit);
    }

    // ── Confirm payment ──────────────────────────────
    @Transactional
    public void confirmPayment(Long orderId, String method) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn không tồn tại"));
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaymentMethod(Order.PaymentMethod.valueOf("CASH".equals(method) ? "COD" : method));
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            order.setStatus(Order.OrderStatus.DELIVERED);
        }
        orderRepository.save(order);
    }

    // ── Confirm return rental ────────────────────────
    @Transactional
    public void confirmReturn(Long orderId, boolean damaged, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn không tồn tại"));

        // Cập nhật rental_status của các order items
        for (OrderItem item : order.getItems()) {
            if (item.getItemType() == OrderItem.ItemType.RENTAL) {
                item.setRentalStatus(damaged
                        ? OrderItem.RentalStatus.OVERDUE
                        : OrderItem.RentalStatus.RETURNED);
                orderItemRepository.save(item);

                // Hoàn lại rental_quantity nếu không hỏng
                if (!damaged) {
                    ProductVariant v = item.getProductVariant();
                    v.setRentalQuantity(v.getRentalQuantity() + item.getQuantity());
                    variantRepository.save(v);
                }
            }
        }

        order.setStatus(Order.OrderStatus.RETURNED);
        if (note != null) {
            String existingNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(existingNote + "Trả đồ: " + (damaged ? "⚠️ Hư hỏng" : "✅ Nguyên vẹn")
                    + (note != null && !note.isBlank() ? " - " + note : ""));
        }

        // Hoàn tiền cọc nếu không hỏng
        if (!damaged) {
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }
        orderRepository.save(order);
    }

    // ── Get active rentals ───────────────────────────
    public List<Map<String, Object>> getActiveRentals() {
        return orderItemRepository.findActiveRentals().stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getId());
                    m.put("orderId", item.getOrder().getId());
                    m.put("orderNumber", item.getOrder().getOrderNumber());
                    m.put("productName", item.getProductName());
                    m.put("size", item.getProductVariant().getSize());
                    m.put("quantity", item.getQuantity());
                    m.put("rentalStartDate", item.getRentalStartDate());
                    m.put("rentalEndDate", item.getRentalEndDate());
                    m.put("daysLeft", item.getRentalEndDate() != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), item.getRentalEndDate())
                            : null);
                    m.put("depositAmount", item.getDepositAmount());
                    m.put("phone", item.getOrder().getPhone());
                    m.put("note", item.getOrder().getNote());
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOverdueRentals() {
        return orderItemRepository.findOverdueRentals(LocalDate.now()).stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("itemId", item.getId());
                    m.put("orderId", item.getOrder().getId());
                    m.put("orderNumber", item.getOrder().getOrderNumber());
                    m.put("productName", item.getProductName());
                    m.put("size", item.getProductVariant().getSize());
                    m.put("rentalEndDate", item.getRentalEndDate());
                    m.put("overdueDays", ChronoUnit.DAYS.between(item.getRentalEndDate(), LocalDate.now()));
                    m.put("depositAmount", item.getDepositAmount());
                    m.put("phone", item.getOrder().getPhone());
                    return m;
                }).collect(Collectors.toList());
    }

    // ── Reports ──────────────────────────────────────
    public Map<String, Object> getTodayReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        BigDecimal revenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getFinalPrice).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long rentalCount = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getItemType() == OrderItem.ItemType.RENTAL).count();
        long saleCount = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getItemType() == OrderItem.ItemType.SALE).count();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("date", today.toString());
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", revenue);
        report.put("rentalCount", rentalCount);
        report.put("saleCount", saleCount);
        report.put("paidOrders", orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID).count());
        report.put("pendingOrders", orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PENDING).count());
        return report;
    }

    public Map<String, Object> getMonthlyReport(int month, int year) {
        LocalDate now = LocalDate.now();
        int m = month == 0 ? now.getMonthValue() : month;
        int y = year == 0 ? now.getYear() : year;

        LocalDateTime start = LocalDate.of(y, m, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(y, m, 1).plusMonths(1).atStartOfDay();

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        BigDecimal revenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getFinalPrice).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", m);
        report.put("year", y);
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", revenue);
        return report;
    }

    // ── Helpers ──────────────────────────────────────
    public PageResponse<Map<String, Object>> getOfflineOrders(int page, int size, String status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = status != null && !status.isBlank()
                ? orderRepository.findByStatus(Order.OrderStatus.valueOf(status), pageable)
                : orderRepository.findAll(pageable);
        return PageResponse.of(result.map(o -> buildOrderMap(o, o.getItems(), null)));
    }

    public Map<String, Object> getOrderDetail(Long id) {
        Order o = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn không tồn tại"));
        return buildOrderMap(o, o.getItems(), null);
    }

    private Map<String, Object> buildOrderMap(Order o, List<OrderItem> items, BigDecimal deposit) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNumber", o.getOrderNumber());
        m.put("status", o.getStatus());
        m.put("paymentStatus", o.getPaymentStatus());
        m.put("paymentMethod", o.getPaymentMethod());
        m.put("subtotalPrice", o.getSubtotalPrice());
        m.put("finalPrice", o.getFinalPrice());
        m.put("phone", o.getPhone());
        m.put("note", o.getNote());
        m.put("createdAt", o.getCreatedAt());

        // Tổng tiền cọc
        BigDecimal dep = deposit != null ? deposit :
                items.stream().map(i -> i.getDepositAmount() != null ? i.getDepositAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("totalDeposit", dep);

        m.put("items", items.stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", i.getId());
            im.put("productName", i.getProductName());
            im.put("size", i.getProductVariant().getSize());
            im.put("color", i.getProductVariant().getColor());
            im.put("itemType", i.getItemType());
            im.put("quantity", i.getQuantity());
            im.put("unitPrice", i.getUnitPrice());
            im.put("totalPrice", i.getTotalPrice());
            im.put("depositAmount", i.getDepositAmount());
            im.put("rentalStartDate", i.getRentalStartDate());
            im.put("rentalEndDate", i.getRentalEndDate());
            im.put("rentalDays", i.getRentalDays());
            im.put("rentalStatus", i.getRentalStatus());
            return im;
        }).collect(Collectors.toList()));
        return m;
    }

    // ── Request DTO ──────────────────────────────────
    @Data
    public static class PosOrderRequest {
        private String customerName;
        private String customerPhone;
        private String paymentMethod; // CASH | BANK_TRANSFER
        private String note;
        private List<PosItem> items;

        @Data
        public static class PosItem {
            private Long variantId;
            private Integer quantity;
            private String orderType; // SALE | RENTAL
            private LocalDate rentalStartDate;
            private LocalDate rentalEndDate;
        }
    }
}