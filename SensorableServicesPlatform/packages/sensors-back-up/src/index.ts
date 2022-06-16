import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"

import debug from "debug"
const log = debug("sensors-back-up")

export function startSensorsBackUpService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  log("running service")
  const mqtt = useMyMqtt()

  mqtt.subscribe(["sensorable/database/sensors/insert"], () => {
    log("subscribed to topic %o", ["sensorable/database/sensors/insert"])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    log("received message in sensors-back-up topic:", topic)
    manager.doQuery({
      query: "INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES ?",
      data: JSON.parse(payload.toString()),
      queryCallback: (err, rows) => {
        if (!err) {
          log("SensorData was inserted correctly")
        } else {
          log("Error inserting sensor data in sensors table in sensors-back-up")
        }
      },
    })
  })
}
