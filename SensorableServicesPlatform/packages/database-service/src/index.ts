import mysql from "mysql"
import { MQTT_TEST_TOPIC, DATABASE_TABLES, DATABASE_ACTIONS } from "../../sensorable-constants/src"
import { useMyMqtt } from "../../my-mqtt/src"

import debug from "debug"
const log = debug("database-service")

export interface QueryParams {
  query: string
  data?: any[]
  queryCallback: (err: mysql.MysqlError | null, rows: any) => void
}

export function useDatabase() {
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

    /**    
    params.query = "INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES ?"
    params.data = [[0, 21, 67, -1, -1, 1234567981]]
   */
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
    const strPayload = payload.toString()

    log("received message format string: %o", strPayload)

    let sensorData

    try {
      sensorData = JSON.parse(strPayload)
      log("received message format json: %o", sensorData)

      // An example of expected string format
      // "{\"device_type\": 15,\"sensor_type\":12, \"sensor_values\": [-12, 15, 91]}"
      // we have to use this symbol '"' and it's important to put it companied by '\'

      log("el device es: ", sensorData.device_type)
      log("el sensor es: ", sensorData.sensor_type)
    } catch (error) {
      log("Error, <%s> is not a valid json format", strPayload)
    }

    const topics = topic.toLowerCase().split("/")
    const table = topics[2]

    for (let s in DATABASE_TABLES) {
      if (s === table) {
        log("FOUND THIS TABLE NAME %s", s)
      }
    }

    if (topics.length > 3) {
      switch (topics[3]) {
        case DATABASE_ACTIONS.INSERT:
          log("received insert in topic %s", topic)
          database.doQuery({
            query:
              "INSERT INTO " + table + " (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES ?",
            queryCallback: (err, rows) => {
              log("query successfully done mah G, in custom callback")
            },
            data: sensorData,
          })
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
