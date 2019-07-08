package com.rokid.rokidfacesample.sdk.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {FaceMapping.class}, version = 1, exportSchema = false)
public abstract class FaceMappingDatabase extends RoomDatabase {
    private static FaceMappingDatabase fdb;

    public static FaceMappingDatabase create(final Context context, final String dbname) {
        if (null == fdb) {
            fdb = Room.databaseBuilder(context, FaceMappingDatabase.class, dbname)
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build();
        }

        return fdb;
    }

    public abstract FaceMappingDao faceMappingDao();


}
