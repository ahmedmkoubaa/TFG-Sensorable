package com.commons.utils;

import android.hardware.Sensor;
import android.util.Pair;

import java.util.Arrays;
import java.util.List;

public class SensorableConstants {
    // for time operations
    public final static int MINUTES_TO_SECONDS = 60;
    public final static int SECONDS_TO_MILLIS = 1000;
    public static final double BACKUP_PART_SIZE = 10000;

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
    public final static String ACTIVITY_ID = "activity_id";

    //for syntactical separators at mqtt communication with nodejs services
    public final static String JSON_TABLES_SEPARATOR = "#";
    public final static String JSON_FIELDS_SEPARATOR = "\\|";
    public final static String JSON_ROWS_SEPARATOR = "\\}\\{";
    public final static String MQTT_FIELDS_SEPARATOR = "/";

    // code number to identify gps as a sensor
    public final static int SENSOR_GPS = 2411;

    // Umbrals and limits of data to send
    public final static int COLLECTED_SENSOR_DATA_SIZE = 1024;
    public final static int WEAR_SENDING_BUFFER_SIZE = 1024;
    public final static int EMPATICA_BUFFER_SIZE = 128;
    public final static int SENSORS_PROVIDER_SERVICE_BUFFER_SIZE = 512;
    public static final int MAX_WEAR_OS_COLLECTED_DATA = 2056;


    // for mobile database
    public final static String SENSORABLE_DATABASE_NAME = "default-mobile-database";
    public final static String WEAR_DATABASE_NAME = "default-wear-database";
    public final static int MOBILE_DATABASE_NUMBER_THREADS = 1;
    public final static int MOBILE_DATABASE_VERSION = 62;

    // time that uses the adl detection to service to filter data
    // the lower is this value more accurate is the filtering, it means
    // it filter less info and process a bigger amount of data
    public final static int ADL_FILTER_TIME = 2 * SECONDS_TO_MILLIS; // in milliseconds
    public final static long TIME_SINCE_LAST_ADL_DETECTION = 5 * MINUTES_TO_SECONDS * SECONDS_TO_MILLIS;
    public final static long TIME_SINCE_LAST_BLUETOOTH_DETECTION = 30 * MINUTES_TO_SECONDS * SECONDS_TO_MILLIS; // in milliseconds (those are 30 minutes)
    public final static int SCHEDULE_BLUETOOTH_DISCOVERY = 5 * SECONDS_TO_MILLIS; // in milliseconds
    public final static int SCHEDULE_DATABASE_BACKUP = 5000; // 5 * MINUTES_TO_SECONDS * SECONDS_TO_MILLIS; // in milliseconds
    public static final int SCHEDULE_LOGGER_REFRESH = 10 * SECONDS_TO_MILLIS;
    public final static int TIME_SINCE_LAST_HEART_CHART_UPDATE = 5 * SECONDS_TO_MILLIS;


    // for mqtt client communication
    public static final int MQTT_BROKER_PORT = 1883;
    public static final String MQTT_BROKER_HOST = "192.168.1.108";
    public static final String MQTT_DEFAULT_USERNAME = "default-username";
    public static final String MQTT_DEFAULT_PASSWORD = "default-password";
    public static final String MQTT_TEST_TOPIC = "sensorable/test";
    public static final String MQTT_CONNECT_URL = "broker.hivemq.com";

    public static final String MQTT_SENSORS_INSERT = "sensorable/database/sensors/insert";
    public static final String MQTT_REQUEST_CUSTOM_ADLS = "sensorable/database/adls/custom/request";
    public static final String MQQTT_INFORM_CUSTOM_ADLS = "sensorable/database/adls/custom/inform";
    public static final String MQTT_REQUEST_GENERIC_ADLS = "sensorable/database/adls/generics/request";
    public static final String MQTT_INFORM_GENERIC_ADLS = "sensorable/database/adls/generics/inform";
    public static final String MQTT_INFORM_ACTIVITIES = "sensorable/database/activities/inform";
    public static final String MQTT_REQUEST_ACTIVITIES = "sensorable/database/activities/request";
    public static final String MQTT_ACTIVITIES_INSERT = "sensorable/database/activities/insert";


    public static final String DATE_SEPARATOR = "/";
    public static final String TIME_SEPARATOR = ":";


    public static final int MQTT_TIMEOUT = 4 * SECONDS_TO_MILLIS;
    public static final int MQTT_RECONNECT_PERIOD = 1 * SECONDS_TO_MILLIS;

    public static final String LOGIN_DONE = "login_done";
    public static final String USER_SESSION_CODE = "user_session_code";
    public static final int MAX_WEAR_OS_LOGGER_ELEMENTS = 3;

    // for sensors shown in screen
    public static final double DISTANCE_OF_STEP_IN_M = 0.5;

    // action name for intents
    public static final String SENSORS_PROVIDER_DEVICE_TYPE = "DEVICE_TYPE";
    public static final String SENSORS_PROVIDER_ACTION = "SENSORS_PROVIDER_ACTION";
    public static final String SENSORS_PROVIDER_LOCATION = "SENSORS_PROVIDER_LOCATION";
    public static final String ROOT_DIRECTOTY_NAME = "sensorable";
    public static final String FILE_EXTENSION_SEPARATOR = ".";
    public static final String CSV_EXTENSION = "csv";
    public static final String FILE_PATH_SEPARATOR = "/";


    public static final List<Pair<Integer, String>> LISTENED_SENSORS = Arrays.asList(
            new Pair(Sensor.TYPE_PROXIMITY, "TYPE_PROXIMITY"),
            new Pair(Sensor.TYPE_HEART_RATE, "TYPE_HEART_RATE"),
            new Pair(Sensor.TYPE_STEP_COUNTER, "TYPE_STEP_COUNTER"),
            new Pair(Sensor.TYPE_LIGHT, "TYPE_LIGHT"),
            new Pair(Sensor.TYPE_ACCELEROMETER, "TYPE_ACCELEROMETER"),
            new Pair(Sensor.TYPE_LINEAR_ACCELERATION, "TYPE_LINEAR_ACCELERATION"),
            new Pair(Sensor.TYPE_RELATIVE_HUMIDITY, "TYPE_RELATIVE_HUMIDITY"),
            new Pair(Sensor.TYPE_AMBIENT_TEMPERATURE, "TYPE_AMBIENT_TEMPERATURE")
    );


    public static final int MAX_COLLECTED_DATA_EXPORT_CSV = 8192 ;
    public static final String ACTIVITIES_REGISTRY_PATH = "ACTIVITIES_REGISTRY";
    public static final String ACTIVITIES_FILE_NAME = "ACTIVITIES";
}
