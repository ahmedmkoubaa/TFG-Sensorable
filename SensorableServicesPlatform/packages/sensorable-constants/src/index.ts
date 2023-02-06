// MQTT settings constants
export const MQTT_BROKER_PORT = 1883
export const MQTT_BROKER_HOST = "broker.hivemq.com"
export const MQTT_TIMEOUT = 4000
export const MQTT_RECONNECT_PERIOD = 1000
export const MQTT_DEFAULT_USERNAME = "default-username"
export const MQTT_DEFAULT_PASSWORD = "default-password"
export const MQTT_TEST_TOPIC = "sensorable/test"
export const JSON_FIELDS_SEPARATOR = "|"
export const JSON_TABLES_SEPARATOR = "#"
export const MQTT_CONNECT_URL = `mqtt://${MQTT_BROKER_HOST}:${MQTT_BROKER_PORT}`

// MQTT communication constants
export const MQTT_SENSORS_INSERT = "sensorable/database/sensors/insert"
export const MQTT_REQUEST_CUSTOM_ADLS = "sensorable/database/adls/custom/request"
export const MQQTT_INFORM_CUSTOM_ADLS = "sensorable/database/adls/custom/inform"
export const MQTT_REQUEST_GENERIC_ADLS = "sensorable/database/adls/generics/request"
export const MQTT_INFORM_GENERIC_ADLS = "sensorable/database/adls/generics/inform"
export const MQTT_INFORM_ACTIVITIES = "sensorable/database/activities/inform"
export const MQTT_REQUEST_ACTIVITIES = "sensorable/database/activities/request"
export const MQTT_ACTIVITIES_INSERT = "sensorable/database/activities/insert"

// Names of database tables
export enum DATABASE_TABLES {
  SENSORS = "sensors",
  USERS = "users",
  ADLS = "adls",
  EVENTS = "events",
}

// Actions to perform on the database
export enum DATABASE_ACTIONS {
  INSERT = "insert",
  UPDATE = "update",
  DELETE = "delete",
  SELECT = "select",
}

// Codes of sensors, each one indicates an action
export enum SensorAction {
  FIRST = 0,
  SECOND = 1,
  THIRD = 2,
  DISTANCE = 3,
  ANY = 4,
  ALL = 5,
}

// Constants to indetify sensors types (based on android studio coding)
export enum SensorIdentifier {
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
  TYPE_GPS = 2411,
}
