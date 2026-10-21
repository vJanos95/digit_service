package hu.nje.digitservice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hu.nje.digitservice.database.Car;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView nav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nav = findViewById(R.id.bottom_nav);


        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showFragment(new CarListFragment());
                nav.getMenu().findItem(R.id.nav_service).setVisible(false);
                nav.getMenu().findItem(R.id.nav_home).setVisible(false);
                nav.getMenu().findItem(R.id.nav_car).setVisible(true);
            } else if (id == R.id.nav_service) {
                showFragment(new ServiceEntryFragment());
                nav.getMenu().findItem(R.id.nav_home).setVisible(true);
                nav.getMenu().findItem(R.id.nav_car).setVisible(false);
            } else if (id == R.id.nav_car) {
                nav.getMenu().findItem(R.id.nav_home).setVisible(true);
                nav.getMenu().findItem(R.id.nav_car).setVisible(false);
                showFragment(new CarEntryFragment());
            } else {
                return false;
            }

            return true;
        });
        // induláskor a Kezdőlap látszik
        if (savedInstanceState == null) {
            nav.getMenu().findItem(R.id.nav_home).setVisible(false);
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    public void openCarServices(Car car) {
        nav.getMenu().findItem(R.id.nav_home).setVisible(true);
        nav.getMenu().findItem(R.id.nav_service).setVisible(true);
        nav.getMenu().findItem(R.id.nav_car).setVisible(false);
        ServiceListFragment fragment = ServiceListFragment.newInstance(car);
        showFragment(fragment);
    }
}