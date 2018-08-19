//
// Created by desperado on 18-8-18.
//

#include "SLAudioRecorder.h"

void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    (static_cast<SLAudioRecorder *>(context))->processCallback(bq);
}

SLAudioRecorder::SLAudioRecorder(SampleFormat sampleFormat, SLEngineItf slEngineItf,
                                 RecorderBuffer &buf) : recorderBuffer(buf) {
    SLresult sLresult;
    SLAndroidDataFormat_PCM_EX format_pcm_ex;
    convertToSLSampleFormat(&format_pcm_ex, &sampleFormat);

    SLDataLocator_IODevice locator_ioDevice = {
            SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
            SL_DEFAULTDEVICEID_AUDIOINPUT, NULL
    };
    SLDataSource dataSource = {
            &locator_ioDevice, NULL
    };
    SLDataLocator_AndroidBufferQueue locator_androidBufferQueue = {
            SL_DATALOCATOR_ANDROIDBUFFERQUEUE, 2
    };

    SLDataSink dataSink = {
            &locator_androidBufferQueue, &format_pcm_ex
    };

    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    sLresult = (*slEngineItf)->CreateAudioRecorder(slEngineItf, &recorderObjectItf, &dataSource,
                                                   &dataSink,
                                                   1, id, req);
    SLASSTER(sLresult);

    sLresult = (*recorderObjectItf)->Realize(recorderObjectItf, SL_BOOLEAN_FALSE);
    SLASSTER(sLresult);

    sLresult = (*recorderObjectItf)->GetInterface(recorderObjectItf, SL_IID_RECORD, &recordItf);
    SLASSTER(sLresult);

    sLresult = (*recorderObjectItf)->GetInterface(recorderObjectItf,
                                                  SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &bufferQueueItf);
    SLASSTER(sLresult);

    sLresult = (*bufferQueueItf)->RegisterCallback(bufferQueueItf, bqRecorderCallback, this);
    SLASSTER(sLresult);
}

SLboolean SLAudioRecorder::start() {
    SLresult sLresult;
    sLresult = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
    SLASSTER(sLresult);
    sLresult = (*bufferQueueItf)->Clear(bufferQueueItf);
    SLASSTER(sLresult);
    sLresult = (*bufferQueueItf)->Enqueue(bufferQueueItf, recorderBuffer.getFreeBuffer().buffer,
                                          recorderBuffer.getFreeBuffer().size);
    SLASSTER(sLresult);

    sLresult = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    SLASSTER(sLresult);
    return SL_BOOLEAN_TRUE;
}

void SLAudioRecorder::processCallback(SLAndroidSimpleBufferQueueItf bq) {
    //选择: 1. 编码 2. 实时播放 3. 写成wav格式保存
}