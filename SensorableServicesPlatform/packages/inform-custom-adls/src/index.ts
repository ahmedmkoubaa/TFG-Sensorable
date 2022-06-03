import { useMyMqtt, MyMqttInterface } from "../../my-mqtt/src"
import { databaseManager } from "../../database-client/src"
import { JSON_FIELDS_SEPARATOR, JSON_TABLES_SEPARATOR } from "../../sensorable-constants/src"

import debug from "debug"
import { IPublishPacket } from "mqtt"
const log = debug("inform-custom-adls")

export function startInformAdlsSchemeService() {
  const manager = databaseManager()
  manager.init()
  manager.connect()

  const mqtt = useMyMqtt()

  log("running service")

  mqtt.subscribe(["sensorable/database/adls/custom/request"], () => {
    log("subscribed to topic %o", ["sensorable/database/adls/custom/request"])
  })

  mqtt.onMessage((topic: string, payload: Buffer, packet: IPublishPacket) => {
    log("received message from topic ", topic)

    // Finish the function if there is no response topic. This service
    // is intended to extract data from database and send it to the request emitter.
    if (!packet.properties?.responseTopic) {
      return log("Exiting messge callback because there is no response topic")
    }

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
            "SELECT * FROM events WHERE events.id IN ( " +
            "SELECT DISTINCT id_event FROM events_for_adls, custom_adls_for_users, generic_adls " +
            "WHERE ( custom_adls_for_users.id_adl = events_for_adls.id_adl AND custom_adls_for_users.version = events_for_adls.version ) OR " +
            "( generic_adls.id_adl = events_for_adls.id_adl AND generic_adls.version = events_for_adls.version ) AND " +
            "custom_adls_for_users.id_user = ? );",
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

                // send this as a response to the main message
                if (packet.properties?.responseTopic) {
                  mqtt.publish(packet.properties?.responseTopic, newAdls)
                }
              },
            })
          },
        })
      },
    })
  })
}

startInformAdlsSchemeService()
