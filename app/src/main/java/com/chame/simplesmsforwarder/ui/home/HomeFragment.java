package com.chame.simplesmsforwarder.ui.home;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.R;
import com.chame.simplesmsforwarder.databinding.FragmentHomeBinding;
import com.chame.simplesmsforwarder.models.EventViewModel;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        setLiveDataListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setLiveDataListeners() {
        EventViewModel eventPropagator = MainActivity.getInstance().getEventViewModel();

        eventPropagator.getSocketStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                binding.socketImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
            } else {
                binding.socketImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.purplish_red));
            }
        });

        eventPropagator.getGsmStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                binding.gsmImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
            } else {
                binding.gsmImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.purplish_red));
            }
        });

        eventPropagator.getSentMessages().observe(getViewLifecycleOwner(), count -> {
            binding.sentMessages.setText(String.valueOf(count));
        });

        eventPropagator.getFailedMessages().observe(getViewLifecycleOwner(), count -> {
            binding.failedMessages.setText(String.valueOf(count));
        });

        eventPropagator.getRescheduledMessages().observe(getViewLifecycleOwner(), count -> {
            binding.rescheduledMessages.setText(String.valueOf(count));
        });

        eventPropagator.getRunningTime().observe(getViewLifecycleOwner(), uptime -> {
            binding.uptimeTotal.setText(DateUtils.formatElapsedTime(uptime));
        });

        eventPropagator.getCurrentEvent().observe(getViewLifecycleOwner(), event -> {
            String eventVerbose;
            switch(event) {
                case Ready:
                    eventVerbose = "Ready";
                    break;
                case Sending:
                    eventVerbose = "Sending SMS...";
                    break;
                case Timeout:
                    eventVerbose = "In timeout...";
                    break;
                default:
                    eventVerbose = "Unknown event";
                    break;
            }
            binding.statusValue.setText(eventVerbose);
        });
    }
}