package hu.nje.digitservice;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ServiceEntryFragment extends Fragment {
    private EditText szerviz;
    private EditText kilometer;
    private EditText osszeg;
    private Button mentes;
    private  Button vissza;


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
        mentes.setOnClickListener(v -> {
            String SzervizFajta = szerviz.getText().toString().trim();
            String KmAllas = kilometer.getText().toString().trim();
            String KoltsegOsszeg = osszeg.getText().toString().trim();


            if (SzervizFajta.isEmpty()){
                szerviz.setError("Add meg a Szerviz fajtáját");
                return;
            }
            if (KmAllas.isEmpty()){
                kilometer.setError("Add meg a kilométert");
                return;
            }



            Toast.makeText(requireContext(),"Szerviz: " + SzervizFajta + ", " + KmAllas + " km, " + KoltsegOsszeg + " FT", Toast.LENGTH_SHORT).show();

        });
        vissza.setOnClickListener(v -> {

            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            nav.setSelectedItemId(R.id.nav_home);
        });




    }
}

