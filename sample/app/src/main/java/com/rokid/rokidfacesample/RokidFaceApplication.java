package com.rokid.rokidfacesample;

import android.app.Application;

import com.rokid.facelib.citrusfacesdk.CitrusFaceEngine;


public class RokidFaceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CitrusFaceEngine.Init(this);
    }
}
