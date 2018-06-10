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