package com.cu.gastossales.model;

import java.io.Serializable;

public class sales_provider_data implements Serializable {

    private String number;
    private String date;
    private String uid;
    private String order_amount;

    public sales_provider_data(String number, String date, String uid,String order_amount) {
        this.number = number;
        this.date = date;
        this.uid = uid;
        this.order_amount=order_amount;
    }

    public sales_provider_data() {
    }

    public String getOrder_amount() {
        return order_amount;
    }

    public void setOrder_amount(String order_amount) {
        this.order_amount = order_amount;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
