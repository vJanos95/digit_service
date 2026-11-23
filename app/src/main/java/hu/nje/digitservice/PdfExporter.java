package hu.nje.digitservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import hu.nje.digitservice.database.AppDatabase;
import hu.nje.digitservice.database.Car;
import hu.nje.digitservice.database.ServiceEntry;
import hu.nje.digitservice.database.ServiceImage;

public class PdfExporter {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;

    private static final int MARGIN = 40;

    public static File generate(
            Context context,
            AppDatabase db,
            Car car
    ) throws IOException {

        PdfDocument document = new PdfDocument();

        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.BLACK);
        paint.setTextSize(11);
        paint.setAntiAlias(true);

        Paint titlePaint = new Paint();
        titlePaint.setColor(android.graphics.Color.BLACK);
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);
        titlePaint.setAntiAlias(true);

        Paint boldPaint = new Paint();
        boldPaint.setColor(android.graphics.Color.BLACK);
        boldPaint.setTextSize(13);
        boldPaint.setFakeBoldText(true);
        boldPaint.setAntiAlias(true);

        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        pageNumber
                ).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 50;

        List<ServiceEntry> entries =
                db.serviceEntryDao()
                        .getServiceEntriesByCarId(car.getId());

        long totalCost = 0;

        for (ServiceEntry entry : entries) {
            totalCost += entry.getCost();
        }

        canvas.drawText(
                "DigitService",
                MARGIN,
                y,
                titlePaint
        );

        y += 30;

        canvas.drawText(
                "Jármű szerviztörténete",
                MARGIN,
                y,
                boldPaint
        );

        y += 30;

        canvas.drawText("Jármű adatai:", MARGIN, y, boldPaint);
        y += 22;

        canvas.drawText(
                "Gyártó: " + car.getManufacturer(),
                MARGIN,
                y,
                paint
        );
        y += 18;

        canvas.drawText(
                "Modell: " + car.getModel(),
                MARGIN,
                y,
                paint
        );
        y += 18;

        canvas.drawText(
                "Rendszám: " + car.getLicensePlate(),
                MARGIN,
                y,
                paint
        );
        y += 18;

        canvas.drawText(
                "Gyártási év: " + car.getProductionYear(),
                MARGIN,
                y,
                paint
        );
        y += 30;

        canvas.drawText(
                "Szervizösszesítés:",
                MARGIN,
                y,
                boldPaint
        );
        y += 20;

        canvas.drawText(
                "Bejegyzések száma: " + entries.size(),
                MARGIN,
                y,
                paint
        );
        y += 18;

        NumberFormat formatter =
                NumberFormat.getIntegerInstance(
                        new Locale("hu", "HU")
                );

        canvas.drawText(
                "Összes költség: "
                        + formatter.format(totalCost) + " Ft",
                MARGIN,
                y,
                paint
        );
        y += 30;

        canvas.drawText(
                "Szervizbejegyzések:",
                MARGIN,
                y,
                boldPaint
        );
        y += 25;

        File imagesDir = new File(
                context.getFilesDir(),
                "service_images"
        );

        for (ServiceEntry entry : entries) {

            if (y > 730) {

                canvas.drawText(
                        pageNumber + ". oldal",
                        PAGE_WIDTH - 100,
                        815,
                        paint
                );

                document.finishPage(page);

                pageNumber++;

                pageInfo =
                        new PdfDocument.PageInfo.Builder(
                                PAGE_WIDTH,
                                PAGE_HEIGHT,
                                pageNumber
                        ).create();

                page = document.startPage(pageInfo);
                canvas = page.getCanvas();

                y = 55;

                canvas.drawText(
                        "DigitService - Szerviztörténet",
                        MARGIN,
                        y,
                        boldPaint
                );

                y += 30;
            }

            canvas.drawText(
                    entry.getEntryName() == null
                            ? "Szervizbejegyzés"
                            : entry.getEntryName(),
                    MARGIN,
                    y,
                    boldPaint
            );

            y += 20;

            canvas.drawText(
                    "Dátum: " + entry.getServiceDate(),
                    MARGIN,
                    y,
                    paint
            );
            y += 17;

            canvas.drawText(
                    "Kilométeróra: "
                            + formatter.format(entry.getMileage())
                            + " km",
                    MARGIN,
                    y,
                    paint
            );
            y += 17;

            canvas.drawText(
                    "Költség: "
                            + formatter.format(entry.getCost())
                            + " Ft",
                    MARGIN,
                    y,
                    paint
            );
            y += 17;

            if (entry.getDescription() != null
                    && !entry.getDescription().isEmpty()) {

                canvas.drawText(
                        "Leírás: " + entry.getDescription(),
                        MARGIN,
                        y,
                        paint
                );

                y += 20;
            }

            List<ServiceImage> images =
                    db.serviceImageDao()
                            .getServiceImagesByServiceId(
                                    entry.getId()
                            );

            for (ServiceImage image : images) {

                File imageFile = new File(
                        imagesDir,
                        image.getFileName()
                );

                if (!imageFile.exists()) {
                    continue;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(
                        imageFile.getAbsolutePath()
                );

                if (bitmap == null) {
                    continue;
                }

                if (y > 620) {

                    document.finishPage(page);

                    pageNumber++;

                    pageInfo =
                            new PdfDocument.PageInfo.Builder(
                                    PAGE_WIDTH,
                                    PAGE_HEIGHT,
                                    pageNumber
                            ).create();

                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();

                    y = 55;

                    canvas.drawText(
                            "DigitService - Szerviztörténet",
                            MARGIN,
                            y,
                            boldPaint
                    );

                    y += 30;
                }

                float maxWidth = PAGE_WIDTH - 2 * MARGIN;
                float maxHeight = 180;

                float scale = Math.min(
                        maxWidth / bitmap.getWidth(),
                        maxHeight / bitmap.getHeight()
                );

                float width = bitmap.getWidth() * scale;
                float height = bitmap.getHeight() * scale;

                RectF destination = new RectF(
                        MARGIN,
                        y,
                        MARGIN + width,
                        y + height
                );

                canvas.drawBitmap(
                        bitmap,
                        null,
                        destination,
                        paint
                );

                y += (int) height + 15;

                bitmap.recycle();
            }

            y += 20;
        }

        document.finishPage(page);

        File exportDir = new File(
                context.getCacheDir(),
                "exports"
        );

        if (!exportDir.exists() && !exportDir.mkdirs()) {
            document.close();
            throw new IOException(
                    "Nem sikerült létrehozni az export mappát."
            );
        }

        File pdfFile = new File(
                exportDir,
                "szervizkonyv_"
                        + System.currentTimeMillis()
                        + ".pdf"
        );

        try (FileOutputStream output =
                     new FileOutputStream(pdfFile)) {

            document.writeTo(output);

        } finally {
            document.close();
        }

        return pdfFile;
    }
}
