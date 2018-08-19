//
// Created by desperado on 18-8-18.
//
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

SLObjectItf enginObject = NULL;
SLEngineItf engineItf = NULL;

SLObjectItf outputMixObject = NULL;
SLEnvironmentalReverbItf environmentalReverbItf = NULL;

void createEngine() {
    SLresult sLresult;
    sLresult = slCreateEngine(&enginObject, 0, NULL, 0, NULL, NULL);
    sLresult = (*enginObject)->Realize(enginObject, SL_BOOLEAN_FALSE);
    sLresult = (*enginObject)->GetInterface(enginObject, SL_IID_ENGINE, &engineItf);
}

void createMix() {
    SLresult sLresult;
    sLresult = (*engineItf)->CreateOutputMix(engineItf, )
}

