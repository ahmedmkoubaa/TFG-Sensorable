export const MQTT_BROKER_PORT = 1883
export const MQTT_BROKER_HOST = "broker.hivemq.com"
export const MQTT_TIMEOUT = 4000
export const MQTT_RECONNECT_PERIOD = 1000
export const MQTT_DEFAULT_USERNAME = "default-username"
export const MQTT_DEFAULT_PASSWORD = "default-password"
export const MQTT_TEST_TOPIC = "sensorable/test"

export const MQTT_CONNECT_URL = `mqtt://${MQTT_BROKER_HOST}:${MQTT_BROKER_PORT}`

export enum DATABASE_TABLES {
  SENSORS = "sensors",
  USERS = "users",
  ADLS = "adls",
  EVENTS = "events",
}

export enum DATABASE_ACTIONS {
  INSERT = "insert",
  UPDATE = "update",
  DELETE = "delete",
  SELECT = "select",
}
