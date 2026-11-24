package hu.nje.digitservice.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseProvider {

    private static AppDatabase database;

    public static synchronized AppDatabase getDatabase(Context context) {

        if (database == null) {
            database = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "digitservice_database"
            ).build();
        }

        return database;
    }
}