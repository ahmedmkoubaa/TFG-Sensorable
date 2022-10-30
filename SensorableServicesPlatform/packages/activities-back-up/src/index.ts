import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"

import debug from "debug"
const log = debug("activities-back-up")

export function startActivitiesBackUpService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  console.log("running service")
  const mqtt = useMyMqtt()

  mqtt.subscribe(["sensorable/database/activities/insert"], () => {
    console.log("subscribed to topic %o", ["sensorable/database/activities/insert"])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    console.log("received message in activities-back-up topic:", topic)
    console.log("received ", JSON.parse(payload.toString()))

    manager.doQuery({
      query: "INSERT INTO steps_for_activities_registry (id, id_activity, id_step, timestamp) VALUES ?",
      data: JSON.parse(payload.toString()),
      queryCallback: (err, rows) => {
        if (!err) {
          console.log("Activities registries were inserted correctly, rows: ", rows)
        } else {
          console.log("Error inserting activities registry data in activities table in activities-back-up", err)
        }
      },
    })
  })
}
