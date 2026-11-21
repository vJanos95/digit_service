
package hu.nje.digitservice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.DatabaseProvider;
import hu.nje.digitservice.database.ServiceEntry;
import hu.nje.digitservice.database.ServiceImage;

public class ServiceEntryFragment extends Fragment {

    private EditText szerviz;
    private EditText kilometer;
    private EditText osszeg;
    private Button mentes;
    private Button vissza;
    private TextView autoNev;
    private TextView tvAttachedCount;
    private LinearLayout attachedImagesContainer;

    private android.widget.FrameLayout attachedCard;

    private hu.nje.digitservice.database.Car kivalasztottAuto;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    // A még el nem mentett képek URI-jai.
    private final List<Uri> selectedImages = new ArrayList<>();

    // A kamerával készített, ideiglenes képfájlok nyilvántartása.
    private final Map<Uri, File> cameraTempFiles = new HashMap<>();

    private Uri pendingCameraUri;
    private File pendingCameraFile;

    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    public ServiceEntryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10),
                uris -> {
                    if (uris == null || uris.isEmpty()) {
                        return;
                    }

                    for (Uri uri : uris) {
                        if (!selectedImages.contains(uri)) {
                            selectedImages.add(uri);
                        }
                    }

                    refreshImagePreviews();
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && pendingCameraUri != null) {
                        selectedImages.add(pendingCameraUri);
                        cameraTempFiles.put(
                                pendingCameraUri,
                                pendingCameraFile
                        );
                    } else {
                        deleteFile(pendingCameraFile);
                    }

                    pendingCameraUri = null;
                    pendingCameraFile = null;

                    refreshImagePreviews();
                }
        );
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_service_entry,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        szerviz = view.findViewById(R.id.SzervizFajta);
        kilometer = view.findViewById(R.id.KmAllas);
        osszeg = view.findViewById(R.id.KoltsegOsszeg);
        mentes = view.findViewById(R.id.BtnMentes);
        vissza = view.findViewById(R.id.btnAutoValtas);

        autoNev = view.findViewById(R.id.AutoNev);

        attachedCard = view.findViewById(R.id.llAttached);
        tvAttachedCount = view.findViewById(R.id.tvAttachedCount);
        attachedImagesContainer =
                view.findViewById(R.id.attachedImagesContainer);

        hu.nje.digitservice.SelectedCarViewModel carVm =
                new ViewModelProvider(requireActivity())
                        .get(hu.nje.digitservice.SelectedCarViewModel.class);

        carVm.getSelectedCar().observe(
                getViewLifecycleOwner(),
                car -> {
                    kivalasztottAuto = car;

                    if (car == null) {
                        autoNev.setText("nincs kiválasztott autó");
                    } else {
                        autoNev.setText(
                                car.getManufacturer() + " "
                                        + car.getModel()
                                        + " (" + car.getLicensePlate() + ")"
                        );
                    }
                }
        );

        view.findViewById(R.id.SzamlaDoboz)
                .setOnClickListener(v -> showPhotoSourceDialog());

        mentes.setOnClickListener(v -> saveServiceEntry());

        vissza.setOnClickListener(v -> visszaAFooldalra());

        refreshImagePreviews();
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    private void showPhotoSourceDialog() {
        String[] options = {
                "Galéria megnyitása",
                "Fénykép készítése"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Fotó hozzáadása")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void openGallery() {
        galleryLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(
                                ActivityResultContracts
                                        .PickVisualMedia.ImageOnly.INSTANCE
                        )
                        .build()
        );
    }

    private void openCamera() {
        try {
            File cameraDirectory =
                    new File(requireContext().getCacheDir(), "camera");

            if (!cameraDirectory.exists()
                    && !cameraDirectory.mkdirs()) {
                throw new IllegalStateException(
                        "Nem sikerült létrehozni a kamera mappáját."
                );
            }

            File photoFile = new File(
                    cameraDirectory,
                    UUID.randomUUID() + ".jpg"
            );

            Uri photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );

            pendingCameraFile = photoFile;
            pendingCameraUri = photoUri;

            cameraLauncher.launch(photoUri);

        } catch (Exception e) {
            deleteFile(pendingCameraFile);
            pendingCameraFile = null;
            pendingCameraUri = null;

            Toast.makeText(
                    requireContext(),
                    "Nem sikerült elindítani a kamerát.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    private void refreshImagePreviews() {
        attachedImagesContainer.removeAllViews();

        if (selectedImages.isEmpty()) {
            attachedCard.setVisibility(View.GONE);
            return;
        }

        attachedCard.setVisibility(View.VISIBLE);
        tvAttachedCount.setText(
                "Csatolt képek: " + selectedImages.size()
        );

        for (Uri uri : selectedImages) {
            FrameLayout imageFrame = new FrameLayout(requireContext());

            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(120),
                            dpToPx(120)
                    );

            frameParams.setMargins(0, 0, dpToPx(12), 0);
            imageFrame.setLayoutParams(frameParams);

            ImageView imageView = new ImageView(requireContext());
            imageView.setLayoutParams(
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(uri);
            imageView.setContentDescription("Csatolt fénykép");

            ImageButton removeButton = new ImageButton(requireContext());

            FrameLayout.LayoutParams removeParams =
                    new FrameLayout.LayoutParams(
                            dpToPx(32),
                            dpToPx(32),
                            android.view.Gravity.END | android.view.Gravity.TOP
                    );

            removeButton.setLayoutParams(removeParams);
            removeButton.setImageResource(
                    android.R.drawable.ic_menu_close_clear_cancel
            );
            removeButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.WHITE
                    )
            );
            removeButton.setPadding(
                    dpToPx(6),
                    dpToPx(6),
                    dpToPx(6),
                    dpToPx(6)
            );
            removeButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            removeButton.setContentDescription("Kép eltávolítása");

            removeButton.setOnClickListener(v -> {
                selectedImages.remove(uri);

                File tempFile = cameraTempFiles.remove(uri);
                deleteFile(tempFile);

                refreshImagePreviews();
            });

            imageFrame.addView(imageView);
            imageFrame.addView(removeButton);

            attachedImagesContainer.addView(imageFrame);
        }
    }

    private void saveServiceEntry() {
        if (kivalasztottAuto == null) {
            Toast.makeText(
                    requireContext(),
                    "Előbb válassz autót a garázsban.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String szervizFajta = szerviz.getText().toString().trim();
        String kmText = kilometer.getText().toString().trim();
        String koltsegText = osszeg.getText().toString().trim();

        if (szervizFajta.isEmpty()) {
            szerviz.setError("Add meg a szerviz fajtáját");
            return;
        }

        if (kmText.isEmpty()) {
            kilometer.setError("Add meg a kilométert");
            return;
        }

        long km;

        try {
            km = Long.parseLong(kmText);
        } catch (NumberFormatException e) {
            kilometer.setError("Túl nagy szám");
            return;
        }

        long koltseg = 0;

        if (!koltsegText.isEmpty()) {
            try {
                koltseg = Long.parseLong(koltsegText);
            } catch (NumberFormatException e) {
                osszeg.setError("A költség túl nagy");
                return;
            }
        }

        String datum = new SimpleDateFormat(
                "yyyy.MM.dd.",
                Locale.getDefault()
        ).format(new Date());

        ServiceEntry bejegyzes = new ServiceEntry(
                kivalasztottAuto.getId(),
                szervizFajta,
                "",
                km,
                datum,
                koltseg
        );

        Context appContext =
                requireContext().getApplicationContext();

        AppDatabase db = DatabaseProvider.getDatabase(appContext);

        List<Uri> imagesToSave =
                new ArrayList<>(selectedImages);

        Map<Uri, File> tempFilesToSave =
                new HashMap<>(cameraTempFiles);

        mentes.setEnabled(false);

        executor.execute(() -> {
            List<File> copiedFiles = new ArrayList<>();
            List<ServiceImage> insertedImages = new ArrayList<>();

            long serviceEntryId = -1;

            try {
                // Először a szervizbejegyzést mentjük el.
                serviceEntryId = db.serviceEntryDao().insert(bejegyzes);

                // Ezután a képeket és a hozzájuk tartozó DB rekordokat.
                for (Uri uri : imagesToSave) {
                    File savedFile = copyImageToInternalStorage(
                            appContext,
                            uri
                    );

                    copiedFiles.add(savedFile);

                    ServiceImage serviceImage = new ServiceImage(
                            serviceEntryId,
                            savedFile.getName()
                    );

                    long imageId = db.serviceImageDao().insert(serviceImage);
                    serviceImage.setId(imageId);

                    insertedImages.add(serviceImage);                }

                // Sikeres mentés után töröljük a kamerás ideiglenes fájlokat.
                for (File tempFile : tempFilesToSave.values()) {
                    deleteFile(tempFile);
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Bejegyzés és fotók mentve",
                                Toast.LENGTH_SHORT
                        ).show();

                        visszaAFooldalra();
                    });
                }

            } catch (Exception e) {
                // Hiba esetén ne maradjanak félbemaradt képfájlok.
                for (File file : copiedFiles) {
                    deleteFile(file);
                }

                for (ServiceImage image : insertedImages) {
                    db.serviceImageDao().delete(image);
                }

                if (serviceEntryId != -1) {
                    ServiceEntry savedEntry =
                            db.serviceEntryDao()
                                    .getServiceEntryById(serviceEntryId);

                    if (savedEntry != null) {
                        db.serviceEntryDao().delete(savedEntry);
                    }
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        mentes.setEnabled(true);

                        Toast.makeText(
                                requireContext(),
                                "Hiba történt a mentés során.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            }
        });
    }

    private File copyImageToInternalStorage(
            Context context,
            Uri sourceUri
    ) throws Exception {

        File imageDirectory = new File(
                context.getFilesDir(),
                "service_images"
        );

        if (!imageDirectory.exists()
                && !imageDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Nem sikerült létrehozni a képek mappáját."
            );
        }

        String extension = ".jpg";

        String mimeType =
                context.getContentResolver().getType(sourceUri);

        if (mimeType != null) {
            String detectedExtension =
                    android.webkit.MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(mimeType);

            if (detectedExtension != null) {
                extension = "." + detectedExtension;
            }
        }

        File destinationFile = new File(
                imageDirectory,
                UUID.randomUUID() + extension
        );

        try (
                InputStream inputStream =
                        context.getContentResolver()
                                .openInputStream(sourceUri);

                FileOutputStream outputStream =
                        new FileOutputStream(destinationFile)
        ) {
            if (inputStream == null) {
                throw new IllegalStateException(
                        "A kiválasztott kép nem olvasható."
                );
            }

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
        }

        return destinationFile;
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void visszaAFooldalra() {
        BottomNavigationView nav =
                requireActivity().findViewById(R.id.bottom_nav);

        nav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
