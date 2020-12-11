package com.sample.usbdemo.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.sample.usbdemo.service.androidService;

import java.io.File;

public class FileUtils {
    /**
     * 获取app文件路径
     */
    public static String getFile(Context context) {
        String filesDir = "";
        try {

            if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()
            ) {
                // 外部存储可用
                File file1 = context.getExternalFilesDir(null);
                if (null == file1) {
                    filesDir = context.getFilesDir().getPath();
                } else {
                    filesDir = file1.getPath();
                }
            } else {
                // 外部存储不可用
                filesDir = context.getFilesDir().getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesDir;
    }
}
