# FastStartup
`FastStartup`是一个组件启动框架，旨在帮助开发人员能够简单、高效地进行各种组件的初始化操作。

## 特点
简单、高效、支持节藕。
没有过多的启动配置方式，只为提供简单的使用体验。
全程无反射，无IO操作，能给您提供最快的启动速度。

## 使用它你能得到什么？
1.  全程无反射，无IO操作，提供最快的启动速度
2.  组件只需要关注自己依赖的其他组件，自动维护初始化顺序
3.  组件支持配置运行在UI线程和非UI线程
4.  支持UI线程等待操作，可以让UI线程阻塞到必要组件初始化完成，而必要组件可以运行在任意线程
5.  支持组件初始化参数配置，您可以创建一个任意形式配置信息，该配置信息将贯穿所有组件的初始化操作
6.  支持节藕，采用接口进行依赖管理，避免组件间的强依赖，让您的工程更加简洁
7.  支持组件自动注入，提供AOP方案，让您无需再处理每一个组件，只需要一个注解就可以自动进行初始化，实现依赖即配置
8.  支持组件完成回调，提供了三种回调，分别为每个组件初始化完成的回调、UI线程任务完成的回调、所有任务完成的回调
9.  支持组件初始化耗时统计
10. 支持依赖缺失检测和依赖循环依赖检测

## 使用方式
### 1. 基础配置
在项目工程目录下`build.gradle`中添加依赖
```
dependencies {
    implementation "io.github.wizzxu:fast-startup:0.0.1"
}
```
### 2. 组件初始化
在`Application onCreat()`方法中进行初始化和启动
```
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FastStartup.init().start()
    }
}
```
在`init()`方法中需要传入配置信息
```
简单配置
FastStartup.init(StartupConfig(application = this, BuildConfig.DEBUG)).start(listOf(A(), B()))
详细配置
FastStartup.init(
    StartupConfig.Builder()
        .setApplication(this)                    //application (默认为空)
        .setIsDebug(BuildConfig.DEBUG)           //是否是debug (默认为空)
        .setParams(mapOf("key" to "value"))      //通用配置参数 (默认为空)
        .setEnableTimeStatistics(true)           //是否打印每一个startup启动耗时(需要日志级别Log.ERROR以下，默认不分析耗时)
        .setLogLevel(Log.DEBUG)                  //组件内打印的日志级别 (默认不打印)
        .setStartupCompleteListener(object : StartupCompleteListener {
            override fun startupComplete(startup: IStartup<*>) {
                SLog.d("FastStartup", "目前完成的startup为:" + startup::class.java.simpleName)
            }

        })
        .setUIStartupCompleteListener(object : UIStartupCompleteListener {
            override fun startupComplete() {
                SLog.d("FastStartup", "所有运行在UI线程和需要UI线程等待的Startup已经全部完成")
            }
        })
        .setAllStartupCompleteListener(object : AllStartupCompleteListener {
            override fun startupComplete() {
                SLog.d("FastStartup", "所有Startup已经全部完成")
            }
        })
        .build()
).start(listOf(A(), B()))
```

