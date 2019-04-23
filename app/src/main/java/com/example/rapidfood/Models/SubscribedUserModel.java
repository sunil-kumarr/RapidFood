package com.example.rapidfood.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class SubscribedUserModel {
    private String balance;
    @ServerTimestamp
    private Date start_date;
    private String duration;
    private String subscriptionType;
    private String trans_id;
    private String mobile;

    public SubscribedUserModel(String pBalance) {
        balance = pBalance;
    }

    public SubscribedUserModel() {
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public String getMobile() {
        return mobile;
    }

    public String getBalance() {
        return balance;
    }

    public Date getStart_date() {
        return start_date;
    }

    public String getDuration() {
        return duration;
    }

    public String getTrans_id() {
        return trans_id;
    }
}
