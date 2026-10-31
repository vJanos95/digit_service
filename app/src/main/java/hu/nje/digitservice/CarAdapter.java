package hu.nje.digitservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.nje.digitservice.database.Car;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    private final List<CarItem> items = new ArrayList<>();
    private final OnCarClickListener listener;
    private long selectedCarId = -1;

    public CarAdapter(OnCarClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CarItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setSelectedCarId(long carId) {
        selectedCarId = carId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarItem item = items.get(position);
        Car car = item.car;

        holder.name.setText(car.getManufacturer() + " " + car.getModel());
        holder.plate.setText(car.getLicensePlate());

        if (item.hasService) {
            holder.km.setText(String.format(Locale.getDefault(), "%,d km", item.maxKm));
            holder.lastService.setText(item.lastServiceDate);
        } else {
            holder.km.setText("Még nincs szerviz");
            holder.lastService.setText("");
        }

        // a kiválasztott csempe kap egy színes keretet
        boolean selected = car.getId() == selectedCarId;
        float density = holder.card.getResources().getDisplayMetrics().density;
        holder.card.setStrokeWidth(selected ? (int) (3 * density) : 0);

        holder.card.setOnClickListener(v -> listener.onCarClick(car));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView plate;
        final TextView km;
        final TextView lastService;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.car_name);
            plate = itemView.findViewById(R.id.car_plate);
            km = itemView.findViewById(R.id.car_km);
            lastService = itemView.findViewById(R.id.car_last_service);
        }
    }
}