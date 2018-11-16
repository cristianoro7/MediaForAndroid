//
// Created by desperado on 2018/11/12.
//

#include "SLAudioPlayer.h"

SLAudioPlayer::SLAudioPlayer(SampleFormat *sampleFormat, SLEngineItf engineItf) {
    assert(sampleFormat);
    this->sampleInfo = *sampleFormat;

    SLresult result = (*engineItf)->CreateOutputMix(engineItf, &outputMixObjectItf, 0, NULL, NULL);
    SLASSTER(result);

    result = (*outputMixObjectItf)->Realize(outputMixObjectItf, SL_BOOLEAN_FALSE);
    SLASSTER(result);

    //配置数据源
    SLDataLocator_AndroidSimpleBufferQueue locator_androidSimpleBufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2
    };
    SLAndroidDataFormat_PCM_EX fm;
    convertToSLSampleFormat(&fm, &sampleInfo);
    SLDataSource audioSource = {&locator_androidSimpleBufferQueue, &fm};

    //配置输出源
    SLDataLocator_OutputMix locator_outputMix = {
            SL_DATALOCATOR_OUTPUTMIX,
            outputMixObjectItf
    };
    SLDataSink audioSnk = {
            &locator_outputMix, NULL
    };

    //创建接口
    SLInterfaceID  ids[2] = {
            SL_IID_BUFFERQUEUE, SL_IID_VOLUME
    };
    SLboolean req[2] = {
            SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE
    };
    result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObjectItf, &audioSource, &audioSnk,
                                             sizeof(ids) / sizeof(ids[0]), ids, req);
    SLASSTER(result);

    result = (*playerObjectItf)->Realize(playerObjectItf, SL_BOOLEAN_FALSE);
    SLASSTER(result);

    result = (*playerObjectItf)->GetInterface(playerObjectItf, SL_IID_PLAY, &playerItf);
    SLASSTER(result);

    result = (*playerObjectItf)->GetInterface(playerObjectItf, SL_IID_BUFFERQUEUE, &androidSimpleBufferQueueItf);
    SLASSTER(result);

    result = (*playerItf)->SetPlayState(playerItf, SL_PLAYSTATE_STOPPED);
    SLASSTER(result);
}

SLAudioPlayer::~SLAudioPlayer() {

}

SLresult SLAudioPlayer::start() {
    SLuint32 state;
    SLresult  result = (*playerItf)->GetPlayState(playerItf, &state);
    if (result != SL_RESULT_SUCCESS) {
        return SL_BOOLEAN_FALSE;
    }
    if (state == SL_PLAYSTATE_PLAYING) {
        return SL_BOOLEAN_TRUE;
    }

    result = (*playerItf)->SetPlayState(playerItf, SL_PLAYSTATE_STOPPED);
    SLASSTER(result);

    result = (*androidSimpleBufferQueueItf)->Enqueue()
    return 0;
}

void SLAudioPlayer::stop() {

}

void SLAudioPlayer::processSLCallback(SLAndroidSimpleBufferQueueItf bq) {

}
