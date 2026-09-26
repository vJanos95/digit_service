package hu.nje.digitservice.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ServiceEntryDao {
    @Insert
    long insert(ServiceEntry serviceEntry);
    @Update
    void update(ServiceEntry serviceEntry);
    @Delete
    void delete(ServiceEntry serviceEntry);
    @Query("SELECT * FROM service_entries WHERE id = :id")
    ServiceEntry getServiceEntryById(long id);
    @Query("SELECT * FROM service_entries WHERE carId = :carId")
    List<ServiceEntry> getServiceEntriesByCarId(long carId);
}
