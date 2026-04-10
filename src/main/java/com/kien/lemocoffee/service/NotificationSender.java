package com.kien.lemocoffee.service;

public interface NotificationSender {

    boolean send(String toEmail, String subject, String content);
}