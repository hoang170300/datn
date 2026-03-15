package cosplayshop.service;

import cosplayshop.dto.request.LoginRequest;
import cosplayshop.dto.request.RegisterRequest;
import cosplayshop.dto.response.JwtResponse;
import cosplayshop.entity.Cart;
import cosplayshop.entity.Role;
import cosplayshop.entity.User;
import cosplayshop.repository.CartRepository;
import cosplayshop.repository.RoleRepository;   // ← FIX: import RoleRepository
import cosplayshop.repository.UserRepository;
import cosplayshop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;        // ← FIX
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Login ──────────────────────────────────────────
    public JwtResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(auth);
        User user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow();

        return JwtResponse.builder()
                .accessToken(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRoleName())
                .build();
    }

    // ── Register ───────────────────────────────────────
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER chưa được khởi tạo trong DB"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Tạo cart rỗng cho user mới
        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);
    }

    // ── Forgot Password ────────────────────────────────
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // TODO: gửi email thực tế qua Spring Mail
        // mailService.sendResetPasswordEmail(user.getEmail(), token);
        System.out.println("[DEV] Reset token: " + token);
    }

    // ── Reset Password ─────────────────────────────────
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}