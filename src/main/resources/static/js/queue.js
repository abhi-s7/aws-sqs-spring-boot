// Load queues for selection
async function loadQueues() {
    const queueSelectListDiv = document.getElementById('queueSelectList');

    try {
        const response = await axios.get('/api/sqs/list-queues');
        
        if (response.data.length === 0) {
            queueSelectListDiv.innerHTML = '<div class="empty-state">No queues available</div>';
        } else {
            queueSelectListDiv.innerHTML = response.data.map(url => 
                `<div class="queue-item" style="cursor: pointer;" onclick="selectQueue('${url}')">${url}</div>`
            ).join('');
        }
    } catch (error) {
        queueSelectListDiv.innerHTML = '<div class="error">Failed to load queues</div>';
    }
}

// Select queue from list
function selectQueue(queueUrl) {
    document.getElementById('selectedQueueUrl').value = queueUrl;
}

// Send message to queue (Producer)
async function sendMessage(event) {
    event.preventDefault();
    const queueUrl = document.getElementById('selectedQueueUrl').value;
    const userName = document.getElementById('userName').value;
    const giftCardType = document.getElementById('giftCardType').value;
    const amount = parseFloat(document.getElementById('amount').value);
    const date = document.getElementById('date').value;
    const resultDiv = document.getElementById('producerResult');

    if (!queueUrl) {
        resultDiv.className = 'result error';
        resultDiv.textContent = 'Please select a queue URL first';
        return;
    }

    try {
        const response = await axios.post('/api/sqs/send-message', {
            queueUrl: queueUrl,
            giftCard: {
                userName: userName,
                giftCardType: giftCardType,
                amount: amount,
                date: date
            }
        });
        
        resultDiv.className = 'result';
        resultDiv.textContent = `${response.data.message} (ID: ${response.data.messageId})`;
        document.getElementById('producerForm').reset();
        
        // Set today's date as default
        document.getElementById('date').valueAsDate = new Date();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to send message';
    }
}

// Poll messages from queue (Consumer)
async function pollMessages() {
    const queueUrl = document.getElementById('selectedQueueUrl').value;
    const resultDiv = document.getElementById('consumerResult');
    const messageListDiv = document.getElementById('messageList');

    if (!queueUrl) {
        resultDiv.className = 'result error';
        resultDiv.textContent = 'Please select a queue URL first';
        return;
    }

    try {
        const response = await axios.get(`/api/sqs/receive-messages?queueUrl=${encodeURIComponent(queueUrl)}&maxMessages=10`);
        
        if (response.data.length === 0) {
            resultDiv.className = 'result';
            resultDiv.textContent = 'No messages available in queue';
            messageListDiv.innerHTML = '<div class="empty-state">Queue is empty</div>';
        } else {
            resultDiv.className = 'result';
            resultDiv.textContent = `Received ${response.data.length} message(s)`;
            
            messageListDiv.innerHTML = response.data.map(msg => `
                <div class="message-item">
                    <p><strong>User:</strong> ${msg.giftCard.userName}</p>
                    <p><strong>Gift Card:</strong> ${msg.giftCard.giftCardType}</p>
                    <p><strong>Amount:</strong> $${msg.giftCard.amount}</p>
                    <p><strong>Date:</strong> ${msg.giftCard.date}</p>
                    <p><strong>Message ID:</strong> ${msg.messageId}</p>
                    <button onclick="deleteMessage('${queueUrl}', '${msg.receiptHandle}')" class="danger">Delete Message</button>
                </div>
            `).join('');
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to poll messages';
    }
}

// Delete a specific message
async function deleteMessage(queueUrl, receiptHandle) {
    try {
        await axios.delete(`/api/sqs/delete-message?queueUrl=${encodeURIComponent(queueUrl)}&receiptHandle=${encodeURIComponent(receiptHandle)}`);
        alert('Message deleted successfully');
        pollMessages(); // Refresh the message list
    } catch (error) {
        alert('Failed to delete message: ' + (error.response?.data || error.message));
    }
}

// Set today's date as default on page load
window.onload = () => {
    document.getElementById('date').valueAsDate = new Date();
    loadQueues();
};
