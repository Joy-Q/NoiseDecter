package com.example.noisedemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int GET_RECODE_AUDIO = 1;
    private static final int refreshTime = 10;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        System.loadLibrary("native-lib");
    }

    public boolean isThreadRun;
    private SoundDiscView soundDiscView;
    private MyMediaRecorder mRecorder;

    /*
     * 申请录音权限*/
    public void verifyAudioPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSION_AUDIO, GET_RECODE_AUDIO);
            }
        }
    }
    // Used to load the 'native-lib' library on application startup.

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_RECODE_AUDIO) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyAudioPermissions(this);
        mRecorder = new MyMediaRecorder();
        noiseMonitorInit();
    }

    private void startListenAudio() {
        //改为线程的方式来实现io操作，解决卡顿问题
        isThreadRun = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        int sampleRate = mRecorder.getAudioRecord().getSampleRate();
                        int channelCount = mRecorder.getAudioRecord().getChannelCount();
                        int audioFormat = mRecorder.getAudioRecord().getAudioFormat();
                        short[] buffer = new short[160];
                        Log.d(TAG, "  sampleRate是:" + sampleRate + "  channelCount是:" + channelCount + "  audioFormat是:" + audioFormat);
                        int read = mRecorder.getAudioRecord().read(buffer, 0, buffer.length);
                        Log.d(TAG, "read是:" + read);
                        Log.d(TAG, "buffer长度:" + buffer.length);
                        Log.d(TAG, "buffer是:" + Arrays.toString(buffer));
                        float volume = noiseMonitorWork(buffer);
                        World.setDbCount(volume);  //将声压值转为分贝值
                        soundDiscView.refresh(); //子线程刷新view，调用了postInvalidate
                        Log.d(TAG, "当前噪音是:" + volume);
                        Thread.sleep(refreshTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * 开始记录
     */
    public void startRecord() {
        try {
            if (mRecorder.startRecorder()) {
                mRecorder.getAudioRecord().startRecording();
                startListenAudio();
            } else {
                Log.e("file", "启动录音失败");
            }
        } catch (Exception e) {
            Log.e("file", "录音机已被占用或录音权限被禁止");
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        soundDiscView = findViewById(R.id.soundDiscView);
        startRecord();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecorder.stopRecording();
        isThreadRun = false;
        noiseMonitorClean();

    }

    /**
     * 停止记录
     */
    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    public native void noiseMonitorInit();

    public native float noiseMonitorWork(short[] voice);

    public native void noiseMonitorClean();

}
