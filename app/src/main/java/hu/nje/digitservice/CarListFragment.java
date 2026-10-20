package hu.nje.digitservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;

public class CarListFragment extends Fragment {

    private static final String TAG = "CarListFragment";

    private AppDatabase db;
    private CarAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyMessage;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public CarListFragment() {
        // Required empty public constructor
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
                R.layout.fragment_car_list,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.car_recycler_view);
        emptyMessage = view.findViewById(R.id.car_list_empty);

        adapter = new CarAdapter(this::confirmDelete);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        recyclerView.setAdapter(adapter);

        loadCars();
    }

    private void confirmDelete(Car car) {
        if (!isAdded()) {
            return;
        }

        String carName = car.getManufacturer() + " " + car.getModel();

        new AlertDialog.Builder(requireContext())
                .setTitle("Autó törlése?")
                .setMessage(
                        "Biztosan törölni akarod a(z) "
                                + carName + "autót?"
                                + "\n\nA hozzá tartozó szervíz rekordok is törlésre kerülnek."
                )
                .setNegativeButton("Mégse", (dialog, which) -> {
                })
                .setPositiveButton("Törlés", (dialog, which) ->
                        deleteCar(car)
                )
                .show();
    }

    private void deleteCar(Car car) {
        databaseExecutor.execute(() -> {
            try {
                db.carDao().delete(car);

                // Reload the list after deletion.
                loadCars();

            } catch (Exception e) {
                Log.e(TAG, "Failed to delete car", e);

                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                "Could not delete the car.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private void loadCars() {
        databaseExecutor.execute(() -> {
            try {
                List<Car> cars = db.carDao().getAll();

                mainHandler.post(() -> {
                    if (!isAdded()
                            || recyclerView == null
                            || emptyMessage == null
                            || adapter == null) {
                        return;
                    }

                    adapter.setCars(cars);

                    boolean isEmpty = cars.isEmpty();

                    emptyMessage.setVisibility(
                            isEmpty ? View.VISIBLE : View.GONE
                    );

                    recyclerView.setVisibility(
                            isEmpty ? View.GONE : View.VISIBLE
                    );
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load cars from database", e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        recyclerView = null;
        emptyMessage = null;
        adapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}