package hu.nje.digitservice.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


@Entity(tableName = "service_entries",
        foreignKeys = @ForeignKey(
                entity = Car.class,
                parentColumns = "id",
                childColumns = "carId",
                onDelete = ForeignKey.CASCADE
        ))
public class ServiceEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long carId;
    private String entryName;
    private String description;
    private long mileage;
    private String serviceDate;
    private long cost;

    public ServiceEntry(long carId,
                        String entryName,
                        String description,
                        long mileage,
                        String serviceDate,
                        long cost) {
        this.carId = carId;
        this.entryName = entryName;
        this.description = description;
        this.mileage = mileage;
        this.serviceDate = serviceDate;
        this.cost = cost;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getEntryName() {
        return entryName;
    }
    public String getDescription() {
        return description;
    }
    public long getMileage() {
        return mileage;
    }
    public String getServiceDate() {
        return serviceDate;
    }
    public long getCost() {
        return cost;
    }
}
