package com.kien.lemocoffee.feature.user.service;

public interface NotificationSender {

    boolean send(String toEmail, String subject, String content);
}