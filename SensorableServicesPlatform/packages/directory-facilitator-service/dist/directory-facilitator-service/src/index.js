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
exports.runDirectoryFacilitator = void 0;
var mqtt = __importStar(require("mqtt"));
var pure_uuid_1 = __importDefault(require("pure-uuid"));
var src_1 = require("../../sensorable-constants/src");
function runDirectoryFacilitator() {
    console.log("running directory facilitator service");
    var connectUrl = src_1.MQTT_CONNECT_URL;
    console.log("Esta es la url", connectUrl);
    var client = mqtt.connect(src_1.MQTT_CONNECT_URL, {
        clientId: new pure_uuid_1["default"](4).toString(),
        clean: true,
        connectTimeout: src_1.MQTT_TIMEOUT,
        username: src_1.MQTT_DEFAULT_PASSWORD,
        password: src_1.MQTT_DEFAULT_USERNAME,
        reconnectPeriod: src_1.MQTT_RECONNECT_PERIOD
    });
    var topic = "services/topics";
    client.on("connect", function () {
        console.log("Connected");
        client.subscribe([topic], function () {
            console.log("Subscribe to topic '".concat(topic, "'"));
        });
        client.publish(topic, "I am directory facilitator", {
            qos: 0,
            retain: false,
            properties: { responseTopic: "resonseTopic-uuid" }
        }, function (error) {
            if (error) {
                console.error(error);
            }
        });
        client.on("message", function (topic, payload, packet) {
            var _a;
            console.log("Received Message:", topic, payload.toString());
            // we receive this response topic, then is just a request not a publishing
            console.log((_a = packet.properties) === null || _a === void 0 ? void 0 : _a.responseTopic);
        });
    });
}
exports.runDirectoryFacilitator = runDirectoryFacilitator;
