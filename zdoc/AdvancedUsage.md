# FastStartup 高级用法

## 1. 节藕
有的时候，我们组件是节藕的，对外提供接口，靠接口来进行依赖和调用，这种方式在FastStartup要怎么使用呢？
#### 1. 组件工程添加`IStartup`组件依赖
```
dependencies {
    implementation "io.github.wizzxu:fast-startup-api:0.0.1-SNAPSHOT"
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
    ext.fast_startup_transformer = '0.0.1-SNAPSHOT'

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
`@AFastStartup`注解在`"io.github.wizzxu:fast-startup-api:0.0.1-SNAPSHOT"`库中
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
