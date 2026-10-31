package hu.nje.digitservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.nje.digitservice.database.ServiceEntry;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private final List<ServiceEntry> entries = new ArrayList<>();

    public void setEntries(List<ServiceEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceEntry entry = entries.get(position);

        holder.name.setText(entry.getEntryName());
        holder.dateKm.setText(entry.getServiceDate() + " • " + entry.getMileage() + " km");
        holder.cost.setText(String.format(Locale.getDefault(), "%,d Ft", entry.getCost()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView dateKm;
        final TextView cost;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.service_name);
            dateKm = itemView.findViewById(R.id.service_date_km);
            cost = itemView.findViewById(R.id.service_cost);
        }
    }
}