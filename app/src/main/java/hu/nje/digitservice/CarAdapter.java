package hu.nje.digitservice;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import hu.nje.digitservice.database.Car;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(Car car);
    }

    // a kiválasztott kártya színei (itt egy helyen átírhatók)
    private static final int SELECTED_BACKGROUND = Color.parseColor("#1B4FC4");
    private static final int SELECTED_TEXT = Color.WHITE;

    private final List<Car> cars = new ArrayList<>();
    private final OnCarClickListener listener;
    private long selectedCarId = -1;

    public CarAdapter(OnCarClickListener listener) {
        this.listener = listener;
    }

    public void setCars(List<Car> newCars) {
        cars.clear();
        cars.addAll(newCars);
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
        Car car = cars.get(position);

        holder.name.setText(car.getManufacturer() + " " + car.getModel());
        holder.plate.setText(car.getLicensePlate());
        holder.year.setText(car.getProductionYear() + "-es évjárat");

        boolean selected = car.getId() == selectedCarId;
        if (selected) {
            // kiválasztott kártya: kék kitöltés + fehér szöveg
            holder.card.setCardBackgroundColor(SELECTED_BACKGROUND);
            holder.name.setTextColor(SELECTED_TEXT);
            holder.plate.setTextColor(SELECTED_TEXT);
            holder.year.setTextColor(SELECTED_TEXT);
        } else {
            // a többi kártya visszakapja az eredeti színeit
            holder.card.setCardBackgroundColor(holder.defaultCardColor);
            holder.name.setTextColor(holder.defaultTextColors);
            holder.plate.setTextColor(holder.defaultTextColors);
            holder.year.setTextColor(holder.defaultTextColors);
        }

        holder.card.setOnClickListener(v -> listener.onCarClick(car));
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView plate;
        final TextView year;

        // az eredeti színek, hogy vissza tudjuk állítani őket
        final ColorStateList defaultCardColor;
        final ColorStateList defaultTextColors;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.car_name);
            plate = itemView.findViewById(R.id.car_plate);
            year = itemView.findViewById(R.id.car_year);

            defaultCardColor = card.getCardBackgroundColor();
            defaultTextColors = name.getTextColors();
        }
    }
}