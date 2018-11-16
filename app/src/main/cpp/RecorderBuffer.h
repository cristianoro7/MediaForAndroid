//
// Created by desperado on 18-8-18.
//

#ifndef MEDIAFORANDROID_RECORDERBUFFER_H
#define MEDIAFORANDROID_RECORDERBUFFER_H

#include <stdint.h>

struct SampleBuf {
    char *buffer;
    uint32_t size;
};

class RecorderBuffer {

private:
    SampleBuf *buffer;
    int index;

public:
    RecorderBuffer(int bufferSize);
    ~RecorderBuffer();
    SampleBuf getFreeBuffer();
    SampleBuf getFillBuffer();
};


#endif //MEDIAFORANDROID_RECORDERBUFFER_H
