//
// Created by desperado on 18-8-18.
//

#ifndef MEDIAFORANDROID_SLAUDIORECORDER_H
#define MEDIAFORANDROID_SLAUDIORECORDER_H

#include "CommonHelper.h"

class SLAudioRecorder {

private:
    SLObjectItf recorderObjectItf;
    SLRecordItf recordItf;
    SLAndroidSimpleBufferQueueItf bufferQueueItf;
    RecorderBuffer recorderBuffer;

public:
    explicit SLAudioRecorder(SampleFormat sampleFormat, SLEngineItf slEngineItf, RecorderBuffer &buf);

    void processCallback(SLAndroidSimpleBufferQueueItf bq);

    SLboolean start();
};
#endif //MEDIAFORANDROID_SLAUDIORECORDER_H
