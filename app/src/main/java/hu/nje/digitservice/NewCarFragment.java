package hu.nje.digitservice;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;

public class NewCarFragment extends Fragment {

    // Adatbázis összerendelése
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AppDatabase db;

    private TextInputLayout layoutManufacturer;
    private TextInputLayout layoutModel;
    private TextInputLayout layoutPlate;
    private TextInputLayout layoutYear;

    private TextInputEditText editManufacturer;
    private TextInputEditText editModel;
    private TextInputEditText editPlate;
    private TextInputEditText editYear;

    public NewCarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseProvider.getDatabase(requireContext());

        layoutManufacturer = view.findViewById(R.id.layoutManufacturer);
        layoutModel = view.findViewById(R.id.layoutModel);
        layoutPlate = view.findViewById(R.id.layoutPlate);
        layoutYear = view.findViewById(R.id.layoutYear);

        editManufacturer = view.findViewById(R.id.editManufacturer);
        editModel = view.findViewById(R.id.editModel);
        editPlate = view.findViewById(R.id.editPlate);
        editYear = view.findViewById(R.id.editYear);

        view.findViewById(R.id.buttonSave).setOnClickListener(v -> saveCar());
        view.findViewById(R.id.buttonCancel).setOnClickListener(v -> closeFragment());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void saveCar() {
        String manufacturer = getText(editManufacturer);
        String model = getText(editModel);
        String plate = getText(editPlate).toUpperCase();
        String yearText = getText(editYear);

        if (!validate(manufacturer, model, plate, yearText)) {
            return;
        }

        final Car car = new Car(manufacturer, model, plate, Integer.parseInt(yearText));

        // Mentés után visszatérés,állapot visszaküldés
        executor.execute(() -> {
            try {
                db.carDao().insert(car);
                runOnUi(() -> {
                    Toast.makeText(requireContext(), "Autó mentve",
                            Toast.LENGTH_SHORT).show();
                    closeFragment();
                });
            } catch (Exception e) {
                runOnUi(() -> Toast.makeText(requireContext(),
                        "Nem sikerült menteni az autót",
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private boolean validate(String manufacturer, String model, String plate, String yearText) {
        boolean valid = true;

        layoutManufacturer.setError(null);
        layoutModel.setError(null);
        layoutPlate.setError(null);
        layoutYear.setError(null);

//HIBAKEZELÉSEK
        if (TextUtils.isEmpty(manufacturer)) {
            layoutManufacturer.setError("A gyártó megadása kötelező");
            valid = false;
        }

        if (TextUtils.isEmpty(model)) {
            layoutModel.setError("A típus megadása kötelező");
            valid = false;
        }

        if (TextUtils.isEmpty(plate)) {
            layoutPlate.setError("A rendszám megadása kötelező");
            valid = false;
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (TextUtils.isEmpty(yearText)) {
            layoutYear.setError("Az évjárat megadása kötelező");
            valid = false;
        } else {
            int year = Integer.parseInt(yearText);
            if (year < 1900 || year > currentYear + 1) {
                layoutYear.setError("Érvénytelen évjárat");
                valid = false;
            }
        }

        return valid;
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void closeFragment() {
        getParentFragmentManager().popBackStack();
    }

    // Ez már csak az UI
    private void runOnUi(Runnable action) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(action);
    }
}