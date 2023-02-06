"use strict";
exports.__esModule = true;
exports.SensorIdentifier = exports.SensorAction = exports.DATABASE_ACTIONS = exports.DATABASE_TABLES = exports.MQTT_ACTIVITIES_INSERT = exports.MQTT_REQUEST_ACTIVITIES = exports.MQTT_INFORM_ACTIVITIES = exports.MQTT_INFORM_GENERIC_ADLS = exports.MQTT_REQUEST_GENERIC_ADLS = exports.MQQTT_INFORM_CUSTOM_ADLS = exports.MQTT_REQUEST_CUSTOM_ADLS = exports.MQTT_SENSORS_INSERT = exports.MQTT_CONNECT_URL = exports.JSON_TABLES_SEPARATOR = exports.JSON_FIELDS_SEPARATOR = exports.MQTT_TEST_TOPIC = exports.MQTT_DEFAULT_PASSWORD = exports.MQTT_DEFAULT_USERNAME = exports.MQTT_RECONNECT_PERIOD = exports.MQTT_TIMEOUT = exports.MQTT_BROKER_HOST = exports.MQTT_BROKER_PORT = void 0;
// MQTT settings constants
exports.MQTT_BROKER_PORT = 1883;
exports.MQTT_BROKER_HOST = "broker.hivemq.com";
exports.MQTT_TIMEOUT = 4000;
exports.MQTT_RECONNECT_PERIOD = 1000;
exports.MQTT_DEFAULT_USERNAME = "default-username";
exports.MQTT_DEFAULT_PASSWORD = "default-password";
exports.MQTT_TEST_TOPIC = "sensorable/test";
exports.JSON_FIELDS_SEPARATOR = "|";
exports.JSON_TABLES_SEPARATOR = "#";
exports.MQTT_CONNECT_URL = "mqtt://".concat(exports.MQTT_BROKER_HOST, ":").concat(exports.MQTT_BROKER_PORT);
// MQTT communication constants
exports.MQTT_SENSORS_INSERT = "sensorable/database/sensors/insert";
exports.MQTT_REQUEST_CUSTOM_ADLS = "sensorable/database/adls/custom/request";
exports.MQQTT_INFORM_CUSTOM_ADLS = "sensorable/database/adls/custom/inform";
exports.MQTT_REQUEST_GENERIC_ADLS = "sensorable/database/adls/generics/request";
exports.MQTT_INFORM_GENERIC_ADLS = "sensorable/database/adls/generics/inform";
exports.MQTT_INFORM_ACTIVITIES = "sensorable/database/activities/inform";
exports.MQTT_REQUEST_ACTIVITIES = "sensorable/database/activities/request";
exports.MQTT_ACTIVITIES_INSERT = "sensorable/database/activities/insert";
// Names of database tables
var DATABASE_TABLES;
(function (DATABASE_TABLES) {
    DATABASE_TABLES["SENSORS"] = "sensors";
    DATABASE_TABLES["USERS"] = "users";
    DATABASE_TABLES["ADLS"] = "adls";
    DATABASE_TABLES["EVENTS"] = "events";
})(DATABASE_TABLES = exports.DATABASE_TABLES || (exports.DATABASE_TABLES = {}));
// Actions to perform on the database
var DATABASE_ACTIONS;
(function (DATABASE_ACTIONS) {
    DATABASE_ACTIONS["INSERT"] = "insert";
    DATABASE_ACTIONS["UPDATE"] = "update";
    DATABASE_ACTIONS["DELETE"] = "delete";
    DATABASE_ACTIONS["SELECT"] = "select";
})(DATABASE_ACTIONS = exports.DATABASE_ACTIONS || (exports.DATABASE_ACTIONS = {}));
// Codes of sensors, each one indicates an action
var SensorAction;
(function (SensorAction) {
    SensorAction[SensorAction["FIRST"] = 0] = "FIRST";
    SensorAction[SensorAction["SECOND"] = 1] = "SECOND";
    SensorAction[SensorAction["THIRD"] = 2] = "THIRD";
    SensorAction[SensorAction["DISTANCE"] = 3] = "DISTANCE";
    SensorAction[SensorAction["ANY"] = 4] = "ANY";
    SensorAction[SensorAction["ALL"] = 5] = "ALL";
})(SensorAction = exports.SensorAction || (exports.SensorAction = {}));
// Constants to indetify sensors types (based on android studio coding)
var SensorIdentifier;
(function (SensorIdentifier) {
    SensorIdentifier[SensorIdentifier["TYPE_ACCELEROMETER"] = 1] = "TYPE_ACCELEROMETER";
    SensorIdentifier[SensorIdentifier["TYPE_LIGHT"] = 5] = "TYPE_LIGHT";
    SensorIdentifier[SensorIdentifier["TYPE_PRESSURE"] = 6] = "TYPE_PRESSURE";
    SensorIdentifier[SensorIdentifier["TYPE_PROXIMITY"] = 8] = "TYPE_PROXIMITY";
    SensorIdentifier[SensorIdentifier["TYPE_LINEAR_ACCELERATION"] = 10] = "TYPE_LINEAR_ACCELERATION";
    SensorIdentifier[SensorIdentifier["TYPE_RELATIVE_HUMIDITY"] = 12] = "TYPE_RELATIVE_HUMIDITY";
    SensorIdentifier[SensorIdentifier["TYPE_AMBIENT_TEMPERATURE"] = 13] = "TYPE_AMBIENT_TEMPERATURE";
    SensorIdentifier[SensorIdentifier["TYPE_STEP_DETECTOR"] = 18] = "TYPE_STEP_DETECTOR";
    SensorIdentifier[SensorIdentifier["TYPE_STEP_COUNTER"] = 19] = "TYPE_STEP_COUNTER";
    SensorIdentifier[SensorIdentifier["TYPE_HEART_RATE"] = 21] = "TYPE_HEART_RATE";
    SensorIdentifier[SensorIdentifier["TYPE_GPS"] = 2411] = "TYPE_GPS";
})(SensorIdentifier = exports.SensorIdentifier || (exports.SensorIdentifier = {}));
