## 机试前复习与练习建议（基于本项目）

### 1) 基础打牢
- Kotlin 语法与惯用法：作用域函数(let/apply/run/also/with)、空安全、数据类、扩展函数（项目中已有 `extensions/*` 可复习）。
- 协程基础：`launch/async/withContext` 区别、`Dispatchers` 场景。结合项目的 `CoroutineDispatcherProvider` 思考测试替换。
- Android 架构：Activity 生命周期、ViewBinding/DataBinding、Fragment back stack、ViewModel + LiveData/StateFlow。

### 2) 项目骨架理解（对照源码）

#### 2.1 DI：Koin 模块拆分与依赖注入

**示例：新增一个用户列表功能，完整展示 Koin 注入流程**

```kotlin
// 1. 定义数据模型
data class User(val id: Int, val name: String, val email: String)

// 2. 定义 Retrofit API 接口（网络层）
interface UserApi {
    @GET("users")
    suspend fun getUsers(@Query("page") page: Int): List<UserDto>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto
}

data class UserDto(val id: Int, val name: String, val email: String) {
    fun toDomain() = User(id, name, email)
}

// 3. 定义 Repository 接口（领域层）
interface UserRepository {
    suspend fun loadUsers(page: Int): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
}

// 4. Repository 实现（数据层）
class RealUserRepository(
    private val api: UserApi,
    private val dispatchers: CoroutineDispatcherProvider
) : UserRepository {
    override suspend fun loadUsers(page: Int): Result<List<User>> = 
        withContext(dispatchers.io) {
            runCatching { 
                api.getUsers(page).map { it.toDomain() }
            }
        }
    
    override suspend fun getUserById(id: Int): Result<User> = 
        withContext(dispatchers.io) {
            runCatching { api.getUserById(id).toDomain() }
        }
}

// 5. ViewModel（UI 层）
class UserListViewModel(
    private val repository: UserRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val state: StateFlow<UiState<List<User>>> = _state.asStateFlow()
    
    fun loadUsers(page: Int = 1) = viewModelScope.launch(dispatchers.io) {
        _state.value = UiState.Loading
        repository.loadUsers(page)
            .onSuccess { list ->
                _state.value = if (list.isEmpty()) 
                    UiState.Empty 
                else 
                    UiState.Success(list)
            }
            .onFailure { e ->
                _state.value = UiState.Error(e.message ?: "加载失败")
            }
    }
}

// 6. 在 appModule 中注册所有依赖（Koin 模块）
val appModule = module {
    // 注册 API 接口（factory：每次获取新实例）
    factory<UserApi> { 
        get<Retrofit>().create(UserApi::class.java) 
    }
    
    // 注册 Repository（single：单例）
    single<UserRepository> { 
        RealUserRepository(get(), get()) 
    }
    
    // 注册 ViewModel（viewModel：按 Activity/Fragment 生命周期管理）
    viewModel { 
        UserListViewModel(get(), get()) 
    }
}

// 7. 在 Activity/Fragment 中使用（自动注入）
class MainActivity : AppCompatActivity() {
    // 方式1：通过 by inject() 延迟注入
    private val viewModel: UserListViewModel by inject()
    
    // 方式2：通过 get() 直接获取
    // private val viewModel: UserListViewModel = get()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 观察状态变化
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> showUsers(state.data)
                    is UiState.Error -> showError(state.msg)
                    is UiState.Empty -> showEmpty()
                }
            }
        }
        
        // 加载数据
        viewModel.loadUsers()
    }
}
```

**Koin 注入类型说明：**
- `single { }`：单例，整个应用生命周期只有一个实例
- `factory { }`：工厂模式，每次调用 `get()` 都创建新实例（适合 API 接口）
- `viewModel { }`：ViewModel 专用，按 Activity/Fragment 生命周期管理

---

#### 2.2 网络：Retrofit + OkHttp 配置详解

