package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.regions.Regions;

public class Properties {

    public static String getTableName() {
        return System.getenv("tableName");
    }

    public static Regions getRegion() {
        return Regions.fromName(System.getenv("dbRegion"));
    }

    public static String getApiKey() {
        return System.getenv("myEnergiHubApiKey");
    }

    public static String getSerialNumber() {
        return System.getenv("myEnergiHubSerialNumber");
    }

    public static boolean isDisabled() {
        return Boolean.parseBoolean(System.getenv("disabled"));
    }

    public static String getDataDumpSqsUrl() {
        return System.getenv("dataDumpSqsUrl");
    }

    private Properties() { }
}
