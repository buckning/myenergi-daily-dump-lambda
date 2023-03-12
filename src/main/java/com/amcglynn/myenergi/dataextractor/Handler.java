package com.amcglynn.myenergi.dataextractor;

import java.time.LocalDateTime;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<ScheduledEvent, ScheduledEvent> {

    public ScheduledEvent handleRequest(ScheduledEvent event, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Properties.getRegion()).build();

        var request = new GetItemRequest()
                .withTableName(Properties.getTableName())
                .addKeyEntry("date", new AttributeValue("20230309"));

        var result = client.getItem(request);
        var rawReading = result.getItem().get("data").getS();
        System.out.println("Value from DB is " + rawReading);

        return event;
    }
}
