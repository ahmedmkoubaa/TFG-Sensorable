import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { MQTT_SENSORS_INSERT } from "../../sensorable-constants/src"
import { IPublishPacket } from "mqtt"

export function startSensorsBackUpService() {
  const manager = databaseManager()
  manager.connect()

  console.log("running service sensors-back-up")
  const mqtt = useMyMqtt()

  mqtt.subscribe([MQTT_SENSORS_INSERT], () => {
    console.log("subscribed to topic %o", [MQTT_SENSORS_INSERT])
  })

  mqtt.onMessage((topic: string, payload: Buffer, packet: IPublishPacket) => {
    manager.doQuery({
      query:
        "INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp, user_id) VALUES ?",
      data: JSON.parse(payload.toString()),
      queryCallback: (err, rows) => {
        if (!err) {
          const responseTopic = packet.properties?.responseTopic

          if (responseTopic) {
            mqtt.publish(responseTopic, "OK")
            console.log(`Inserted data with responseTopic ${responseTopic} `)
          } else {
            console.error("ResponseTopic wasn't specificed")
          }
        } else {
          console.log("Error inserting sensor data in sensors table in sensors-back-up")
        }
      },
    })
  })
}
