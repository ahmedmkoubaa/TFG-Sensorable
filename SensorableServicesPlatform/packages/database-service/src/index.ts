import mysql from "mysql"
import { MQTT_TEST_TOPIC, DATABASE_TABLES, DATABASE_ACTIONS } from "../../sensorable-constants/src"
import { useMyMqtt } from "../../my-mqtt/src"

import debug from "debug"
const log = debug("database-service")

export function useDatabase() {
  log("called usedatabase")
  let database: mysql.Connection

  function init() {
    database = mysql.createConnection({
      host: "127.0.0.1",
      user: "node-service",
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

  function doQuery(query: string, queryCallback: (err: mysql.MysqlError | null, rows: any) => void) {
    checkInitialized()

    database.query(query, (err, rows) => {
      checkQueryErrors(err)
      queryCallback(err, rows)
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
  const database = useDatabase()

  database.init()
  database.connect()

  log("running database service")
  const mqtt = useMyMqtt()

  mqtt.subscribe(["sensorable/test", "sensorable/database/#"], () => {
    log("subscribed to topic %o", ["sensorable/test", "sensorable/database/#"])
  })

  mqtt.publish(MQTT_TEST_TOPIC, "Hello I am database service")

  mqtt.onMessage((topic: string, payload: Buffer) => {
    log("received message:", topic, payload.toString())
    const topics = topic.toUpperCase().split("/")

    for (let s in DATABASE_TABLES) {
      if (s === topics[2]) {
        log("FOUND THIS TABLE NAME %s", s)
      }
    }

    if (topics.length > 3) {
      switch (topics[3]) {
        case DATABASE_ACTIONS.INSERT:
          log("received insert in topic %s", topic)
          break

        case DATABASE_ACTIONS.SELECT:
          log("received select in topic %s", topic)
          break

        case DATABASE_ACTIONS.UPDATE:
          log("received update in topic %s", topic)
          break

        case DATABASE_ACTIONS.DELETE:
          log("received delete in topic %s", topic)
          break

        default:
          log("received unknown topic command %s", topic)
          break
      }
    }
  })
}
