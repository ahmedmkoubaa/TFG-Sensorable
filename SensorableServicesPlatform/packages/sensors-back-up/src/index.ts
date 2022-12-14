import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { MQTT_SENSORS_INSERT } from "../../sensorable-constants/src"

export function startSensorsBackUpService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  console.log("running service sensors-back-up")
  const mqtt = useMyMqtt()

  mqtt.subscribe([MQTT_SENSORS_INSERT], () => {
    console.log("subscribed to topic %o", [MQTT_SENSORS_INSERT])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    console.log("received message in sensors-back-up topic:", topic)

    manager.doQuery({
      query:
        "INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp, user_id) VALUES ?",
      data: JSON.parse(payload.toString()),
      queryCallback: (err, rows) => {
        if (!err) {
          console.error("SensorData was inserted correctly")
        } else {
          console.log("Error inserting sensor data in sensors table in sensors-back-up")
        }
      },
    })
  })
}
