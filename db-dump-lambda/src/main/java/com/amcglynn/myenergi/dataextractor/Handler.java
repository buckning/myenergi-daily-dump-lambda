package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amcglynn.myenergi.MyEnergiClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Handler implements RequestHandler<SQSEvent, Void> {

    private final MyEnergiClient myEnergiClient;
    private final ZappiHistoryRepository historyRepository;
    private final SqsMessageClient sqsMessageClient;
    private final boolean disabled;

    public Handler() {
        disabled = Properties.isDisabled();
        myEnergiClient = new MyEnergiClient(Properties.getSerialNumber(), Properties.getApiKey());
        historyRepository = new ZappiHistoryRepository();
        sqsMessageClient = new SqsMessageClient();
    }

    public Void handleRequest(SQSEvent event, Context context) {
        if (event.getRecords().isEmpty()) {
            System.out.println("Received no records");
            return null;
        }
        var dateStr = event.getRecords().get(0).getBody();
        System.out.println("SQS event received for date: " + dateStr);
        if (disabled) {
            System.out.println("This Lambda is disabled, exiting...");
            return null;
        }


        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        var date = LocalDate.parse(dateStr, formatter);

        if (date.isAfter(LocalDate.now())) {
            System.out.println("Cannot query data for dates in the future");
            return null;
        }

        var response = myEnergiClient.getZappiHistoryRaw(date);
        historyRepository.writeData(date, response.getValue(), response.getKey().getReadings().size());
        event.getRecords().forEach(System.out::println);


        historyRepository.readData(dateStr).ifPresentOrElse(s -> System.out.println("Value from DB is " + s),
                () -> System.out.println("No reading found in DB for " + dateStr));

        System.out.println("Pushing next date of " + date.plus(1, ChronoUnit.DAYS).format(formatter));

        sqsMessageClient.scheduleSqsMessage(date.plus(1, ChronoUnit.DAYS).format(formatter));
        return null;
    }
}
