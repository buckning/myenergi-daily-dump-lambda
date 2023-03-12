package com.amcglynn.myenergi.dataextractor;

import com.amazonaws.regions.Regions;

public class Properties {

    public static String getTableName() {
        return System.getenv("tableName");
    }

    public static Regions getRegion() {
        return Regions.fromName(System.getenv("dbRegion"));
    }
}
