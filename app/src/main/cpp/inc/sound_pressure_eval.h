// Copyright 2019 Mobvoi Inc. All Rights Reserved.
// Author: nanli@mobvoi.com (Li Nan)

#ifndef SDK_UTILS_SOUND_PRESSURE_EVAL_H_
#define SDK_UTILS_SOUND_PRESSURE_EVAL_H_

//#include "sdk/utils/os_support.h"

#define REFERENCE_LEVEL 94.0f

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    A_Weighting = 1,
    Z_Weighting
} WeightingType;

typedef struct {
    int frame_len;
    int frame_sample_len;
    int smooth_window_len;
    int smooth_frame_num;
    int seq_head;
    int count_frame_num;

    float *in_data;
    float *out_data;
    float *mean_square_seq;
    float sum_mean_square_seq;
    float sensitivity;

    WeightingType weight_type;

    void *filter_inst;
} mob_sound_pressure;

void *mobvoi_dsp_sound_pressure_init(int frame_len,
                                     int smooth_window_len,
                                     int fs,
                                     float sensitivity,
                                     WeightingType weight_type);

float mobvoi_dsp_sound_pressure_process(void *instance, const short *in, float *ms);

void mobvoi_dsp_sound_pressure_cleanup(void *instance);

#ifdef __cplusplus
}
#endif

#endif  // SDK_UTILS_SOUND_PRESSURE_EVAL_H_
