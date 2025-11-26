package com.example.TodoList.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class EmailService { // gửi email: html + link

    private final JavaMailSender mailSender;

    public void sendVerifyRegisterMail(String verifyLink, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper textSend = new MimeMessageHelper(message, true); // cho phép multipart

            textSend.setTo(userMail);
            textSend.setSubject("Xác minh tài khoản");
            textSend.setText("<p> Chào bạn,</p>" +
                    "<p> Vui lòng nhấn link sau đây để xác minh tài khoản:" +
                    "<a href=\"" + verifyLink + "\">Xác minh tài khoản</a>", true
            );

            mailSender.send(message);
        } catch (MessagingException e){
            throw new MessagingException("Ko gửi được link xác minh");
        }
    }

    public void sendVerifyLoginMail(String otp, String userMail) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper textSend = new MimeMessageHelper(message, true);

            textSend.setTo(userMail);
            textSend.setSubject("Mã OTP xác minh tài khoản");

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #1a73e8;">Xác thực đăng nhập</h2>
                    <p>Chào bạn,</p>
                    <p>Dưới đây là mã xác minh (OTP) của bạn. Mã này sẽ hết hạn sau 5 phút:</p>
                    
                    <h1 style="color: #333; letter-spacing: 5px; font-size: 32px;">%s</h1>
                    
                    <p>Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>
                    <hr style="border: none; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #888;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                </div>
            """.formatted(otp); // Truyền OTP vào %s

            textSend.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e){
            throw new MessagingException("Ko gửi được email OTP");
        }
    }
}
