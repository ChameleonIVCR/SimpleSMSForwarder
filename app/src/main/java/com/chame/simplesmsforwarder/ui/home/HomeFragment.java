package com.chame.simplesmsforwarder.ui.home;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.chame.simplesmsforwarder.R;
import com.chame.simplesmsforwarder.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Button button = root.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager sms = SmsManager.getDefault();
                Context context = getActivity();
                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent =PendingIntent.getBroadcast(context, 0,new  Intent("SMS_DELIVERED"), 0);
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                System.out.println("OK");
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                System.out.println("GENERIC ERROR");
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                System.out.println("NO SERVICE");
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                System.out.println("NULL PDU");
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                System.out.println("RADIO OFF");
                                break;
                        }
                    }
                }, new IntentFilter("SMS_SENT"));

                // ---when the SMS has been delivered---
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                System.out.println("DELIVERED");
                                break;
                            case Activity.RESULT_CANCELED:
                                System.out.println("CANCELED");
                                break;
                        }
                    }
                }, new IntentFilter("SMS_DELIVERED"));

                sms.sendTextMessage("+573138816730", null, "Mensaje de prueba", sentIntent, deliveredIntent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}