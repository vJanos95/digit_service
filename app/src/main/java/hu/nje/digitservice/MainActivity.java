package hu.nje.digitservice;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService exportExecutor =
            Executors.newSingleThreadExecutor();

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
                showManageDialog();
                return false;
            }

            return false;
        });

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

    // A Beállítások fül felugró menüje.
    private void showManageDialog() {
        String[] options = {
                "Új autó hozzáadása",
                "Kiválasztott autó törlése",
                "Szervizadatok exportálása"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Jármű kezelése")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.fragment_container,
                                        new NewCarFragment()
                                )
                                .addToBackStack(null)
                                .commit();

                    } else if (which == 1) {
                        deleteSelectedCar();

                    } else if (which == 2) {
                        exportSelectedCar();
                    }
                })
                .setNegativeButton("Bezárás", null)
                .show();
    }
    private void exportSelectedCar() {

        SelectedCarViewModel carVm =
                new ViewModelProvider(this)
                        .get(SelectedCarViewModel.class);

        Car selectedCar =
                carVm.getSelectedCar().getValue();

        if (selectedCar == null) {
            Toast.makeText(
                    this,
                    "Nincs kiválasztott autó az exportáláshoz.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "PDF készítése folyamatban...",
                Toast.LENGTH_SHORT
        ).show();

        AppDatabase db = DatabaseProvider.getDatabase(
                getApplicationContext()
        );

        exportExecutor.execute(() -> {

            try {
                File pdfFile = PdfExporter.generate(
                        getApplicationContext(),
                        db,
                        selectedCar
                );

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        sharePdf(pdfFile);
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                this,
                                "Hiba történt a PDF készítésekor: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void sharePdf(File pdfFile) {

        Uri pdfUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("application/pdf");

        shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                pdfUri
        );

        shareIntent.setClipData(
                ClipData.newUri(
                        getContentResolver(),
                        "Szervizkönyv PDF",
                        pdfUri
                )
        );

        shareIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Szervizkönyv megosztása"
                )
        );
    }

    private void deleteSelectedCar() {

        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (current instanceof CarListFragment) {
            ((CarListFragment) current).confirmDeleteSelectedCar();

        } else {
            Toast.makeText(
                    this,
                    "Az autó törléséhez nyisd meg a Garázs fület",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exportExecutor.shutdown();
    }
}