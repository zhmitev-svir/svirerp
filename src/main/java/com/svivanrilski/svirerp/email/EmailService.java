package com.svivanrilski.svirerp.email;

import com.svivanrilski.svirerp.settings.AppSettingService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

/**
 * Sends mail through the Gmail API (not SMTP) using the OAuth2 refresh token
 * captured by the Connect Gmail flow (see GmailOAuthController). A plain
 * jakarta.mail Session (no host/transport configured) is used purely to
 * build a well-formed RFC 822 message in memory; the bytes are handed to
 * Gmail's REST endpoint rather than ever being sent over SMTP.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String SEND_URI = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final GmailTokenService tokenService;
    private final AppSettingService settingService;
    private final RestClient restClient = RestClient.create();

    public void send(String to, String subject, String htmlBody) {
        String senderAddress = settingService.getDecryptedValue("gmail.sender-address")
                .orElseThrow(() -> new IllegalArgumentException("Gmail account is not connected yet — use Connect Gmail in Settings"));

        String raw = buildRawMessage(senderAddress, to, subject, htmlBody);
        String accessToken = tokenService.getAccessToken();

        restClient.post()
                .uri(SEND_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(Map.of("raw", raw))
                .retrieve()
                .toBodilessEntity();
    }

    private String buildRawMessage(String from, String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build email message", e);
        }
    }
}
