package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.UUID;

public class SqsMessageClient {
    private final AmazonSQS sqs;

    public SqsMessageClient() {
        sqs = AmazonSQSClientBuilder.defaultClient();
    }

    public void scheduleSqsMessage(String body) {
        var request = new SendMessageRequest(Properties.getDataDumpSqsUrl(), body)
                .withMessageGroupId("raw-data-request-" + body)
                .withMessageDeduplicationId(UUID.randomUUID().toString());
        sqs.sendMessage(request);
    }
}
