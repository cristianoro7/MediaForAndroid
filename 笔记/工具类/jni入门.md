# JNI介绍

JNI 是 Java 语言提供的 Java 和 C/C++ 相互沟通的一种协议，Java 可以通过 JNI 调用本地的 C/C++ 代码，本地的 C/C++ 的代码也可以通过JNI 调用 Java 代码。

# JNI适用场景

1.  含有大量的计算密集型任务，例如音视频的编解码
2. 复用已有的 ``C/C++``代码库



# 注册方式

* 静态注册
* 动态注册



## 静态注册

我们在调用 native函数之前，必须先调用 System.loadLibrary() 接口加载一个实现了 navive 方法的动态库。那么加载动态库后，JVM 到底是怎么在动态库中找到实现的 native 函数呢？



### JVM 查找 native 方法规则

对于静态注册，JVM 是根据 JNI规范的命名规则去查询 实现的 native 函数。

JNI 函数的命名规则：Java\_类全路径\_方法名。例如：

```java
public class JNITest {
    public native void helloWorld();
}
```

上面是在Java 类中声明的一个 native函数，它对应的 JNI 函数如下：

```c++
extern "C"
JNIEXPORT void JNICALL
Java_com_desperado_mediaforandroid_jni_JNITest_helloWorld(JNIEnv *env, jobject instance) {
}
```

拿着 native 函数的名字和上面说的 JNI 函数命名规则对照，应该能更好理解这个命名规则。下面说说函数中的参数：

1. JNIEnv*: 它是每个 JNI 函数的第一个参数，指向一个函数表指针，表中的每个入口都指向一个 JNI 函数，用于访问 JVM 内部的数据结构
2.  声明native 方法所在的类的实例或者 Class 对象。如果 native 方法被声明为静态，则该参数是 jclass；如果 native 方法被声明为实例方法，那么该参数是 jobject

> JNIEXPORT和JNICALL是两个跨平台相关的宏，是 JNI 协议的关键字，用于表示此函数是被 JNI 调用的



# JNI数据类型与Java数据类型的映射

当我们在 Java 层调用一个 native 方法时，Java 层的参数是如何转换为 C/C++层的参数的呢？显然，两者的转换关系是由 JNI 协议来指定的，也就是接下来要讲的 JNI数据类型和Java数据类型的映射关系。

在 Java 语言中，数据类型分为两种：基本数据类型和引用数据类型。

其中基本类型和 JNI 的基本类型的映射关系如下：

| Java 数据类型 | JNI 数据累心 |       描述       |
| :-----------: | :----------: | :--------------: |
|    boolean    |   jboolean   | unsigned 8 bits  |
|     byte      |    jbyte     |  signed 8 bits   |
|     char      |    jchar     | unsigned 16 bits |
|     short     |    jshort    |  signed 16 bits  |
|      int      |     jint     |  signed 32 bits  |
|     long      |    jlong     |  signed 64 bits  |
|     float     |    jfloat    |     32 bits      |
|    double     |   jdouble    |     64 bits      |

引用类型和 JNI 的引用类型如下：

| Java引用类型 | JNI 引用类型  |
| :----------: | :-----------: |
| all objects  |    jobject    |
|    Class     |    jclass     |
|    String    |    jstring    |
|    arrays    |    jarray     |
|   Object[]   | jobjectArray  |
|  boolean[]   | jbooleanArray |
|    byte[]    |  jbyteArray   |
|    char[]    |  jcharArray   |
|   short[]    |  jshortArray  |
|    int[] 	   |  jintArray    |
|          long[]             |  jlongArray |
| float[] | jfloatArray|
| double[] | jdoubleArray |
| Throwable | jthrowable |

如果 JNI 是用 c++ 编写的， 那么所有引用类型继承自 jobject，对应的继承关系如下：

```c++
class _jobject {};
class _jclass : public _jobject {};
class _jstring : public _jobject {};
class _jarray : public _jobject {};
class _jbooleanArray : public _jarray {};
class _jbyteArray : public _jarray {};
...
```

如果是用 c 编写的，那么所有的引用类型都是 jobject。除了jobject外，其他引用类型都是使用 ``typedef``重新定义的，例如jstring：

```c
typedef jobject jstring
```

另外，在 JNI 中还有一个比较特殊的类型：jvalue。它是一个 union 类型，可以表示任意的数据类型，其定义如下：

```c
typedef union jvalue {
	jboolean z;
	jbyte b;
	jchar c;
	jshort s;
	jint i;
	jlong j;
	jfloat f;
	jdouble d;
	jobject l;
} jvalue;
```



# 字符串处理

JNI 的基本数据类型就是对 C/C++ 的基本数据类型用 typedef 重新定义了一个名字， 所以在 JNI 中，我们可以直接访问。而对于引用类型则不能直接访问，必须通过 JNIEnv* 指针来调用操作 JVM 内部数据结构的方法来访问。

