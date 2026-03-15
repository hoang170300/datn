package cosplayshop.service;

import cosplayshop.entity.Notification;
import cosplayshop.entity.User;
import cosplayshop.repository.NotificationRepository;
import cosplayshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendOrderNotification(Long userId, String orderNumber, String status) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Map<String, String[]> statusMessages = Map.of(
                "CONFIRMED",   new String[]{"✅ Đơn hàng đã xác nhận",
                        "Đơn #" + orderNumber + " đã được xác nhận thanh toán. Shop đang chuẩn bị hàng cho bạn!"},
                "PROCESSING",  new String[]{"📦 Đơn hàng đang chuẩn bị",
                        "Đơn #" + orderNumber + " đang được đóng gói. Sẽ sớm được giao cho đơn vị vận chuyển."},
                "SHIPPING",    new String[]{"🚚 Đơn hàng đang giao",
                        "Đơn #" + orderNumber + " đang trên đường đến bạn. Vui lòng chú ý điện thoại!"},
                "DELIVERED",   new String[]{"🎉 Đơn hàng đã giao thành công",
                        "Đơn #" + orderNumber + " đã được giao. Cảm ơn bạn đã mua sắm tại CosPlay Shop! ❤️"},
                "CANCELLED",   new String[]{"❌ Đơn hàng đã bị huỷ",
                        "Đơn #" + orderNumber + " đã bị huỷ. Liên hệ shop nếu có thắc mắc: 0909 123 456"},
                "RETURNED",    new String[]{"↩️ Đơn hàng đã hoàn trả",
                        "Đơn #" + orderNumber + " đã được xử lý hoàn trả. Tiền cọc sẽ được hoàn lại sớm."}
        );

        String[] msg = statusMessages.get(status);
        if (msg == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .title(msg[0])
                .message(msg[1])
                .type(Notification.NotificationType.ORDER)
                .linkUrl("/profile/orders")
                .build();

        notificationRepository.save(notification);
    }

    public List<Map<String, Object>> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 30))
                .getContent().stream().map(n -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", n.getId());
                    m.put("title", n.getTitle());
                    m.put("message", n.getMessage());
                    m.put("type", n.getType());
                    m.put("isRead", n.getIsRead());
                    m.put("linkUrl", n.getLinkUrl());
                    m.put("createdAt", n.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
