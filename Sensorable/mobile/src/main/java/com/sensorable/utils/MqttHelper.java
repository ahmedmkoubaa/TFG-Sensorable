package com.sensorable.utils;


import android.util.Log;

import com.commons.utils.SensorableConstants;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MqttHelper {
    private static final Mqtt5AsyncClient client =
            Mqtt5Client
                    .builder()
                    .serverHost(SensorableConstants.MQTT_CONNECT_URL)
                    .buildAsync();

    // is necessary to make it blocking for lately requests
    public static boolean connect() {
        final MqttClientState status = client.toBlocking().getState();

        if (!status.isConnectedOrReconnect()) {
            try {
                client.toBlocking().connect();
            } catch (Exception e) {
                Log.e("MQTT", e.getMessage());
                return false;

            }
            Log.i("MQTT", "client not connected, trying to connect");
        }
        return true;
    }


    public static void testSubscribe() {
        subscribe(SensorableConstants.MQTT_TEST_TOPIC, message -> {
            Log.i("MQTT", "received message " + message.toString());
        });
    }

    public static void subscribe(List<String> topics, Consumer<Mqtt5Publish> callback) {
        for (String topic : topics) {
            subscribe(topic, callback);
        }
    }

    public static void unsubscribe(final String topic) {
        // Create an Mqtt5Unsubscribe instance for the topic you want to unsubscribe from

        client.unsubscribe(Mqtt5Unsubscribe.builder().topicFilter(topic).build());
    }

    public static void subscribe(String topic, Consumer<Mqtt5Publish> callback) {


        client.toAsync()
                .subscribeWith()
                .topicFilter(topic)
                .noLocal(true)
                .retainHandling(Mqtt5RetainHandling.DO_NOT_SEND)
                .retainAsPublished(true)
                .callback(callback)
                .send()
                .whenComplete((mqtt5PublishResult, throwable) ->
                        Log.i("MQTT", throwable == null ? "success in susbcribe" : throwable.getMessage())
                )
                .thenAccept(mqtt5SubAck -> Log.i("MQTT", "subscribed to topic " + topic));
    }

    public static void publish() {
        publish(SensorableConstants.MQTT_TEST_TOPIC, ("test message").getBytes());
    }

    public static void publish(final String topic) {
        publish(topic, "".getBytes());
    }

    public static CompletableFuture<Void> publish(final String topic, final byte[] payload) {
        client.connect();
        return client.toAsync()
                .publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload)
                .send()
                .whenComplete((mqtt5PublishResult, throwable) ->
                        Log.i("MQTT", throwable == null ? "success in publishment" : throwable.getMessage())
                )
                .thenAccept(accept -> Log.i("MQTT", "published the message"));
    }

    public static void publish(final String topic, final byte[] payload, final String responseTopic) {
        client.connect();
        client.toAsync()
                .publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload)
                .responseTopic(responseTopic)
                .send()
                .whenComplete((mqtt5PublishResult, throwable) ->
                        Log.i("MQTT", throwable == null ? "success in publishment" : throwable.getMessage())
                )
                .thenAccept(accept -> Log.i("MQTT", "published the message"));
    }

    public static void disconnect() {
        client.disconnect();
    }

}
