package cosplayshop.service;

import cosplayshop.dto.response.PageResponse;
import cosplayshop.dto.response.ProductResponse;
import cosplayshop.entity.Favorite;
import cosplayshop.entity.Product;
import cosplayshop.entity.User;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.FavoriteRepository;
import cosplayshop.repository.ProductRepository;
import cosplayshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;
    private final ProductService     productService;

    /** Trả về danh sách sản phẩm yêu thích (dạng ProductResponse) */
    public PageResponse<ProductResponse> getFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // FIX: dùng findByUserId — đã có trong FavoriteRepository
        Page<Favorite> favPage = favoriteRepository.findByUserId(userId, pageable);
        Page<ProductResponse> result = favPage.map(f -> productService.toResponse(f.getProduct()));
        return PageResponse.of(result);
    }

    /** Toggle: thêm nếu chưa có, bỏ nếu đã có. Trả về true = đã thêm, false = đã bỏ */
    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            return false;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        return true;
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
