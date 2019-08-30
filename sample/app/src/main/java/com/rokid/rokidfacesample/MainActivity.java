package com.rokid.rokidfacesample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.KeyEvent;

import com.rokid.facelib.RokidFace;
import com.rokid.rokidfacesample.activity.CameraAutoRecogActivity;
import com.rokid.rokidfacesample.activity.DbControlActivity;
import com.rokid.rokidfacesample.activity.ImageActivity;

/**
 * 人脸识别Sample使用说明：
 * 一、生成数据库：
 *      1.准备待识别人脸图片，人脸图片请满足以下条件：
 *          1.1图片中人脸所占像素大于150*150
 *          1.2图片中人脸尽量保持正面
 *          1.3图片中人脸特征清晰
 *      2.将待识别的人脸图片重命名为"姓名.png"的格式
 *      3.将待识别图片放入"/sdcard/input"文件夹下，没有该文件夹请自行创建
 *      4.打开RokidFaceSample，选择"人脸数据库操作"
 *      5.选择"DB_CREATE"按钮
 *      6.选择"DB_ADD"按钮，等待进度条完成
 *      7.选择"DB_SAVE"按钮，生成数据库，生成的数据库在"/sdcard/facesdk/"目录下
 * 二、人脸识别：
 *      1.打开RokidFaceSample，选择"人脸相机自动识别"
 *      2.选择"打开人脸识别"按钮,即可在相机preview中显示识别出的人脸姓名
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Explode());
        setContentView(R.layout.activity_main);

        PermissionUtils.permissionCheck(this);
        //人脸识别SDK的初始化操作
        RokidFace.Init(this);

        findViewById(R.id.camera_auto_recog).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CameraAutoRecogActivity.class));
        });

        findViewById(R.id.img_face).setOnClickListener(view ->{
            startActivity(new Intent(MainActivity.this,ImageActivity.class));
        });

        findViewById(R.id.db_op).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, DbControlActivity.class));
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Process.killProcess(Process.myPid());
        }
        return super.onKeyUp(keyCode, event);
    }
}