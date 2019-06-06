package com.example.compressapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapCompressUtil {

    public static final String PATH = "/pic";

    /**
     * 根据分辨率压缩图片比例
     */
    public static Bitmap compressByResolution(String imgPath, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;
        int widthScale = width / w;
        int heightScale = height / h;

        int scale;
        // 保留压缩比例小的
        if (widthScale < heightScale) {
            scale = widthScale;
        } else {
            scale = heightScale;
        }

        if (scale < 1) {
            scale = 1;
        }
//        Log.e("TAG","图片分辨率压缩比例：" + scale);

        opts.inSampleSize = scale;

        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, opts);

        return bitmap;
    }
    /**
     * 根据分辨率压缩
     *
     * @param srcPath 图片路径
     * @param ImageSize 图片大小 单位kb
     * @return
     */
    public static File compressBitmap(Context mContext,String srcPath, int ImageSize) {
        int subtract;
        Log.e("TAG","图片处理开始..");
        // 分辨率压缩
        Bitmap bitmap = compressByResolution(srcPath, 1280, 720);
        if (bitmap == null) {
            Log.e("TAG","bitmap 为空");
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        // 取得图片旋转角度
        int angle = readPictureDegree(srcPath);
        // 修复图片被旋转的角度
        Bitmap bitmapBefore = rotaingImageView(angle, bitmap);
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        bitmapBefore.compress(Bitmap.CompressFormat.JPEG, options, baos);
        Log.e("TAG", "图片分辨率压缩后：" + baos.toByteArray().length / 1024 + "KB");
        // 循环判断如果压缩后图片是否大于ImageSize kb,大于继续压缩
        while (baos.toByteArray().length > ImageSize * 1280) {
            subtract = setSubstractSize(baos.toByteArray().length / 1280);
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            options -= subtract;
            // 这里压缩options%，把压缩后的数据存放到baos中
            bitmapBefore.compress(Bitmap.CompressFormat.JPEG, options, baos);
            Log.e("TAG","图片压缩后：" + baos.toByteArray().length / 1024 + "KB");
        }
        Log.e("TAG","图片处理完成!" + baos.toByteArray().length / 1280 + "KB");

        // 这个文件夹，用来存放压缩后得图片
//        String temporaryPath= Environment.getExternalStorageDirectory()+PATH;
//        File temFile=new File(temporaryPath);
//        if (!temFile.exists()){
//            temFile.mkdir();
//        }
        // 放在项目的data目录下
        String temFile = mContext.getFilesDir() + File.separator;

        File file = new File(temFile,"index.png");
        boolean saved = false;
        try {
            // 使用Bitmap将自身保存为文件
//            Log.e("TAG", "Saving File To Cache " + file.getPath());
//            FileOutputStream os = new FileOutputStream(file);
//            bitmapBefore.compress(Bitmap.CompressFormat.PNG, 100, os);
//            os.flush();
//            os.close();

            //将压缩后的图片保存的本地上指定路径中
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();

            saved = true;
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            bitmap.recycle();
        }
        // 压缩成功返回ture
        return file;
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     * @param angle 被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
//        Log.e("TAG","开始处理图片被旋转得角度");
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
//        Log.e("TAG","图片旋转得角度处理完成...");
        return returnBm;
    }
    /**
     * 根据图片的大小设置压缩的比例，提高速度
     *
     * @param imageMB
     * @return
     */
    private static int setSubstractSize(int imageMB) {
        if (imageMB > 1000) {
            return 60;
        } else if (imageMB > 750) {
            return 40;
        } else if (imageMB > 500) {
            return 20;
        } else {
            return 10;
        }

    }
    //flie：要删除的文件夹的所在位置
    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFiles(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }}

}
