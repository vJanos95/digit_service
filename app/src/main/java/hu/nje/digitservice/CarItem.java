package hu.nje.digitservice;

import hu.nje.digitservice.database.Car;

// Egy autó és a csempén megjelenő összesített adatai
public class CarItem {
    public final Car car;
    public final boolean hasService;
    public final long maxKm;
    public final String lastServiceDate;

    public CarItem(Car car, boolean hasService, long maxKm, String lastServiceDate) {
        this.car = car;
        this.hasService = hasService;
        this.maxKm = maxKm;
        this.lastServiceDate = lastServiceDate;
    }
}