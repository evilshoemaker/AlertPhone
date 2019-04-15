package ru.digios.alertphone.services;

import java.io.Serializable;
import java.util.Date;

public class SmsToReceive implements Serializable {
    private long id = -1;
    private Date date = new Date();
    private String phoneNumber = "";
    private String text = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
