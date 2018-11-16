//
// Created by desperado on 18-8-18.
//

#include "SLAudioRecorder.h"
#include "CommonHelper.h"

void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    (static_cast<SLAudioRecorder *>(context))->processCallback(bq);
}

SLAudioRecorder::SLAudioRecorder(SampleFormat sampleFormat, SLEngineItf slEngineItf,
                                 RecorderBuffer &buf, std::string fp) : recorderBuffer(buf), filePath(fp) {
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
    SLDataLocator_AndroidSimpleBufferQueue locator_androidBufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2
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
    SampleBuf buffer = recorderBuffer.getFreeBuffer();
    sLresult = (*bufferQueueItf)->Enqueue(bufferQueueItf, buffer.buffer, buffer.size);
    SLASSTER(sLresult);

    sLresult = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    SLASSTER(sLresult);

    fileWriter.open(filePath, std::ios::out);
    return SL_BOOLEAN_TRUE;
}

void SLAudioRecorder::processCallback(SLAndroidSimpleBufferQueueItf bq) {
    //选择: 1. 编码 2. 实时播放 3. 写成wav格式保存
    if (!isStop) {
        fileWriter.write(recorderBuffer.getFillBuffer().buffer, recorderBuffer.getFillBuffer().size);
        SampleBuf buf = recorderBuffer.getFreeBuffer();
        (*bq)->Enqueue(bufferQueueItf, buf.buffer, buf.size);
    } else {

    }
}

void SLAudioRecorder::stop() {
    if (!isStop) {
        isStop = true;
        fileWriter.flush();
        fileWriter.close();
        (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
    }
}
