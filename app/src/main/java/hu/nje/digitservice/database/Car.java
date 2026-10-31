package hu.nje.digitservice.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "cars")
public class Car {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String manufacturer;
    private String model;
    private String licensePlate;
    private int productionYear;

    public Car(String manufacturer, String model, String licensePlate, int productionYear) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.licensePlate = licensePlate;
        this.productionYear = productionYear;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public String getModel() {
        return model;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
    public int getProductionYear() {
        return productionYear;
    }
}
