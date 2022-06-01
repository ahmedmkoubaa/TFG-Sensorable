import mysql from "mysql"
import {
  MQTT_TEST_TOPIC,
  DATABASE_TABLES,
  DATABASE_ACTIONS,
  JSON_TABLES_SEPARATOR,
} from "../../sensorable-constants/src"
import { useMyMqtt, MyMqttInterface, IPublishPacket } from "../../my-mqtt/src"
import { JSON_FIELDS_SEPARATOR } from "../../sensorable-constants/src"

import debug from "debug"

const log = debug("database-client")

export interface QueryParams {
  query: string
  data?: any[]
  queryCallback: (err: mysql.MysqlError | null, rows: any) => void
}

export interface DatabaseManager {
  init: () => void
  checkInitialized: () => void
  connect: () => void
  doQuery: (queryParams: QueryParams) => void
}

export function databaseManager(): DatabaseManager {
  log("called usedatabase")
  let database: mysql.Connection

  function init() {
    database = mysql.createConnection({
      host: "127.0.0.1",
      user: "database-service",
      password: "12345678",
      database: "test",
    })
  }

  function checkInitialized() {
    if (!database) {
      throw new Error("Error: using unintialized database, call init you use the mentioned database")
    }
  }

  function connect() {
    checkInitialized()

    database.connect((err: unknown) => {
      if (err) {
        log("Error: can't connect to database server, code: %o", err)
        throw err
      }

      log("SUCCESS DATABASE CONNECTION!")
    })
  }

  function checkQueryErrors(err: mysql.MysqlError | null, msg?: string) {
    if (err) {
      log("Error: while executing a query -> %o optional message is -> %s", err, msg)
      throw new Error("Error: trying to execute a query and handled the next error")
    }
  }

  function doQuery(params: QueryParams) {
    checkInitialized()

    if (params.data?.length == 0) {
      log("No params tu push")
      return
    }

    database.query(params.query, [params.data], (err, rows) => {
      checkQueryErrors(err)
      params.queryCallback(err, rows)
    })
  }

  return {
    init,
    checkInitialized,
    connect,
    doQuery,
  }
}

export function statrtDatabaseService() {
  const manager = databaseManager()

  manager.init()
  manager.connect()

  log("running database service")
  const mqtt = useMyMqtt()

  mqtt.subscribe(["sensorable/test", "sensorable/database/#"], () => {
    log("subscribed to topic %o", ["sensorable/test", "sensorable/database/#"])
  })

  mqtt.publish(MQTT_TEST_TOPIC, "Hello I am database service")

  mqtt.onMessage((topic: string, payload: Buffer, packet: IPublishPacket) => {
    handleInformAdlsRequest(topic, payload, packet, manager)
  })

  // TODO remove the following statements are only tests
  log("Let's test mqtt")
}

// statrtDatabaseService()

function handleInformAdlsRequest(topic: string, payload: Buffer, packet: IPublishPacket, database: DatabaseManager) {
  console.log("received handleInformAdlsRequest and topic is", topic)
  if (topic === "sensorable/database/adls/request") {
    const userId = payload.toString()
    console.log("received user id ", userId)

    // get the custom adls associated to the received userid
    // get the events of those adls
    // get the relation between those adls
  }
}

function responseCustomAdlsForUser(userId: string, responseTopic: string, database: DatabaseManager) {
  // to inform about new adls

  const mqtt = useMyMqtt()
  let newAdls = ""

  database.doQuery({
    query: "SELECT * FROM adls",
    queryCallback: (err, rows) => {
      // @ts-ignore
      rows.forEach((element) => {
        newAdls +=
          "{" + element.id + JSON_FIELDS_SEPARATOR + element.title + JSON_FIELDS_SEPARATOR + element.description + "}"
      })

      newAdls += JSON_TABLES_SEPARATOR

      // to inform about new events
      database.doQuery({
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
          database.doQuery({
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
              mqtt.publish("sensorable/database/adls", newAdls)
            },
          })
        },
      })
    },
  })
}
