package hu.nje.digitservice;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hu.nje.digitservice.database.ServiceEntry;
import hu.nje.digitservice.database.ServiceImage;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private final List<ServiceEntry> entries = new ArrayList<>();

    private final Map<Long, List<ServiceImage>> imagesByService =
            new HashMap<>();

    public void setEntries(
            List<ServiceEntry> newEntries,
            Map<Long, List<ServiceImage>> newImages
    ) {
        entries.clear();
        entries.addAll(newEntries);

        imagesByService.clear();
        imagesByService.putAll(newImages);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);

        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ServiceViewHolder holder,
            int position
    ) {
        ServiceEntry entry = entries.get(position);

        holder.name.setText(entry.getEntryName());

        holder.dateKm.setText(
                entry.getServiceDate() + " • " + entry.getMileage() + " km"
        );

        holder.cost.setText(
                String.format(Locale.getDefault(), "%,d Ft", entry.getCost())
        );

        Context context = holder.itemView.getContext();

        for (int i = 0; i < holder.imagesContainer.getChildCount(); i++) {
            View child = holder.imagesContainer.getChildAt(i);

            if (child instanceof ImageView) {
                Glide.with(holder.itemView).clear((ImageView) child);
            }
        }

        holder.imagesContainer.removeAllViews();

        List<ServiceImage> images =
                imagesByService.get(entry.getId());

        if (images == null || images.isEmpty()) {
            holder.imagesScroll.setVisibility(View.GONE);
            return;
        }

        holder.imagesScroll.setVisibility(View.VISIBLE);

        for (ServiceImage serviceImage : images) {

            File imageFile = new File(
                    new File(context.getFilesDir(), "service_images"),
                    serviceImage.getFileName()
            );

            if (!imageFile.exists()) {
                continue;
            }

            ImageView imageView = new ImageView(context);

            int imageSize = dpToPx(context, 96);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            imageSize,
                            imageSize
                    );

            params.setMargins(0, 0, dpToPx(context, 8), 0);

            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setContentDescription("Szervizhez csatolt kép");

            Glide.with(holder.itemView)
                    .load(imageFile)
                    .override(imageSize, imageSize)
                    .centerCrop()
                    .into(imageView);

            imageView.setOnClickListener(v ->
                    showFullImage(context, imageFile)
            );

            holder.imagesContainer.addView(imageView);
        }

        if (holder.imagesContainer.getChildCount() == 0) {
            holder.imagesScroll.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private void showFullImage(Context context, File imageFile) {

        if (!imageFile.exists()) {
            return;
        }

        Dialog dialog = new Dialog(context);

        ImageView imageView = new ImageView(context);

        int maxWidth = context.getResources()
                .getDisplayMetrics().widthPixels;

        int maxHeight = (int) (
                context.getResources()
                        .getDisplayMetrics().heightPixels * 0.8f
        );

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(maxWidth);
        imageView.setMaxHeight(maxHeight);
        imageView.setBackgroundColor(android.graphics.Color.BLACK);

        dialog.setContentView(imageView);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(
                    android.R.color.black
            );

            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setGravity(Gravity.CENTER);
        }

        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(d ->
                Glide.with(context).clear(imageView)
        );

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        Glide.with(context)
                .load(imageFile)
                .override(maxWidth, maxHeight)
                .fitCenter()
                .into(imageView);
    }

    private int dpToPx(Context context, int dp) {
        return (int) (
                dp * context.getResources()
                        .getDisplayMetrics().density
        );
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView dateKm;
        final TextView cost;

        final HorizontalScrollView imagesScroll;
        final LinearLayout imagesContainer;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.service_name);
            dateKm = itemView.findViewById(R.id.service_date_km);
            cost = itemView.findViewById(R.id.service_cost);

            imagesScroll = itemView.findViewById(
                    R.id.service_images_scroll
            );

            imagesContainer = itemView.findViewById(
                    R.id.service_images_container
            );
        }
    }
}