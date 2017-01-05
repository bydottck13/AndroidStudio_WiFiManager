package com.cybernut.wifimanager.model;

import java.util.HashMap;
import java.util.Map;

public class WiFiList {

    public static final Map<String, String> WIFI_CHANNELS = new HashMap<>();
    static{
        WIFI_CHANNELS.put("2412", "2.4G Ch01");
        WIFI_CHANNELS.put("2417", "2.4G Ch02");
        WIFI_CHANNELS.put("2422", "2.4G Ch03");
        WIFI_CHANNELS.put("2427", "2.4G Ch04");
        WIFI_CHANNELS.put("2432", "2.4G Ch05");
        WIFI_CHANNELS.put("2437", "2.4G Ch06");
        WIFI_CHANNELS.put("2442", "2.4G Ch07");
        WIFI_CHANNELS.put("2447", "2.4G Ch08");
        WIFI_CHANNELS.put("2452", "2.4G Ch09");
        WIFI_CHANNELS.put("2457", "2.4G Ch10");
        WIFI_CHANNELS.put("2462", "2.4G Ch11");
        WIFI_CHANNELS.put("2467", "2.4G Ch12");
        WIFI_CHANNELS.put("2472", "2.4G Ch13");
        WIFI_CHANNELS.put("2484", "2.4G Ch14");
    }
}
