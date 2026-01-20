package com.abhi.aws.sqs.controller;

import com.abhi.aws.sqs.model.GiftCard;
import com.abhi.aws.sqs.model.ReceivedMessage;
import com.abhi.aws.sqs.service.AwsSqsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sqs")
public class AwsSqsController {

    @Autowired
    private AwsSqsService awsSqsService;

    // Creates a new SQS queue
    @PostMapping("/create-queue")
    public ResponseEntity<?> createQueue(@RequestParam String queueName) {
        try {
            String queueUrl = awsSqsService.createQueue(queueName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Queue created successfully");
            response.put("queueUrl", queueUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create queue: " + e.getMessage());
        }
    }

    // Gets queue URL by name
    @GetMapping("/queue-url")
    public ResponseEntity<?> getQueueUrl(@RequestParam String queueName) {
        try {
            String queueUrl = awsSqsService.getQueueUrl(queueName);
            Map<String, String> response = new HashMap<>();
            response.put("queueUrl", queueUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Queue not found: " + e.getMessage());
        }
    }

    // Sends a message to the queue
    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            String queueUrl = (String) request.get("queueUrl");
            Map<String, Object> giftCardData = (Map<String, Object>) request.get("giftCard");
            
            GiftCard giftCard = new GiftCard(
                    (String) giftCardData.get("userName"),
                    (String) giftCardData.get("giftCardType"),
                    Double.parseDouble(giftCardData.get("amount").toString()),
                    (String) giftCardData.get("date")
            );
            
            String messageId = awsSqsService.sendMessage(queueUrl, giftCard);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Message sent successfully");
            response.put("messageId", messageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send message: " + e.getMessage());
        }
    }

    // Receives messages from the queue
    @GetMapping("/receive-messages")
    public ResponseEntity<?> receiveMessages(@RequestParam String queueUrl, 
                                            @RequestParam(defaultValue = "10") int maxMessages) {
        try {
            List<ReceivedMessage> messages = awsSqsService.receiveMessages(queueUrl, maxMessages);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to receive messages: " + e.getMessage());
        }
    }

    // Deletes a message from the queue
    @DeleteMapping("/delete-message")
    public ResponseEntity<?> deleteMessage(@RequestParam String queueUrl, 
                                          @RequestParam String receiptHandle) {
        try {
            awsSqsService.deleteMessage(queueUrl, receiptHandle);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete message: " + e.getMessage());
        }
    }

    // Deletes a queue
    @DeleteMapping("/delete-queue")
    public ResponseEntity<?> deleteQueue(@RequestParam String queueUrl) {
        try {
            awsSqsService.deleteQueue(queueUrl);
            return ResponseEntity.ok("Queue deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete queue: " + e.getMessage());
        }
    }

    // Lists all queues
    @GetMapping("/list-queues")
    public ResponseEntity<?> listQueues() {
        try {
            List<String> queueUrls = awsSqsService.listQueues();
            return ResponseEntity.ok(queueUrls);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to list queues: " + e.getMessage());
        }
    }
}
