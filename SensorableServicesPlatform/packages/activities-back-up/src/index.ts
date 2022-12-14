import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { MQTT_ACTIVITIES_INSERT } from "../../sensorable-constants/src"

export function startActivitiesBackUpService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  console.log("running service activities-registry-back-up")
  const mqtt = useMyMqtt()

  mqtt.subscribe([MQTT_ACTIVITIES_INSERT], () => {
    console.log("subscribed to topic %o", [MQTT_ACTIVITIES_INSERT])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    console.log("received ", JSON.parse(payload.toString()))

    manager.doQuery({
      query: "INSERT INTO steps_for_activities_registry (id_activity, id_step, timestamp, user_id) VALUES ?",
      data: JSON.parse(payload.toString()),
      queryCallback: (err, rows) => {
        if (!err) {
          console.error("Activities registries were inserted correctly, rows: ", rows)
        } else {
          console.log("Error inserting activities registry data in activities table in activities-back-up", err)
        }
      },
    })
  })
}
