# Android提供的API

* Android提供了两套音频采集的API, 分别是:
  1. MediaRecorder: 比较上层的API, 它可以直接把手机麦克风的音频数据进行编码然后储存成文件.
  2. AudioRecord: 比较底层的一个API, 能够得到原始的PCM音频数据.
* 选择:
  1. 如果是简单是录制音频, 然后播放, 例如录音机, 推荐使用第一种
  2. 如果是需要对音频做进一步算法的处理, 或者编码以及网络传输, 建议使用后者.

#AudioRecord工作流程

1.  配置参数, 初始化音频内部的缓冲区
2. 开始采集原始的音频数据
3. 开辟一个工作线程, 不断地从AudioRecord中将音频数据读出来.
4. 停止采集, 及时释放资源.

## 配置参数

1. **audioSource**: 该参数指的是音频采集的输入源，可选的值以常量的形式定义在 MediaRecorder.AudioSource 类中，常用的值包括：DEFAULT（默认），VOICE_RECOGNITION（用于语音识别，等同于DEFAULT），MIC（由手机麦克风输入），VOICE_COMMUNICATION（用于VoIP应用）等等。
2. **sampleRateInHz**: 采样率，注意，目前44100Hz是唯一可以保证兼容所有Android手机的采样率。
3. **channelConfig**: 通道数的配置，可选的值以常量的形式定义在 AudioFormat 类中，常用的是 CHANNEL_IN_MONO（单通道），CHANNEL_IN_STEREO（双通道)
4. **audioFormat**: 这个参数是用来配置“数据位宽”的，可选的值也是以常量的形式定义在 AudioFormat 类中，常用的是 ENCODING_PCM_16BIT（16bit），ENCODING_PCM_8BIT（8bit），注意，前者是可以保证兼容所有Android手机的。
5. **bufferSizeInBytes**: 它配置的是 AudioRecord 内部的音频缓冲区的大小，该缓冲区的值不能低于一帧“音频帧”（Frame）的大小, AudioRecord 类提供了一个帮助你确定这个 bufferSizeInBytes 的函数。

## 采集线程

* 创建好AudioRecord对象后, 调用AudioRecord.startRecording()进行开始采集.
* 采集线程进入循环, 调用AudioRecord.read(byte[] audioData, int offsetInBytes, int sizeInBytes); 不断从取走音频.
* 采集结束后, 调用AudioRecord.stop()和release()停止和释放资源.