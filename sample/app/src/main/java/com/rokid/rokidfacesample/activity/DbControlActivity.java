package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.rokidfacesample.R;
import com.rokid.rokidfacesample.userdb.UserDatabase;
import com.rokid.rokidfacesample.userdb.UserInfo;
import com.rokid.rokidfacesample.userdb.UserInfoDao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.rokid.facelib.face.FaceDbHelper.PATH_OUTPUT;

public class DbControlActivity extends Activity {
    private static final String TAG = "DbControlActivity";
    private Button db_add, db_remove, db_save, db_clear, db_create,db_query,db_export;
    private Handler mH;
    private HandlerThread mT;
    private FaceDbHelper faceDbHelper;
    private String uuid;
    private Button db_update;
    private AlertDialog dialog;
    private UserDatabase userDatabase;
    private UserInfoDao userDao;
    private ProgressBar progressBar;
    private static final String USER_DB_NAME = "user.db";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_control);
        mT = new HandlerThread("dbThread");
        mT.start();
        mH = new Handler(mT.getLooper());
        initView();
        setListener();
        initUserDb();
    }

    /**
     * 初始化人脸信息数据库
     */
    private void initUserDb() {
        userDatabase = UserDatabase.create(this, USER_DB_NAME);
        userDao = userDatabase.getUserInfoDao();
    }

    private void initView() {
        db_add = findViewById(R.id.db_add);
        db_remove = findViewById(R.id.db_remove);
        db_query = findViewById(R.id.db_query);
        db_save = findViewById(R.id.db_save);
        db_clear = findViewById(R.id.db_clear);
        db_create = findViewById(R.id.db_create);
        db_update = findViewById(R.id.db_update);
        db_export = findViewById(R.id.db_export);
        progressBar = findViewById(R.id.progressBar);
        File dir = new File("/sdcard/input/");
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(dir.list()!=null) {
            progressBar.setMax(dir.list().length);
        }else{
            Toast.makeText(this,"请在/sdcard/input文件夹下放入待识别图片",Toast.LENGTH_LONG).show();
        }
    }
    int i=0;
    private void setListener() {
        db_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = SystemClock.elapsedRealtime();
                        faceDbHelper = new FaceDbHelper(getApplicationContext());
                        //清空之前的数据库
                        faceDbHelper.clearDb();
                        //创建数据库
                        faceDbHelper.createDb();
                        Log.i(TAG,"cost Time:"+(SystemClock.elapsedRealtime()-currentTime));
                        Toast.makeText(DbControlActivity.this, "dbCreate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        db_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DbControlActivity.this,"正在添加人脸数据库",Toast.LENGTH_LONG).show();
                new Thread(){
                    @Override
                    public void run() {

                        File dir = new File("/sdcard/input/");
                        for(File file :dir.listFiles()){
                            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                            String name = file.getName().split("\\.")[0];
                            UserInfo info = new UserInfo(name,"11111111");
                            if(bm==null){
                                continue;
                            }
                            //添加人脸数据，返回的featId是图片人脸特征值唯一识别号
                            String featId = faceDbHelper.add(bm);
                            if(featId!=null){
                                //将该featId所对应的用户信息，添加到人脸信息数据库
                                Log.i(TAG,"add "+name+(i)+" success");
                                info.uuid = featId;
                                userDao.addUserInfo(info);
                                uuid = info.uuid;
                            }else{
                                Log.i(TAG,"add "+name+(i)+" fail");
                            }
                            mH.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(i);
                                    i++;
                                    if(i == progressBar.getMax()-1){
                                        Toast.makeText(DbControlActivity.this,"人脸数据库添加完成",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }

                    }
                }.start();
            }
        });

        db_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在人脸信息库中查询人脸信息
                String name = userDao.getUserInfo(uuid).name;
                Log.i(TAG,"name:"+name);
            }
        });

        db_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在人脸信息库中更新人脸信息
                UserInfo info = new UserInfo("anwei", "3522031989");
                info.uuid = uuid;
                userDao.updateUserInfo(info);
            }
        });
        db_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //在人脸信息库中删除人脸信息
                UserInfo userInfo = userDao.getUserInfo(uuid);
                userDao.removeUserInfo(userInfo);
                faceDbHelper.remove(uuid);
            }
        });
        db_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存添加的人脸特征，生成的文件为"SearchEngine.bin"，保存在"/sdcard/facesdk"目录下
                faceDbHelper.save();
                Toast.makeText(DbControlActivity.this,"已保存",Toast.LENGTH_SHORT).show();
            }
        });
        db_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除人脸特征数据库和"SearchEngine.bin"文件
                faceDbHelper.clearDb();
            }
        });
        db_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将feat.db数据库导出到/sdcard/facesdk/文件夹下
                faceDbHelper.exportFeatDb();
                File featDbFile = new File(getDatabasePath(USER_DB_NAME).getAbsolutePath());
                try {
                    File file = new File(PATH_OUTPUT);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    FaceFileUtils.copyFileByChannel(featDbFile, new File(PATH_OUTPUT +File.separator+ USER_DB_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
            createDialog(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createDialog(final Bitmap bm){
        View view = View.inflate(this,R.layout.dialog_db,null);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        EditText et_name = view.findViewById(R.id.et_name);
        ImageView iv = view.findViewById(R.id.iv_bitmap);
        iv.setImageBitmap(bm);
        dialog = new AlertDialog.Builder(this).setView(view).show();
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                UserInfo info = new UserInfo(name, "3522031989");
                uuid = faceDbHelper.add(bm);
                info.uuid = uuid;
                Toast.makeText(DbControlActivity.this,name+"已添加",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mT.quit();
        super.onDestroy();
    }
}
