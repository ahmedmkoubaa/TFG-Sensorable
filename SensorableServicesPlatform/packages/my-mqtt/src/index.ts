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
import { getPackedSettings } from "http2"
const log = debug("my-mqtt")

export interface MyMqttInterface {
  subscribe(topic: string | string[], callback: () => void): void
  publish(topic: string, payload: string): void
  onMessage(callback: (topic: string, payload: Buffer) => void): void
}

export type { IPublishPacket } from "mqtt"

export function useMyMqtt() {
  // initialize the client connecting to default url
  let client: MqttClient = connect(MQTT_CONNECT_URL, {
    clientId: new UUID(4).toString(),
    clean: true,
    connectTimeout: MQTT_TIMEOUT,
    username: MQTT_DEFAULT_PASSWORD,
    password: MQTT_DEFAULT_USERNAME,
    reconnectPeriod: MQTT_RECONNECT_PERIOD,
    protocolVersion: 5,
  })

  // export subscribe function
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

  // export publish function
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

  // export received message function
  function onMessage(callback: (topic: string, payload: Buffer, packet: IPublishPacket) => void) {
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
    subscribe,
    publish,
    onMessage,
  }
}
