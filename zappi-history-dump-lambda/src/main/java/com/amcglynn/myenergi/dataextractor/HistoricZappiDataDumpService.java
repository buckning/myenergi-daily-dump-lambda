package com.amcglynn.myenergi.dataextractor;

import com.amcglynn.myenergi.MyEnergiClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HistoricZappiDataDumpService {

    private MyEnergiClient myEnergiClient;
    private ZappiHistoryRepository historyRepository;
    private SqsMessageClient sqsMessageClient;

    public HistoricZappiDataDumpService(MyEnergiClient myEnergiClient,
                                        ZappiHistoryRepository historyRepository,
                                        SqsMessageClient sqsMessageClient) {
        this.myEnergiClient = myEnergiClient;
        this.historyRepository = historyRepository;
        this.sqsMessageClient = sqsMessageClient;
    }

    public void process(String dateStr) {
        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        var date = LocalDate.parse(dateStr, formatter);

        if (date.isAfter(LocalDate.now())) {
            System.out.println("Cannot query data for dates in the future");
            return;
        }

        var response = myEnergiClient.getZappiHistoryRaw(date);
        historyRepository.writeData(date, response.getValue(), response.getKey().getReadings().size());

        sqsMessageClient.scheduleSqsMessage(date.plus(1, ChronoUnit.DAYS).format(formatter));
    }
}
