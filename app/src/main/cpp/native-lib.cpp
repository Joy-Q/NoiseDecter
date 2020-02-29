#include <jni.h>
#include <string>
#include <android/log.h>
#include <sound_pressure_eval.h>
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "androidlog.h"


const int smooth_window_len = 1000;  // by ms
const int sample_rate = 16000;
const float sensitivity = -26.0f;
const WeightingType weight_type = A_Weighting;
const int frame_len = 10;  // by ms
size_t frame_sample_num = sample_rate / 1000 * frame_len;
short *in_data = (short *) calloc(sizeof(short), frame_sample_num);
void *instance;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_noisedemo_MainActivity_noiseMonitorInit(JNIEnv *env, jobject /* this */) {
    __android_log_print(ANDROID_LOG_ERROR, "noiseMonitorInit", "ok了");
    instance = mobvoi_dsp_sound_pressure_init(frame_len,
                                              smooth_window_len,
                                              sample_rate,
                                              sensitivity,
                                              weight_type);
}

extern "C"
JNIEXPORT float JNICALL
Java_com_example_noisedemo_MainActivity_noiseMonitorWork(JNIEnv *env, jobject /* this */,
                                                         jshortArray bufferArray) {
    jshort *test = env->GetShortArrayElements(bufferArray, NULL);
    int size = (sizeof(test) / sizeof(*test)) - 1;

    LOGD("user info----name:%s, age:%d, sex:%s,FF:%d", "xxx", 18, "男", size);
    LOGD("JNI HELLO WORLD %p\n", test);
    float rms_db = mobvoi_dsp_sound_pressure_process(instance, test, NULL);
    __android_log_print(ANDROID_LOG_ERROR, "TEST", "wokao");
    env->ReleaseShortArrayElements(bufferArray, test, 0);
    return rms_db;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_noisedemo_MainActivity_noiseMonitorClean(JNIEnv *env, jobject /* this */) {
    __android_log_print(ANDROID_LOG_ERROR, "noiseMonitorClean", "ok了");
    mobvoi_dsp_sound_pressure_cleanup(instance);
}