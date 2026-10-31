package hu.nje.digitservice;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
                return true;
            }
            if (id == R.id.nav_service) {
                showFragment(new ServiceEntryFragment());
                return true;
            }
            if (id == R.id.nav_settings) {
                // nem váltunk képernyőt, csak felugró ablakot nyitunk,
                // ezért false: a korábbi fül marad kijelölve
                showManageDialog();
                return false;
            }
            return false;
        });

        // induláskor a Garázs látszik
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

    // A Beállítások fül felugró menüje
    private void showManageDialog() {
        String[] options = {
                "+ Új autó",
                "Kiválasztott autó törlése",
                "Szervizadatok exportálása"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Jármű kezelése")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // TODO: ide jön az "Új autó" képernyő, ha elkészül
                        Toast.makeText(this, "Az új autó képernyő még készül",
                                Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        deleteSelectedCar();
                    } else {
                        // TODO: exportálás (PDF vagy CSV)
                        Toast.makeText(this, "Az exportálás még készül",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Bezárás", null)
                .show();
    }

    // A törlést a Garázs képernyő végzi, mert ott van a kiválasztott autó
    private void deleteSelectedCar() {
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (current instanceof CarListFragment) {
            ((CarListFragment) current).confirmDeleteSelectedCar();
        } else {
            Toast.makeText(this, "Az autó törléséhez nyisd meg a Garázs fület",
                    Toast.LENGTH_SHORT).show();
        }
    }
}