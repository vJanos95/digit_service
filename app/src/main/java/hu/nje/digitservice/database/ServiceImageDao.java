package hu.nje.digitservice.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ServiceImageDao {
    @Insert
    long insert(ServiceImage serviceImage);
    @Update
    void update(ServiceImage serviceImage);
    @Delete
    void delete(ServiceImage serviceImage);
    @Query("SELECT * FROM service_images WHERE id = :id")
    ServiceImage getServiceImageById(long id);
    @Query("SELECT * FROM service_images WHERE serviceEntryId = :serviceEntryId")
    List<ServiceImage> getServiceImagesByServiceId(long serviceEntryId);
}
