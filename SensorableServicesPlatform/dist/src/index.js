"use strict";
exports.__esModule = true;
var src_1 = require("../packages/inform-custom-adls/src");
var src_2 = require("../packages/inform-generic-adls/src");
var src_3 = require("../packages/sensors-back-up/src");
(0, src_1.startInformCustomAdls)();
(0, src_2.startInformGenericAdls)();
(0, src_3.startSensorsBackUpService)();
