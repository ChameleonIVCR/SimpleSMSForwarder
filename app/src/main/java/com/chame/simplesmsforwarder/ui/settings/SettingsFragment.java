package com.chame.simplesmsforwarder.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.databinding.FragmentSettingsBinding;
import com.chame.simplesmsforwarder.utils.Configuration;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        // Listeners
        binding.networkTestPopup.setOnClickListener(this::onNetworkPopup);
        binding.networkTimeoutPopup.setOnClickListener(this::onTimeoutPopup);

        loadConfiguration();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onNetworkPopup(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set new testing phone number");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
                MainActivity.getInstance().getDataAssistant().getConfiguration()
                        .setProperty("test-phone", input.getText().toString());
                dialog.cancel();
                saveConfiguration();
            }
        );

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void onTimeoutPopup(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set new GSM timeout");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
                    MainActivity.getInstance().getDataAssistant().getConfiguration()
                            .setProperty("timeout", input.getText().toString());
                    dialog.cancel();
                    saveConfiguration();
                }
        );

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveConfiguration(){
        MainActivity.getInstance().getDataAssistant().getConfiguration().save();
        loadConfiguration();
    }

    private void loadConfiguration() {
        Configuration config = MainActivity.getInstance().getDataAssistant().getConfiguration();
        binding.networkTimeoutValue.setText(config.getProperty("timeout"));
        binding.networkTestValue.setText(config.getProperty("test-phone"));
    }
}