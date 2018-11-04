# OpenGL For Andrid

## GLSurfaceView

GLSurfaceView继承自SurfaceView, 在SurfaceView的基础上帮我们做了OpenGL的一些初始化操作,例如一些OpenGL需要的配置初始化和渲染线程的管理, 并且提供了onPause、onResume这两个生命周期方法,以便能够跟着Activity的生命周期进行联动

> TODO: SurfaceView和TextureView的区别, 将GLSurfaceView移植到TextureView中

下面是使用GLSurfaceView作为OpenGL渲染的一个框架结构:

```java
public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private boolean isSupportGL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSupportGL()) { //1.检查是否支持GL2.0
            mGLSurfaceView = new GLSurfaceView(this); //渲染容器
            mGLSurfaceView.setRenderer(new GLRender()); //渲染逻辑
            isSupportGL = true;
        } else { //不支持的话提示给用户
            isSupportGL = false;
            Toast.makeText(this, "not support gl", Toast.LENGTH_LONG).show();
        }
        setContentView(mGLSurfaceView);
    }

    private boolean isSupportGL() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = activityManager.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK build for x86")));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSupportGL) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSupportGL) {
            mGLSurfaceView.onResume();
        }
    }

    class GLRender implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 0, 0, 0);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }
}
```

上例中的逻辑: 检查设备是否支持OpenGL2.0, 不支持的话, 弹Toast提示. 否则实例化GLSurfaceView并设置渲染逻辑的实现类.

使用GLSurfaceView, 我们主要的工作在于实现渲染的逻辑. GLSurfaceVIew提供了Renderer接口给我们实现渲染的逻辑, 该接口包含三个方法:

1. onSurfaceCreated(GL10 gl, EGLConfig config): 当Surface创建后, 会回调这个接口. 方法中的参数是面向OpenGL1.0的, 我们使用2.0版本无需关心这两个参数
2. onSurfaceChanged(GL10 gl, int width, int height): 每次Surface的大小发生改变时就会回调
3. onDrawFrame(GL10 gl): 每次绘制一帧时会被回调. 每次回到这个方法时, 我们必须画一些东西上去, 不然会有黑屏的现象. 因为这个方法返回后, 系统会清除掉渲染缓冲区的旧数据, 然后填充新数据. 如果我们没画的话, 会出现黑屏现象.

## 定义Vertices和Shaders

### Vertices

Vertices从几何学上来讲, 是一个顶点, 这个顶点是用一个向量表示(可以理解为坐标), 在OpenGL中, 它除了包含位置信息, 还其他属性

在OpenGL ES中, 我们能画的只有点、线和三角形, 这三种形状的组合能绘制各种各样的图形. 例如矩形, 可以由两个三角形组成, 用Java代码描述如下:

![54124748409](/home/desperado/下载/open1.png)

```java
private float[] tableVerticesWithTriangles = {
                //triangle 1
                0, 0,
                9f, 14f,
                0, 14f,
                //triangle 2
                0, 0,
                9, 0,
                9, 14
        };
```

在上面, 我们用两个三角形组成成一个矩形, 下面我们再矩形中, 增加一条横线和两个点, 同样用Java代码表示如下:

![](/home/desperado/下载/open2.png)

```java
        private float[] tableVerticesWithTriangles = {
                //triangle 1
                0, 0,
                9f, 14f,
                0, 14f,
                //triangle 2
                0, 0,
                9, 0,
                9, 14,
                //Line 1
                0f, 7f,
                9f, 7f,
                //point 1
                4.5f, 2f,
                //point 2
                4.5f, 12f
        };
```

我们上面定义的数据都是在Java堆上定义的, OpenGL不能直接直接访问到Java堆上的数据, 因为OpenGL不是运行在Android 虚拟机上的, 我们需要将Java层的数据拷贝到Native堆上或者通过JNI, 将数据直接在Native堆上分配, 这样OpenGL才能访问到我们定义的Vertices数据. 下面我们使用第一种方法.