在`start()`方法中需要传入组件列表信息
组件需要实现`IStartup`接口，具体详细用法，请查看[`IStartup`](https://github.com/WizzXu/FastStartup/blob/main/fast-startup-api/src/main/java/io/github/fast_startup/IStartup.kt)

```
class A : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }
}

class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(A::class.java)
    }
}
```
### 3. 获取组件实例对象获取获取组件初始化返回的结果
```
获取组件实例对象
val class1:A? = FastStartup.getStartup(A::class.java)
val class2:IA<*>? = FastStartup.getStartup(A::class.java)
val class3:IA<*>? = FastStartup.getStartup(IA::class.java)

获取组件`start`方法返回的结果
Log.d("TestGetResult", "${FastStartup.getStartupResult(A::class.java)}")
Log.d("TestGetResult", "${FastStartup.getStartupResult(IA::class.java)}")
```
至此，您已经可以愉快的使用`FastStartup`了

## 依赖环检测和缺失检测
在FastStartup启动的时候会自动进行依赖环检测和缺失检测
如果依赖有环，会抛出异常并会打印如下信息
![dep_pic_1](https://github.com/WizzXu/FastStartup/blob/main/pic/dep_pic_1.png?raw=true)

如果依赖有缺失，会打印如下消息
![dep_pic_2](https://github.com/WizzXu/FastStartup/blob/main/pic/dep_pic_2.png?raw=true)

## 耗时统计
![cost_time](https://github.com/WizzXu/FastStartup/blob/main/pic/cost_time.png?raw=true)


# FastStartup 高级用法 节藕、AOP

## 1. 节藕
有的时候，我们组件是节藕的，对外提供接口，靠接口来进行依赖和调用，这种方式在FastStartup要怎么使用呢？
#### 1. 组件工程添加`IStartup`组件依赖
```
dependencies {
    implementation "io.github.wizzxu:fast-startup-api:0.0.1"
}
```
组件接口必须直接继承`IStartup`
```
interface IA<T> : IStartup<T>
class A : IA<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }
}

class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(IA::class.java)
    }
}
```
这样在B组件中就可以依赖A组件的接口IA完成依赖关系。从而B组件不必直接依赖A组件，只需要依赖A组件的接口所在工程就可以完成依赖。
但是`FastStartup`调用`start`方法的时候必须添加所有组件的实现类
```
FastStartup.init(StartupConfig(application = this, BuildConfig.DEBUG)).start(listOf(A(), B()))
```

## 2. AOP方式进行组件初始化
如果您的组件分布在不同仓库，而您不想在初始化的时候去统一添加这些组件，那么您可以通过使用AOP插件来实现。
AOP方案的实现基于[Booster](https://github.com/didi/booster)实现
### 1. 配置
在项目根目录下`build.gradle`中添加依赖
```
buildscript {
    ext.kotlin_version = "1.5.31"
    ext.booster_version = '4.0.0'
    ext.fast_startup_transformer = '0.0.1'

    dependencies {
        // 添加booster插件
        classpath "com.didiglobal.booster:booster-gradle-plugin:$booster_version"
        // 添加fast-startup-transformer依赖
        classpath "io.github.wizzxu:fast-startup-transformer:$fast_startup_transformer"
    }
}
```
在项目目录下`build.gradle`中添加插件
```
plugins {
    id 'com.didiglobal.booster'
}
```
### 2. 组件添加注解 `@AFastStartup`
`@AFastStartup`注解在`"io.github.wizzxu:fast-startup-api:0.0.1"`库中
```
interface IA<T> : IStartup<T>
@AFastStartup
class A : IA<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return null
    }
}
@AFastStartup
class B : IStartup<String> {
    override fun start(context: Context?, isDebug: Boolean?, any: Any?): String? {
        return null
    }

    override fun runOnUIThread(): Boolean {
        return true
    }

    override fun dependencies(): List<Class<out IStartup<*>>>? {
        return listOf(IA::class.java)
    }
}
```
配置完毕，现在您就可以摆脱手动添加组件啦，在启动的时候
```
FastStartup.init(StartupConfig(application = this, BuildConfig.DEBUG)).start())
```
`start()`方法里面不需要传入任何内容，就可以自动注入组件啦



## 感谢
在开发的过程中也是参考了和借鉴了部分其他开源库，在此特感谢各位大佬。  

[【Booster】](https://github.com/didi/booster) 一个优秀的AOP解决方案  
[【android-startup】](https://github.com/idisfkj/android-startup) 一个优秀的Android启动方案，本组件的很多实现细节有参考`android-startup`，但是设计理念有些不同，并且提供了节藕方案、ASM插桩自动注册方案，去除了`android-startup`里面很多复杂的配置方式


## License
请查看[LICENSE](https://github.com/WizzXu/FastStartup/blob/main/LICENSE)。