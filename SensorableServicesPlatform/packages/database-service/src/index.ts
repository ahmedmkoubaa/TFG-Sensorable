import * as mqtt from "mqtt"
import UUID from "pure-uuid"
import {
  MQTT_RECONNECT_PERIOD,
  MQTT_TIMEOUT,
  MQTT_DEFAULT_USERNAME,
  MQTT_DEFAULT_PASSWORD,
  MQTT_CONNECT_URL,
  MQTT_TEST_TOPIC,
} from "../../sensorable-constants/src"

import debug from "debug"
const log = debug("database-service")

export function statrtDatabaseService() {
  log("running database service")
  const connectUrl = MQTT_CONNECT_URL

  log("database-service broker connection url", connectUrl)

  const client = mqtt.connect(MQTT_CONNECT_URL, {
    clientId: new UUID(4).toString(),
    clean: true,
    connectTimeout: MQTT_TIMEOUT,
    username: MQTT_DEFAULT_PASSWORD,
    password: MQTT_DEFAULT_USERNAME,
    reconnectPeriod: MQTT_RECONNECT_PERIOD,
  })

  const topic = MQTT_TEST_TOPIC
  client.on("connect", () => {
    log("database service connected")

    client.subscribe([topic], () => {
      log(`Subscribe to topic '${topic}'`)
    })

    client.publish(
      topic,
      "I am database service",
      {
        qos: 0,
        retain: false,
        // TO DO remove this statement or use it correctly
        properties: { responseTopic: "resonseTopic-uuid" },
      },
      (error) => {
        if (error) {
          console.error(error)
        }
      }
    )

    client.on("message", (topic, payload, packet) => {
      log("Received Message:", topic, payload.toString())
      // we receive this response topic, then is just a request not a publishing
      log(packet.properties?.responseTopic)
    })
  })
}