```java
public static final int BYTE_PER_FLOAT = 4;
        public final FloatBuffer vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
```

### OpenGL Pipeline

在我们的数据被绘制在屏幕之前, 我们需要将我们的数据作为输入给OpenGL Pipeline处理. OpenGL Pipeline简单理解就是一个将输入的数据经过一系列的处理, 最终输出Frame Buffer展示在屏幕上

![](/home/desperado/下载/open3.png)

在OpenGL2.0中, 我们可以通过一个shaders子程序, 将我们的数据交给GPU Pipeline处理. shaders告诉GPU如何绘制我们的数据. shaders有下面两种分类:

1. vertex shader: 确定每个顶点的最终位置, OpenGL会将顶点集组成成点、线或者三角形.
2. fragment shader: fragment的意思是碎片, 表示一个带有颜色的小矩形, 可以看做是屏幕上的一个像素点

### 定义Vertex Shader

```java
attribute vec4 a_Position;

void main() {
    gl_Position = a_Position;
}
```

上面是一个vectex shader程序, 它的语法与C语言类似. 我们可以编写shader程序来告诉GPU如何绘制渲染.

vec4是一个四维的向量, 代表vectex的一个位置信息, 前三维分别是x, y和z, 第四维是w, 它是一个特殊的坐标, 这个后面再讲.

gl_Position是一个内置变量, 它代表vectex的最终位置.

我们定义了多少个vectex, vectex shader程序就会被调用多少次. 每次被调用, 都会将当前的vectext的位置信息赋值给a_Position

main方法是shader程序的入口方法, 每次被调用时, 都会将当前的位置信息a_Position赋值个gl_Positon作为Vectex的最终位置信息.

### 定义Fragment Shader

我们定义了vertex shader 程序后, 已经确定了每个vertex的最终位置, 但是仍然需要定义fragment shader, 来确定每个fragment的最终颜色. 在我们编写fragment之前, 先来理解一下fragment这个概念

#### 光栅化

OpenGL将点、线、三角形或者组合图形分割成一个一个fragment, 然后再将fragment映射到屏幕的像素点上, 这个过程就叫光栅化或者像素化. 即fragment代表屏幕上的一个像素点(高分辨率的屏幕可能几个fragment对应一个像素点). 每个fragment带有一个包含rgba的颜色值.

简单来说: OpenGL将矢量图形映射到屏幕的过程

![](/home/desperado/下载/open4.png)

### fragment shader

fragment shader程序主要是用来告诉GPU, 每个fragment最终显示在屏幕的颜色值. 对于一个图形, 它有多少个fragment, 那么fragment shader程序就会被调用多少次, 下面是一个例子:

```java
precision mediump float;

uniform vec4 u_Color;

void main() {
    gl_FragColor = u_Color;
}

```

##### precision描述符

precision描述符用于定义数据类型的精度. 例如上面第一行代码, 定义了浮点数数据类型的精度为mediump. 精度分为三种类型: lowp、mediump和hightp,分别代表低、中和高精度。

vertex shader中的默认精度是hightp, 这是因为位置信息对精确度要求比较高。

##### uniform

被uniform修饰的变量对于所有的vertexs都是相同的值. u_Color也是一个四维向量, 代表一个颜色值. 为rgba

##### gl_FragColor

作为当前fragment的最终颜色值



## 编译链接Shaders程序

### 加载Shader程序到内存

在我们编译Shader程序之前, 必须先把他们加载到内存

```java
public class ShaderLoader {

    public static String loadShaderFromResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
```

### 编译Shader程序

加载到内存后, 我们就可以开始编译shader程序了.

```java
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    public static final int INVALID_ID = 0;

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = GLES20.glCreateShader(type); //1.创建shader对象
        if (shaderObjectId == INVALID_ID) { //2.检查是否创建成功
            return INVALID_ID;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode); //3.为shader对象设置源代码
        GLES20.glCompileShader(shaderObjectId); //4.编译shader对象
        final int compileStatus[] = new int[1];
        //5.检查编译的结果
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == INVALID_ID) {
            GLES20.glDeleteShader(shaderObjectId); //失败的话, 删除shader对象

            Log.d(TAG, "compileShader: compile fail");
            return INVALID_ID;
        }
        return shaderObjectId; //6.返回shader对象的id
    }
}
```

