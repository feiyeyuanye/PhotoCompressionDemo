package com.example.compressapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView ivImage;
    private ImageView ivCommpressImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 11: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    compress();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void initData() {
        // 一般来讲，都是从sd卡中读取图片
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.e("TAG",permissionCheck+"---");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                compress();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        11);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        ivImage.setImageBitmap(bitmap);
//        也是用来设置图片。
//        但封装了bitmap的读入和解析的过程，并且过程是在UI线程完成的，对于性能是有所影响的。
//        ivImage.setImageResource(R.mipmap.ic_launcher_round);

        // 位图所占用的内存字节数（147456000） = 宽（7680） * 高（4800） * 占用字节大小（ARGB_8888模式为 4 byte）
        // 也就是说，2560 * 1600 的图片加载到内存中，需要近 147.456M 。

        // 可以看到，源图片的尺寸数据，和decode到内存中的Bitmap的横纵像素数量实际上缩小了 3 倍。
        // 通过源码，bitmap最终通过canvas绘制出来，而canvas在绘制之前，有一个scale的操作，scale的值由：
        // scale = (float) targetDensity / density;这一行代码决定。
        // 即缩放的倍率和targetDensity和density相关，而这两个参数都是从传入的options中获取到的。

//        Log.e("TAG", bitmap.getWidth() + "--" + bitmap.getHeight());
//        Log.e("TAG", bitmap.getByteCount() + "");
//        Log.e("TAG",bitmap.getConfig()+"");    // ARGB_8888，
//        Log.e("TAG",bitmap.getDensity()+"");   // 480
//        Log.e("TAG",getResources().getDisplayMetrics().density+""); // 3.0
//        float xdpi = getResources().getDisplayMetrics().xdpi;
//        float ydpi = getResources().getDisplayMetrics().ydpi;
//        Log.e("TAG",xdpi+"---"+ydpi);   // 422.03---424.069


        // 华为荣耀7
        // 源图片的尺寸为 2560 * 1600
        // drawable目录下：// 7680 -- 4800，147456000，缩放倍率：3
        // drawable-ldpi目录下：OOM
        // drawable-mdpi目录下：// 7680 -- 4800，147456000， 缩放倍率：3  density：1
        // drawable-hdpi目录下：// 5120 -- 3200，65536000，  缩放倍率：2  density：1.5
        // drawable-xhdpi目录下：// 3840 -- 2400，36864000， 缩放倍率：1.5  density：2
        // drawable-xxhdpi目录下：// 2560 -- 1600，16384000， 缩放倍率：1  density：3
        // drawable-xxxhdpi目录下：// 1920 -- 1200， 9216000， 缩放倍率：0.75  density：4

        // Bitmap内存占用（147456000） = 图片宽（2560） × 图片高（1600）× (设备密度（3）/资源目录密度（1）)^2 × 每个像素的字节大小（4）

    }

    private void compress(){
        // 这里为了测试，先在data下放置一张图片
        // 这里，将源图片和压缩后的图片都保存在了项目的data下
        Resources res = this.getResources();
        BitmapDrawable d = (BitmapDrawable) res.getDrawable(R.drawable.img);
        Bitmap img = d.getBitmap();

        String fn = "image_test.png";

//        this.getFilesDir(); 这个是得到当前app目录下的files目录路径
//        /data/user/0/com.example.compressapplication/files/image_test.png
        String path = this.getFilesDir() + File.separator + fn;
        // 放到指定目录下
        //  String path = Environment.getExternalStorageDirectory()+"/"+fn;
        Log.e("TAG",path);

        try{
            OutputStream os = new FileOutputStream(path);
            img.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }catch(Exception e){
            Log.e("TAG", "", e);
        }

        // 开始压缩图片
        File file = BitmapCompressUtil.compressBitmap(this,path, 500);
        if (file != null) {
            Log.e("TAG","压缩完成："+file.getPath());
            Bitmap bitmap1 = BitmapFactory.decodeFile(file.getPath(), null);
            ivCommpressImg.setImageBitmap(bitmap1);

//            Log.e("TAG",bitmap.getWidth()+"------"+bitmap.getHeight());
//            Log.e("TAG",bitmap1.getWidth()+"------"+bitmap1.getHeight());
//            Log.e("TAG",bitmap.getByteCount()+"----"+bitmap1.getByteCount());

//            如果是放在指定位置，可以删除
//            BitmapCompressUtil.deleteFiles(new File(Environment.getExternalStorageDirectory() + BitmapCompressUtil.PATH));
        } else {
            Log.e("TAG","file文件为null");
        }

    }

    private void initView() {
        ivImage = findViewById(R.id.iv_img);
        ivCommpressImg = findViewById(R.id.iv_commpress_img);
    }
}