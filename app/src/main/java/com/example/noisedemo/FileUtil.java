package com.example.noisedemo;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by su on 2016/6/4.
 */
public class FileUtil {

    public static final String LOCAL = "Test";
    public static final String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    /**
     * 录音文件目录
     */
    public static final String REC_PATH = LOCAL_PATH + "/";
    private static final String TAG = "FileUtil";

    /**
     * 自动在SD卡创建相关的目录
     */
    static {
        File dirRootFile = new File(LOCAL_PATH);
        if (!dirRootFile.exists()) {
            dirRootFile.mkdirs();
        }
        File recFile = new File(REC_PATH);
        if (!recFile.exists()) {
            recFile.mkdirs();
        }
    }

    private FileUtil() {
    }


    public static File createFile(String fileName) {
        File dirRootFile = new File(LOCAL_PATH);
        if (!dirRootFile.exists()) {
            dirRootFile.mkdirs();
        }
        File recFile = new File(REC_PATH);
        if (!recFile.exists()) {
            recFile.mkdirs();
        }
        File myCaptureFile = new File(REC_PATH + fileName);
        if (myCaptureFile.exists()) {
            myCaptureFile.delete();
        }
        File parentFile = myCaptureFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            myCaptureFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }


}
