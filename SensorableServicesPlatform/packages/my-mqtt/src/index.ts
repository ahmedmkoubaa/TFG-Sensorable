import UUID from "pure-uuid"
import {
  MQTT_RECONNECT_PERIOD,
  MQTT_TIMEOUT,
  MQTT_DEFAULT_USERNAME,
  MQTT_DEFAULT_PASSWORD,
  MQTT_CONNECT_URL,
} from "../../sensorable-constants/src"

import debug from "debug"
import { MqttClient, connect, IPublishPacket } from "mqtt"
const log = debug("my-mqtt")

export function useMyMqtt() {
  let client: MqttClient

  function init() {
    client = connect(MQTT_CONNECT_URL, {
      clientId: new UUID(4).toString(),
      clean: true,
      connectTimeout: MQTT_TIMEOUT,
      username: MQTT_DEFAULT_PASSWORD,
      password: MQTT_DEFAULT_USERNAME,
      reconnectPeriod: MQTT_RECONNECT_PERIOD,
    })
  }

  function subscribe(topic: string | string[], callback: () => void) {
    const subscribeWhenConnected = () => {
      client.subscribe(topic, { qos: 0, nl: true }, callback)
    }

    if (client.connected) {
      subscribeWhenConnected()
    } else {
      client.on("connect", subscribeWhenConnected)
    }
  }

  function publish(topic: string, payload: string) {
    const publishWhenConnected = () => {
      client.publish(
        topic,
        payload,
        {
          qos: 0,
          retain: false,
        },
        (error) => {
          if (error) {
            log("Error: publishing on topic", topic)
            console.error(error)
          }
        }
      )
    }

    if (client.connected) {
      publishWhenConnected()
    } else {
      client.on("connect", publishWhenConnected)
    }
  }

  function onMessage(callback: (topic: string, payload: Buffer) => void) {
    const onMessageWhenConnected = () => {
      client.on("message", callback)
    }

    if (client.connected) {
      onMessageWhenConnected()
    } else {
      client.on("connect", onMessageWhenConnected)
    }
  }

  return {
    init,
    subscribe,
    publish,
    onMessage,
  }
}
