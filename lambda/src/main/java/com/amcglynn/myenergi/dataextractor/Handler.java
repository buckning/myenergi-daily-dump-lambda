package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Handler implements RequestHandler<SQSEvent, Void> {

    private final AmazonDynamoDB dbClient;

    public Handler() {
        dbClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Properties.getRegion()).build();
    }

    public Void handleRequest(SQSEvent event, Context context) {
        if (event.getRecords().isEmpty()) {
            System.out.println("Received no records");
            return null;
        }
        var dateStr = event.getRecords().get(0).getBody();
        System.out.println("SQS event received for date: " + dateStr);

        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        var date = LocalDate.parse(dateStr, formatter);

        if (date.isAfter(LocalDate.now())) {
            writeData();
            System.out.println("Cannot query data for dates in the future");
            return null;
        }

        event.getRecords().forEach(System.out::println);


        readData(dateStr).ifPresentOrElse(s -> System.out.println("Value from DB is " + s),
                () -> System.out.println("No reading found in DB for " + dateStr));

        System.out.println("Pushing next date of " + date.plus(1, ChronoUnit.DAYS).format(formatter));

        pushNextMessage(date.plus(1, ChronoUnit.DAYS).format(formatter));
        return null;
    }

    private void pushNextMessage(String body) {
        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        String queueUrl = "https://sqs.us-east-1.amazonaws.com/629904027893/raw-data-catch-up-queue.fifo";
        SendMessageRequest request = new SendMessageRequest(queueUrl, body)
                .withMessageGroupId("raw-data-request-" + body)
                .withMessageDeduplicationId(UUID.randomUUID().toString());

        sqs.sendMessage(request);
        System.out.println("Sent message to SQS for " + body);
    }

    public void writeData() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("date", new AttributeValue("20210101"));
        item.put("data", new AttributeValue("rawReadings"));
        item.put("samples", new AttributeValue().withN("1444"));
        var request = new PutItemRequest()
                .withTableName(Properties.getTableName())
                .withItem(item);
        dbClient.putItem(request);
    }

    public Optional<String> readData(String date) {
        var request = new GetItemRequest()
                .withTableName(Properties.getTableName())
                .addKeyEntry("date", new AttributeValue(date));

        var result = dbClient.getItem(request);
        if (result.getItem() == null) {
            return Optional.empty();
        }
        return Optional.of(result.getItem().get("data").getS());
    }
}