**示例：如何新增 API 服务、添加自定义拦截器、切换环境**

```kotlin
// 1. 定义 API 接口（支持协程）
interface PaymentApi {
    @POST("payments")
    suspend fun createPayment(@Body request: PaymentRequest): PaymentResponse
    
    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: String): PaymentResponse
    
    @GET("payments")
    suspend fun listPayments(
        @Query("page") page: Int,
        @Query("status") status: String?
    ): List<PaymentResponse>
}

// 2. 自定义拦截器：添加认证 Token
class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider.getToken()}")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

// 3. 自定义拦截器：统一错误处理
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        when (response.code) {
            401 -> throw UnauthorizedException("未授权，请重新登录")
            403 -> throw ForbiddenException("无权限访问")
            404 -> throw NotFoundException("资源不存在")
            500 -> throw ServerException("服务器错误")
        }
        return response
    }
}

// 4. 更新 networkModule，添加新的 API 和拦截器
val networkModule = module {
    // 提供 TokenProvider（可以从 SharedPreferences 读取）
    single<TokenProvider> { 
        object : TokenProvider {
            override fun getToken(): String = 
                get<SharedPreferences>().getString("auth_token", "") ?: ""
        }
    }
    
    // 提供认证拦截器
    factory<Interceptor> { AuthInterceptor(get()) }
    
    // 提供错误拦截器
    factory<Interceptor> { ErrorInterceptor() }
    
    // 更新 OkHttpClient，添加自定义拦截器
    factory<OkHttpClient> {
        val cache = providesCache(get())
        val builder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            // 添加认证拦截器（在日志拦截器之前）
            .addInterceptor(get<Interceptor>(named("auth")))
            // 添加错误拦截器
            .addInterceptor(get<Interceptor>(named("error")))
        
        // Debug 模式下添加日志拦截器（最后添加，可以看到完整请求）
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }
        
        builder.build()
    }
    
    // 提供 Retrofit 实例（使用更新后的 OkHttpClient）
    single<Retrofit> {
        providesRetrofit(get(), get(), get())
    }
    
    // 注册 PaymentApi
    factory<PaymentApi> { 
        get<Retrofit>().create(PaymentApi::class.java) 
    }
}

// 5. 切换环境（开发/测试/生产）
// 在 build.gradle.kts 中配置：
android {
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE", "\"https://dev-api.example.com/\"")
        }
        release {
            buildConfigField("String", "API_BASE", "\"https://api.example.com/\"")
        }
    }
}

// 6. 使用协程替代 RxJava2（推荐）
// 如果项目使用协程，可以移除 RxJava2CallAdapterFactory，直接使用 suspend 函数
// Retrofit 原生支持协程，无需额外适配器
```

---

#### 2.3 本地存储：Room + SharedPreferences 完整示例

**示例：Room Dao、Flow 查询、SharedPreferences 使用**

