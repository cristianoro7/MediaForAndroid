# WAV格式

* ``WAV``是微软开发的一种**无损压缩**音频文件格式，它的格式比较简单， 只在原始的``PCM``数据上加了一下元信息。 这种文件格式主要分为两个部分：
  1. 头文件：主要记录音频文件的一些元信息， 方便播放器等进行识别。例如：采样率、通道数、位宽等
  2. 数据块：也就是``PCM``数据
* 这种文件格式由于没有压缩， 所以音质会比较好，但是文件也会比较大。

## WAV文件头

![img](http://soundfile.sapp.org/doc/WaveFormat/wav-sound-format.gif)

* ``WAV``文件主要分为三部分：
  1. ``RIFF``块
  2. ``fmt``块
  3. ``data``块

### RIFF块

``ChunkID``: 取值为“RIFF”

`` ChunkSize ``:  排除``ChunkId``和``ChunkSize``后，文件剩余的大小

``Format``:  固定值为”WAVE“	



### ``fmt``块

``Subchunk1ID``: 固定值为“fmt ”; 占4字节（fmt后面还跟着一个空格凑够4字节）

`` Subchunk1Size``: 固定值为16， 标识``fmt``块除了``Subchunk1ID``外占用的字节大小

``AudioFormat``: 固定值1

``NumChannels``:  音频的通道数

``SampleRate``:  音频的采样率

``ByteRate``: 1S的音频数据占用的字节数，计算公式：``SampleRate * NumChannels * BitsPerSample/8``

``BlockAlign``:  一个音频采样点占用的字节数， 计算公式：``NumChannels * BitsPerSample/8``

``BitsPerSample``: 音频数据的位宽



### ``data``数据块

``Subchunk2ID``: 固定为``data``

``Subchunk2Size``:  音频数据的占用的字节大小

# Android提供的API

* Android提供了3套API给我们:
  1. MediaPlayer: 适合在后台长时间播放本地音乐文件或者在线的流式资源
  2. SoundPool: 适合播放比较短的音频片段，比如游戏声音、按键声、铃声片段等等
  3. AudioTrack: 更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景

# AudioTrack 的工作流程

1. 配置参数, 初始化内部的音频缓冲区
2. 开始播放
3. 开启一个工作线程, 不断地向 AudioTrack 的缓冲区“写入”音频数据，注意，这个过程一定要及时，否则就会出现“underrun”的错误，该错误在音频开发中比较常见，意味着应用层没有及时地“送入”音频数据，导致内部的音频播放缓冲区为空。
4. 停止播放, 释放资源

## 配置参数

1. **streamType**: 这个参数代表着当前应用使用的哪一种音频管理策略，当系统有多个进程需要播放音频时，这个管理策略会决定最终的展现效果，该参数的可选的值以常量的形式定义在 AudioManager 类中，主要包括：
   1. STREAM_VOCIE_CALL：电话声音
   2. STREAM_SYSTEM：系统声音
   3. STREAM_RING：铃声
   4. STREAM_MUSCI：音乐声
   5. STREAM_ALARM：警告声
   6. STREAM_NOTIFICATION：通知声
2. **sampleRateInHz**: ，这个采样率的取值范围必须在 4000Hz～192000Hz 之间。
3. **channelConfig**: 通道数的配置，可选的值以常量的形式定义在 AudioFormat 类中，常用的是 CHANNEL_IN_MONO（单通道），CHANNEL_IN_STEREO（双通道）
4. **audioFormat**: 这个参数是用来配置“数据位宽”的，可选的值也是以常量的形式定义在 AudioFormat 类中，常用的是 ENCODING_PCM_16BIT（16bit），ENCODING_PCM_8BIT（8bit），注意，前者是可以保证兼容所有Android手机的。
5. **bufferSizeInBytes**: AudioTrack 内部的音频缓冲区的大小，该缓冲区的值不能低于一帧“音频帧”（Frame）的大小
6. **mode**: AudioTrack 提供了两种播放模式，一种是 static 方式，一种是 streaming 方式，前者需要一次性将所有的数据都写入播放缓冲区，简单高效，通常用于播放铃声、系统提醒的音频片段; 后者则是按照一定的时间间隔不间断地写入音频数据，理论上它可用于任何音频播放的场景。



# MediaCodec(4.1加入的)

## 介绍

``MediaCodec``是``Android``底层多媒体组件之一，我们可以用它来访问底层的编解码器组件。它的工作原理可以用官方的一张图来理解：

![](/home/desperado/下载/mediacodec_buffers.png)

``MediaCodec``内部含有两个圆形缓冲区，一个是输入缓冲区，另外一个是输出缓冲区。我们将数据(原始数据或者编码后的数据)填入一个空的输入缓冲区，然后推回给``MediaCodec``。这时，``MediaCodec``会进行编解码并将编解码后的数据填入输出缓冲区内。最后我们客户端再请求``MediaCodec``输出缓冲区的数据以获得编解码后的数据。

简单理解就是一个``input``和``output``的过程，输入数据-->``MediaCodec``编解码-->输出数据。



## 生命周期

![](/home/desperado/下载/mediacodec_states.png)

``MediaCodec``的生命周期分为三个:

1. ``Stopped``
2. ``Executing``
3. ``Released``

### Stopped

``Stopped``内包含三个子状态: ``Uninitialized``, ``Configured`` 和``Error``。当我们用``MediaCodec``的工厂方法创建一个实例时，``MediaCodec``处于``Uninitialized``状态。得到实例后，调用``MediaCodec.configure(...)``，``MediaCodec``进入``Configured``状态。当``MediaCodec``处理数据发生错误时会转移到``Error``状态。



### Executing

``Executing``内部也分为三个状态: ``Flushed``，``Running`` 和``End-of-Stream``。一旦调用``MediaCodec.start``后，``MediaCodec``便从``Configure``状态进入``Flushed``状态。处于这个状态的``MediaCodec``，其内部的输入和输出缓冲区都是空闲的。一旦外部向``MediaCodec``请求输入缓冲区填数据时，``MediaCodec``进入``Running``状态。而当外部在输入缓冲区填入``end-of-stream``标记时，``MediaCodec``内部状态将转移到``End-of-Stream``状态，在这个状态的``MediaCodec``，不再接收新的请求。

当处于``MediaCodec``处于``Executing``状态时，调用它的``flush``可以令``MediaCodec``返回``Flushed``子状态。



### Released

当``MediaCodec``完成它的工作后，应该调用``MediaCodec.release()``释放掉它内部的资源。



### 使用

上述只是对``MediaCodec``的一个简短介绍，详细内容可以参考[官方文档](https://developer.android.com/reference/android/media/MediaCodec)。下面我们来看看``MediaCodec``的一个使用套路。总体来说，``MediaCodec``的使用步骤可以分为三个：

1. 创建实例
2. 配置``MediaCodec``, 并调用它的``start``方法
3. 数据处理

### 创建实例

``MediaCodec``提供了两个静态方法给我们创建实例，一个是``createEncoderByType(mineType)``，另外一个是``createByCodecName(name)``。前者是使用一个``mineType``创建``MediaCodec``，由于可能存在设备不支持``mimeType``对应的编解码器，所以不推荐使用这种方式来创建。建议使用``createByCodecName(name)``和``MediaCodecList.findEncoderForFormat``创建实例。这种方式与前面的一种不同，它会首先判断当前设备是否支持指定的编解码器，如果支持的话，我们可以创建``MediaCodec``。如果不支持的话，我们再提示给用户。

```java
public static final int DEFAULT_BIT_RATE = 128 * 1024; //128kb

public static final int DEFAULT_SIMPLE_RATE = 44100; //44100Hz

public static final int DEFAULT_CHANNEL_COUNTS = 1; //单通道

public static final int DEFAULT_MAX_INPUT_SIZE = 16384; //16k

private MediaCodec createMediaCodec() {
    mediaFormat = createMediaFormat();
    MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
    String name = mediaCodecList.findEncoderForFormat(mediaFormat);
    if (name != null) {
    	try {
        	return MediaCodec.createByCodecName(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return null; //不支持对应的编解码器
}

private MediaFormat createMediaFormat() {
    MediaFormat mediaFormat=MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                                             DEFAULT_SIMPLE_RATE, DEFAULT_CHANNEL_COUNTS);
    mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                           MediaCodecInfo.CodecProfileLevel.AACObjectLC);
    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
    mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, DEFAULT_MAX_INPUT_SIZE);
    return mediaFormat;
}
```

### 配置``MediaCodec``, 并调用它的``start``方法

这个步骤比较简单，只需要调用对应的方法即可

```java
public void start() {
	configure();
    mediaCodec.start();
}

private void configure() {
	mediaCodec = createMediaCodec();
    if (mediaCodec == null) {
    	throw new IllegalStateException("该设备不支持AAC编码器");
    }
    //configure函数的第二个参数Surface
    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
}
```

### 数据处理

``MediaCodec``的数据处理分为两种模式: 同步模式和异步模式。其中异步模式是``API``为21之后引入的。不管是同步还是异步模式，``MediaCodec``数据处理的思想都是一样的: 

1. 向``MediaCode``请求空闲的输入缓冲区。在同步模式中，调用`dequeueInput`获得可用的输入缓冲区；在异步模式中，可以在`MediaCodec.Callback.onInputBufferAvailable(…)`回调接口获取空闲的输入缓冲区。
2. 向缓冲区填入数据，接着调用``queueInputBuffer(...)``将缓冲区提交给``MediaCode``。
3. 向``MediaCode``请求编解码后的输出缓冲区。在同步模式中，调用``dequeueOutputBuffer``获得可用的输出缓冲区；在异步模式中，我们可以在``MediaCodec.Callback.onOutputBufferAvailable(…)`回调接口中获得可用的输出缓冲区。
4. 从输出缓冲区取出数据，最后调用``releaseOutputBuffer``方法释放缓冲区。

#### 同步模式

