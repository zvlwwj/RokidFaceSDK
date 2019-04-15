package com.rokid.rokidfacesample.activity;

import android.app.Activity;
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
import android.widget.Toast;

import com.rokid.facelib.db.UserInfo;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.rokidfacesample.R;

public class DbControlActivity extends Activity {
    private static final String TAG = "DbControlActivity";
    private Button db_add, db_remove, db_save, db_clear, db_create,db_query;
    private Handler mH;
    private HandlerThread mT;
    private FaceDbHelper dbCreator;
    private String uuid;
    private Button db_update;
    private Bitmap bm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_control);
        mT = new HandlerThread("dbThread");
        mT.start();
        mH = new Handler(mT.getLooper());
        initView();
        setListener();

    }

    private void initView() {
        db_add = findViewById(R.id.db_add);
        db_remove = findViewById(R.id.db_remove);
        db_query = findViewById(R.id.db_query);
        db_save = findViewById(R.id.db_save);
        db_clear = findViewById(R.id.db_clear);
        db_create = findViewById(R.id.db_create);
        db_update = findViewById(R.id.db_update);
    }

    private void setListener() {
        db_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = SystemClock.elapsedRealtime();
                        dbCreator = new FaceDbHelper(getApplicationContext());
                        dbCreator.setModel(FaceDbHelper.MODEL_DB);
                        dbCreator.clearDb();
                        dbCreator.configDb("user.db");
                        Log.i(TAG,"cost Time:"+(SystemClock.elapsedRealtime()-currentTime));
                        Toast.makeText(DbControlActivity.this, "dbCreate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        db_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
//                        int i=0;
//                        File dir = new File("/sdcard/input2");
//                        for(File file :dir.listFiles()){
//                            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
//                            UserInfo info = new UserInfo(file.getName(),"11111111");
//                            if(bm==null){
//                                continue;
//                            }
//                            dbCreator.add(bm, info);
//                            Log.i(TAG,"add"+(i++));
//                        }
                        UserInfo info = new UserInfo("安慰", "3522031989");
                        bm = BitmapFactory.decodeFile("sdcard/安慰.jpg");
                        uuid = dbCreator.add(bm, info).uuid;

//                        UserInfo info = new UserInfo("鲍国春", "3522031989");
//                        Bitmap bm = BitmapFactory.decodeFile("sdcard/鲍国春.png");
//                        uuid = dbCreator.add(bm, info).uuid;
                    }
                });
            }
        });

        db_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = dbCreator.query(uuid).name;
                Log.i(TAG,"name:"+name);
            }
        });

        db_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo info = new UserInfo("anwei", "3522031989");
                info.uuid = uuid;
                dbCreator.update(bm,info);
            }
        });
        db_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        dbCreator.remove(uuid);
                    }
                });
            }
        });
        db_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbCreator.save();
            }
        });
        db_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbCreator.clearDb();
            }
        });
    }
}
