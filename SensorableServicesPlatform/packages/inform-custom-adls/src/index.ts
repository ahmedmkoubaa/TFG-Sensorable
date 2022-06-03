import { useMyMqtt, MyMqttInterface } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { JSON_FIELDS_SEPARATOR, JSON_TABLES_SEPARATOR } from "../../sensorable-constants/src"

import debug from "debug"
const log = debug("inform-adls-scheme-service")

export function startInformAdlsSchemeService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  const mqtt = useMyMqtt()

  manager.doQuery({
    query:
      "SELECT * FROM adls WHERE id IN ( SELECT id_adl FROM custom_adls_for_users WHERE id_user = ? UNION SELECT id_adl FROM generic_adls )",
    data: [["1"]],
    queryCallback: (err, rows) => {
      // @ts-ignore
      rows.forEach((element) => {
        console.log("this is the response ", element)
      })
    },
  })

  log("running service")

  mqtt.subscribe(["sensorable/database/adls/request/"], () => {
    log("subscribed to topic %o", ["sensorable/database/adls/request/"])
  })

  mqtt.onMessage((topic: string, payload: Buffer) => {
    log("received message from topic ", topic)

    // for inform about new adls
    let newAdls = ""

    manager.doQuery({
      query:
        "SELECT * FROM adls WHERE id IN ( SELECT id_adl FROM custom_adls_for_users WHERE id_user = ? UNION SELECT id_adl FROM generic_adls )",
      data: [[payload.toString()]],
      queryCallback: (err, rows) => {
        // @ts-ignore
        rows.forEach((element) => {
          newAdls +=
            "{" + element.id + JSON_FIELDS_SEPARATOR + element.title + JSON_FIELDS_SEPARATOR + element.description + "}"
        })

        newAdls += JSON_TABLES_SEPARATOR

        // to inform about new events
        manager.doQuery({
          query:
            "SELECT DISTINCT events_for_adls.* FROM events_for_adls, custom_adls_for_users, generic_adls " +
            "WHERE ( custom_adls_for_users.id_adl = events_for_adls.id_adl AND custom_adls_for_users.version = events_for_adls.version ) OR " +
            "( generic_adls.id_adl = events_for_adls.id_adl AND generic_adls.version = events_for_adls.version ) AND " +
            "custom_adls_for_users.id_user = ? ORDER BY id ASC, id_adl ASC, id_event ASC;",
          data: [[payload.toString()]],
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
              query:
                "SELECT DISTINCT events_for_adls.* FROM events_for_adls, custom_adls_for_users, generic_adls " +
                "WHERE ( custom_adls_for_users.id_adl = events_for_adls.id_adl AND custom_adls_for_users.version = events_for_adls.version ) OR " +
                "( generic_adls.id_adl = events_for_adls.id_adl AND generic_adls.version = events_for_adls.version ) AND " +
                "custom_adls_for_users.id_user = ? ORDER BY id ASC, id_adl ASC, id_event ASC;",
              data: [[payload.toString()]],
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
                // mqtt.publish("sensorable/database/adls", newAdls)
              },
            })
          },
        })
      },
    })
  })
}

startInformAdlsSchemeService()
