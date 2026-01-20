package com.abhi.aws.sqs.model;

public class ReceivedMessage {
    private String messageId;
    private String receiptHandle;
    private GiftCard giftCard;

    public ReceivedMessage() {
    }

    public ReceivedMessage(String messageId, String receiptHandle, GiftCard giftCard) {
        this.messageId = messageId;
        this.receiptHandle = receiptHandle;
        this.giftCard = giftCard;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }
}
