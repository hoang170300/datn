package cosplayshop.service;

import cosplayshop.entity.*;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.*;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    // ── Lấy giỏ hàng (tạo mới nếu chưa có) ──────────
    public CartDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toDto(cart);
    }

    // ── Thêm sản phẩm vào giỏ ────────────────────────
    @Transactional
    public CartDto addItem(Long userId, Long variantId, Integer quantity,
                           String orderType, LocalDate rentalStart, LocalDate rentalEnd) {
        Cart cart = getOrCreateCart(userId);
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant không tồn tại"));

        CartItem.OrderType type = CartItem.OrderType.valueOf(orderType != null ? orderType : "SALE");

        // Kiểm tra tồn kho
        if (type == CartItem.OrderType.SALE && variant.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Số lượng tồn kho không đủ");
        }
        if (type == CartItem.OrderType.RENTAL && variant.getRentalQuantity() < quantity) {
            throw new IllegalArgumentException("Số lượng thuê không đủ");
        }

        // Gộp nếu đã có item cùng variant + type
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductVariant().getId().equals(variantId)
                        && i.getOrderType() == type)
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(quantity)
                    .orderType(type)
                    .rentalStartDate(rentalStart)
                    .rentalEndDate(rentalEnd)
                    .build();
            cartItemRepository.save(item);
            cart.getItems().add(item);
        }

        return toDto(cartRepository.findById(cart.getId()).orElseThrow());
    }

    // ── Cập nhật số lượng ─────────────────────────────
    @Transactional
    public CartDto updateQuantity(Long userId, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại"));

        validateOwnership(item, userId);

        if (quantity <= 0) {
            Cart cart = item.getCart();
            cart.getItems().removeIf(i -> i.getId().equals(itemId));
            cartRepository.save(cart);
            return toDto(cart);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return toDto(getOrCreateCart(userId));
    }

    // ── Xóa item ──────────────────────────────────────
    @Transactional
    public CartDto removeItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại"));
        validateOwnership(item, userId);

        // FIX: dùng orphanRemoval - remove khỏi collection rồi save Cart
        // Cart có orphanRemoval=true nên Hibernate tự DELETE CartItem
        Cart cart = item.getCart();
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepository.save(cart); // trigger orphanRemoval DELETE

        return toDto(cart);
    }

    // ── Xóa toàn bộ giỏ ──────────────────────────────
    @Transactional
    public CartDto clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        return toDto(cart);
    }

    // ── Helpers ───────────────────────────────────────
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    private void validateOwnership(CartItem item, Long userId) {
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền truy cập item này");
        }
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            ProductVariant v = item.getProductVariant();
            Product product = v.getProduct();

            BigDecimal unitPrice;
            int rentalDays = 0;

            if (item.getOrderType() == CartItem.OrderType.RENTAL) {
                unitPrice = product.getRentalPricePerDay() != null
                        ? product.getRentalPricePerDay() : BigDecimal.ZERO;
                if (item.getRentalStartDate() != null && item.getRentalEndDate() != null) {
                    // +1 vì tính cả ngày bắt đầu và ngày kết thúc (inclusive)
                    // VD: 15/3 → 17/3 = 3 ngày (15, 16, 17)
                    rentalDays = (int) ChronoUnit.DAYS.between(
                            item.getRentalStartDate(), item.getRentalEndDate()) + 1;
                    if (rentalDays < 1) rentalDays = 1;
                }
            } else {
                // Giá variant ưu tiên, nếu không có thì dùng giá product
                unitPrice = v.getPrice() != null ? v.getPrice()
                        : (product.getSalePrice() != null ? product.getSalePrice() : BigDecimal.ZERO);
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(
                    item.getOrderType() == CartItem.OrderType.RENTAL && rentalDays > 0
                            ? (long) item.getQuantity() * rentalDays
                            : item.getQuantity()));

            total = total.add(subtotal);
            totalItems += item.getQuantity();

            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId());
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductSlug(product.getSlug());
            dto.setVariantId(v.getId());
            dto.setSize(v.getSize());
            dto.setColor(v.getColor());
            // ── FIX: không dùng getSku() ──
            dto.setImageUrl(product.getPrimaryImageUrl());
            dto.setQuantity(item.getQuantity());
            dto.setOrderType(item.getOrderType().name());
            dto.setUnitPrice(unitPrice);
            dto.setSubtotal(subtotal);
            dto.setRentalStartDate(item.getRentalStartDate());
            dto.setRentalEndDate(item.getRentalEndDate());
            dto.setRentalDays(rentalDays);
            // Tính tiền cọc cho item thuê
            if (item.getOrderType() == CartItem.OrderType.RENTAL) {
                BigDecimal dep = product.getDepositAmount();
                dto.setDepositAmount(dep != null ? dep.multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO);
            } else {
                dto.setDepositAmount(BigDecimal.ZERO);
            }
            // Cộng tiền cọc vào totalDeposit
            totalDeposit = totalDeposit.add(dto.getDepositAmount() != null ? dto.getDepositAmount() : BigDecimal.ZERO);
            itemDtos.add(dto);
        }

        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setItems(itemDtos);
        cartDto.setTotalItems(totalItems);
        cartDto.setTotalPrice(total);
        cartDto.setTotalDeposit(totalDeposit);
        cartDto.setGrandTotal(total.add(totalDeposit));
        return cartDto;
    }

    // ── DTOs ──────────────────────────────────────────
    @Data
    public static class CartDto {
        private Long id;
        private List<CartItemDto> items;
        private Integer totalItems;
        private BigDecimal totalPrice;      // tiền hàng (chưa gồm cọc + ship)
        private BigDecimal totalDeposit;    // tổng tiền đặt cọc thuê
        private BigDecimal grandTotal;      // tổng tất cả = totalPrice + totalDeposit
    }

    @Data
    public static class CartItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private Long variantId;
        private String size;
        private String color;
        private String imageUrl;
        private Integer quantity;
        private String orderType;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private Integer rentalDays;
        private BigDecimal depositAmount; // tiền cọc thuê
    }
}