```kotlin
// 1. 定义 Room Entity（数据表）
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

// 2. 定义 Dao（数据访问对象）
@Dao
interface UserDao {
    // 查询所有用户（返回 Flow，自动观察数据库变化）
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun observeAll(): Flow<List<UserEntity>>
    
    // 查询单个用户（Flow）
    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Int): Flow<UserEntity?>
    
    // 普通查询（suspend 函数）
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Int): UserEntity?
    
    // 插入或更新（冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)
    
    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)
    
    // 删除
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    // 清空表
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

// 3. 更新 AppDatabase
@Database(
    entities = [UserEntity::class, ExampleUser::class], // 可以添加多个 Entity
    version = 2, // 版本号，修改表结构时需要递增
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    // 可以添加更多 Dao
}

// 4. 在 storageModule 中注册（注意版本升级）
val storageModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(get(), AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration() // 开发时：直接重建表（生产环境需要 Migration）
            .build()
    }
    
    // 提供 Dao（通过 database 获取）
    factory<UserDao> { 
        get<AppDatabase>().userDao() 
    }
    
    // SharedPreferences（用于存储简单配置）
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(get())
    }
}

// 5. Repository 中使用 Room + SharedPreferences
class UserRepository(
    private val api: UserApi,
    private val dao: UserDao,
    private val prefs: SharedPreferences,
    private val dispatchers: CoroutineDispatcherProvider
) {
    // 缓存优先策略：先读本地，再刷新网络
    fun observeUsers(): Flow<List<User>> = flow {
        // 先发射本地数据
        dao.observeAll().collect { entities ->
            emit(entities.map { it.toDomain() })
        }
    }.flowOn(dispatchers.io)
    
    suspend fun refreshUsers(): Result<List<User>> = withContext(dispatchers.io) {
        runCatching {
            val users = api.getUsers(1).map { it.toDomain() }
            // 保存到本地
            dao.upsertAll(users.map { it.toEntity() })
            users
        }
    }
    
    // 使用 SharedPreferences 存储用户偏好
    fun saveUserPreference(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    fun getUserPreference(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}

// 6. ViewModel 中使用 Flow 观察数据库变化
class UserListViewModel(
    private val repository: UserRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    // 直接观察数据库变化（Room Flow）
    val users: StateFlow<List<User>> = repository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun refresh() = viewModelScope.launch(dispatchers.io) {
        repository.refreshUsers()
    }
}
```

**Room 版本升级示例（Migration）：**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新列
        database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT")
    }
}

// 在 databaseBuilder 中使用
Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

---

#### 2.4 UI 组件：FullScreenView 使用示例

**示例：在列表加载、详情页、错误重试场景中使用**

```kotlin
// 1. 在 Activity/Fragment 布局中添加 FullScreenView
// activity_main.xml
<androidx.constraintlayout.widget.ConstraintLayout>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        ... />
    
    <!-- 全屏加载/错误视图 -->
    <com.gopay.customviews.FullScreenView
        android:id="@+id/fullScreenView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</androidx.constraintlayout.widget.ConstraintLayout>

// 2. 在 Activity 中使用
class MainActivity : AppCompatActivity() {
    private val viewModel: UserListViewModel by inject()
    private lateinit var fullScreenView: FullScreenView
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        fullScreenView = findViewById(R.id.fullScreenView)
        recyclerView = findViewById(R.id.recyclerView)
        
        // 观察状态并更新 UI
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 显示加载动画
                        fullScreenView.show(FullScreenViewType.LoadingView)
                        recyclerView.gone() // 隐藏列表
                    }
                    is UiState.Success -> {
                        // 隐藏加载，显示列表
                        fullScreenView.hide(FullScreenViewType.LoadingView)
                        recyclerView.visible()
                        adapter.submitList(state.data)
                    }
                    is UiState.Error -> {
                        // 显示错误视图
                        fullScreenView.show(FullScreenViewType.ErrorView)
                        recyclerView.gone()
                        // 可以设置错误文本（如果 FullScreenView 支持）
                    }
                    is UiState.Empty -> {
                        fullScreenView.show(FullScreenViewType.ErrorView)
                        recyclerView.gone()
                    }
                }
            }
        }
        
        // 点击重试
        fullScreenView.setOnClickListener {
            viewModel.loadUsers()
        }
    }
}

// 3. 扩展 ViewExtensions 的使用
class UserItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val nameText: TextView = findViewById(R.id.nameText)
    private val avatarImage: ImageView = findViewById(R.id.avatarImage)
    
    fun bind(user: User) {
        nameText.text = user.name
        
        // 使用扩展函数控制可见性
        if (user.avatarUrl.isValid()) {
            avatarImage.visible()
            // 使用项目中注入的 Picasso
            val picasso: Picasso = get()
            picasso.load(user.avatarUrl).into(avatarImage)
        } else {
            avatarImage.gone()
        }
    }
}
```

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
