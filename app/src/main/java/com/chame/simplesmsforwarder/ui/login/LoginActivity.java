package com.chame.simplesmsforwarder.ui.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.R;
import com.chame.simplesmsforwarder.socket.SocketClient;
import com.chame.simplesmsforwarder.utils.Configuration;
import com.google.android.material.snackbar.Snackbar;
import com.chame.simplesmsforwarder.utils.Utils;


public class LoginActivity extends AppCompatActivity {
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this::onLogin);

        Configuration configuration = MainActivity.getInstance().getDataAssistant().getConfiguration();

        // Set initial test-phone
        if (configuration.getProperty("test-phone") == null) {
            setTestPhoneNumber(configuration);
        }

        // Permissions check
        if (ContextCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            showPermissionPopup();
        }

        // region If credentials were remembered
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
                message = "The provided Token was incorrect, please try again.";
                break;
            case InvalidUri:
                message = "The provided IP and Port combination is not valid, please try again.";
                break;
            case ConnectionError:
                message = "Connection could not be established. Check the credentials, or your connection.";
                break;
        }

        Snackbar.make(
                findViewById(R.id.loginBackground),
                message,
                Snackbar.LENGTH_LONG
        ).show(
        );

        MainActivity.getInstance().getAppViewModel().disconnectSocket();
        Utils.runOnUiThread(() -> connectButton.setEnabled(true));
    }

    private void showPermissionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SMS Permissions");
        final TextView description = new TextView(this);
        description.setText(R.string.smsPermissionDescription);
        builder.setView(description);

        builder.setPositiveButton("Accept", (dialog, which) -> {
            dialog.cancel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.SEND_SMS}, 1);
            }
        });

        builder.setNegativeButton("Decline", (dialog, which) -> {
            dialog.cancel();
            finish();
        });

        builder.show();
    }

    private void setTestPhoneNumber(Configuration config) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set testing phone number (Can be your own)");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Ok", (dialog, which) -> {
            config.setProperty("test-phone", input.getText().toString());
            config.save();
            dialog.cancel();
        });

        builder.show();
    }
}