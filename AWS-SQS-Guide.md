# AWS SQS Setup Guide

This document outlines the end-to-end process for setting up AWS SQS access for the **AWS-SQS-Spring-Boot** application.

---

## What is Amazon SQS?

**Amazon SQS (Simple Queue Service)** is AWS's fully managed message queuing service for decoupling and scaling microservices, distributed systems, and serverless applications. Messages remain in the queue until processed and deleted. Common uses include task queues, event-driven architectures, background job processing, and microservice communication.

---

## 1. Create an IAM User (Identity)

We create a dedicated IAM user with programmatic access instead of using root credentials for security best practices.

**Steps:**
1.  Go to **IAM** → **Users** → **Create user**.
2.  **User name**: `sqs-spring-boot-user`
3.  **Access type**: Enable **Programmatic access** (this generates Access Keys).
4.  Click **Next: Permissions**.

---

## 2. Attach SQS Permissions Policy

The user needs permission to perform SQS operations (create queues, send/receive messages, delete messages and queues).

### Option A: Full SQS Access (Development/Testing)
1.  Select **Attach existing policies directly**.
2.  Search for `AmazonSQSFullAccess`.
3.  Check the box and click **Next: Tags** → **Next: Review** → **Create user**.

### Option B: Custom Minimal Policy (Production - Recommended)
If you want tighter security, create a custom policy with only required permissions:

**Steps:**
1.  Go to **IAM** → **Policies** → **Create policy**.
2.  Click **JSON** tab and paste the following policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sqs:CreateQueue",
                "sqs:DeleteQueue",
                "sqs:GetQueueUrl",
                "sqs:ListQueues",
                "sqs:SendMessage",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueAttributes"
            ],
            "Resource": "*"
        }
    ]
}
```

3.  Click **Next: Tags** → **Next: Review**.
4.  **Policy name**: `SQS-Spring-Boot-Custom-Policy`
5.  Click **Create policy**.
6.  Go back to your user → **Add permissions** → **Attach policies directly** → Select your custom policy.

---

## 3. Generate Access Keys

After creating the user, you need to generate access keys for programmatic access. AWS will show you the **Access Key ID** and **Secret Access Key** ONCE. You must save these immediately.

**Steps:**
1.  Go to **IAM** → **Users** → Select your user (`sqs-spring-boot-user`).
2.  Click **Security credentials** tab.
3.  Scroll to **Access keys** section → Click **Create access key**.
4.  Select **Use case**: Choose **Application running outside AWS**.
5.  Click **Next** → Add description (optional) → Click **Create access key**.
6.  AWS displays:
    *   **Access key ID**: `AKIAIOSFODNN7EXAMPLE`
    *   **Secret access key**: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`
7.  Click **Download .csv file** to save these credentials.
8.  **Important**: Keep these secure. Never commit them to version control.

**If you lose the keys:**
*   You cannot retrieve the secret key again.
*   You must create a new access key pair.
*   Delete old keys if no longer needed for security.

---

## 4. Understanding SQS Queues

**Queue Types:**
*   **Standard Queue**: Maximum throughput, best-effort ordering, at-least-once delivery
*   **FIFO Queue**: First-In-First-Out ordering, exactly-once processing (queue name must end with `.fifo`)

**This application creates Standard queues by default.**

**Key Concepts:**

**Visibility Timeout:**
*   When a message is received, it becomes invisible to other consumers for 30 seconds (default)
*   This prevents duplicate processing
*   If not deleted within timeout, message reappears in queue

**Message Retention:**
*   Messages remain in queue for 4 days by default (configurable: 1 minute to 14 days)
*   Messages are automatically deleted after retention period

**Polling:**
*   SQS is pull-based (not push)
*   Applications must poll the queue to receive messages
*   This application uses manual polling via "Poll Queue" button

---

## 5. Configure Application Environment Variables

Now that you have your AWS credentials, configure the application to use them.

**Steps:**

1.  **Navigate to project directory**:
    ```bash
    cd aws-sqs-spring-boot
    ```

2.  **Copy the example file**:
    ```bash
    cp .env.example .env
    ```

3.  **Edit the `.env` file**:
    ```bash
    # On Mac/Linux
    nano .env
    
    # On Windows
    notepad .env
    ```

