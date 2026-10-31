package hu.nje.digitservice.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CarDao {
    @Insert
    long insert(Car car);
    @Update
    void update(Car car);
    @Delete
    void delete(Car car);
    @Query("SELECT * FROM cars")
    List<Car> getAll();
    @Query("SELECT * FROM cars WHERE id = :id")
    Car getCarById(long id);
}
