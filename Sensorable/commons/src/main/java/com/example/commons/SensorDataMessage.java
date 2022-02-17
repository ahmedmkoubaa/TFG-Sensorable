package com.example.commons;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SensorDataMessage {
    private static Charset charset = StandardCharsets.US_ASCII;
    private static final String separator = "~";
    public static void setCharset(Charset newCharset) {
        charset = newCharset;
    }

    public static String getSeparator() {
        return separator;
    }

    public static class SensorMessage implements Parcelable {
        private int sensorType;
        private String value;

        public SensorMessage(int sensorType, String value) {
            this.sensorType = sensorType;
            this.value = value;
        }

        protected SensorMessage(Parcel in) {
            sensorType = in.readInt();
            value = in.readString();
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

        public int getSensorType() {
            return sensorType;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return sensorType + separator + value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(sensorType);
            parcel.writeString(value);
        }
    }

    public static byte[] codeMessage(int sensorType, String value) {
        String msg = sensorType + separator + value;
        byte[] sensorData = msg.getBytes(charset);
        return sensorData;
    }

    public static byte[] codeMessage(SensorMessage sensorMessage) {
        return codeMessage(sensorMessage.getSensorType(), sensorMessage.getValue());
    }

    public static SensorMessage decodeMessage(byte[] message) {
        String stringMessage = new String(message, (charset));
        String[] splitedMessage = stringMessage.split(separator);
        int type = Integer.parseInt(splitedMessage[0]);

        return (new SensorMessage(type, splitedMessage[1]));
    }


}
