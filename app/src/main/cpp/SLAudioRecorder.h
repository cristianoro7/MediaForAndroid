//
// Created by desperado on 18-8-18.
//

#ifndef MEDIAFORANDROID_SLAUDIORECORDER_H
#define MEDIAFORANDROID_SLAUDIORECORDER_H

#include "RecorderBuffer.h"
#include "SampleFormat.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <string>
#include <fstream>
#include <iostream>

class SLAudioRecorder {

private:
    SLObjectItf recorderObjectItf;
    SLRecordItf recordItf;
    SLAndroidSimpleBufferQueueItf bufferQueueItf;
    RecorderBuffer recorderBuffer;
    bool isStop;
    std::string filePath;
    std::ofstream fileWriter;

public:
    explicit SLAudioRecorder(SampleFormat sampleFormat, SLEngineItf slEngineItf, RecorderBuffer &buf, std::string fp);

    void processCallback(SLAndroidSimpleBufferQueueItf bq);

    SLboolean start();

    void stop();
};
#endif //MEDIAFORANDROID_SLAUDIORECORDER_H
