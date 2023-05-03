package socialnet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender javaMailSender;
    private static final ResourceBundle textProperties = ResourceBundle.getBundle("text");
    @Value("${spring.mail.username}")
    private String userName;

    public void send(String emailTo, String subject, String message) {
        MimeMessage htmlMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(htmlMessage);
        try {
            helper.setFrom(textProperties.getString("mail.from"));
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setText(message,true);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        javaMailSender.send(htmlMessage);
    }
}
