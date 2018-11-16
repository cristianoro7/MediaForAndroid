//
// Created by desperado on 18-8-18.
//

#include "RecorderBuffer.h"

RecorderBuffer::RecorderBuffer(int bufferSize) {
    buffer = new SampleBuf[2];
    for (int i = 0; i < 2; ++i) {
        buffer[i].buffer = new char[bufferSize];
        buffer[i].size = static_cast<uint32_t>(bufferSize);
    }
    index = -1;
}

SampleBuf RecorderBuffer::getFreeBuffer() {
    index++;
    if (index > 1) {
        index = 0;
    }
    return buffer[index];
}

SampleBuf RecorderBuffer::getFillBuffer() {
    return buffer[index];
}

RecorderBuffer::~RecorderBuffer() {
    if (buffer) {
        delete buffer;
    }
}
