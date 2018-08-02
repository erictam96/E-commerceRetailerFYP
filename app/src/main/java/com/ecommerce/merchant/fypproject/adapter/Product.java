package com.ecommerce.merchant.fypproject.adapter;

/**
 * Created by leeyipfung on 3/5/2018.
 */

public class Product {

    private String prodID;
    private String prodCode;
    private String prodName;
    private String prodDate;
    private String prodCategory;
    private String prodDesc;
    private String prodSize;
    private String prodStatus;
    private String RID;
    private String productURL;
    private String shopName;
    private double prodPrice;
    private int prodDiscount;
    private String endBoostDate;
    private String boostPrice;

    public String getBoostPrice() {
        return boostPrice;
    }

    public void setBoostPrice(String boostPrice) {
        this.boostPrice = boostPrice;
    }

    public String getEndBoostDate() {
        return endBoostDate;
    }

    public void setEndBoostDate(String endBoostDate) {
        this.endBoostDate = endBoostDate;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getProductURL() {
        return productURL;
    }

    public void setProductURL(String productURL) {
        this.productURL = productURL;
    }

    public String getProdID() {
        return prodID;
    }

    public void setProdID(String prodID) {
        this.prodID = prodID;
    }

    public String getProdCode() {
        return prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdDate() {
        return prodDate;
    }

    public void setProdDate(String prodDate) {
        this.prodDate = prodDate;
    }

    public String getProdCategory() {
        return prodCategory;
    }

    public void setProdCategory(String prodCategory) {
        this.prodCategory = prodCategory;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public String getProdSize() {
        return prodSize;
    }

    public void setProdSize(String prodSize) {
        this.prodSize = prodSize;
    }

    public String getProdStatus() {
        return prodStatus;
    }

    public void setProdStatus(String prodStatus) {
        this.prodStatus = prodStatus;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public double getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(double prodPrice) {
        this.prodPrice = prodPrice;
    }

    public int getProdDiscount() {
        return prodDiscount;
    }

    public void setProdDiscount(int prodDiscount) {
        this.prodDiscount = prodDiscount;
    }
}
