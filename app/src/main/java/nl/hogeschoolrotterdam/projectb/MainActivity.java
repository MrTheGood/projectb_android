package nl.hogeschoolrotterdam.projectb;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import nl.hogeschoolrotterdam.projectb.util.AnalyticsUtil;
import nl.hogeschoolrotterdam.projectb.util.LocationManager;

/**
 * Created by maartendegoede on 05/03/2019.
 * Copyright © 2019 Anass El Mahdaoui, Hicham El Marzgioui, Michaël van Asperen, Wesley de Man, Maarten de Goede all rights reserved.
 */
public class MainActivity extends AppCompatActivity {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(WhibApp.getInstance().getThemeId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean hasShownTutorial = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hasShownTutorial", false);
        if (!hasShownTutorial) {
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.putExtra("startMainActivity", true);
            startActivity(intent);
            finish();
        }
        if (WhibApp.getInstance().shouldLogIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // This connects the bottom navigation bar with the navigation graph,
        // this way the navigation for the fragments is handled automatically,
        // as long as the menu_main_bottom_navigation.xml ids match the ids of the pages in navigation_main.xml
        NavController navController = Navigation.findNavController(findViewById(R.id.nav_host));
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);


        View button_add_memory = findViewById(R.id.button_add_memory);
        button_add_memory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MemoryEditActivity.class);
                startActivity(intent);
                AnalyticsUtil.addContent(MainActivity.this, "FAB");
            }
        });

        LocationManager
                .getInstance()
                .initialize(this)
                .updateLocation(this, new LocationManager.OnLocationResultListener() {
                    @Override
                    public void onLocationResult(@Nullable Location location) {
                        //initialized location
                    }
                });
    }

}
