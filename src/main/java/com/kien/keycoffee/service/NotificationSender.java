package com.kien.keycoffee.service;

public interface NotificationSender {

    boolean send(String toEmail, String subject, String content);
}