package org.citrusframework.mail.client;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

/**
 * This class exists mainly for testing purposes (mocking).
 */
public class MailSender {

    public void send(MimeMessage mimeMessage) throws MessagingException {
        Transport.send(mimeMessage);
    }
}
