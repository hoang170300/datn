package cosplayshop.service;

import cosplayshop.dto.request.ChangePasswordRequest;
import cosplayshop.dto.request.UpdateProfileRequest;
import cosplayshop.entity.Role;
import cosplayshop.entity.User;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.RoleRepository;
import cosplayshop.repository.UserRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Hồ sơ cá nhân ────────────────────────────────
    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        return toDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        if (req.getFullName() != null)        user.setFullName(req.getFullName());
        if (req.getPhone() != null)           user.setPhone(req.getPhone());
        if (req.getAddressStreet() != null)   user.setAddressStreet(req.getAddressStreet());
        if (req.getAddressWard() != null)     user.setAddressWard(req.getAddressWard());
        if (req.getAddressDistrict() != null) user.setAddressDistrict(req.getAddressDistrict());
        if (req.getAddressProvince() != null) user.setAddressProvince(req.getAddressProvince());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ── Admin ──────────────────────────────────────────
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    // ── FIX: thêm changeRole() còn thiếu ──────────────
    @Transactional
    public void changeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        // Đảm bảo roleName có prefix ROLE_
        String normalizedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        Role role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại: " + normalizedRole));
        user.setRole(role);
        userRepository.save(user);
    }

    // ── Mapper ────────────────────────────────────────
    private UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .addressStreet(u.getAddressStreet())
                .addressWard(u.getAddressWard())
                .addressDistrict(u.getAddressDistrict())
                .addressProvince(u.getAddressProvince())
                .avatarUrl(u.getAvatarUrl())
                .role(u.getRoleName())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String addressStreet;
        private String addressWard;
        private String addressDistrict;
        private String addressProvince;
        private String avatarUrl;
        private String role;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}