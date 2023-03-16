package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amcglynn.myenergi.MyEnergiClient;

public class Handler implements RequestHandler<SQSEvent, Void> {

    private final MyEnergiClient myEnergiClient;
    private final ZappiHistoryRepository historyRepository;
    private final SqsMessageClient sqsMessageClient;
    private final boolean disabled;
    private HistoricZappiDataDumpService service;

    public Handler() {
        disabled = Properties.isDisabled();
        myEnergiClient = new MyEnergiClient(Properties.getSerialNumber(), Properties.getApiKey());
        historyRepository = new ZappiHistoryRepository();
        sqsMessageClient = new SqsMessageClient();
        service = new HistoricZappiDataDumpService(myEnergiClient, historyRepository, sqsMessageClient);
    }

    public Void handleRequest(SQSEvent event, Context context) {
        if (disabled) {
            System.out.println("This Lambda is disabled, exiting...");
            return null;
        }

        if (event.getRecords().isEmpty()) {
            System.out.println("Received no records, exiting...");
            return null;
        }

        var dateStr = event.getRecords().get(0).getBody();

        System.out.println("Received event to save Zappi data for date: " + dateStr);
        service.process(dateStr);

        return null;
    }
}
