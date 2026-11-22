package hu.nje.digitservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        List<CarItem> safeNewItems = newItems != null ? newItems : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return safeNewItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Car oldCar = items.get(oldItemPosition).car;
                Car newCar = safeNewItems.get(newItemPosition).car;
                if (oldCar == null || newCar == null) {
                    return oldCar == newCar;
                }
                return oldCar.getId() == newCar.getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                CarItem oldItem = items.get(oldItemPosition);
                CarItem newItem = safeNewItems.get(newItemPosition);

                boolean carEqual = areCarsEqual(oldItem.car, newItem.car);

                return carEqual
                        && oldItem.hasService == newItem.hasService
                        && oldItem.maxKm == newItem.maxKm
                        && Objects.equals(oldItem.lastServiceDate, newItem.lastServiceDate);
            }
        });

        items.clear();
        items.addAll(safeNewItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectedCarId(long carId) {
        if (this.selectedCarId == carId) {
            return;
        }

        long previousSelectedId = this.selectedCarId;
        this.selectedCarId = carId;

        int previousIndex = findIndexByCarId(previousSelectedId);
        if (previousIndex != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousIndex);
        }

        int newIndex = findIndexByCarId(carId);
        if (newIndex != RecyclerView.NO_POSITION && newIndex != previousIndex) {
            notifyItemChanged(newIndex);
        }
    }

    private int findIndexByCarId(long carId) {
        if (carId == -1) {
            return RecyclerView.NO_POSITION;
        }
        for (int i = 0; i < items.size(); i++) {
            Car car = items.get(i).car;
            if (car != null && car.getId() == carId) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
    private boolean areCarsEqual(Car oldCar, Car newCar) {
        return Objects.equals(
                oldCar != null ? oldCar.getManufacturer() : null,
                newCar != null ? newCar.getManufacturer() : null
        ) && Objects.equals(
                oldCar != null ? oldCar.getModel() : null,
                newCar != null ? newCar.getModel() : null
        ) && Objects.equals(
                oldCar != null ? oldCar.getLicensePlate() : null,
                newCar != null ? newCar.getLicensePlate() : null
        );
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

        holder.name.setText(holder.name.getContext().getString(
                R.string.car_manufacturer_model,
                car.getManufacturer(),
                car.getModel()
        ));
        holder.plate.setText(car.getLicensePlate());

        if (item.hasService) {
            holder.km.setText(String.format(Locale.getDefault(), "%,d km", item.maxKm));
            holder.lastService.setText(item.lastServiceDate);
        } else {
            holder.km.setText(holder.km.getContext().getString(R.string.no_service_yet));
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

    public static class CarViewHolder extends RecyclerView.ViewHolder {
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