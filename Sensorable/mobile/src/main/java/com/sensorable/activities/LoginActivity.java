package com.sensorable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.sensorable.MainActivity;
import com.sensorable.R;
import com.commons.LoginHelper;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button confirmButton = (Button) findViewById(R.id.confirmLoginButton);
        EditText userCodeInput = (EditText) findViewById(R.id.userCodeInput);

        confirmButton.setOnClickListener(view -> {
            String userCode = userCodeInput.getText().toString();
            if (LoginHelper.validateUserCode(userCode)) {
                LoginHelper.saveLogin(getApplicationContext(), userCode);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            } else {
                findViewById(R.id.loginErrorText).setVisibility(View.VISIBLE);
            }
        });

    }


}