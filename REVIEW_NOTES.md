## 机试前复习与练习建议（基于本项目）

### 1) 基础打牢
- Kotlin 语法与惯用法：作用域函数(let/apply/run/also/with)、空安全、数据类、扩展函数（项目中已有 `extensions/*` 可复习）。
- 协程基础：`launch/async/withContext` 区别、`Dispatchers` 场景。结合项目的 `CoroutineDispatcherProvider` 思考测试替换。
- Android 架构：Activity 生命周期、ViewBinding/DataBinding、Fragment back stack、ViewModel + LiveData/StateFlow。

### 2) 项目骨架理解（对照源码）
- DI：Koin 模块拆分（`appModule/dispatcherModule/networkModule/imageModule/storageModule`），会写单例/factory，知道如何在 Activity/ViewModel 中注入。
- 网络：Retrofit + OkHttp 配置，如何新增接口服务、添加超时/拦截器、切换 baseUrl（`BuildConfig.API_BASE`）。RxJava2 适配器已配置，若改用协程可替换为 `CoroutineCallAdapter` 或直接返回 `suspend`.
- 本地存储：Room 示例 `AppDatabase`，会新增 `Dao`，掌握 `suspend`/Flow 查询；SharedPreferences 用途与局限。
- UI 组件：`FullScreenView` 的 show/hide 逻辑，如何在列表/整页加载时复用；`ViewExtensions` 控制可见性。

### 3) 设计与分层练习（匹配考核点）
- 问题拆解：拿到需求先列数据源、状态、边界条件（加载/空/错误）、缓存与刷新策略。
- 分层建议：UI(Compose/Activity/Fragment) -> ViewModel -> UseCase/Repository -> DataSource(Remote/Local)。清晰接口定义，易于替换/mock。
- 状态管理：为列表或详情定义 `sealed class UiState`（Loading/Success/Error/Empty）；使用 LiveData/StateFlow 驱动 UI。
- 错误处理：网络错误分类（超时、无网、业务错误），对应到 UI 的提示与重试按钮，结合 `FullScreenView`。
- 并发与取消：使用 `viewModelScope`/`lifecycleScope`，合理选择 `Dispatchers`（IO/Default/Main），注意生命周期取消。

### 4) 可快速演示的练习题方向
- 列表 + 详情：调用示例 API（可 mock），列表加载、下拉刷新、空态/错误态、点击进入详情。
- 表单 + 校验：提交前校验、提交中 loading、失败重试、成功后的 UI 更新。
- 缓存策略：先读本地(Room/SP)，再网络刷新；失败回退本地。
- 图片加载：使用 Picasso（或切换到 Coil/Glide）演示占位图、错误图、缓存策略。

### 5) 编码习惯与测试
- 提前准备：常用扩展函数、`Result` 封装、`UiState` 模板；写好简单的 `FakeRepository` 方便面试时 mock。
- 单元测试：为 Repository/UseCase 写协程测试，注入 TestDispatcher（接口已在项目中）；验证状态流转与错误分支。
- 日志与调试：OkHttp logging 只在 DEBUG；注意泄漏与异常的捕获位置。

### 6) 沟通与展示
- 开始前复述需求与边界；说明分层设计与取舍。
- 边写边解释：为什么用 sealed state、为什么此处用 IO dispatcher、重试/取消策略。
- 时间不够时：先把 Happy Path 跑通，再补错误态/空态，最后提到剩余风险与 TODO。

### 7) 常见题型与代码片段
- 列表 + 状态管理：定义通用状态 `UiState`，ViewModel 拉取数据后驱动 UI。
```
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val msg: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
```
- 协程加载 + 空/错态：结合 `CoroutineDispatcherProvider` 和 Room/Retrofit。
```
fun load(page: Int = 1) = viewModelScope.launch(dispatchers.io) {
    _state.value = UiState.Loading
    repo.loadPage(page)
        .onSuccess { list -> _state.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list) }
        .onFailure { e -> _state.value = UiState.Error(e.message ?: "加载失败") }
}
```
- 输入防抖搜索（Flow）：
```
val query = MutableStateFlow("")
val results = query
    .debounce(300)
    .filter { it.length >= 2 }
    .flatMapLatest { key -> flow { emit(repo.search(key)) } }
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```
- 表单校验 + 提交：
```
fun submit() = viewModelScope.launch(dispatchers.io) {
    if (!email.isValidEmail()) { _state.update { it.copy(error = "邮箱格式不正确") }; return@launch }
    _state.update { it.copy(submitting = true, error = null) }
    repo.login(email, password)
        .onSuccess { _sideEffect.emit(NavigateHome) }
        .onFailure { e -> _state.update { it.copy(submitting = false, error = e.message) } }
}
```
- 并行请求（详情 + 关联）：
```
viewModelScope.launch(dispatchers.io) {
    val detail = async { repo.detail(id) }
    val related = async { repo.related(id) }
    runCatching { detail.await() to related.await() }
        .onSuccess { (d, r) -> _state.value = UiState.Success(d to r) }
        .onFailure { e -> _state.value = UiState.Error(e.message ?: "加载失败") }
}
```
- 缓存优先：先读 Room/SP，再远端刷新，失败回退本地。
```
val local = dao.getAll()
if (local.isNotEmpty()) _state.value = UiState.Success(local)
repo.fetch().onSuccess { list -> dao.replaceAll(list); _state.value = UiState.Success(list) }
    .onFailure { e -> if (local.isEmpty()) _state.value = UiState.Error("网络失败: ${e.message}") }
```
- 图片加载占位/错误图（Picasso，换 Coil/Glide 也同理）：
```
Picasso.get().load(url)
    .placeholder(R.drawable.ic_placeholder)
    .error(R.drawable.ic_error)
    .into(imageView)
```
- 重试/取消（协程）：
```
private var currentJob: Job? = null
fun reload() {
    currentJob?.cancel()
    currentJob = viewModelScope.launch(dispatchers.io) {
        repo.loadWithRetry(times = 3)
    }
}
```
