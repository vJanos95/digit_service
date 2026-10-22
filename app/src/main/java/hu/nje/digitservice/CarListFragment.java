package hu.nje.digitservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;
import hu.nje.digitservice.database.ServiceEntry;

public class CarListFragment extends Fragment {

    // Tesztelés: ha true, és az adatbázis üres, beír 2 autót és pár szervizt.
    // Élesben legyen false!
    private static final boolean SEED_TEST_DATA = false;

    // háttérszál az adatbázis-műveletekhez
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AppDatabase db;
    private CarAdapter carAdapter;
    private ServiceAdapter serviceAdapter;
    private TextView emptyText;

    private long selectedCarId = -1;
    private List<CarItem> currentItems = new ArrayList<>();

    public CarListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseProvider.getDatabase(requireContext());

        emptyText = view.findViewById(R.id.car_empty);
        RecyclerView carRecycler = view.findViewById(R.id.car_recycler);
        RecyclerView serviceRecycler = view.findViewById(R.id.service_recycler);

        carAdapter = new CarAdapter(this::showCar);
        serviceAdapter = new ServiceAdapter();
        carRecycler.setAdapter(carAdapter);
        serviceRecycler.setAdapter(serviceAdapter);
    }

    // Minden alkalommal frissítünk, amikor a képernyő előtérbe kerül
    @Override
    public void onResume() {
        super.onResume();
        loadCars();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // Autók betöltése az adatbázisból (háttérszálon), csempénként összesítve
    private void loadCars() {
        executor.execute(() -> {
            if (SEED_TEST_DATA) {
                seedIfEmpty();
            }

            List<Car> cars = db.carDao().getAll();
            List<CarItem> items = new ArrayList<>();

            for (Car car : cars) {
                List<ServiceEntry> entries =
                        db.serviceEntryDao().getServiceEntriesByCarId(car.getId());

                // a legnagyobb km-es szerviz számít a legutóbbinak
                boolean found = false;
                long maxKm = 0;
                String lastDate = "";
                for (ServiceEntry entry : entries) {
                    if (!found || entry.getMileage() > maxKm) {
                        found = true;
                        maxKm = entry.getMileage();
                        lastDate = entry.getServiceDate();
                    }
                }
                items.add(new CarItem(car, found, maxKm, lastDate));
            }

            runOnUi(() -> {
                currentItems = items;
                carAdapter.setItems(items);

                if (items.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    serviceAdapter.setEntries(new ArrayList<>());
                    return;
                }
                emptyText.setVisibility(View.GONE);

                // ha volt kiválasztott autó, az marad, különben az első
                long idToSelect = items.get(0).car.getId();
                for (CarItem item : items) {
                    if (item.car.getId() == selectedCarId) {
                        idToSelect = selectedCarId;
                        break;
                    }
                }
                showServices(idToSelect);
            });
        });
    }

    // Kattintás egy csempére
    private void showCar(Car car) {
        showServices(car.getId());
    }

    // Kiválasztott autó: keret a csempén + szervizlista frissítése
    private void showServices(long carId) {
        selectedCarId = carId;
        carAdapter.setSelectedCarId(carId);

        executor.execute(() -> {
            List<ServiceEntry> entries = new ArrayList<>(
                    db.serviceEntryDao().getServiceEntriesByCarId(carId));

            // legnagyobb km elöl (ez az újabb szerviz)
            Collections.sort(entries,
                    (a, b) -> Long.compare(b.getMileage(), a.getMileage()));

            runOnUi(() -> serviceAdapter.setEntries(entries));
        });
    }

    // A MainActivity hívja a Beállítások felugró menüjéből
    public void confirmDeleteSelectedCar() {
        Car selected = null;
        for (CarItem item : currentItems) {
            if (item.car.getId() == selectedCarId) {
                selected = item.car;
                break;
            }
        }

        if (selected == null) {
            Toast.makeText(requireContext(), "Nincs kiválasztott autó",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final Car carToDelete = selected;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Autó törlése")
                .setMessage("Biztosan törlöd: " + carToDelete.getManufacturer() + " "
                        + carToDelete.getModel() + " (" + carToDelete.getLicensePlate() + ")?\n\n"
                        + "A hozzá tartozó szervizbejegyzések is törlődnek.")
                .setNegativeButton("Mégse", null)
                .setPositiveButton("Törlés", (dialog, which) -> {
                    selectedCarId = -1;
                    deleteCar(carToDelete);
                })
                .show();
    }

    // Törlés háttérszálon, utána a lista újratöltése
    private void deleteCar(Car car) {
        executor.execute(() -> {
            db.carDao().delete(car);
            runOnUi(this::loadCars);
        });
    }

    // A UI-t csak a főszálon szabad módosítani
    private void runOnUi(Runnable action) {
        Activity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        activity.runOnUiThread(action);
    }

    // ---- Csak tesztelésre: ha üres az adatbázis, beír pár adatot ----

    private void seedIfEmpty() {
        if (!db.carDao().getAll().isEmpty()) {
            return;
        }

        long laguna = db.carDao().insert(new Car("Renault", "Laguna II", "DOK-987", 2004));
        long peugeot = db.carDao().insert(new Car("Peugeot", "206", "ABC-123", 2002));

        db.serviceEntryDao().insert(new ServiceEntry(laguna,
                "Motorolaj & Szűrők cseréje", "", 182000, "2026.09.12.", 42000));
        db.serviceEntryDao().insert(new ServiceEntry(laguna,
                "Vezérlés készlet csere", "", 165000, "2025.11.04.", 135000));
        db.serviceEntryDao().insert(new ServiceEntry(laguna,
                "Fékfolyadék & Első fékek", "", 158000, "2025.03.18.", 68000));
        db.serviceEntryDao().insert(new ServiceEntry(peugeot,
                "Olajcsere", "", 142500, "2026.05.20.", 28000));
    }
}