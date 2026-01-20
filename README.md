# AWS SQS Spring Boot Integration

A Spring Boot web application demonstrating Amazon SQS operations through a responsive UI. Supports queue creation, message sending/receiving (producer/consumer pattern), and queue management using AWS SDK v2.

---

## AWS SQS Setup Guide

Before running this application, set up AWS SQS access and credentials.

**Complete setup instructions: [AWS SQS Setup Guide](AWS-SQS-Guide.md)**

**The guide covers:**
*   What is Amazon SQS and its use cases
*   IAM User creation and permissions
*   Access key generation
*   Environment configuration
*   IAM role setup for EC2
*   Troubleshooting common issues

---

## Features

*   Create SQS queues dynamically
*   Send messages to queue (Producer)
*   Receive messages from queue (Consumer with manual polling)
*   Delete messages after processing
*   Delete queues

**Access**: `http://localhost:8080`

---

## Architecture

**Backend** (Spring Boot):
*   `AwsConfig.java` - Initializes SQS client with credentials or IAM role
*   `AwsSqsService.java` - Handles SQS operations (create, send, receive, delete)
*   `AwsSqsController.java` - REST API endpoints at `/api/sqs/*`
*   `AwsSqsIntegrationWithSpringBootApplication.java` - Main application with .env loader

**Models**:
*   `GiftCard.java` - Data model for gift card messages
*   `ReceivedMessage.java` - Wrapper for received SQS messages

**Frontend** (HTML/CSS/JS):
*   `index.html` - Queue management page
*   `queue.html` - Producer/Consumer page with split layout
*   `style.css` - Responsive styling
*   `app.js` - Queue management operations
*   `queue.js` - Producer/Consumer operations with Axios

**API Endpoints:**
*   `POST /api/sqs/create-queue?queueName={name}`
    *   Creates a new SQS queue and returns queue URL
*   `GET /api/sqs/queue-url?queueName={name}`
    *   Retrieves queue URL by queue name
*   `GET /api/sqs/list-queues`
    *   Lists all available queues in the region
*   `POST /api/sqs/send-message`
    *   Sends JSON serialized gift card message to queue
*   `GET /api/sqs/receive-messages?queueUrl={url}&maxMessages={count}`
    *   Polls and receives messages from queue (up to 10)
*   `DELETE /api/sqs/delete-message?queueUrl={url}&receiptHandle={handle}`
    *   Deletes a specific message from queue after processing
*   `DELETE /api/sqs/delete-queue?queueUrl={url}`
    *   Deletes the entire queue

---

## Local Setup

**1. Clone the repository**
```bash
git clone <repository-url>
cd aws-sqs-spring-boot
```

**2. Configure environment variables**

Follow the **[AWS SQS Setup Guide](AWS-SQS-Guide.md)** to get credentials, then:
```bash
cp .env.example .env
```

Edit `.env` with your AWS credentials:
```env
AWS_SQS_ACCESS_KEY=your-access-key
AWS_SQS_SECRET_KEY=your-secret-key
AWS_SQS_REGION=us-west-1
```

**3. Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Access at `http://localhost:8080`

---

## Usage

### Queue Management Page (`/`)

**Create Queue**: Enter a unique queue name and click Create Queue.

**List Queues**: Click "Refresh Queue List" to view all queues.

**Get Queue URL**: Enter queue name to retrieve its URL.

**Delete Queue**: Enter queue URL and click Delete Queue.

### Producer/Consumer Page (`/queue.html`)

**Select Queue**: Enter queue URL or click "Load Available Queues" to select from list.

**Producer (Left Side)**:
*   Enter user name, select gift card type, enter amount and date
*   Click "Publish Message" to send to queue

**Consumer (Right Side)**:
*   Click "Poll Queue" to receive messages (manual polling)
*   View message details in list format
*   Click "Delete Message" to remove processed messages

---

## How Messages Work

**Message Format:**

The application sends structured JSON messages representing gift cards:

```json
{
  "userName": "Abhishek Kumar",
  "giftCardType": "Amazon",
  "amount": 150.00,
  "date": "2026-01-20"
}
```

**Producer Flow:**
1. Fill form with gift card details
2. Click "Publish Message"
3. Backend serializes data to JSON
4. JSON message sent to SQS queue
5. Success confirmation displayed

**Consumer Flow:**
1. Click "Poll Queue" button
2. Backend polls SQS (requests up to 10 messages)
3. Messages deserialized from JSON to GiftCard objects
4. Messages displayed in list format
5. Click "Delete Message" to remove from queue

