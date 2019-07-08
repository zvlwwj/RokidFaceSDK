package com.rokid.rokidfacesample.sdk.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * @author jian.yang
 */

@Dao
public interface FaceMappingDao {
    @Query("SELECT * FROM " + DbCst.TABLE_FACE_MAPPGING)
    List<FaceMapping> getAll();

    @Query("SELECT * FROM " + DbCst.TABLE_FACE_MAPPGING + " WHERE isCover = 1")
    List<FaceMapping> getAllUserWithCover();

    @Query("SELECT * FROM " + DbCst.TABLE_FACE_MAPPGING + " WHERE uid = :uid")
    List<FaceMapping> getAllUserWithUID(String uid);

    @Query("SELECT * FROM " + DbCst.TABLE_FACE_MAPPGING + " WHERE uuid LIKE :id LIMIT 1")
    FaceMapping getFaceMappingByUUID(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFaceMapping(FaceMapping mapping);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFaceMapping(FaceMapping mapping);

    @Delete
    void removeUserInfo(FaceMapping mapping);
}
