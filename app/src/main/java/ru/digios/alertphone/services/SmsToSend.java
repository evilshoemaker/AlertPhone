package ru.digios.alertphone.services;

import java.io.Serializable;

public class SmsToSend implements Serializable {
    private String phoneNumber;
    private String message;

    public SmsToSend() {}

    public SmsToSend(String phoneNumber, String message)
    {
        this.setPhoneNumber(phoneNumber);
        this.setMessage(message);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