shader程序的编译可分为6个步骤:

1. 调用GLES20.glCreateShader(type)创建shader对象, 其中type表示shader对象的类型, 有两种选择:1. GL_VERTEX_SHADER, vertex shader对象; 2.GL_FRAGMENT_SHADER, fragment shader对象
2. 创建完对象后, 我们还要检查该对象的id是否是有效, 如果为0说明创建shader对象失败.
3. 成功创建对象后, 需要为对象设置shader代码, GLES20.glShaderSource(shaderObjectId, shaderCode)接收两个参数, 一个是shader对象的id, 另外是需要设置给shader对象的源代码字符串.
4. 设置好源代码, 就可以调用GLES20.glCompileShader(shaderObjectId)方法并传入需要编译的shader对象
5. 最后调用 GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)检查编译结果. 其中第四个参数表示将检查结果写入到compileStatus的offset.

### 链接shader程序

编译完shader程序后, 需要将vertex和fragment shader程序链接成OpenGL程序.

#### 什么是OpenGL 程序

OpenGL程序就是将vertex shader程序和fragment shader程序编译链接后的一个程序. 这两个程序总是成对出现的. 没有fragment shader程序, GPU不知道怎么画每个像素点的颜色; 没有vertex shader程序, GPU, 不知道每个fragment需要被画在屏幕的哪个像素点.

vertex shader计算每个vertex在屏幕的最终位置, 接着OpenGL将vertex组成点、线或者三角形, 然后把他们分割成一个个fragment, 再通过fragment shader设置每个fragment的颜色, 最后一个完成的图像才能在屏幕上显示.

介绍完OpenGL程序后, 我们来开始链接vertex shader和fragment shader

```java
    public static int linkProgram(int vertexShader, int fragmentShader) {
        int programObjectId = GLES20.glCreateProgram(); //1.创建opengl程序对象
        if (programObjectId == INVALID_ID) { //2.检查创建对象是否成功
            Log.d(TAG, "linkProgram: cannt create opengl program!");
            return INVALID_ID;
        }
        GLES20.glAttachShader(programObjectId, vertexShader); //2. 为OpenGL程序设置vertex shader对象
        GLES20.glAttachShader(programObjectId, fragmentShader); //3. 为OpenGL程序设置fragment shader对象
        GLES20.glLinkProgram(programObjectId); //4. 开始链接
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == INVALID_ID) { //5. 检查连接结果
            GLES20.glDeleteProgram(programObjectId);
            Log.d(TAG, "linkProgram: link program error");
            return 0;
        }
        return programObjectId;
    }
```

```java
    class GLRender implements GLSurfaceView.Renderer {

        private int programId;

        private float[] tableVerticesWithTriangles = {
                //triangle 1
                0, 0,
                9f, 14f,
                0, 14f,
                //triangle 2
                0, 0,
                9, 0,
                9, 14,
                //Line 1
                0f, 7f,
                9f, 7f,
                //point 1
                4.5f, 2f,
                //point 2
                4.5f, 12f
        };

        public static final int BYTE_PER_FLOAT = 4;
        public final FloatBuffer vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 0, 0, 0);
            String vertexShaderSource = ShaderLoader.loadShaderFromResource(MainActivity.this, R.raw.simple_vertex_shader);
            String fragmentShaderSource = ShaderLoader.loadShaderFromResource(MainActivity.this, R.raw.simple_fragment_shader);

            int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
            int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
            programId = ShaderHelper.linkProgram(vertexShader, fragmentShader);
            if (BuildConfig.DEBUG) {
                ShaderHelper.validateProgram(programId);
            }
            GLES20.glUseProgram(programId);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }
```

