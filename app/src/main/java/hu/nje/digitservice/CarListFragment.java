package hu.nje.digitservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
    // Amíg nincs "Új autó" képernyő, ezzel látod a Kezdőlapot. Élesben legyen false!
    private static final boolean SEED_TEST_DATA = true;

    // háttérszál az adatbázis-műveletekhez
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AppDatabase db;
    private CarAdapter carAdapter;
    private ServiceAdapter serviceAdapter;
    private TextView summaryName;
    private TextView summaryDetails;

    private long selectedCarId = -1;

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

        summaryName = view.findViewById(R.id.summary_name);
        summaryDetails = view.findViewById(R.id.summary_details);
        RecyclerView carRecycler = view.findViewById(R.id.car_recycler);
        RecyclerView serviceRecycler = view.findViewById(R.id.service_recycler);

        carAdapter = new CarAdapter(this::showCar);
        serviceAdapter = new ServiceAdapter();
        carRecycler.setAdapter(carAdapter);
        serviceRecycler.setAdapter(serviceAdapter);
    }

    // Minden alkalommal frissítünk, amikor a képernyő előtérbe kerül
    // (pl. ha egy új autót vettünk fel másik képernyőn).
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

    // Autók betöltése az adatbázisból (háttérszálon)
    private void loadCars() {
        executor.execute(() -> {
            if (SEED_TEST_DATA) {
                seedIfEmpty();
            }

            List<Car> cars = db.carDao().getAll();

            runOnUi(() -> {
                carAdapter.setCars(cars);

                if (cars.isEmpty()) {
                    showEmpty();
                    return;
                }

                // ha volt kiválasztott autó, az marad, különben az első
                Car toSelect = cars.get(0);
                for (Car car : cars) {
                    if (car.getId() == selectedCarId) {
                        toSelect = car;
                        break;
                    }
                }
                showCar(toSelect);
            });
        });
    }

    // Egy autó kiválasztása: összegző kártya + szervizlista frissítése
    private void showCar(Car car) {
        selectedCarId = car.getId();
        carAdapter.setSelectedCarId(car.getId());
        summaryName.setText(car.getManufacturer() + " " + car.getModel());

        executor.execute(() -> {
            List<ServiceEntry> entries = new ArrayList<>(
                    db.serviceEntryDao().getServiceEntriesByCarId(car.getId()));

            // legnagyobb km elöl (ez az újabb szerviz)
            Collections.sort(entries,
                    (a, b) -> Long.compare(b.getMileage(), a.getMileage()));

            long maxKm = entries.isEmpty() ? 0 : entries.get(0).getMileage();

            runOnUi(() -> {
                if (entries.isEmpty()) {
                    summaryDetails.setText(car.getLicensePlate() + " • még nincs szerviz");
                } else {
                    summaryDetails.setText(car.getLicensePlate() + " • " + maxKm + " km");
                }
                serviceAdapter.setEntries(entries);
            });
        });
    }

    // Üres adatbázis: nincs autó
    private void showEmpty() {
        summaryName.setText("Nincs kiválasztott autó");
        summaryDetails.setText("Még nincs autó felvéve");
        serviceAdapter.setEntries(new ArrayList<>());
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