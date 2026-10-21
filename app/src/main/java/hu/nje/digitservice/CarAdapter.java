package hu.nje.digitservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hu.nje.digitservice.database.Car;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Car car);
    }
    public interface OnCarClickListener {
        void onCarClick(Car car);
    }
    private final List<Car> cars = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;
    private final OnCarClickListener carClickListener;

    public CarAdapter(OnDeleteClickListener deleteClickListener, OnCarClickListener carClickListener) {
        this.deleteClickListener = deleteClickListener;
        this.carClickListener = carClickListener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);

        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CarViewHolder holder, int position) {

        Car car = cars.get(position);

        holder.name.setText(car.getManufacturer() + " " + car.getModel());
        holder.details.setText(
                "Évjárat: " + car.getProductionYear()
                        + " | Rendszám: " + car.getLicensePlate()
        );

        holder.deleteButton.setOnClickListener(v ->
                deleteClickListener.onDeleteClick(car)
        );
        holder.itemView.setOnClickListener(
                v -> carClickListener.onCarClick(car)
        );
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public void setCars(List<Car> newCars) {
        cars.clear();
        cars.addAll(newCars);
        notifyDataSetChanged();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView details;
        Button deleteButton;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.car_name);
            details = itemView.findViewById(R.id.car_details);
            deleteButton = itemView.findViewById(R.id.car_delete_button);
        }
    }
}
