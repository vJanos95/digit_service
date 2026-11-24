package hu.nje.digitservice.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "service_images",
        foreignKeys = @ForeignKey(
                entity = ServiceEntry.class,
                parentColumns = "id",
                childColumns = "serviceEntryId",
                onDelete = ForeignKey.CASCADE))
public class ServiceImage {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long serviceEntryId;
    private String fileName;

    public ServiceImage(long serviceEntryId, String fileName) {
        this.serviceEntryId = serviceEntryId;
        this.fileName = fileName;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getServiceEntryId() {
        return serviceEntryId;
    }
    public String getFileName() {
        return fileName;
    }
}