## GetStringUTFChars#ReleaseStringUTFChars

```java
public class JNITest {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native String sayHello(String name);
}
```

```c++
extern "C"
JNIEXPORT jstring JNICALL
Java_com_desperado_mediaforandroid_jni_JNITest_sayHello(JNIEnv *env, jobject instance,
                                                        jstring name_) {
    jboolean isCopy;
    const char *name = env->GetStringUTFChars(name_, &isCopy);
    
    if (name == NULL) {
        return NULL;
    }

    char buffer[128];
    sprintf(buffer, "hello %s\n", name);

    env->ReleaseStringUTFChars(name_, name);

    return env->NewStringUTF(buffer);
}
```

sayHello 函数接收一个 jstring 对象，这个jstring类型是指向JVM内部的一个字符串，在JNI 中不能直接访问，必须通过JNI函数来访问JVM内部的数据结构。

GetStringUTFChars(env, j_str, &isCopy)函数可以将 jstring指针（指向 JVM内部一个 Unicode 字符序列）转换为一个 UTF-8编码的字符串，下面是它的参数说明：

1. jstring：Java层传递给本地代码的指针
2. isCopy: 取值 JNI_TRUE 和 JNI_FALSE，如果值为 JNI_TRUE，表示返回 JVM 内部源字符串的一份
   拷贝，并为新产生的字符串分配内存空间。如果值为 JNI_FALSE，表示返回 JVM 内部源字符串的指
   针，意味着可以通过指针修改源字符串的内容，不推荐这么做，因为这样做就打破了 Java 字符串不能修改
   的规定。但我们在开发当中，并不关心这个值是多少，通常情况下这个参数填 NULL 即可。

> 因为 Java 默认使用 Unicode 编码，而 C/C++ 默认使用 UTF 编码，所以在本地代码中操作字符串的时候，必
> 须使用合适的 JNI 函数把 jstring 转换成 C 风格的字符串。



调用 GetStringUTFChars 函数从 JVM 内部获取一个字符串之后，JVM 内部会分配一块新的内存，用于存储源字符串的拷贝，以便本地代码访问和修改。。通过调用ReleaseStringUTFChars 函数通知 JVM 这块内存已经不使用了，你可以清除了。

## NewStringUTF

NewStringUTF 可以创建java.lang.String 字符串对象，这个新创建的字符串会自动转
换成 Java 支持的 Unicode 编码。例如上例子中，将UTF-8编码的buffer转为一个jstring对象，然后返回给Java层



## GetStringChars和ReleaseStringChars

用于获取和释放以 Unicode 格式编码的字符串

## GetStringLength和GetStringUTFLength

由于 UTF-8 编码的字符串以'\0'结尾，而 Unicode 字符串不是。如果想获取一个指向 Unicode 编码的 jstring
字符串长度，在 JNI 中可通过GetStringLength函数获取。GetStringUTFLength获取 UTF-8 编码字符串的长度，



## GetStringCritical和ReleaseStringCritical

用 Get/ReleaseStringCritical 可直接返回源字符串的指针。不过这对函数
有一个很大的限制，在这两个函数之间的本地代码不能调用任何会让线程阻塞或等待 JVM 中其它线程的本地函
数或 JNI 函数。因为通过 GetStringCritical 得到的是一个指向 JVM 内部字符串的直接指针，获取这个直接指针
后会导致暂停 GC 线程，当 GC 被暂停后，如果其它线程触发 GC 继续运行的话，都会导致阻塞调用者。所以在
Get/ReleaseStringCritical 这对函数中间的任何本地代码都不可以执行导致阻塞的调用或为新对象在 JVM 中分
配内存，否则，JVM 有可能死锁。另外一定要记住检查是否因为内存溢出而导致它的返回值为 NULL，因为 JV
M 在执行 GetStringCritical 这个函数时，仍有发生数据复制的可能性，尤其是当 JVM 内部存储的数组不连续
时，为了返回一个指向连续内存空间的指针，JVM 必须复制所有数据。

## GetStringRegion和GetStringUTFRegion

分别表示获取 Unicode 和 UTF-8 编码字符串指定范围内的内容。这对函数会把源字符串复制到一个预先分配的
缓冲区内。

```c++
extern "C"
JNIEXPORT jstring JNICALL
Java_com_desperado_mediaforandroid_jni_JNITest_sayHello(JNIEnv *env, jobject instance,
                                                        jstring name_) {
    jsize len = env->GetStringLength(name_);
    char buf[128] = "hello";
    char *pbuff = buf + 6;
    env->GetStringUTFRegion(name_, 0, len, pbuff);

    return env->NewStringUTF(pbuff);
}
```

GetStringUTFRegion 这个函数会做越界检查，如果检查发现越界了，会抛出StringIndexOutOfBoundsExce
ption 异常，这个方法与 GetStringUTFChars 比较相似，不同的是，GetStringUTFRegion 内部不分配内
存，不会抛出内存溢出异常。

## 

