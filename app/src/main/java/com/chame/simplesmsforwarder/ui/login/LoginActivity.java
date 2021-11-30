package com.chame.simplesmsforwarder.ui.login;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.R;
import com.chame.simplesmsforwarder.socket.SocketClient;
import com.chame.simplesmsforwarder.utils.Configuration;
import com.google.android.material.snackbar.Snackbar;
import com.chame.simplesmsforwarder.utils.Utils;


public class LoginActivity extends AppCompatActivity {
    private SocketClient socketClient;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this::onLogin);

        // region If credentials were remembered
        Configuration configuration = MainActivity.getInstance().getDataAssistant().getConfiguration();
        final boolean rememberCredentials = Boolean.parseBoolean(
                configuration.getProperty("remember-credentials")
        );

        if (rememberCredentials && configuration.hasCredentials()) {
            connectButton.setEnabled(false);
            MainActivity.getInstance().getAppViewModel().setSocketClientAndConnect(
                    this::onLoginFailure,
                    this::onLoginSuccess,
                    configuration.getProperty("ip"),
                    configuration.getProperty("port"),
                    configuration.getProperty("token"),
                    false
            );
        }
        // endregion
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK;
    }

    private void onLogin(View v) {
        connectButton.setEnabled(false);

        EditText ipField = findViewById(R.id.ipField);
        EditText portField = findViewById(R.id.portField);
        EditText tokenField = findViewById(R.id.tokenField);

        MainActivity.getInstance().getAppViewModel().setSocketClientAndConnect(
                this::onLoginFailure,
                this::onLoginSuccess,
                ipField.getText().toString(),
                portField.getText().toString(),
                tokenField.getText().toString(),
                false
        );
    }

    private void onLoginSuccess() {
        MainActivity.getInstance().getAppViewModel().setSocketClientMainListeners();
        finish();
    }

    private void onLoginFailure(SocketClient.FailureCode f) {
        String message = "";

        switch(f) {
            case AuthError:
                message = "The provided Token was incorrect, please try again";
                break;
            case InvalidUri:
                message = "The provided IP and Port combination is not valid, please try again.";
                break;
            case ConnectionError:
                message = "There was a connection error, please check your internet connection, and server reachability.";
                break;
        }

        Snackbar.make(
                findViewById(R.id.loginBackground),
                message,
                Snackbar.LENGTH_LONG
        ).show(
        );

        Utils.runOnUiThread(() -> connectButton.setEnabled(true));
    }
}