**Important Notes:**
*   Polling may not return all messages at once (SQS distributed system behavior)
*   Messages remain in queue until explicitly deleted
*   Same message can be received multiple times if not deleted before visibility timeout expires

---

## Tech Stack

*   Spring Boot 3.5.9, Java 21, Maven
*   AWS SDK v2.29.3 (SQS, Auth, Regions)
*   dotenv-java 3.0.0
*   Frontend: HTML5, CSS3, Axios

---

## EC2 Deployment

The application supports deployment on EC2 instances using IAM roles instead of access keys.

**Local Development**: Uses IAM User with Access Keys (stored in `.env`)

**EC2 Production**: Uses IAM Role (no keys needed, more secure)

**Benefit**: Same JAR file works in both environments automatically.

---

### Automated Deployment (Recommended)

**First Time:**
```bash
cp deploy-first-time.sh.example deploy-first-time.sh
nano deploy-first-time.sh  # Update EC2_HOST and KEY_FILE
chmod +x deploy-first-time.sh
./deploy-first-time.sh
```

**Updates:**
```bash
cp deploy.sh.example deploy.sh
nano deploy.sh  # Update EC2_HOST and KEY_FILE
chmod +x deploy.sh
./deploy.sh
```

---

### Manual Deployment Steps

**1. Create IAM Role for EC2**

See **[AWS SQS Setup Guide - Section 9](AWS-SQS-Guide.md#9-iam-role-for-ec2-for-deployment)** for IAM role creation.

**2. Launch EC2 Instance**

1.  Go to **EC2** → **Launch instance**.
2.  **Name**: `sqs-spring-boot-app`
3.  **AMI**: Amazon Linux 2023 or Ubuntu 22.04
4.  **Instance type**: t3.micro (free tier eligible)
5.  **Key pair**: Create new or select existing
6.  **Network settings** - Configure Security Group:
    *   Allow SSH (port 22) from your IP
    *   Allow Custom TCP (port 8080) from anywhere (0.0.0.0/0)
7.  **Advanced details** → **IAM instance profile**: Select `ec2-sqs-role`
8.  Click **Launch instance**.

**3. Install Java on EC2**

SSH into your instance:
```bash
ssh -i your-key.pem ec2-user@your-ec2-public-ip
```

Install Java 21:
```bash
# Amazon Linux 2023
sudo yum install java-21-amazon-corretto -y

# Ubuntu
sudo apt update
sudo apt install openjdk-21-jdk -y

# Verify
java -version
```

**4. Build and Upload JAR**

On your local machine:
```bash
# Build JAR
./mvnw clean package

# Upload to EC2
scp -i your-key.pem target/aws-sqs-spring-boot-1.0.0-SNAPSHOT.jar ec2-user@your-ec2-ip:/home/ec2-user/
```

**5. Run Application on EC2**

SSH into EC2 and run:
```bash
java -jar aws-sqs-spring-boot-1.0.0-SNAPSHOT.jar
```

**Note**: No `.env` file needed on EC2. The application automatically detects the IAM role and uses it for authentication.

**Access your application:**
```
http://your-ec2-public-ip:8080
```

**6. Run as Background Service (Optional)**

Create systemd service file:
```bash
sudo nano /etc/systemd/system/sqs-app.service
```

Add:
```ini
[Unit]
Description=AWS SQS Spring Boot Application
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user
ExecStart=/usr/bin/java -jar aws-sqs-spring-boot-1.0.0-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable sqs-app
sudo systemctl start sqs-app
sudo systemctl status sqs-app
```

View logs:
```bash
sudo journalctl -u sqs-app -f
```

**7. Managing the Service**

```bash
# Stop the service
sudo systemctl stop sqs-app

# Start the service
sudo systemctl start sqs-app

# Restart the service
sudo systemctl restart sqs-app

# Check status
sudo systemctl status sqs-app

# View logs (follow)
sudo journalctl -u sqs-app -f

# Disable auto-start on boot
sudo systemctl disable sqs-app
```

---

## Troubleshooting

See **[AWS SQS Setup Guide - Troubleshooting](AWS-SQS-Guide.md#8-troubleshooting)** for detailed solutions.

*   **Access Denied**: Check credentials and IAM permissions
*   **Queue not found**: Ensure queue name is correct and matches region
*   **Region mismatch**: Match `.env` region with queue region
*   **Messages not appearing**: Click "Poll Queue" button (SQS is pull-based, not push)
*   **Deleted queue still showing**: AWS takes up to 60 seconds to propagate deletion across all servers
*   **Build fails**: Run `./mvnw clean install`
