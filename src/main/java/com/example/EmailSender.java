package com.example;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import javax.swing.*;
import java.io.File;
import java.util.Properties;

/**
 * Класс для отправки email-сообщений с использованием SMTP.
 * Предоставляет метод для отправки письма с заданным текстом и темой.
 *
 * @author Soldatov N. V.
 * @version 22.0.2
 */
public class EmailSender {

    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String recipientEmail; // Новое поле для адреса получателя

    /**
     * Конструктор для инициализации параметров подключения и получателя письма.
     *
     * @param username       Имя пользователя для SMTP-сервера.
     * @param password       Пароль для SMTP-сервера.
     * @param host           Хост SMTP-сервера.
     * @param port           Порт SMTP-сервера.
     * @param recipientEmail Адрес электронной почты получателя.
     */
    public EmailSender(String username, String password, String host, int port, String recipientEmail) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.recipientEmail = recipientEmail;
    }

    /**
     * Метод для отправки email-сообщения с заданной темой и текстом.
     *
     * @param subject    Тема письма.
     * @param text       Текст письма.
     * @param parentFrame Окно-родитель, которое используется для отображения сообщений об успехе или ошибке.
     */
    public void sendEmail(String subject, String text, JFrame parentFrame) {
        Properties props = new Properties();

        // Для SSL (порт 465):
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Создание сессии для отправки email
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Создание email-сообщения
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(text);

            // Отправка письма
            Transport.send(message);

            // Информирование пользователя об успешной отправке письма
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parentFrame, "Письмо успешно отправлено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            });

        } catch (MessagingException e) {
            // В случае ошибки информируем пользователя
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(parentFrame, "Ошибка отправки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }
}
