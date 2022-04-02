import * as mqtt from "mqtt"
import mysql from "mysql"
import UUID from "pure-uuid"
import {
  MQTT_RECONNECT_PERIOD,
  MQTT_TIMEOUT,
  MQTT_DEFAULT_USERNAME,
  MQTT_DEFAULT_PASSWORD,
  MQTT_CONNECT_URL,
  MQTT_TEST_TOPIC,
} from "../../sensorable-constants/src"
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

  /**  database.doQuery("SELECT * FROM example_table", (err, rows) => {
    log("in callback received rows: %o", rows)
  })
 */
  log("running database service")
  const mqtt = useMyMqtt()

  mqtt.init()
  mqtt.subscribe(MQTT_TEST_TOPIC, () => {
    log("subscribed to topic %o", MQTT_TEST_TOPIC)
  })

  mqtt.publish(MQTT_TEST_TOPIC, "Hello I am database service")

  mqtt.onMessage((topic: string, payload: Buffer) => {
    log("received message:", topic, payload.toString())
  })

  /** const client = mqtt.connect(MQTT_CONNECT_URL, {
    clientId: new UUID(4).toString(),
    clean: true,
    connectTimeout: MQTT_TIMEOUT,
    username: MQTT_DEFAULT_PASSWORD,
    password: MQTT_DEFAULT_USERNAME,
    reconnectPeriod: MQTT_RECONNECT_PERIOD,
  })

  const topic = MQTT_TEST_TOPIC
  client.on("connect", () => {
    log("database service connected")

    client.subscribe([topic], { qos: 0, nl: true }, () => {
      log(`Subscribe to topic '${topic}'`)
    })

    client.publish(
      topic,
      "I am database service",
      {
        qos: 0,
        retain: false,
        // TO DO remove this statement or use it correctly
        properties: { responseTopic: "resonseTopic-uuid" },
      },
      (error) => {
        if (error) {
          log("Error: publishing on topic", topic)
          console.error(error)
        }
      }
    )

    client.on("message", (topic, payload, packet) => {
      log("Received Message:", topic, payload.toString())
      // we receive this response topic, then is just a request not a publishing
      // log(packet.properties?.responseTopic)

      database.doQuery("SELECT * FROM example_table", (err, rows) => {
        log("passed callback to doQuery function, received rows: %o", rows)
      })
    })
  }) */
}
