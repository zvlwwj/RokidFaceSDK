package com.rokid.rokidfacesample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.facelib.db.UserInfo;
import com.rokid.facelib.face.FaceDbHelper;
import com.rokid.facelib.model.FaceDO;
import com.rokid.facelib.model.UserFace;
import com.rokid.facelib.utils.FaceFileUtils;
import com.rokid.facelib.utils.FaceRectUtils;
import com.rokid.rokidfacesample.R;
import com.rokid.rokidfacesample.RokidConfig;
import com.rokid.rokidfacesample.sdk.db.FaceMapping;
import com.rokid.rokidfacesample.sdk.db.FaceMappingDatabase;
import com.rokid.rokidfacesample.sdk.utils.FileUtils;
import com.rokid.rokidfacesample.sdk.utils.ImageUtils;
import com.rokid.rokidfacesample.sdk.utils.UUIDUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.rokid.facelib.face.FaceDbHelper.PATH_OUTPUT;


// 注意，这个版本人脸数要小于1W
public class DbControlActivity extends Activity {
    private static final String TAG = "DbControlActivity";
    private Button db_add, db_remove, db_save, db_clear, db_create,db_query;
    private Handler mH;
    private HandlerThread mT;
    private FaceDbHelper dbCreator;
    private String uuid;
    private Button db_update;
    private Bitmap bm;
    private AlertDialog dialog;
    private FaceMappingDatabase fmd;

    private TextView mResultTv;

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

