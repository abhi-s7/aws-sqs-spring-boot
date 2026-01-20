package com.abhi.aws.sqs.service;

import com.abhi.aws.sqs.model.GiftCard;
import com.abhi.aws.sqs.model.ReceivedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AwsSqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AwsSqsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
    }

    // Creates a new SQS queue
    public String createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        CreateQueueResponse response = sqsClient.createQueue(createQueueRequest);
        return response.queueUrl();
    }

    // Gets the queue URL by queue name
    public String getQueueUrl(String queueName) {
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        GetQueueUrlResponse response = sqsClient.getQueueUrl(getQueueUrlRequest);
        return response.queueUrl();
    }

    // Sends a message to the queue
    public String sendMessage(String queueUrl, GiftCard giftCard) throws Exception {
        String messageBody = objectMapper.writeValueAsString(giftCard);
        
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
        
        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        return response.messageId();
    }

    // Receives messages from the queue
    public List<ReceivedMessage> receiveMessages(String queueUrl, int maxMessages) throws Exception {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(5)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
        List<ReceivedMessage> receivedMessages = new ArrayList<>();

        for (Message message : response.messages()) {
            GiftCard giftCard = objectMapper.readValue(message.body(), GiftCard.class);
            ReceivedMessage receivedMessage = new ReceivedMessage(
                    message.messageId(),
                    message.receiptHandle(),
                    giftCard
            );
            receivedMessages.add(receivedMessage);
        }

        return receivedMessages;
    }

    // Deletes a message from the queue
    public void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    // Deletes a queue
    public void deleteQueue(String queueUrl) {
        DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder()
                .queueUrl(queueUrl)
                .build();
        sqsClient.deleteQueue(deleteQueueRequest);
    }

    // Lists all queues
    public List<String> listQueues() {
        ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder().build();
        ListQueuesResponse response = sqsClient.listQueues(listQueuesRequest);
        return response.queueUrls();
    }
}
