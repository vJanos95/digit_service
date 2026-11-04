package hu.nje.digitservice;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.DatabaseProvider;
import hu.nje.digitservice.database.ServiceEntry;

public class ServiceEntryFragment extends Fragment {
    private EditText szerviz;
    private EditText kilometer;
    private EditText osszeg;
    private Button mentes;
    private Button vissza;
    private TextView autoNev;
    private Car kivalasztottAuto;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onDestroy(){
        super.onDestroy();
        executor.shutdown();
    }

    private void visszaAFooldalra(){
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);
    }

    public ServiceEntryFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_entry, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        szerviz = view.findViewById(R.id.SzervizFajta);
        kilometer = view.findViewById(R.id.KmAllas);
        osszeg = view.findViewById(R.id.KoltsegOsszeg);
        mentes = view.findViewById(R.id.BtnMentes);
        vissza = view.findViewById(R.id.btnAutoValtas);

        autoNev = view.findViewById(R.id.AutoNev);
        SelectedCarViewModel carVm = new ViewModelProvider(requireActivity()).get(SelectedCarViewModel.class);
        carVm.getSelectedCar().observe(getViewLifecycleOwner(), car -> {
            kivalasztottAuto = car;
            if (car == null) {
                autoNev.setText("nincs kiválasztott autó");
            } else {
                autoNev.setText(car.getManufacturer() + " " + car.getModel()
                + " (" + car.getLicensePlate() + ") ");
            }
        });


        mentes.setOnClickListener(v -> {

            if (kivalasztottAuto == null) {

                Toast.makeText(requireContext(), "Előbb válassz autót a garázsban.", Toast.LENGTH_SHORT).show();
                return;
            }

            String SzervizFajta = szerviz.getText().toString().trim();
            String KmAllas = kilometer.getText().toString().trim();
            String KoltsegOsszeg = osszeg.getText().toString().trim();


            if (SzervizFajta.isEmpty()) {
                szerviz.setError("Add meg a Szerviz fajtáját");
                return;
            }
            if (KmAllas.isEmpty()) {
                kilometer.setError("Add meg a kilométert");
                return;

            }
            long km;
            try {
                km = Long.parseLong(KmAllas);
            }   catch (NumberFormatException e){
                kilometer.setError("Túl nagy szám");
                return;
            }
            long koltseg;
            if(KoltsegOsszeg.isEmpty()){
                koltseg = 0;
            }else{
                try {
                    koltseg = Long.parseLong(KoltsegOsszeg);

                } catch (NumberFormatException e){
                    osszeg.setError("A költség túl nagy");
                    return;
                }
            }

            String datum = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(new Date());

            ServiceEntry bejegyzes = new ServiceEntry(kivalasztottAuto.getId(), SzervizFajta, "", km, datum, koltseg);

            AppDatabase db = DatabaseProvider.getDatabase(requireContext());

            executor.execute(() -> {
                db.serviceEntryDao().insert(bejegyzes);

                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(activity, "Bejegyzés mentve", Toast.LENGTH_SHORT).show();
                    visszaAFooldalra();
                });
            });
        });
        vissza.setOnClickListener(v -> {

            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            nav.setSelectedItemId(R.id.nav_home);
        });


    }



}