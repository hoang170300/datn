package cosplayshop.controller;

import cosplayshop.dto.response.ApiResponse;
import cosplayshop.entity.Notification;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.NotificationRepository;
import cosplayshop.repository.UserRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails u) {
        return userRepository.findByUsername(u.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Long uid = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> result = notificationRepo
                .findByUserIdOrderByCreatedAtDesc(uid, pageable)
                .map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long uid = getUserId(userDetails);
        long count = notificationRepo.countByUserIdAndIsReadFalse(uid);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setIsRead(true);
        notificationRepo.save(n);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long uid = getUserId(userDetails);
        Pageable all = PageRequest.of(0, Integer.MAX_VALUE);
        notificationRepo.findByUserIdOrderByCreatedAtDesc(uid, all)
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationRepo.save(n);
                });
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả đã đọc", null));
    }

    // Admin: gửi thông báo cho user hoặc broadcast
    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestBody SendNotificationRequest request) {
        cosplayshop.entity.User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification n = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(Notification.NotificationType.valueOf(request.getType()))
                .isRead(false)
                .linkUrl(request.getLinkUrl())
                .build();
        notificationRepo.save(n);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi thông báo", null));
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType() != null ? n.getType().name() : null)
                .isRead(n.getIsRead())
                .linkUrl(n.getLinkUrl())
                .createdAt(n.getCreatedAt())
                .build();
    }

    @Data @Builder
    public static class NotificationDto {
        private Long id;
        private String title;
        private String message;
        private String type;
        private Boolean isRead;
        private String linkUrl;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SendNotificationRequest {
        private Long userId;
        private String title;
        private String message;
        private String type; // ORDER, PROMOTION, RENTAL, SYSTEM, NEW_PRODUCT
        private String linkUrl;
    }
}

