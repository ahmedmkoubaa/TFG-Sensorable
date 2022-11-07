package com.commons;

import android.os.Parcel;
import android.os.Parcelable;

import com.commons.database.SensorMessageEntity;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class SensorTransmissionCoder {
    private static final String FIELDS_SEPARATOR = "~";
    private static final String VALUES_SEPARATOR = ",";
    private static Charset charset = StandardCharsets.US_ASCII;

    public static void setCharset(Charset newCharset) {
        charset = newCharset;
    }

    public static String getFieldsSeparator() {
        return FIELDS_SEPARATOR;
    }

    public static String getValuesSeparator() {
        return VALUES_SEPARATOR;
    }

    // it recevies an array and transform it to a String using the value_separator
    private static String codeValue(float[] value) {
        String msg = "";
        for (Float f : value) {
            msg += f + VALUES_SEPARATOR;
        }

        return msg;
    }

    // it receives a value in string format and decodes it to a normal float array
    private static float[] decodeValue(String value) {
        String[] msg = value.split(VALUES_SEPARATOR);

        int size = msg.length;
        float[] arrayValue = new float[size];

        for (int i = 0; i < size; i++) {
            arrayValue[i] = Float.parseFloat(msg[i]);
        }

        return arrayValue;
    }

    // it receives the necessary data to form a SensorMessage and it
    // transforms it to String and later to bytes
    public static byte[] codeMessage(int device, int sensorType, float[] value, long timestamp) {
        String msg = device + FIELDS_SEPARATOR + sensorType + FIELDS_SEPARATOR + codeValue(value) + FIELDS_SEPARATOR + timestamp;
        return msg.getBytes(charset);
    }

    // Code the sensor message using the splitedCodeMessage version
    public static byte[] codeMessage(SensorMessage sensorMessage) {
        return codeMessage(
                sensorMessage.getDeviceType(),
                sensorMessage.getSensorType(),
                sensorMessage.getValue(),
                sensorMessage.getTimestamp()
        );
    }

    // it decodes a byte array returning the corresponding SensorMessage data
    public static SensorMessage decodeMessage(byte[] message) {
        String stringMessage = new String(message, (charset));
        String[] splitedMessage = stringMessage.split(FIELDS_SEPARATOR);
        int deviceType = Integer.parseInt(splitedMessage[0]);
        int sensorType = Integer.parseInt(splitedMessage[1]);
        float[] value = decodeValue(splitedMessage[2]);
        long timestamp = Long.parseLong(splitedMessage[3]);


        return (new SensorMessage(deviceType, sensorType, value, timestamp));
    }

    public static class SensorMessage implements Parcelable {
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

        private int sensorType;
        private float[] value;
        private int deviceType;
        private long timestamp;

        public SensorMessage(int deviceType, int sensorType, float[] value) {
            initializeSensorMessage(deviceType, sensorType, value, new Date().getTime());
        }

        public SensorMessage(int deviceType, int sensorType, float[] value, long timestamp) {
            initializeSensorMessage(deviceType, sensorType, value, timestamp);
        }

        protected SensorMessage(Parcel in) {
            deviceType = in.readInt();
            sensorType = in.readInt();
            value = decodeValue(in.readString());
            timestamp = in.readLong();
        }

        public SensorMessageEntity toSensorDataMessage(final String userCode) {
            float valueY = 0, valueZ = 0;

            if (value.length > 1) {
               valueY = value[1];
            }

            if (value.length > 2) {
                valueZ = value[2];
            }

            return new SensorMessageEntity(deviceType, sensorType, value[0], valueY, valueZ, timestamp, userCode);
        }

        private void initializeSensorMessage(int deviceType, int sensorType, float[] value, long timestamp) {
            this.deviceType = deviceType;
            this.sensorType = sensorType;
            this.value = value;
            this.timestamp = timestamp;
        }

        public int getDeviceType() {
            return deviceType;
        }

        public int getSensorType() {
            return sensorType;
        }

        public float[] getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String toString() {
            return deviceType + FIELDS_SEPARATOR +
                    sensorType + FIELDS_SEPARATOR +
                    Arrays.toString(value) + FIELDS_SEPARATOR +
                    timestamp;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(deviceType);
            parcel.writeInt(sensorType);
            parcel.writeString(codeValue(value));
            parcel.writeLong(timestamp);
        }
    }


}
