package hu.nje.digitservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;

public class CarEntryFragment extends Fragment {

    private static final String TAG = "CarEntryFragment";

    private TextInputLayout manufacturerLayout;
    private TextInputLayout modelLayout;
    private TextInputLayout productionYearLayout;
    private TextInputLayout licensePlateLayout;

    private TextInputEditText manufacturerInput;
    private TextInputEditText modelInput;
    private TextInputEditText productionYearInput;
    private TextInputEditText licensePlateInput;

    private MaterialButton saveButton;

    private AppDatabase db;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public CarEntryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DatabaseProvider.getDatabase(
                requireContext().getApplicationContext()
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_car_entry,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        manufacturerLayout =
                view.findViewById(R.id.layout_manufacturer);
        modelLayout =
                view.findViewById(R.id.layout_model);
        productionYearLayout =
                view.findViewById(R.id.layout_production_year);
        licensePlateLayout =
                view.findViewById(R.id.layout_license_plate);

        manufacturerInput =
                view.findViewById(R.id.input_manufacturer);
        modelInput =
                view.findViewById(R.id.input_model);
        productionYearInput =
                view.findViewById(R.id.input_production_year);
        licensePlateInput =
                view.findViewById(R.id.input_license_plate);

        saveButton = view.findViewById(R.id.button_save_car);
        saveButton.setOnClickListener(v -> saveCar());
    }

    private void saveCar() {
        clearErrors();

        String manufacturer = getText(manufacturerInput);
        String model = getText(modelInput);
        String productionYear = getText(productionYearInput);
        String licensePlate = getText(licensePlateInput);

        boolean valid = true;

        if (manufacturer.isEmpty()) {
            manufacturerLayout.setError("Add meg a gyártót.");
            valid = false;
        }

        if (model.isEmpty()) {
            modelLayout.setError("Add meg a modellt.");
            valid = false;
        }

        if (productionYear.isEmpty()) {
            productionYearLayout.setError("Add meg a gyártási évet.");
            valid = false;
        }

        if (licensePlate.isEmpty()) {
            licensePlateLayout.setError("Add meg a rendszámot.");
            valid = false;
        }

        int year = 0;

        if (!productionYear.isEmpty()) {
            try {
                year = Integer.parseInt(productionYear);

            } catch (NumberFormatException e) {
                productionYearLayout.setError("Az év csak szám lehet.");
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        Car car = new Car(manufacturer, model, licensePlate, year);

        saveButton.setEnabled(false);

        databaseExecutor.execute(() -> {
            try {
                db.carDao().insert(car);

                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Az autó sikeresen mentve.",
                            Toast.LENGTH_SHORT
                    ).show();

                    BottomNavigationView nav =
                            requireActivity().findViewById(R.id.bottom_nav);

                    nav.setSelectedItemId(R.id.nav_home);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to save car", e);

                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    saveButton.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Nem sikerült menteni az autót.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }

    private void clearErrors() {
        manufacturerLayout.setError(null);
        modelLayout.setError(null);
        productionYearLayout.setError(null);
        licensePlateLayout.setError(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
