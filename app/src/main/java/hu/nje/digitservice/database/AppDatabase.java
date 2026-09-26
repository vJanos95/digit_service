package hu.nje.digitservice.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                Car.class,
                ServiceEntry.class,
                ServiceImage.class
        },
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    //public abstract CarDao carDao();
    //public abstract ServiceEntryDao serviceEntryDao();
    //public abstract ServiceImageDao serviceImageDao();
}
