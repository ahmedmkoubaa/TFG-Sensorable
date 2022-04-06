package com.commons;

public class SensorableConstants {
    // for requests
    public final static int REQUEST_PERMISSIONS_CODE = 1;
    public final static int REQUEST_ENABLE_BT = 2;
    public final static int REQUEST_ALARM_ID = 3;

    // for services and activities communication
    public final static String ADL_UPDATE = "AdlUpdates";
    public final static String EXTRA_MESSAGE = "extra_message";
    public final static String BROADCAST_MESSAGE = "broadcast_message";
    public final static String BROADCAST_LOCATION = "broacast_update_location";
    public final static String SERVICE_SENDS_INFO = "info";
    public final static String WEAR_SENDS_SENSOR_DATA = "SensorDataUpdates";
    public final static String MOBILE_SENDS_SENSOR_DATA = "MobileSendsSensorData";
    public final static String EMPATICA_SENDS_SENSOR_DATA = "EmpaticaDataUpdates";
    public final static String SENSORS_PROVIDER_SENDS_SENSORS = "SensorProviderSendsSensors";
    public final static String SENSORS_PROVIDER_SENDS_LOCATION = "SensorsProviderSendsLocation";

    public final static int COLLECTED_SENSOR_DATA_SIZE = 2048;
    public final static int WEAR_BUFFER_SIZE = 512;
    public final static int EMPATICA_BUFFER_SIZE = 512;
    public final static int SENSORS_PROVIDER_SERVICE_BUFFER_SIZE = 512;

    // for mobile database
    public final static String MOBILE_DATABASE_NAME = "default-mobile-database";
    public final static int MOBILE_DATABASE_NUMBER_THREADS = 1;
    public final static int MOBILE_DATABASE_VERSION = 25;

    // time that uses the adl detection to service to filter data
    // the lower is this value more accurate is the filtering, it means
    // it filter less info and process a bigger amount of data
    public final static int ADL_FILTER_TIME = 2000; // in milliseconds
    public final static long TIME_SINCE_LAST_ADL_DETECTION = 5 * 60 * 1000;

    public final static long TIME_SINCE_LAST_BLUETOOTH_DETECTION = 30 * 60 * 1000; // in milliseconds
    public final static int SCHEDULE_BLUETOOTH_DISCOVERY = 5 * 1000; // in milliseconds
    public final static int SCHEDULE_DATABASE_BACKUP = 5 * 60 * 1000; // in milliseconds


    // for mqtt client communication
    public static final int MQTT_BROKER_PORT = 1883;
    public static  final String MQTT_BROKER_HOST = "192.168.1.108";
    public static final int QTT_TIMEOUT = 4000;
    public static final int  MQTT_RECONNECT_PERIOD = 1000;
    public static final String MQTT_DEFAULT_USERNAME = "default-username";
    public static final String MQTT_DEFAULT_PASSWORD = "default-password";
    public static final String MQTT_TEST_TOPIC = "sensorable/test";
    public static final String  MQTT_CONNECT_URL = "broker.hivemq.com";

}