4.  **Fill in your AWS credentials**:
    ```env
    AWS_SQS_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
    AWS_SQS_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    AWS_SQS_REGION=us-west-1
    ```

    *Replace the example values with your actual credentials from Step 3.*

5.  **Save and close** the file.

**Security Note:**
*   The `.env` file is already added to `.gitignore` to prevent accidental commits.
*   Never share your Secret Access Key publicly or commit it to version control.
*   If you accidentally expose your keys, delete them immediately in IAM and create new ones.

---

## 6. Verify Configuration

**1. Start the application:**
```bash
./mvnw spring-boot:run
```

**2. Access the UI:**
```
http://localhost:8080
```

**3. Create a test queue:**
*   Enter a queue name (e.g., `test-queue-2026`)
*   Click "Create Queue"
*   You should see a success message with the queue URL

**4. Verify in AWS Console:**
*   Go to **SQS** in AWS Console
*   You should see your newly created queue

---

## 7. Common SQS Operations

**Create Queue:**
*   Queue names must be unique within your AWS account and region
*   Can contain alphanumeric characters, hyphens, and underscores
*   Maximum 80 characters

**Send Message:**
*   Maximum message size: 1024 KB
*   Messages stored for 4 days by default
*   No limit on number of messages in queue

**Receive Message:**
*   Can receive up to 10 messages per poll
*   Long polling (5 seconds) reduces empty responses
*   Messages become invisible after retrieval

**Delete Message:**
*   Requires receipt handle (unique identifier for received message)
*   Must be deleted within visibility timeout
*   If not deleted, message reappears in queue

---

## 8. Troubleshooting

### Error: "Access Denied"
*   **Cause**: Invalid credentials or insufficient IAM permissions.
*   **Fix**: 
    1.  Verify your Access Key and Secret Key in `.env`.
    2.  Check IAM user has SQS permissions attached.
    3.  Ensure no typos in credentials (no extra spaces).

### Error: "Queue does not exist"
*   **Cause**: Queue name is incorrect or doesn't exist in the specified region.
*   **Fix**: 
    1.  Verify queue name is correct.
    2.  Check `AWS_SQS_REGION` matches queue region.
    3.  List queues to see available queues.

### Error: "The security token included in the request is invalid"
*   **Cause**: Access key has been deleted or deactivated in IAM.
*   **Fix**: Create new access keys in IAM console and update `.env`.

### Error: "AWS was not able to validate the provided access credentials"
*   **Cause**: Secret key is incorrect or contains extra characters.
*   **Fix**: Re-download credentials CSV and carefully copy the secret key (watch for trailing spaces).

### Messages Not Appearing After Polling
*   **Cause**: SQS doesn't guarantee all messages returned in one poll.
*   **Fix**: 
    1.  Poll multiple times.
    2.  Messages might be invisible (already being processed by another consumer).
    3.  Check queue has messages in AWS Console.

### Message Appears Multiple Times
*   **Cause**: Message not deleted before visibility timeout expired.
*   **Fix**: Delete messages after processing using "Delete Message" button.

### Region Mismatch Error
*   **Cause**: Queue exists in different region than specified.
*   **Fix**: Update `AWS_SQS_REGION` in `.env` to match queue region.

---

## 9. IAM Role for EC2 (For Deployment)

When deploying to EC2, create an IAM role instead of using access keys.

**1. Create IAM Role for EC2**

1.  Go to **IAM** → **Roles** → **Create role**.
2.  **Trusted entity type**: AWS service
3.  **Use case**: EC2
4.  Click **Next**.
5.  **Permissions**: Search and select `AmazonSQSFullAccess`
6.  Click **Next**.
7.  **Role name**: `ec2-sqs-role`
8.  Click **Create role**.

**Note**: Attach this role to your EC2 instance when launching. See README.md for complete EC2 deployment steps.

---

## 10. Best Practices

*   **Never hardcode credentials**: Use environment variables or IAM roles.
*   **Use IAM roles on EC2**: Attach an IAM role instead of access keys when deploying to EC2.
*   **Delete processed messages**: Prevent duplicate processing and reduce costs.
*   **Set appropriate visibility timeout**: Match your processing time.
*   **Use dead-letter queues**: Capture messages that fail processing repeatedly.
*   **Monitor queue metrics**: Track message count, age, and processing time.
*   **Choose correct region**: Select closest to your application for lower latency.
*   **Enable encryption**: Use server-side encryption for sensitive data.
