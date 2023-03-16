package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ZappiHistoryRepository {

    private final AmazonDynamoDB dbClient;

    public ZappiHistoryRepository() {
        dbClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Properties.getRegion()).build();
    }

    public void writeData(LocalDate date, String rawData, int sampleSize) {
        var dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("date", new AttributeValue(dateStr));
        item.put("data", new AttributeValue(rawData));
        item.put("samples", new AttributeValue().withN(Integer.toString(sampleSize)));
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
