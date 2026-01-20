// Create Queue
async function createQueue(event) {
    event.preventDefault();
    const queueName = document.getElementById('queueName').value;
    const resultDiv = document.getElementById('createQueueResult');

    try {
        const response = await axios.post(`/api/sqs/create-queue?queueName=${queueName}`);
        resultDiv.className = 'result';
        resultDiv.textContent = `${response.data.message} - Please wait a few seconds for the queue to appear in the list.`;
        document.getElementById('createQueueForm').reset();
        listQueues();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to create queue';
    }
}

// List Queues
async function listQueues() {
    const resultDiv = document.getElementById('queueListResult');
    const queueListDiv = document.getElementById('queueList');

    // Show loading state
    resultDiv.className = 'result';
    resultDiv.textContent = 'Loading queues...';
    queueListDiv.innerHTML = '<div class="empty-state">Please wait...</div>';

    try {
        const response = await axios.get('/api/sqs/list-queues');
        resultDiv.className = 'result';
        
        if (response.data.length === 0) {
            queueListDiv.innerHTML = '<div class="empty-state">No queues found</div>';
            resultDiv.textContent = 'No queues available';
        } else {
            resultDiv.textContent = `Found ${response.data.length} queue(s)`;
            queueListDiv.innerHTML = response.data.map(url => 
                `<div class="queue-item">${url}</div>`
            ).join('');
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to list queues';
        queueListDiv.innerHTML = '';
    }
}

// Get Queue URL
async function getQueueUrl(event) {
    event.preventDefault();
    const queueName = document.getElementById('getQueueName').value;
    const resultDiv = document.getElementById('getQueueUrlResult');

    try {
        const response = await axios.get(`/api/sqs/queue-url?queueName=${queueName}`);
        resultDiv.className = 'result';
        resultDiv.textContent = `Queue URL: ${response.data.queueUrl}`;
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Queue not found';
    }
}

// Delete Queue
async function deleteQueue(event) {
    event.preventDefault();
    const queueUrl = document.getElementById('deleteQueueUrl').value;
    const resultDiv = document.getElementById('deleteQueueResult');

    if (!confirm('Are you sure you want to delete this queue?')) {
        return;
    }

    try {
        const response = await axios.delete(`/api/sqs/delete-queue?queueUrl=${encodeURIComponent(queueUrl)}`);
        resultDiv.className = 'result';
        resultDiv.textContent = response.data + ' - Please wait up to 60 seconds for the queue to disappear from the list.';
        document.getElementById('deleteQueueForm').reset();
        listQueues();
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = error.response?.data || 'Failed to delete queue';
    }
}

// Load queues on page load
window.onload = () => {
    listQueues();
};
