//
// Created by desperado on 2018/11/12.
//

#ifndef MEDIAFORANDROID_SLAUDIOPLAYER_H
#define MEDIAFORANDROID_SLAUDIOPLAYER_H

#include "CommonHelper.h"

class SLAudioPlayer {

private:
    //player and buffer queue
    SLObjectItf outputMixObjectItf;
    SLObjectItf playerObjectItf;
    SLPlayItf playerItf;
    SLAndroidSimpleBufferQueueItf androidSimpleBufferQueueItf;

    SampleFormat sampleInfo;

public:
    explicit SLAudioPlayer(SampleFormat *sampleFormat, SLEngineItf engineItf);
    ~SLAudioPlayer();
    SLresult start();
    void stop();
    void processSLCallback(SLAndroidSimpleBufferQueueItf bq);
};


#endif //MEDIAFORANDROID_SLAUDIOPLAYER_H
