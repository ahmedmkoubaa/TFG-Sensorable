package com.sensorable.utils;

import android.util.Log;

import com.commons.OperatorType;
import com.commons.database.EventEntity;
import com.commons.database.KnownLocationEntity;

import java.util.ArrayList;

public class SensorOperations {
    private static boolean equal(float leftOperand, float rightOperand) {
        return leftOperand == rightOperand;
    }

    private static boolean notEqual(float leftOperand, float rightOperand) {
        return leftOperand != rightOperand;
    }

    private static boolean greaterEqual(float leftOperand, float rightOperand) {
        return leftOperand >= rightOperand;
    }

    private static boolean lessEqual(float leftOperand, float rightOperand) {
        return leftOperand <= rightOperand;
    }

    private static boolean greater(float leftOperand, float rightOperand) {
        return leftOperand > rightOperand;
    }

    private static boolean less(float leftOperand, float rightOperand) {
        return leftOperand < rightOperand;
    }

    public static SensorOperation switchOperation(OperatorType operator) {
        switch (operator) {
            case EQUAL:
                return SensorOperations::equal;

            case NOT_EQUAL:
                return SensorOperations::notEqual;

            case LESS:
                return SensorOperations::less;

            case LESS_EQUAL:
                return SensorOperations::lessEqual;

            case GREATER:
                return SensorOperations::greater;

            case GREATER_EQUAL:
                return SensorOperations::greaterEqual;

            default:
                Log.i("SensorOperations", "not recognized operand, something went wrong");
                return null;
        }
    }

    public static boolean switchOperate(SensorOperation operation, float[] values, EventEntity e, final ArrayList<KnownLocationEntity> knownLocations) {
        switch (e.pos) {
            case SensorAction.FIRST:
            case SensorAction.SECOND:
            case SensorAction.THIRD:
                return operation.operate(values[e.pos], e.operand);

            case SensorAction.DISTANCE:
                // Let's look for the desired location
                for (KnownLocationEntity k : knownLocations) {
                    if (k.tag.equals(e.tag)) {

                        // calculate 3d distance from current gps value (in s) and the known location whose tag fits
                        float distance = (float) Math.sqrt(
                                Math.pow(values[0] - k.altitude, 2) +
                                        Math.pow(values[1] - k.latitude, 2) +
                                        Math.pow(values[2] - k.longitude, 2)
                        );

                        // when a true case is found, the loop is finished
                        if (operation.operate(distance, e.operand)) {
                            return true;
                        }
                    }
                }

                return false;

            case SensorAction.ANY:
                return operation.operate(values[0], e.operand) ||
                        operation.operate(values[1], e.operand) ||
                        operation.operate(values[2], e.operand);


            case SensorAction.ALL:
                return operation.operate(values[0], e.operand) &&
                        operation.operate(values[1], e.operand) &&
                        operation.operate(values[2], e.operand);
            default:
                Log.e("SensorOperations", "received a non expected position in switchOperate");
                return false;
        }
    }


    @FunctionalInterface
    public interface SensorOperation {
        boolean operate(float leftOperand, float rightOperand);
    }


}
