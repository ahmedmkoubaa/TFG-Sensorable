"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
exports.__esModule = true;
exports.statrtDatabaseService = exports.useDatabase = void 0;
var mqtt = __importStar(require("mqtt"));
var mysql_1 = __importDefault(require("mysql"));
var pure_uuid_1 = __importDefault(require("pure-uuid"));
var src_1 = require("../../sensorable-constants/src");
var debug_1 = __importDefault(require("debug"));
var log = (0, debug_1["default"])("database-service");
function useDatabase() {
    log("called usedatabase");
    var database;
    function init() {
        database = mysql_1["default"].createConnection({
            host: "127.0.0.1",
            user: "node-service",
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
    function doQuery(query, queryCallback) {
        checkInitialized();
        database.query(query, function (err, rows) {
            checkQueryErrors(err);
            log("Query %o was done susccessfully, returned rows %o", query, rows);
            queryCallback(err, rows);
        });
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
    /**  database.doQuery("SELECT * FROM example_table", (err, rows) => {
      log("in callback received rows: %o", rows)
    })
   */
    log("running database service");
    var connectUrl = src_1.MQTT_CONNECT_URL;
    log("database-service broker connection url", connectUrl);
    var client = mqtt.connect(src_1.MQTT_CONNECT_URL, {
        clientId: new pure_uuid_1["default"](4).toString(),
        clean: true,
        connectTimeout: src_1.MQTT_TIMEOUT,
        username: src_1.MQTT_DEFAULT_PASSWORD,
        password: src_1.MQTT_DEFAULT_USERNAME,
        reconnectPeriod: src_1.MQTT_RECONNECT_PERIOD
    });
    var topic = src_1.MQTT_TEST_TOPIC;
    client.on("connect", function () {
        log("database service connected");
        client.subscribe([topic], function () {
            log("Subscribe to topic '".concat(topic, "'"));
        });
        client.publish(topic, "I am database service", {
            qos: 0,
            retain: false,
            // TO DO remove this statement or use it correctly
            properties: { responseTopic: "resonseTopic-uuid" }
        }, function (error) {
            if (error) {
                console.error(error);
            }
        });
        client.on("message", function (topic, payload, packet) {
            var _a;
            log("Received Message:", topic, payload.toString());
            // we receive this response topic, then is just a request not a publishing
            log((_a = packet.properties) === null || _a === void 0 ? void 0 : _a.responseTopic);
        });
    });
}
exports.statrtDatabaseService = statrtDatabaseService;
