package com.sensorable.utils;


import android.util.Log;

import com.commons.SensorableConstants;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;

import java.util.function.Consumer;

public class MqttHelper {
    private static final Mqtt5AsyncClient client = Mqtt5Client.builder().serverHost(SensorableConstants.MQTT_CONNECT_URL).buildAsync();
    public static void connect() {
        client.toBlocking().connect();
    }


    public static void  subscribe() {
        subscribe(SensorableConstants.MQTT_TEST_TOPIC, message -> {
            Log.i("MQTT", "received message " + message.toString());
        });
    }

    public static void subscribe(String topic, Consumer<Mqtt5Publish> callback) {
        client.toAsync()
                .subscribeWith().topicFilter(topic)
                .noLocal(true)
                .retainHandling(Mqtt5RetainHandling.DO_NOT_SEND)
                .retainAsPublished(true)
                .callback(callback)
                .send()
                .thenAccept(mqtt5SubAck -> Log.i("MQTT", "subscribed to topic " + topic));
    }

    public static void publish() {
        publish(SensorableConstants.MQTT_TEST_TOPIC, "hello world");
    }

    public static void publish(final String topic, final String payload) {
        client.connect();
        client.toAsync()
                .publishWith()
                .topic(topic)
                .responseTopic("sensorable/S454A-4-4HTH-4HJGHG4-GH47")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload.getBytes())
                .send()
                .whenComplete((mqtt5PublishResult, throwable) ->
                    Log.i("MQTT", throwable == null ? "success in publishment": throwable.getMessage())
                )


                .thenAccept(accept -> Log.i("MQTT", "published the message"));

    }

    public static void disconnect() {
        client.disconnect();
    }

}
