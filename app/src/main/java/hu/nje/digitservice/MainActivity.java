package hu.nje.digitservice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);


        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showFragment(new CarListFragment());
            } else if (id == R.id.nav_service) {
                showFragment(new ServiceEntryFragment());
            } else if (id == R.id.nav_car) {
                showFragment(new CarEntryFragment());
            } else {
                return false;
            }

            return true;
        });

        // induláskor a Kezdőlap látszik
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}