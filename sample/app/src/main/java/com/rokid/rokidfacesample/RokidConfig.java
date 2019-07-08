package com.rokid.rokidfacesample;

import com.rokid.facelib.face.FaceDbHelper;

public interface RokidConfig {
    interface Face {
        String FACE_ITEM = "face_item";
        String UPDATE_FACE = "update_face";
        String BATCH_ADD_FACE = "batch_add_face";

        String SYNC_DEVICE_DB= "need_sync_device_db";
        String START_SYNC = "start_sync_db";

        String FACE_FEATURE_DB = "feature.db";
        String FACE_SEARCH_ENGINE = "SearchEngine.bin";
        String FACE_USR_DB = "user.db";
        String FACE_MAPPING_DB = "facemapping.db";
        String ZIP_FILE_NAME = "database";
        String ZIP_FILE_PATH = FaceDbHelper.PATH_OUTPUT + ZIP_FILE_NAME;
        String BATCH_FACE_IMAGE_PATH = "/sdcard/faceid/";
    }
}
