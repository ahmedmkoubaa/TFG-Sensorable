package com.example.commons;

import android.util.Log;

import java.util.Date;

public class AdlRule {
    private long firstTimestamp = 0;
    private long lastTimestmap = 0;
    private boolean previuslyDetected = false;

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public long getLastTimestmap() {
        return lastTimestmap;
    }

    public boolean isPreviuslyDetected() {
        return previuslyDetected;
    }

    public boolean checkIfDetectedrule(boolean clausures[]) {
        // Evaluate the adl detection using the received params
        boolean adlRule = true;
        for (boolean clausure: clausures) {
            adlRule = adlRule & clausure;
        }

        // if the adl evaluation is positive then we track this adl state
        if (adlRule) {
            Log.i("NEWADL", "PHONE-CALL-DETECTED");

            // get starting time of this ADL
            if (!previuslyDetected) {
                firstTimestamp = getTimestamp();
            }

            // get always last time
            lastTimestmap = getTimestamp();
        }

        // if the adl was detected then is true in other case is false
        return previuslyDetected = adlRule;
    }

    private long getTimestamp() {
        return (new Date().getTime());
    }


};