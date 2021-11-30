package com.chame.simplesmsforwarder.ui.home;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.R;
import com.chame.simplesmsforwarder.databinding.FragmentHomeBinding;
import com.chame.simplesmsforwarder.utils.AppViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        AppViewModel mainViewModel = MainActivity.getInstance().getAppViewModel();
        mainViewModel.getSocketStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                binding.socketImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
            } else {
                binding.socketImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.purplish_red));
            }
        });

        mainViewModel.getGsmStatus().observe(getViewLifecycleOwner(), status -> {
            if (status) {
                binding.gsmImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));
            } else {
                binding.gsmImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.purplish_red));
            }
        });

        return binding.getRoot();
    }

        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}