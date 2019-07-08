package com.rokid.rokidfacesample.sdk.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author jian.yang
 */
@Entity(tableName = DbCst.TABLE_FACE_MAPPGING, indices = {@Index(value = {"uuid"}, unique = true)})
public class FaceMapping implements Serializable {
    private static final long serialVersionUID = -2423337939695564614L;

    @PrimaryKey(autoGenerate = true)
    public int _id;

    public String uid;

    public boolean isCover;

    /**
     * 特征库UUID 32 bit
     */
    public String uuid;

    public byte[] faceImg;

    @Override
    public String toString() {
        return "FaceMapping{" +
                "uid='" + uid + '\'' +
                ", isCover=" + isCover +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
