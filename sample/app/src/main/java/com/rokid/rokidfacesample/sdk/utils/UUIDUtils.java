package com.rokid.rokidfacesample.sdk.utils;

import java.util.UUID;

/**
 * Created by wangshuwen on 2017/6/19.
 */

public class UUIDUtils {

    public static String generateUUID(){
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();
        return uuidStr.replaceAll("-","");
    }
}
