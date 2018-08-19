//
// Created by desperado on 18-8-19.
//

#ifndef MEDIAFORANDROID_COMMONHELPER_H
#define MEDIAFORANDROID_COMMONHELPER_H

#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <string.h>
#include <assert.h>

#include "SLAudioRecorder.h"
#include "RecorderBuffer.h"


#define SLASSTER(x) \
    do {            \
        assert(SL_RESULT_SUCCESS == (x)); \
        (void) (x); \
    } while(0)

typedef struct SampleFormat {
    uint32_t sampleRate;
    uint32_t framesPerBuffer;
    uint16_t channels;
    uint16_t pcmFormat;
    uint32_t representation;
} SampleFormat;

typedef struct SLAudioEngine_ {
    SLmilliHertz fastPathSampleRate;
    uint32_t fastPathFramesPerBuffer;
    uint16_t sampleChannels;
    uint16_t bitsPerSample;

    SLObjectItf slObjectItf;
    SLEngineItf slEngineItf;

    SLAudioRecorder *slAudioRecorder;
    RecorderBuffer *recorderBuffer;
} SLAudioEngine;

void convertToSLSampleFormat(SLAndroidDataFormat_PCM_EX *slAndroidDataFormat_pcm_ex, SampleFormat *format);

#endif //MEDIAFORANDROID_COMMONHELPER_H
