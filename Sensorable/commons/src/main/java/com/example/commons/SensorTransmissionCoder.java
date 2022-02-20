package com.example.commons;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SensorTransmissionCoder {
    private static Charset charset = StandardCharsets.US_ASCII;
    private static final String FIELDS_SEPARATOR = "~";
    private static final String VALUES_SEPARATOR = ",";
    public static void setCharset(Charset newCharset) {
        charset = newCharset;
    }

    public static String getFieldsSeparator() {
        return FIELDS_SEPARATOR;
    }
    public static String getValuesSeparator() { return VALUES_SEPARATOR; }

    public static class SensorMessage implements Parcelable {
        private int sensorType;
        private float[] value;
        private int deviceType;

        public SensorMessage(int deviceType, int sensorType, float[] value) {
            this.sensorType = sensorType;
            this.value = value;
            this.deviceType = deviceType;
        }

        public int getDeviceType() {return deviceType; }
        public int getSensorType() {
            return sensorType;
        }

        public float[] getValue() {
            return value;
        }

        public String toString() {
            return sensorType + FIELDS_SEPARATOR + value;
        }

        protected SensorMessage(Parcel in) {
            deviceType = in.readInt();
            sensorType = in.readInt();
            value = decodeValue(in.readString());
        }

        public static final Creator<SensorMessage> CREATOR = new Creator<SensorMessage>() {
            @Override
            public SensorMessage createFromParcel(Parcel in) {
                return new SensorMessage(in);
            }

            @Override
            public SensorMessage[] newArray(int size) {
                return new SensorMessage[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(deviceType);
            parcel.writeInt(sensorType);
            parcel.writeString(codeValue(value));
        }
    }

    // it recevies an array and transform it to a String using the value_separator
    private static String codeValue(float[] value) {
        String msg = "";
        for (Float f: value) {
            msg += f + VALUES_SEPARATOR;
        }

        return msg;
    }

    // it receives a value in string format and decodes it to a normal float array
    private static float [] decodeValue(String value) {
        String msg[] = value.split(VALUES_SEPARATOR);


        int size = msg.length;
        float arrayValue[] = new float[size];

        for (int i = 0; i < size; i++) {
            arrayValue[i] = Float.parseFloat(msg[i]);
        }

        return arrayValue;
    }


    // it receives the necessary data to form a SensorMessage and it
    // transforms it to String and later to bytes
    public static byte[] codeMessage(int device, int sensorType, float[] value) {
        String msg = device + FIELDS_SEPARATOR + sensorType + FIELDS_SEPARATOR;
        msg += codeValue(value);

        byte[] sensorData = msg.getBytes(charset);
        return sensorData;
    }

    // Code the sensor message using the splitedCodeMessage version
    public static byte[] codeMessage(SensorMessage sensorMessage) {
        return codeMessage(
                sensorMessage.getDeviceType(),
                sensorMessage.getSensorType(),
                sensorMessage.getValue()
        );
    }

    // it decodes a byte array returning the corresponding SensorMessage data
    public static SensorMessage decodeMessage(byte[] message) {
        String stringMessage = new String(message, (charset));
        String[] splitedMessage = stringMessage.split(FIELDS_SEPARATOR);
        int deviceType = Integer.parseInt(splitedMessage[0]);
        int sensorType = Integer.parseInt(splitedMessage[1]);
        float[] value = decodeValue(splitedMessage[2]);


        return (new SensorMessage(deviceType, sensorType, value));
    }


}
