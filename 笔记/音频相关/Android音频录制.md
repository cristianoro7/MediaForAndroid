# Android音频录制

## 写作计划

学习入门 ``Android`` 平台下的音视频技术, 我打算按照下图所示的步骤去学习：
![](https://diycode.b0.upaiyun.com/photo/2018/e2c45f97464bcc894cf5ee717cbdf705.png)

从上图可以看出, 学习的步骤大概有三大部分(可以穿插学习):
1. 音视频的录制
2. 音视频在网络传输的流媒体协议
3. 音视频的播放

### 音视频的录制
音频和视频的录制流程为: 音频和视频录制 -> 将录制得到的音频和视频进行编码 -> 将编码后的音频和视频数据按照一定的格式进行合成得到视频文件. 

### 流媒体协议

将得到的视频文件根据一定的协议进行网络传输, 不同的协议适用不同的场景

### 音视频的播放

从网络得到一个视频文件后, 我们需要经过: 将视频文件按照一定的格式分离出其中的音频和视频->对音频和视频进行解码->音视频同步播放

作为本专栏的开篇, 我们先学习音频的基础知识和Android下音频的录制.

## 音频基础知识

自然界的声音经过麦克风采集后, 可以得到模拟信号, 接着, 我们可以编写程序采集麦克风发出的采集信号, 最后得到数字信号. 在这个过程中, 含有三个信号的转变:

![](https://diycode.b0.upaiyun.com/photo/2018/8047efa60a3a3336ca4bd220586d15a1.png)

关于声信号到模拟信号的转换, 我们一般无需关心, 手机的麦克风都帮我们转换好了。 我们比较关心的是从麦克风得到的模拟信号, 程序如何去采集得到数字信号, 最后保存为音频文件。

### 模拟信号到数字信号的转换
![](https://diycode.b0.upaiyun.com/photo/2018/70e04d01a5ba67e3835c3a4a9b511f0e.png)
拟信号一般通过脉冲编码调制(Pulse Code Modulation, PCM)方法转换为数字信号, 这种方法包含三个步骤:

1. 采样: 模拟信号本身是一种连续信号，它在一定的时间范围内可以有无限多个不同的取值。而数字信号是指在取值上是离散的、不连续的信号。所谓的采样, 就是将一段时间内的连续信号转为离散信号。在上图中, 按照一定的频率, 对连续的模拟信号进行采集, 然后记录下来。根据采样定理, 按比声音最高频率的二倍进行采样, 声音就能被完整地恢复. 由于人耳能听到的频率范围是在20~20KHz, 所以采样率一般为44.1kHz, 这样才能保证声音达到20KHz时, 也能够被完整地恢复。

2. 量化: 指采样得到后的数据, 我们如何多少位的二进制数字来表示声音的振幅。例如 16 比特的二进制信号来表示一个声音的采样, 而 16bit 能表示的范围是: [-32768, 32767], 一共有65536个取值。

3. 编码: 将采样量化后的数据按照一定的格式进行记录, 比如顺序储存或者压缩储存。

### 音频开发中的重要参数

* 采样率

* 量化精度

* 声道数

* 帧间隔

#### 采样率

将模拟信号转为数字信号时, 需要隔一定的时间对模拟信号进行一个采样, 然后将这个采样, 用 01 来表示, 也就是数字化的过程. 采样率表示, 1S 内, 对模拟信号进行多少次采样. 采样频率越高, 说明采样点之间越密集, 记录这段音频所有的数据量就越大, 因此音质也就越好.

为了保证声音不失效, 我们采样率通常设置为在 44100Hz。 常用的音频采样频率有：8kHz、11.025kHz、22.05kHz、16kHz、37.8kHz、44.1kHz、48kHz、96kHz、192kHz等。

#### 量化精度

对于一个采样点, 需要用二进制数字来表示, 这个二进制的大小可以是: 4bit, 8bit, 16bit, 32bit. 位数越多, 表示的声音就越精细, 声音的质量就越好. 不过数据量也会变大。常见的位宽: 8bit, 16bit。

#### 声道数

声道数一般表示声音录制时的音源数量或回放时相应的扬声器数量。常用的有: 单通道和双通道。

#### 帧间隔

音频不像视频那样, 有一帧一帧的概念. 它是约定一个时间为单位, 然后这个时间内的数据为一帧, 这个时间被称为采样时间. 这个时间没有特别的标准, 要看具体的编解码器。

### 计算一帧音频的大小

假设某通道的音频信号是采样率为8kHz，位宽为16bit，20ms一帧，双通道，则一帧音频数据的大小为：
```java
int size = 8000 x 16bit x 0.02s  x 2 = 5120 bit = 640 byte
```

## Android 音频录制

了解音频的基础知识后, 我们可以开始来熟悉 ``Android`` 平台下,  音频录制的 ``API`` 了。Android提供了两套音频采集的 ``API``, 分别是:
    1. ``MediaRecorder``: 比较上层的 ``API``, 它可以直接把手机麦克风的音频数据进行编码然后储存成文件。使用简单, 但是支持的格式有限, 并且不支持对音频进行进一步的处理, 例如变声、混音等。
    2. ``AudioRecord``: 比较底层的一个 ``API``, 能够得到原始的 ``PCM``音频数据。由于我们得到的是原始的 ``PCM`` 数据, 我们可以对音频进行进一步的处理, 例如编码、混音和变声等。

关于 ``MediaRecorder`` 的使用比较简单, 这里不做介绍。接下来, 主要介绍 ``AudioRecord`` 的使用套路。

### AudioRecord 工作流程

``AudioRecord``的使用套路大体可以分为以下四个步骤:

1.  根据配置参数, 初始化音频内部的缓冲区
2.  开始采集原始的音频数据
3.  开辟一个 ``worker`` 线程, 不断地从 ``AudioRecord``中的缓冲区将音频数据读出来
4.  停止采集, 及时释放资源

#### 根据配置参数, 初始化音频内部的缓冲区

```java
//默认采样率, 44100Hz 可以保证兼容所有Android手机的采样率。
private static final int DEFAULT_SAMPLE_RATE = 44100; 

//通道数: 单通道, AudioFormat.CHANNEL_IN_STEREO表示双通道
private static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
 
//16位 量化位宽
private static final int SIMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT; 

//声音从麦克风采集而来, 可选的值以常量的形式定义在 MediaRecorder.AudioSource 类中，常用的值包括：DEFAULT（默认），VOICE_RECOGNITION（用于语音识别，等同于DEFAULT），MIC（由手机麦克风输入），VOICE_COMMUNICATION（用于VoIP应用）等等。
private static final int DEFAULT_SOURCE_MIC = MediaRecorder.AudioSource.MIC; 
```

```java
private AudioRecord createAudioRecord(int audioSource, int simpleRate, int channels, int audioFormat) {
     int minBufferSize = AudioRecord.getMinBufferSize(simpleRate, channels, audioFormat); //获取一帧音频帧的大小
     if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
         Log.d(TAG, "获取音频帧大小失败!");
         return null;
     }
    int audioRecordBufferSize = minBufferSize * 4; //AudioRecord内部缓冲设置为4帧音频帧的大小
    AudioRecord audioRecord = new AudioRecord(audioSource, simpleRate, channels, audioFormat, audioRecordBufferSize);
    if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
        Log.d(TAG, "初始化AudioRecord失败!");
        return null;
    }
    return audioRecord;
}
```

初始化``AudioRecord``时, 我们需要先确定一帧音频占用的大小, ``Android``提供了``AudioRecord.getMinBufferSize`` 函数给我们确定, 不建议手动算音频帧的大小。

``audioRecordBufferSize`` 配置的是 ``AudioRecord`` 内部的音频缓冲区的大小，该缓冲区的值不能低于一帧音频帧的大小, 我这里配置为4帧的大小。

####  开始采集原始的音频数据

```java
 mAudioRecord.startRecording(); //开始采集数据
```

#### 开辟一个 ``worker`` 线程, 不断地从 ``AudioRecord``中的缓冲区将音频数据读出来

当我们调用 ``AudioRecord`` 的``startRecording()`` 方法后, ``AudioRecord``就会开始帮我们采集数据, 然后存放在内部的缓冲区, 这时候, 我们应该开辟一个 ``worker``线程, 不断从内部缓冲区将音频数据读出来。来个比较生动的图:

![](https://diycode.b0.upaiyun.com/photo/2018/bc7089a57ded4200207d091bb72b8cb0.png)

拿到后的``PCM``数据, 我们一般通过接口回调给外部使用。外部使用者可以使用 ``AudioTrick`` 进行实时播放, 或者进行一次然后保存成一个音频文件。

```java
private OnAudioCaptureListener listener;

public interface OnAudioCaptureListener {
    void onAudioFrameCaptured(byte[] audioData);
}

public void setOnAudioCaptureListener(OnAudioCaptureListener listener) {
    this.listener = listener;
}

private class AudioCaptureRunnable implements Runnable {

    @Override
    public void run() {
        while (!isExit) {
            byte[] buffer = new byte[1024 * 2]; //每次拿2k
            int result = mAudioRecord.read(buffer, 0, buffer.length);
            if (result == AudioRecord.ERROR_BAD_VALUE) {
                Log.d(TAG, "run: ERROR_BAD_VALUE");
            } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.d(TAG, "run: ERROR_INVALID_OPERATION");
            } else {
                if (listener != null) {
                    Log.d(TAG, "run: capture buffer length is " + result);
                    listener.onAudioFrameCaptured(buffer);
                }
            }
       }
   }
}
```

#### 停止采集, 及时释放资源

当我们录制完时, 需要停止采集然后及时释放掉 ``native`` 层的资源

```java
    public boolean stop() {
        if (!isStart) {
            return false;
        }

        isExit = true;

        try {
            captureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop(); // 不是必须调用的, 因为release内部也会调用
        }
        mAudioRecord.release(); //释放掉资源
        mAudioRecord = null;
        isStart = false;
        Log.d(TAG, "stop: stop successfully");
        return true;
    }
```



最后,附上完整[代码地址](https://github.com/cristianoro7/MediaForAndroid/blob/26eec8191baeacec40a3dc0a6a74e246421df7e5/app/src/main/java/com/desperado/mediaforandroid/audio/AudioCapture.java)。欢迎 `start` 和 `follow`

## 总结

这次主要总结了自己学习音视频的一个大体计划, 接着讲了音频的一些基础知识和 ``Android`` 平台下, ``AudioRecord``的使用。

## 下一步

1. 使用 ``AudioTrick`` 来播放采集得到的 ``PCM`` 数据

2. 将采集得到的 ``PCM`` 数据编码成 ``AAC``、``WAV`` 这两种格式的文件进行保存

## 参考资料

* [](http://blog.51cto.com/ticktick/category15.html)


* 《音视频开发进阶指南》



> 由于本人能力水平限制，文章中难免会有错误。如果大家发现文章有不足之处，欢迎指出。

