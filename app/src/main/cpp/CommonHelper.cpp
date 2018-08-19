//
// Created by desperado on 18-8-19.
//

#include "CommonHelper.h"

void convertToSLSampleFormat(SLAndroidDataFormat_PCM_EX *slAndroidDataFormat_pcm_ex, SampleFormat *format) {
    assert(slAndroidDataFormat_pcm_ex);
    memset(slAndroidDataFormat_pcm_ex, 0, sizeof(slAndroidDataFormat_pcm_ex));

    slAndroidDataFormat_pcm_ex->formatType = SL_DATAFORMAT_PCM;
    if (format->channels <= 1) {
        slAndroidDataFormat_pcm_ex->numChannels = 1;
        slAndroidDataFormat_pcm_ex->channelMask = SL_SPEAKER_FRONT_LEFT;
    } else {
        slAndroidDataFormat_pcm_ex->numChannels = 2;
        slAndroidDataFormat_pcm_ex->channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    }
    slAndroidDataFormat_pcm_ex->sampleRate = format->sampleRate;
    slAndroidDataFormat_pcm_ex->endianness = SL_BYTEORDER_LITTLEENDIAN;
    slAndroidDataFormat_pcm_ex->bitsPerSample = format->pcmFormat;
    slAndroidDataFormat_pcm_ex->containerSize = format->pcmFormat;

    slAndroidDataFormat_pcm_ex->bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16;
    slAndroidDataFormat_pcm_ex->containerSize = SL_PCMSAMPLEFORMAT_FIXED_16;
    slAndroidDataFormat_pcm_ex->formatType = SL_ANDROID_DATAFORMAT_PCM_EX;
}