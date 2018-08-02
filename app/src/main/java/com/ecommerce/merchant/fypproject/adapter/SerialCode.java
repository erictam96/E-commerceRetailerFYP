package com.ecommerce.merchant.fypproject.adapter;

public class SerialCode {
    private String serial;
    private String serialID;
    private String orderID;
    private String prodVariant;

    public String getProdVariant() {
        return prodVariant;
    }

    public void setProdVariant(String prodVariant) {
        this.prodVariant = prodVariant;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSerialID() {
        return serialID;
    }

    public void setSerialID(String serialID) {
        this.serialID = serialID;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }


}
