package com.sensorable.utils;

import com.commons.utils.OperatorType;
import com.commons.utils.SensorableConstants;
import com.commons.database.ActivityEntity;
import com.commons.database.ActivityStepEntity;
import com.commons.database.AdlEntity;
import com.commons.database.EventEntity;
import com.commons.database.EventForAdlEntity;
import com.commons.database.StepsForActivitiesEntity;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.util.ArrayList;
import java.util.Arrays;

public class TablesFormatter {

    public static String getPayloadAsString(final Mqtt5Publish payload) {
        return new String(payload.getPayloadAsBytes());
    }

    public static String[] getTables(final String payload) {
        return payload.split(SensorableConstants.JSON_TABLES_SEPARATOR);
    }

    public static String[] getTables(final Mqtt5Publish payload) {
        return Arrays.stream(getPayloadAsString(payload).split(SensorableConstants.JSON_TABLES_SEPARATOR))
                .map(t -> removeFirstAndLastChar(t))
                .toArray(String[]::new);
    }

    private static String removeFirstAndLastChar(String string) {
        return string.substring(1, string.length() - 1);
    }

    public static final ArrayList<ActivityEntity> composeTableActivities(final String activities) {
        ArrayList<ActivityEntity> activityEntities = new ArrayList<>();
        String[] fields;

        for (String r : activities.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            activityEntities.add(new ActivityEntity(Integer.parseInt(fields[0]), fields[1], fields[2]));

        }

        return activityEntities;
    }

    public static final ArrayList<ActivityStepEntity> composeTableSteps(final String steps) {
        ArrayList<ActivityStepEntity> stepsEntities = new ArrayList<>();
        String[] fields;

        for (String r : steps.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            stepsEntities.add(new ActivityStepEntity(Integer.parseInt(fields[0]), fields[1]));

        }

        return stepsEntities;

    }

    public static final ArrayList<StepsForActivitiesEntity> composeTableStepsForActivities(final String stepsForActivities) {
        ArrayList<StepsForActivitiesEntity> stepsForActivitieEntities = new ArrayList<>();
        String[] fields;

        for (String r : stepsForActivities.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            stepsForActivitieEntities.add(
                    new StepsForActivitiesEntity(
                            Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2])
                    )
            );

        }

        return stepsForActivitieEntities;
    }


    public static final ArrayList<AdlEntity> composeTableAdls(final String stringAdls) {
        ArrayList<AdlEntity> adlEntities = new ArrayList<>();
        String[] fields;

        for (String r : stringAdls.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            adlEntities.add(new AdlEntity(Integer.parseInt(fields[0]), fields[1], fields[2]));
        }

        return adlEntities;
    }

    public static final ArrayList<EventEntity> composeTableEvents(final String stringEvents) {
        ArrayList<EventEntity> eventEntities = new ArrayList<>();
        String[] fields;

        for (String r : stringEvents.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            eventEntities.add(
                    new EventEntity(
                            Integer.parseInt(fields[0]),
                            Integer.parseInt(fields[1]),
                            Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3]),
                            OperatorType.valueOf(fields[4]),
                            Float.parseFloat(fields[5]),
                            fields[6]
                    )
            );
        }

        return eventEntities;
    }

    public static final ArrayList<EventForAdlEntity> composeTableEventsForAdls(final String stringEventsForAdls) {
        ArrayList<EventForAdlEntity> eventsForAdlsEntities = new ArrayList<>();
        String[] fields;

        for (String r : stringEventsForAdls.split(SensorableConstants.JSON_ROWS_SEPARATOR)) {
            fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
            eventsForAdlsEntities.add(
                    new EventForAdlEntity(
                            Integer.parseInt(fields[0]),
                            Integer.parseInt(fields[1]),
                            Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3])
                    )
            );
        }

        return eventsForAdlsEntities;
    }


}
