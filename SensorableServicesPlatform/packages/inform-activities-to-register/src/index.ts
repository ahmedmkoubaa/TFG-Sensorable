import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { JSON_FIELDS_SEPARATOR, JSON_TABLES_SEPARATOR, MQTT_REQUEST_ACTIVITIES } from "../../sensorable-constants/src"

import { IPublishPacket } from "mqtt"

export function startInformActivitiesToRegister() {
  const manager = databaseManager()
  manager.connect()

  const mqtt = useMyMqtt()

  console.log("running service inform-activities-to-register")

  mqtt.subscribe([MQTT_REQUEST_ACTIVITIES], () => {
    console.log("subscribed to topic %o", [MQTT_REQUEST_ACTIVITIES])
  })

  mqtt.onMessage((topic: string, payload: Buffer, packet: IPublishPacket) => {
    console.log("received message from topic ", topic)

    // To inform about activities
    let activities = ""

    manager.doQuery({
      query: "SELECT * FROM activities;",
      queryCallback: (err, rows) => {
        // @ts-ignore
        rows.forEach((element) => {
          activities +=
            "{" + element.id + JSON_FIELDS_SEPARATOR + element.title + JSON_FIELDS_SEPARATOR + element.description + "}"
        })

        activities += JSON_TABLES_SEPARATOR

        // to inform about steps
        manager.doQuery({
          query: "SELECT * FROM activity_steps;",
          queryCallback: (err, rows) => {
            rows.forEach((element: any) => {
              activities += "{" + element.id + JSON_FIELDS_SEPARATOR + element.title + "}"
            })

            activities += JSON_TABLES_SEPARATOR

            // to inform about the relation between steps and activities
            manager.doQuery({
              query: "SELECT * FROM steps_for_activities",
              queryCallback: (err, rows) => {
                // TODO: define a correct type for the element
                // (type is events_for_adls table scheme)
                rows.forEach((element: any) => {
                  activities +=
                    "{" +
                    element.id +
                    JSON_FIELDS_SEPARATOR +
                    element.id_activity +
                    JSON_FIELDS_SEPARATOR +
                    element.id_step +
                    "}"
                })

                // send this as a response to all the connected mqtts peers
                mqtt.publish("sensorable/database/activities/inform", activities)
              },
            })
          },
        })
      },
    })
  })
}
