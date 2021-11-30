package com.chame.simplesmsforwarder;

import android.content.Intent;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.chame.simplesmsforwarder.ui.login.LoginActivity;
import com.chame.simplesmsforwarder.utils.DataAssistant;
import com.chame.simplesmsforwarder.utils.AppViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.chame.simplesmsforwarder.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity {
    public static WeakReference<MainActivity> weakActivity;
    private DataAssistant dataAssistant;
    private AppViewModel appViewModel;
    private ActivityMainBinding binding;
    public static MainActivity getInstance() {
        return weakActivity.get();
    }
    public DataAssistant getDataAssistant() {
        return dataAssistant;
    }
    public AppViewModel getAppViewModel() {
        return appViewModel;
    }


    public void setSnackbar(String msg) {
        Snackbar.make(
                findViewById(R.id.nav_host_fragment_activity_main),
                msg,
                Snackbar.LENGTH_SHORT
        ).show(
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakActivity = new WeakReference<>(MainActivity.this);
        dataAssistant = new DataAssistant(this);
        appViewModel = new ViewModelProvider
                .AndroidViewModelFactory(this.getApplication())
                .create(AppViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(intent);
    }
}