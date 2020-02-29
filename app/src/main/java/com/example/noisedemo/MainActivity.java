package com.example.noisedemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

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


    private boolean mIsThreadRun;
    private SoundDiscView soundDiscView;
    private MyMediaRecorder mRecorder;
    private Thread mDectorthread;
    private InputStream ins;

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


    //TODO 下面代码是麦克风监听，目前结果不准不知为何，通过修改线程的睡眠时间和数据流缓存的大小会改变效果。
    // 暂时不知这二者之间的关系
    private void startListenAudio() {
        //改为线程的方式来实现io操作，解决卡顿问题
        mIsThreadRun = true;
        mDectorthread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsThreadRun) {
                    try {
                        int sampleRate = mRecorder.getAudioRecord().getSampleRate();
                        int channelCount = mRecorder.getAudioRecord().getChannelCount();
                        int audioFormat = mRecorder.getAudioRecord().getAudioFormat();
                        short[] buffer = new short[320];
                        Log.d(TAG, "  sampleRate是:" + sampleRate + "  channelCount是:"
                                + channelCount + "  audioFormat是:" + audioFormat);
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
        mDectorthread.start();
    }


    private void startListenAudioByWav() {
        //改为线程的方式来实现io操作，解决卡顿问题
        AssetManager assetManager = getResources().getAssets();
//        播放录音
//        AssetFileDescriptor afd = null;
//        try {
//            afd = assetManager.openFd("audio/kfc.wav");
//            MediaPlayer mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//            mediaPlayer.setOnCompletionListener(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //TODO 下面代码是读取流文件确定音量，目前结果不准不知为何
        ins = null;
        try {
            ins = assetManager.open("audio/kfc.wav");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "ins:" + ins);
        mIsThreadRun = true;
        mDectorthread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsThreadRun) {
                    try {
                        byte[] buffer = new byte[364];
                        if (ins == null) {
                            Log.e(TAG, "ins is null!" + ins);
                            return;
                        }
                        int read = ins.read(buffer);
                        if (read == -1) {
                            World.setDbCount(0);  //将声压值转为分贝值
                            soundDiscView.refresh(); //子线程刷新view，调用了postInvalidate
                            Log.d(TAG, "当前噪音是:" + 0);
                            return;
                        }
                        //TODO 这样截取前面的wav资源前面44位头部数据的操作正确么？
                        byte[] bytes = new byte[320];
                        for (int i = 0; i < buffer.length; i++) {
                            if (i > 43) {
                                bytes[i - 44] = buffer[i];
                            }
                        }
                        float volume = noiseMonitorWork(toShortArray(bytes));
                        World.setDbCount(volume);  //将声压值转为分贝值
                        soundDiscView.refresh(); //子线程刷新view，调用了postInvalidate
                        Log.d(TAG, "当前噪音是:" + volume);
                        Thread.sleep(refreshTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException exception) {

                    }
                }
            }
        });
        mDectorthread.start();
    }

    public short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public byte[] toByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }

    /**
     * 开始记录
     */
    public void startRecord() {
        try {
            if (mRecorder.startRecorder()) {
                mRecorder.getAudioRecord().startRecording();
                //麦克风检测
                startListenAudio();
                //音频资源检测
//                startListenAudioByWav();
            } else {
                Log.e(TAG, "启动录音失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "录音机已被占用或录音权限被禁止");
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
        mIsThreadRun = false;
        noiseMonitorClean();
        mDectorthread.interrupt();
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


    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(this, "播放完毕", LENGTH_SHORT).show();
    }
}