        mResultTv = findViewById(R.id.db_result_text);
    }

    public static Bitmap getRotateBitmap(String imagePath) {
        Bitmap photo = BitmapFactory.decodeFile(imagePath);
        int rotation = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rotation == 0) {
            return photo;
        } else {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        }
    }


    private void setListener() {
        db_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        removeOldDB();

                        long currentTime = SystemClock.elapsedRealtime();
                        dbCreator = new FaceDbHelper(getApplicationContext());
                        dbCreator.setModel(FaceDbHelper.MODEL_DB);
                        dbCreator.clearDb();
                        dbCreator.configDb("/sdcard/facesdk");

                        fmd = FaceMappingDatabase.create(getApplicationContext(), "facemapping.db");

                        Log.i(TAG,"cost Time:"+(SystemClock.elapsedRealtime()-currentTime));
                        Toast.makeText(DbControlActivity.this, "dbCreate", Toast.LENGTH_SHORT).show();

                        Log.d("zhf_face","创建数据库完成，请开始执行添加....");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultTv.setText("创建数据库完成，请开始执行添加....");
                            }
                        });
                    }
                });
            }
        });
        db_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_create.setEnabled(false);
                db_add.setEnabled(false);

                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        int success = 0;
                        int error = 0;
                        File dir = new File("/sdcard/faceid"); //faceid
                        int count = 0;
                        final int file_count = dir.listFiles().length;
                        for(File file : dir.listFiles()){
                            Bitmap photo = getRotateBitmap(file.getAbsolutePath());
                            if(photo==null || photo.isRecycled()){
                                continue;
                            }

                            String fileName = FileUtils.getFileNameNoEx(file.getName());
                            UserInfo info = new UserInfo();
                            if(fileName.contains("#")){
                                String[] infos = fileName.split("#");
                                info.name = infos[0];
                                info.checkcode = infos[1];
                            }
                            else {
                                info.name = fileName;
                                info.checkcode = " ";
                            }
                            UserFace userFace = dbCreator.addRetrunUserFace(photo, info);

                            if (userFace == null || userFace.faceDO == null || userFace.userInfo == null) {
                                error++;
                                Log.e("zhf_face","RokidFace ##### 批量添加: "+fileName+" 出错啦, 检测不到人脸");
                                continue;
                            }

                            FaceDO faceDO = userFace.faceDO;
                            Rect srcRect = faceDO.toRect(photo.getWidth(), photo.getHeight());
                            Log.d("zhf_face","RokidFace ##### 批量 fileName= "+file.getAbsolutePath()
                                    +", count="+(count++)+", faceDO srcRect="+srcRect);

                            Rect dstRect = FaceRectUtils.toRect(
                                    srcRect, 1, photo.getWidth(), photo.getHeight());
                            int left = dstRect.left;
                            int top = dstRect.top;
                            int right = dstRect.right;
                            int bottom = dstRect.bottom;
//
//                            int width = right - left, height = bottom - top;
//                            int centerX = left + width / 2, centerY = top + height / 2;
//                            int diameter = width > height ? width : height;
//                            left = centerX - diameter * 2 / 3;
//                            if (left < 0) left = 0;
//                            right = centerX + diameter * 2 / 3;
//                            if (right > photo.getWidth()) right = photo.getWidth();
//                            top = centerY - diameter;
//                            if (top < 0) top = 0;
//                            bottom = centerY + diameter;
//                            if (bottom > photo.getHeight()) bottom = photo.getHeight();

                            int width = right - left, height = bottom - top;
                            int centerX = left + width / 2, centerY = top + height / 2;
                            int diameter = width > height ? width : height;

                            //人脸框放到系数，尽可能保证抠出来的小图宽高相等
                            float scaleFactor = 1.2f;
                            float side = diameter * scaleFactor;
                            int radius = (int)(side / 2);

                            left = centerX - radius;
                            if (left < 0) left = 0;
                            right = centerX + radius;
                            if (right > photo.getWidth()) right = photo.getWidth();
                            top = centerY - radius;
                            if (top < 0) top = 0;
                            bottom = centerY + radius;
                            if (bottom > photo.getHeight()) bottom = photo.getHeight();

                            Bitmap cropBitmap = Bitmap.createBitmap(photo, left, top, right - left, bottom - top);
                            if(cropBitmap ==null){
                                error++;
                                Log.e("zhf_face","RokidFace ##### 裁剪: "+fileName+"出错啦");
                                continue;
                            }

                            FaceMapping mapping = new FaceMapping();
                            mapping.uid = UUIDUtils.generateUUID();
                            mapping.uuid = userFace.userInfo.uuid;
                            mapping.faceImg = ImageUtils.Bitmap2Bytes(cropBitmap);
                            mapping.isCover = true;
                            //Log.d("zhf_face","add"+(i++)+", getName()="+file.getName()+", faceImg.length="+mapping.faceImg.length);
                            fmd.faceMappingDao().addFaceMapping(mapping);

                            if (photo != null) {
                                photo.recycle();
                                photo = null;
                            }
                            if (cropBitmap != null) {
                                cropBitmap.recycle();
                                cropBitmap = null;
                            }
                            success++;

                            final int process = count;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultTv.setText("正在添加中....请稍后, ("+process+"/"+file_count+")");
                                }
                            });
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.d("zhf_face","开始拷贝");
                        dbCreator.save();
                        Log.d("zhf_face","开始压缩");
                        zipDatabase();

                        final int done_success = success;
                        final int done_error = error;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultTv.setText("添加完成，成功: "+done_success+", 失败: "+done_error);
                                db_create.setEnabled(true);
                                db_add.setEnabled(true);
                            }
                        });

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
                Toast.makeText(DbControlActivity.this,"已保存",Toast.LENGTH_SHORT).show();
            }
        });
        db_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbCreator.clearDb();
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
                if( dbCreator.add(bm, info)!=null){
                    uuid = dbCreator.add(bm, info).uuid;
                    Toast.makeText(DbControlActivity.this,name+"已添加",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(DbControlActivity.this,name+"没有检测到人脸",Toast.LENGTH_SHORT).show();
                }


                dialog.dismiss();
            }
        });
    }


    public void removeOldDB() {
        // 删除sdcard人脸特征库
        File faceSdk = new File(FaceDbHelper.PATH_OUTPUT);
        if (faceSdk != null && faceSdk.exists() && faceSdk.isDirectory()) {
            FileUtils.deleteDirection(faceSdk);
        }

        // 删除本地data/data下的数据库
        File userDb = this.getDatabasePath(RokidConfig.Face.FACE_USR_DB);
        if (userDb != null && userDb.exists()) {
            userDb.delete();
        }
        File featureDb = this.getDatabasePath(RokidConfig.Face.FACE_FEATURE_DB);
        if (featureDb != null && featureDb.exists()) {
            featureDb.delete();
        }
        File mappingDb = this.getDatabasePath(RokidConfig.Face.FACE_MAPPING_DB);
        if (mappingDb != null && mappingDb.exists()) {
            mappingDb.delete();
        }
        File searchEngine = this.getDatabasePath(RokidConfig.Face.FACE_SEARCH_ENGINE);
        if (searchEngine != null && searchEngine.exists()) {
            searchEngine.delete();
        }

        File userDbJournal = this.getDatabasePath(RokidConfig.Face.FACE_USR_DB+"-journal");
        if (userDbJournal != null && userDbJournal.exists()) {
            userDbJournal.delete();
        }
        File featureDbJournal = this.getDatabasePath(RokidConfig.Face.FACE_FEATURE_DB+"-journal");
        if (featureDbJournal != null && featureDbJournal.exists()) {
            featureDbJournal.delete();
        }
        File mappingDbJournal = this.getDatabasePath(RokidConfig.Face.FACE_MAPPING_DB+"-journal");
        if (mappingDbJournal != null && mappingDbJournal.exists()) {
            mappingDbJournal.delete();
        }
    }

    public boolean zipDatabase() {
        boolean result = true;

        File dbMappingFile = new File(getApplication().getDatabasePath(RokidConfig.Face.FACE_MAPPING_DB).getAbsolutePath());//new File("/data/data/com.rokid.test/databases/user.db");

        File userDbFile = new File(PATH_OUTPUT + RokidConfig.Face.FACE_USR_DB);
        File facingDbFile = new File(PATH_OUTPUT + RokidConfig.Face.FACE_MAPPING_DB);

        try {
            if (facingDbFile.exists()) {
                facingDbFile.delete();
            }
            FaceFileUtils.copyFileByChannel(dbMappingFile, facingDbFile);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }


        List<File> srcFiles = Arrays.asList(userDbFile, facingDbFile,
                new File(PATH_OUTPUT + RokidConfig.Face.FACE_SEARCH_ENGINE),
                new File(PATH_OUTPUT + RokidConfig.Face.FACE_FEATURE_DB));

        File zipFile = new File(RokidConfig.Face.ZIP_FILE_PATH);

        if (zipFile.exists()) {
            zipFile.delete();
        }

        try {
            zipFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(zipFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = false;
        }

        ZipOutputStream zipOut = new ZipOutputStream(fout);
        for (File fileToZip : srcFiles) {
            if (!fileToZip.exists()) {
                continue;
            }

            FileInputStream fin = null;
            try {
                fin = new FileInputStream(fileToZip);
//                ZipEntry zipEntry = new ZipEntry("update_" + fileToZip.getName());
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                try {
                    zipOut.putNextEntry(zipEntry);
                } catch (IOException e) {
                    e.printStackTrace();
                    result = false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = false;
            }

            if (fin != null) {
                try {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fin.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    result = false;
                }
            }
        }
        try {
            zipOut.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }
}
