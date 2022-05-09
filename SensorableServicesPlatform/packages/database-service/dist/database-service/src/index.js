"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
exports.__esModule = true;
exports.statrtDatabaseService = exports.useDatabase = void 0;
var mysql_1 = __importDefault(require("mysql"));
var src_1 = require("../../sensorable-constants/src");
var src_2 = require("../../my-mqtt/src");
var debug_1 = __importDefault(require("debug"));
var log = (0, debug_1["default"])("database-service");
function useDatabase() {
    log("called usedatabase");
    var database;
    function init() {
        database = mysql_1["default"].createConnection({
            host: "127.0.0.1",
            user: "database-service",
            password: "12345678",
            database: "test"
        });
    }
    function checkInitialized() {
        if (!database) {
            throw new Error("Error: using unintialized database, call init you use the mentioned database");
        }
    }
    function connect() {
        checkInitialized();
        database.connect(function (err) {
            if (err) {
                log("Error: can't connect to database server, code: %o", err);
                throw err;
            }
            log("SUCCESS DATABASE CONNECTION!");
        });
    }
    function checkQueryErrors(err, msg) {
        if (err) {
            log("Error: while executing a query -> %o optional message is -> %s", err, msg);
            throw new Error("Error: trying to execute a query and handled the next error");
        }
    }
    function doQuery(params) {
        var _a;
        checkInitialized();
        if (((_a = params.data) === null || _a === void 0 ? void 0 : _a.length) == 0) {
            log("No params tu push");
            return;
        }
        database.query(params.query, [params.data], function (err, rows) {
            checkQueryErrors(err);
            params.queryCallback(err, rows);
        });
    }
    function informNewAdls(mqtt) {
        // to inform about new adls
        var newAdls = "";
        doQuery({
            query: "SELECT * FROM adls",
            queryCallback: function (err, rows) {
                newAdls += rows + "#";
            }
        });
        // to inform about new events
        doQuery({
            query: "SELECT * FROM events",
            queryCallback: function (err, rows) {
                mqtt.publish("sensorable/database/events", rows);
            }
        });
        // to inform about new adls and events
        doQuery({
            query: "SELECT * FROM events_for_adls",
            queryCallback: function (err, rows) {
                mqtt.publish("sensorable/database/events_for_adls", rows);
            }
        });
        // send this to subscribers
        console.log("Estas son las nuevas adls", newAdls);
        mqtt.publish("sensorable/database/adls", newAdls);
    }
    return {
        init: init,
        checkInitialized: checkInitialized,
        connect: connect,
        doQuery: doQuery
    };
}
exports.useDatabase = useDatabase;
function statrtDatabaseService() {
    var database = useDatabase();
    database.init();
    database.connect();
    log("running database service");
    var mqtt = (0, src_2.useMyMqtt)();
    mqtt.subscribe(["sensorable/test", "sensorable/database/#"], function () {
        log("subscribed to topic %o", ["sensorable/test", "sensorable/database/#"]);
    });
    mqtt.publish(src_1.MQTT_TEST_TOPIC, "Hello I am database service");
    mqtt.onMessage(function (topic, payload) {
        var strPayload = payload.toString();
        log("received message format string: %o", strPayload);
        var sensorData;
        try {
            sensorData = JSON.parse(strPayload);
            log("received message format json: %o", sensorData);
            // An example of expected string format
            // "{\"device_type\": 15,\"sensor_type\":12, \"sensor_values\": [-12, 15, 91]}"
            // we have to use this symbol '"' and it's important to put it companied by '\'
            log("el device es: ", sensorData.device_type);
            log("el sensor es: ", sensorData.sensor_type);
        }
        catch (error) {
            log("Error, <%s> is not a valid json format", strPayload);
        }
        var topics = topic.toLowerCase().split("/");
        var table = topics[2];
        for (var s in src_1.DATABASE_TABLES) {
            if (s === table) {
                log("FOUND THIS TABLE NAME %s", s);
            }
        }
        if (topics.length > 3) {
            switch (topics[3]) {
                case src_1.DATABASE_ACTIONS.INSERT:
                    log("received insert in topic %s", topic);
                    database.doQuery({
                        query: "INSERT INTO " + table + " (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES ?",
                        queryCallback: function (err, rows) {
                            log("query successfully done mah G, in custom callback");
                        },
                        data: sensorData
                    });
                    break;
                case src_1.DATABASE_ACTIONS.SELECT:
                    log("received select in topic %s", topic);
                    break;
                case src_1.DATABASE_ACTIONS.UPDATE:
                    log("received update in topic %s", topic);
                    break;
                case src_1.DATABASE_ACTIONS.DELETE:
                    log("received delete in topic %s", topic);
                    break;
                default:
                    log("received unknown topic command %s", topic);
                    break;
            }
        }
    });
}
exports.statrtDatabaseService = statrtDatabaseService;
