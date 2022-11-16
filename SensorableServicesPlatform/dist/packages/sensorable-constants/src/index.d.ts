export declare const MQTT_BROKER_PORT = 1883;
export declare const MQTT_BROKER_HOST = "broker.hivemq.com";
export declare const MQTT_TIMEOUT = 4000;
export declare const MQTT_RECONNECT_PERIOD = 1000;
export declare const MQTT_DEFAULT_USERNAME = "default-username";
export declare const MQTT_DEFAULT_PASSWORD = "default-password";
export declare const MQTT_TEST_TOPIC = "sensorable/test";
export declare const JSON_FIELDS_SEPARATOR = "|";
export declare const JSON_TABLES_SEPARATOR = "#";
export declare const MQTT_CONNECT_URL: string;
export declare const MQTT_SENSORS_INSERT = "sensorable/database/sensors/insert";
export declare const MQTT_REQUEST_CUSTOM_ADLS = "sensorable/database/adls/custom/request";
export declare const MQQTT_INFORM_CUSTOM_ADLS = "sensorable/database/adls/custom/inform";
export declare const MQTT_REQUEST_GENERIC_ADLS = "sensorable/database/adls/generics/request";
export declare const MQTT_INFORM_GENERIC_ADLS = "sensorable/database/adls/generics/inform";
export declare const MQTT_INFORM_ACTIVITIES = "sensorable/database/activities/inform";
export declare const MQTT_REQUEST_ACTIVITIES = "sensorable/database/activities/request";
export declare const MQTT_ACTIVITIES_INSERT = "sensorable/database/activities/insert";
export declare enum DATABASE_TABLES {
    SENSORS = "sensors",
    USERS = "users",
    ADLS = "adls",
    EVENTS = "events"
}
export declare enum DATABASE_ACTIONS {
    INSERT = "insert",
    UPDATE = "update",
    DELETE = "delete",
    SELECT = "select"
}
export declare enum SensorAction {
    FIRST = 0,
    SECOND = 1,
    THIRD = 2,
    DISTANCE = 3,
    ANY = 4,
    ALL = 5
}
export declare enum SensorIdentifier {
    TYPE_ACCELEROMETER = 1,
    TYPE_LIGHT = 5,
    TYPE_PRESSURE = 6,
    TYPE_PROXIMITY = 8,
    TYPE_LINEAR_ACCELERATION = 10,
    TYPE_RELATIVE_HUMIDITY = 12,
    TYPE_AMBIENT_TEMPERATURE = 13,
    TYPE_STEP_DETECTOR = 18,
    TYPE_STEP_COUNTER = 19,
    TYPE_HEART_RATE = 21,
    TYPE_GPS = 2411
}
//# sourceMappingURL=index.d.ts.map