import { useMyMqtt } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { JSON_FIELDS_SEPARATOR, JSON_TABLES_SEPARATOR } from "../../sensorable-constants/src"

import debug from "debug"
const log = debug("inform-generic-adls")

export function startInformGenericAdls() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  const mqtt = useMyMqtt()

  log("running service inform-generic-adls")

  mqtt.subscribe(["sensorable/database/adls/generics/request"], () => {
    log("subscribed to topic %o", ["sensorable/database/adls/generics/request"])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    log("received message from topic ", topic)

    // for inform about new adls
    let newAdls = ""

    manager.doQuery({
      query: "SELECT * FROM adls",
      queryCallback: (err, rows) => {
        // @ts-ignore
        rows.forEach((element) => {
          newAdls +=
            "{" + element.id + JSON_FIELDS_SEPARATOR + element.title + JSON_FIELDS_SEPARATOR + element.description + "}"
        })

        newAdls += JSON_TABLES_SEPARATOR

        // to inform about new events
        manager.doQuery({
          query: "SELECT * FROM events",
          queryCallback: (err, rows) => {
            rows.forEach((element: any) => {
              newAdls +=
                "{" +
                element.id +
                JSON_FIELDS_SEPARATOR +
                element.device_type +
                JSON_FIELDS_SEPARATOR +
                element.sensor_type +
                JSON_FIELDS_SEPARATOR +
                element.pos +
                JSON_FIELDS_SEPARATOR +
                element.operator +
                JSON_FIELDS_SEPARATOR +
                +element.operand +
                JSON_FIELDS_SEPARATOR +
                (element.tag !== null ? element.tag : "NULL") +
                "}"
            })

            newAdls += JSON_TABLES_SEPARATOR

            // to inform about new adls and events
            manager.doQuery({
              query: "SELECT * FROM events_for_adls ORDER BY id ASC",
              queryCallback: (err, rows) => {
                // TODO: define a correct type for the element
                // (type is events_for_adls table scheme)
                rows.forEach((element: any) => {
                  newAdls +=
                    "{" +
                    element.id +
                    JSON_FIELDS_SEPARATOR +
                    element.id_adl +
                    JSON_FIELDS_SEPARATOR +
                    element.id_event +
                    JSON_FIELDS_SEPARATOR +
                    element.version +
                    "}"
                })

                // send this to subscribers
                mqtt.publish("sensorable/database/adls/generics/inform", newAdls)
              },
            })
          },
        })
      },
    })
  })
}
