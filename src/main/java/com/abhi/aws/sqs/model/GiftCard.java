package com.abhi.aws.sqs.model;

public class GiftCard {
    private String userName;
    private String giftCardType;
    private Double amount;
    private String date;

    public GiftCard() {
    }

    public GiftCard(String userName, String giftCardType, Double amount, String date) {
        this.userName = userName;
        this.giftCardType = giftCardType;
        this.amount = amount;
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGiftCardType() {
        return giftCardType;
    }

    public void setGiftCardType(String giftCardType) {
        this.giftCardType = giftCardType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "GiftCard{" +
                "userName='" + userName + '\'' +
                ", giftCardType='" + giftCardType + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                '}';
    }
}
