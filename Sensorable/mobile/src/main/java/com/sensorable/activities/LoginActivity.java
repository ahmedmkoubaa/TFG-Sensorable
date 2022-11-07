package com.sensorable.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.google.android.material.textfield.TextInputEditText;
import com.sensorable.MainActivity;
import com.sensorable.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button confirmButton = (Button) findViewById(R.id.confirmLoginButton);
        EditText userCodeInput = (EditText) findViewById(R.id.userCodeInput);

        confirmButton.setOnClickListener(view -> {
            String userCode = userCodeInput.getText().toString();
            if (validateUserCode(userCode)) {
                saveLogin(userCode);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            } else {
                findViewById(R.id.loginErrorText).setVisibility(View.VISIBLE);
            }
        });

    }

    private void saveLogin(final String userCode) {
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = saved_values.edit();
        editor.putBoolean(SensorableConstants.LOGIN_DONE, true);
        editor.putString(SensorableConstants.USER_SESSION_CODE, userCode);
        editor.commit();
    }

    // the condition to have a verified and good user code
    private boolean validateUserCode(String code) {
        return code.trim().length() > 3;
    }
}