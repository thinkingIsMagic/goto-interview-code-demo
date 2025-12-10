## 项目概览
- 这是一个用于机试练习的 Android Kotlin 示例项目，提供基础骨架（依赖注入、网络、图片、Room、本地存储、全屏加载/错误组件），方便在面试时快速补齐业务。

## 技术栈
- 语言：Kotlin
- DI：Koin
- 网络：Retrofit + OkHttp + Gson + RxJava2 CallAdapter
- 图片：Picasso（可替换为 Coil/Glide）
- 本地：Room、SharedPreferences
- 协程：Kotlin Coroutines，封装 DispatcherProvider

## 模块拆分（Koin Modules）
- `appModule`：预留业务依赖声明位置（Repository/UseCase 等）。
- `dispatcherModule`：提供 `CoroutineDispatcherProvider`，方便测试替换调度器。
- `networkModule`：Retrofit/OkHttp/Gson 及缓存、日志配置。
- `imageModule`：Picasso 单例，复用 OkHttp 下载器。
- `storageModule`：SharedPreferences 与 Room 数据库实例。

## 代码骨架
- `App.kt`：Application，启动时注册 Koin 模块。
- `MainActivity.kt`：入口 Activity，当前只加载 `activity_main` 布局，可扩展导航或 UI。
- `customviews/FullScreenView`：可复用的全屏 Loading/错误视图（Lottie + 文案）。
- `extensions/*`：常用扩展函数（可见性、LiveData、字符串、数值格式化）。
- `persistance/AppDatabase`：Room 示例实体与数据库定义，后续可添加 Dao。
- `dispatcher/*`：调度器接口与默认实现，测试用例占位。

## 运行与配置
- 基于 Gradle 构建，`BuildConfig.API_BASE` 控制网络基地址。
- assets 中包含示例 Lottie 资源，可直接使用于加载/错误场景。

## 面试可扩展点
- 在 `appModule` 中注册业务层依赖，`MainActivity` 衔接 UI。
- 添加 Retrofit 接口与 Repository/UseCase，利用 `FullScreenView` 管理加载/错误态。
- 使用 `CoroutineDispatcherProvider` 便于单元测试调度。

