package trchicuong.ezweb.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import trchicuong.ezweb.config.AppProperties;
import trchicuong.ezweb.models.DownloadToken;
import trchicuong.ezweb.models.Order;
import trchicuong.ezweb.models.OrderItem;
import trchicuong.ezweb.models.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Value("${spring.mail.username:no-reply@ezweb.vn}")
    private String mailFromAddress;

    @Async
    public void sendOrderSuccessMail(User user, Order order, DownloadToken token) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(appProperties.getName() + " <" + mailFromAddress + ">");
            helper.setTo(user.getEmail());
            helper.setSubject("[EZWeb] Thanh toán thành công - Đơn #" + order.getId());

            String downloadUrl = appProperties.getBaseUrl() + "/download/" + token.getToken();
            StringBuilder itemRows = new StringBuilder();
            for (OrderItem item : order.getItems()) {
                itemRows.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>")
                        .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#374151;'>")
                        .append(item.getProduct().getTitle())
                        .append("</td>")
                        .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;font-size:13px;color:#374151;text-align:right;font-weight:600;white-space:nowrap;'>")
                        .append(String.format("%,d₫", item.getPriceSnapshot()))
                        .append("</td>")
                        .append("</tr></table>");
            }

            // arg order: orderId(%d), userName(%s), itemRows(%s), total(%s), downloadUrl(%s)
            String html = """
                    <!DOCTYPE html>
                    <html lang="vi">
                    <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                    <body style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#f1f5f9;margin:0;padding:32px 16px;">
                      <div style="max-width:580px;margin:0 auto;">

                        <div style="background:linear-gradient(135deg,#2563eb 0%%,#4f46e5 100%%);border-radius:16px 16px 0 0;padding:28px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                              <td style="vertical-align:middle;">
                                <div style="font-size:22px;font-weight:800;color:#fff;letter-spacing:-0.5px;">EZWeb</div>
                                <div style="font-size:12px;color:#bfdbfe;margin-top:3px;">Source Code Store</div>
                              </td>
                              <td align="right" style="vertical-align:middle;">
                                <div style="background:rgba(255,255,255,0.2);border-radius:50%%;width:44px;height:44px;text-align:center;line-height:44px;font-size:22px;color:#fff;display:inline-block;">✓</div>
                              </td>
                            </tr>
                          </table>
                        </div>

                        <div style="background:#dcfce7;border-left:4px solid #16a34a;padding:13px 20px;">
                          <span style="font-size:14px;font-weight:600;color:#15803d;">🎉 Thanh toán thành công — Đơn #%d</span>
                        </div>

                        <div style="background:#fff;padding:28px 32px;">
                          <p style="margin:0 0 6px;font-size:16px;font-weight:700;color:#111827;">Xin chào %s!</p>
                          <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.6;">Cảm ơn bạn đã mua hàng tại EZWeb. Đơn hàng đã được xử lý và sẵn sàng tải về.</p>

                          <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden;margin-bottom:20px;">
                            <div style="background:#f1f5f9;padding:9px 16px;border-bottom:1px solid #e2e8f0;">
                              <span style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.6px;">Sản phẩm đã mua</span>
                            </div>
                            %s
                            <div style="padding:12px 16px;border-top:2px solid #e2e8f0;">
                              <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td style="font-size:14px;font-weight:700;color:#111827;">Tổng cộng</td>
                                  <td align="right" style="font-size:15px;font-weight:800;color:#2563eb;">%s₫</td>
                                </tr>
                              </table>
                            </div>
                          </div>

                          <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:12px;padding:20px 24px;margin-bottom:24px;">
                            <div style="font-size:14px;font-weight:700;color:#1e40af;margin-bottom:6px;">📥 Link tải source code</div>
                            <div style="font-size:12px;color:#3b82f6;margin-bottom:16px;line-height:1.6;">Link có hiệu lực trong <strong>24 giờ</strong> và chỉ dùng được <strong>1 lần</strong>. Hãy tải ngay bây giờ.</div>
                            <a href="%s"
                               style="display:inline-block;background:linear-gradient(135deg,#2563eb,#4f46e5);color:#fff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:700;font-size:14px;letter-spacing:0.2px;">
                              Tải source code ngay →
                            </a>
                          </div>

                          <div style="border-top:1px solid #f1f5f9;padding-top:18px;">
                            <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.7;">
                              Gặp vấn đề khi tải? Liên hệ qua <strong style="color:#374151;">Zalo / Messenger</strong> ngay trên trang chủ — chúng tôi phản hồi trong vòng 30 phút.
                            </p>
                          </div>
                        </div>

                        <div style="background:#f8fafc;border-radius:0 0 16px 16px;padding:16px 32px;border-top:1px solid #e2e8f0;text-align:center;">
                          <p style="margin:0;font-size:12px;color:#94a3b8;">© 2025 <strong style="color:#64748b;">EZWeb</strong> — Web Agency &amp; Source Store</p>
                        </div>

                      </div>
                    </body>
                    </html>
                    """.formatted(
                    order.getId(),
                    user.getName(),
                    itemRows.toString(),
                    String.format("%,d", order.getTotalAmount()),
                    downloadUrl
            );

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Sent order success mail to {} for order #{}", user.getEmail(), order.getId());
        } catch (Exception e) {
            log.error("Failed to send order success mail for order #{}: {}", order.getId(), e.getMessage());
        }
    }
}
