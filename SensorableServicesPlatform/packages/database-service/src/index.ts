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

export function runDatabaseService() {
  console.log("running database service")
  const connectUrl = MQTT_CONNECT_URL

  console.log("Esta es la url", connectUrl)

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
    console.log("database service connected")

    client.subscribe([topic], () => {
      console.log(`Subscribe to topic '${topic}'`)
    })

    client.publish(
      topic,
      "I am database service",
      {
        qos: 0,
        retain: false,
        properties: { responseTopic: "resonseTopic-uuid" },
      },
      (error) => {
        if (error) {
          console.error(error)
        }
      }
    )

    client.on("message", (topic, payload, packet) => {
      console.log("Received Message:", topic, payload.toString())
      // we receive this response topic, then is just a request not a publishing
      console.log(packet.properties?.responseTopic)
    })
  })
}
