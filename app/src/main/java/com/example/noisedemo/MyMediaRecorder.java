package com.example.noisedemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class MyMediaRecorder {

    public boolean isRecording = false;
    private AudioRecord mAudioRecord;
    private int mSampleRate = 16000;//采样率
    private int mChannelCount = AudioFormat.CHANNEL_IN_MONO;//双声道
    private int mAudioSource = MediaRecorder.AudioSource.MIC;//麦克风
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//量化精度
    private int mMinBufferSize;

    public int getBufferSize() {
        return mMinBufferSize;
    }

    public AudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    /**
     * 录音
     *
     * @return 是否成功开始录音
     */
    public boolean startRecorder() {
        try {
            //得到一帧数据的大小
            mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelCount, audioFormat);
            //AudioRecord对象实例化
            mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, audioFormat, mMinBufferSize);
            isRecording = true;
            Log.d("zq", "开始录音");
            return true;
        } catch (IllegalStateException e) {
            stopRecording();
            e.printStackTrace();
            isRecording = false;
        }
        return false;
    }

    public void stopRecording() {
        if (mAudioRecord != null) {
            if (isRecording) {
                try {
                    Log.d("zq", "停止录音");
                    mAudioRecord.stop();
                    mAudioRecord.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mAudioRecord = null;
            isRecording = false;
        }
    }
}
