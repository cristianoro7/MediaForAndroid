//
// Created by desperado on 2018/11/12.
//

#ifndef MEDIAFORANDROID_SAMPLEFORMAT_H
#define MEDIAFORANDROID_SAMPLEFORMAT_H


#include <cstdint>

typedef struct SampleFormat {
    uint32_t sampleRate;
    uint32_t framesPerBuffer;
    uint16_t channels;
    uint16_t pcmFormat;
    uint32_t representation;
} SampleFormat;

#endif //MEDIAFORANDROID_SAMPLEFORMAT_H
