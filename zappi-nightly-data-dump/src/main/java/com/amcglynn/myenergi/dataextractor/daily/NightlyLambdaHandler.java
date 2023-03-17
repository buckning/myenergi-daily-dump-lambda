package com.amcglynn.myenergi.dataextractor.daily;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amcglynn.myenergi.MyEnergiClient;
import com.amcglynn.myenergi.dataextractor.Properties;
import com.amcglynn.myenergi.dataextractor.ZappiHistoryRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class NightlyLambdaHandler implements RequestHandler<ScheduledEvent, ScheduledEvent> {

    private final MyEnergiClient myEnergiClient;
    private final ZappiHistoryRepository historyRepository;

    public NightlyLambdaHandler() {
        myEnergiClient = new MyEnergiClient(Properties.getSerialNumber(), Properties.getApiKey());
        historyRepository = new ZappiHistoryRepository();
    }

    @Override
    public ScheduledEvent handleRequest(ScheduledEvent scheduledEvent, Context context) {
        var theDayBeforeYesterday = LocalDate.now().minus(2, ChronoUnit.DAYS);

        var response = myEnergiClient.getZappiHistoryRaw(theDayBeforeYesterday);
        historyRepository.writeData(theDayBeforeYesterday, response.getValue(), response.getKey().getReadings().size());
        return null;
    }
}
