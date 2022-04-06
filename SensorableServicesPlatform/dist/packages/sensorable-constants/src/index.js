"use strict";
exports.__esModule = true;
exports.DATABASE_ACTIONS = exports.DATABASE_TABLES = exports.MQTT_CONNECT_URL = exports.MQTT_TEST_TOPIC = exports.MQTT_DEFAULT_PASSWORD = exports.MQTT_DEFAULT_USERNAME = exports.MQTT_RECONNECT_PERIOD = exports.MQTT_TIMEOUT = exports.MQTT_BROKER_HOST = exports.MQTT_BROKER_PORT = void 0;
exports.MQTT_BROKER_PORT = 1883;
exports.MQTT_BROKER_HOST = "broker.hivemq.com";
exports.MQTT_TIMEOUT = 4000;
exports.MQTT_RECONNECT_PERIOD = 1000;
exports.MQTT_DEFAULT_USERNAME = "default-username";
exports.MQTT_DEFAULT_PASSWORD = "default-password";
exports.MQTT_TEST_TOPIC = "sensorable/test";
exports.MQTT_CONNECT_URL = "mqtt://".concat(exports.MQTT_BROKER_HOST, ":").concat(exports.MQTT_BROKER_PORT);
var DATABASE_TABLES;
(function (DATABASE_TABLES) {
    DATABASE_TABLES["SENSORS"] = "sensors";
    DATABASE_TABLES["USERS"] = "users";
    DATABASE_TABLES["ADLS"] = "adls";
    DATABASE_TABLES["EVENTS"] = "events";
})(DATABASE_TABLES = exports.DATABASE_TABLES || (exports.DATABASE_TABLES = {}));
var DATABASE_ACTIONS;
(function (DATABASE_ACTIONS) {
    DATABASE_ACTIONS["INSERT"] = "insert";
    DATABASE_ACTIONS["UPDATE"] = "update";
    DATABASE_ACTIONS["DELETE"] = "delete";
    DATABASE_ACTIONS["SELECT"] = "select";
})(DATABASE_ACTIONS = exports.DATABASE_ACTIONS || (exports.DATABASE_ACTIONS = {}));
