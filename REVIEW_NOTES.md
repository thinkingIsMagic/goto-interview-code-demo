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

#### 3.1 问题拆解：需求分析与状态设计

**示例：实现一个支付列表功能，展示完整的问题拆解思路**

```kotlin
// 步骤1：需求分析清单
/*
需求：显示用户的支付历史列表

数据源：
  - 远程：GET /api/payments?page=1&status=all
  - 本地：Room 数据库缓存

状态定义：
  - Loading：首次加载、下拉刷新
  - Success：加载成功，显示列表
  - Empty：列表为空
  - Error：网络错误、业务错误

边界条件：
  - 网络断开：显示错误，提供重试
  - 空列表：显示空状态提示
  - 分页加载：第一页、加载更多、刷新
  - 数据过期：本地缓存 + 网络刷新策略

交互流程：
  1. 进入页面 -> Loading -> 先显示本地缓存（如有）-> 请求网络 -> 更新UI
  2. 下拉刷新 -> Loading -> 请求网络 -> 更新UI
  3. 加载更多 -> 追加到列表
  4. 点击重试 -> 重新请求
*/

// 步骤2：定义完整的状态模型
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(
        val message: String,
        val errorType: ErrorType,
        val retryable: Boolean = true
    ) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

enum class ErrorType {
    NETWORK_ERROR,      // 网络连接失败
    TIMEOUT,            // 请求超时
    SERVER_ERROR,       // 服务器错误（5xx）
    CLIENT_ERROR,       // 客户端错误（4xx）
    UNKNOWN_ERROR       // 未知错误
}

// 步骤3：定义业务数据模型
data class Payment(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: PaymentStatus,
    val createdAt: Long,
    val merchantName: String
)

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED, CANCELLED
}

// 步骤4：定义分页数据模型
data class PagedData<T>(
    val items: List<T>,
    val currentPage: Int,
    val hasMore: Boolean,
    val totalCount: Int? = null
)
```

---

#### 3.2 分层架构：清晰的职责分离

**示例：完整的四层架构实现（UI -> ViewModel -> UseCase -> Repository -> DataSource）**

```kotlin
// ========== 第1层：DataSource（数据源层）==========
// 职责：直接与网络/数据库交互，返回原始数据

// 远程数据源
interface PaymentRemoteDataSource {
    suspend fun getPayments(page: Int, status: String?): Result<List<PaymentDto>>
    suspend fun getPaymentById(id: String): Result<PaymentDto>
}

class PaymentRemoteDataSourceImpl(
    private val api: PaymentApi,
    private val dispatchers: CoroutineDispatcherProvider
) : PaymentRemoteDataSource {
    override suspend fun getPayments(page: Int, status: String?): Result<List<PaymentDto>> =
        withContext(dispatchers.io) {
            runCatching { api.listPayments(page, status) }
        }
    
    override suspend fun getPaymentById(id: String): Result<PaymentDto> =
        withContext(dispatchers.io) {
            runCatching { api.getPayment(id) }
        }
}

// 本地数据源
interface PaymentLocalDataSource {
    suspend fun getPayments(): Flow<List<PaymentEntity>>
    suspend fun savePayments(payments: List<PaymentEntity>)
    suspend fun clearPayments()
}

class PaymentLocalDataSourceImpl(
    private val dao: PaymentDao
) : PaymentLocalDataSource {
    override suspend fun getPayments(): Flow<List<PaymentEntity>> = dao.observeAll()
    
    override suspend fun savePayments(payments: List<PaymentEntity>) {
        dao.upsertAll(payments)
    }
    
    override suspend fun clearPayments() {
        dao.deleteAll()
    }
}

// ========== 第2层：Repository（仓储层）==========
// 职责：组合远程和本地数据源，提供统一的数据访问接口

interface PaymentRepository {
    fun observePayments(): Flow<List<Payment>>
    suspend fun refreshPayments(page: Int = 1, status: String? = null): Result<List<Payment>>
    suspend fun loadMorePayments(page: Int, status: String? = null): Result<List<Payment>>
    suspend fun getPaymentById(id: String): Result<Payment>
}

class PaymentRepositoryImpl(
    private val remoteDataSource: PaymentRemoteDataSource,
    private val localDataSource: PaymentLocalDataSource,
    private val dispatchers: CoroutineDispatcherProvider
) : PaymentRepository {
    
    // 观察本地数据库变化（自动更新UI）
    override fun observePayments(): Flow<List<Payment>> =
        localDataSource.getPayments()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    
    // 刷新：先读本地，再请求网络，失败时回退本地
    override suspend fun refreshPayments(page: Int, status: String?): Result<List<Payment>> =
        withContext(dispatchers.io) {
            // 先保存本地数据（如果有）
            val localData = localDataSource.getPayments().firstOrNull().orEmpty()
            
            // 请求网络
            remoteDataSource.getPayments(page, status)
                .onSuccess { remoteData ->
                    // 网络成功：保存到本地并返回
                    localDataSource.savePayments(remoteData.map { it.toEntity() })
                    Result.success(remoteData.map { it.toDomain() })
                }
                .onFailure { error ->
                    // 网络失败：如果有本地数据，返回本地；否则返回错误
                    if (localData.isNotEmpty()) {
                        Result.success(localData.map { it.toDomain() })
                    } else {
                        Result.failure(error)
                    }
                }
        }
    
    override suspend fun loadMorePayments(page: Int, status: String?): Result<List<Payment>> =
        withContext(dispatchers.io) {
            remoteDataSource.getPayments(page, status)
                .map { dtos -> dtos.map { it.toDomain() } }
        }
    
    override suspend fun getPaymentById(id: String): Result<Payment> =
        withContext(dispatchers.io) {
            remoteDataSource.getPaymentById(id)
                .map { it.toDomain() }
        }
}

// ========== 第3层：UseCase（用例层，可选）==========
// 职责：封装业务逻辑，组合多个 Repository 操作

class LoadPaymentsUseCase(
    private val repository: PaymentRepository,
    private val dispatchers: CoroutineDispatcherProvider
) {
    suspend operator fun invoke(
        page: Int = 1,
        status: String? = null,
        isRefresh: Boolean = false
    ): Flow<UiState<PagedData<Payment>>> = flow {
        emit(UiState.Loading)
        
        val result = if (isRefresh) {
            repository.refreshPayments(page, status)
        } else {
            repository.loadMorePayments(page, status)
        }
        
        result.fold(
            onSuccess = { payments ->
                val pagedData = PagedData(
                    items = payments,
                    currentPage = page,
                    hasMore = payments.size >= 20 // 假设每页20条
                )
                emit(
                    if (pagedData.items.isEmpty()) UiState.Empty
                    else UiState.Success(pagedData)
                )
            },
            onFailure = { error ->
                emit(UiState.Error(
                    message = error.message ?: "加载失败",
                    errorType = mapErrorType(error),
                    retryable = true
                ))
            }
        )
    }.flowOn(dispatchers.io)
    
    private fun mapErrorType(error: Throwable): ErrorType {
        return when (error) {
            is java.net.SocketTimeoutException -> ErrorType.TIMEOUT
            is java.net.UnknownHostException -> ErrorType.NETWORK_ERROR
            is retrofit2.HttpException -> {
                when (error.code()) {
                    in 400..499 -> ErrorType.CLIENT_ERROR
                    in 500..599 -> ErrorType.SERVER_ERROR
                    else -> ErrorType.UNKNOWN_ERROR
                }
            }
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
}

// ========== 第4层：ViewModel（视图模型层）==========
// 职责：管理UI状态，处理用户交互

class PaymentListViewModel(
    private val loadPaymentsUseCase: LoadPaymentsUseCase,
    private val repository: PaymentRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    
    // UI 状态
    private val _uiState = MutableStateFlow<UiState<PagedData<Payment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<PagedData<Payment>>> = _uiState.asStateFlow()
    
    // 当前分页信息
    private var currentPage = 1
    private var hasMore = true
    
    // 观察本地数据变化（实时更新列表）
    val payments: StateFlow<List<Payment>> = repository.observePayments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadPayments(page = 1, isRefresh = true)
    }
    
    // 加载支付列表
    fun loadPayments(page: Int = 1, isRefresh: Boolean = false) {
        if (!isRefresh && !hasMore) return // 没有更多数据
        
        viewModelScope.launch(dispatchers.io) {
            loadPaymentsUseCase(page, null, isRefresh)
                .collect { state ->
                    _uiState.value = state
                    
                    // 更新分页信息
                    if (state is UiState.Success) {
                        currentPage = state.data.currentPage
                        hasMore = state.data.hasMore
                    }
                }
        }
    }
    
    // 刷新
    fun refresh() {
        currentPage = 1
        hasMore = true
        loadPayments(page = 1, isRefresh = true)
    }
    
    // 加载更多
    fun loadMore() {
        if (hasMore && _uiState.value !is UiState.Loading) {
            loadPayments(page = currentPage + 1, isRefresh = false)
        }
    }
    
    // 重试
    fun retry() {
        loadPayments(page = currentPage, isRefresh = true)
    }
}

// ========== 第5层：UI层（Activity/Fragment）==========
// 职责：展示UI，响应用户操作

class PaymentListActivity : AppCompatActivity() {
    private val viewModel: PaymentListViewModel by inject()
    private lateinit var fullScreenView: FullScreenView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_list)
        
        fullScreenView = findViewById(R.id.fullScreenView)
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        
        setupRecyclerView()
        observeState()
        observePayments()
        setupSwipeRefresh()
    }
    
    private fun setupRecyclerView() {
        val adapter = PaymentAdapter { payment ->
            // 点击跳转详情
            startActivity(Intent(this, PaymentDetailActivity::class.java).apply {
                putExtra("payment_id", payment.id)
            })
        }
        recyclerView.adapter = adapter
        
        // 滚动到底部加载更多
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMore()
                }
            }
        })
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        if (payments.value.isEmpty()) {
                            fullScreenView.show(FullScreenViewType.LoadingView)
                            recyclerView.gone()
                        }
                        swipeRefresh.isRefreshing = true
                    }
                    is UiState.Success -> {
                        fullScreenView.hide(FullScreenViewType.LoadingView)
                        fullScreenView.hide(FullScreenViewType.ErrorView)
                        recyclerView.visible()
                        swipeRefresh.isRefreshing = false
                    }
                    is UiState.Error -> {
                        swipeRefresh.isRefreshing = false
                        if (payments.value.isEmpty()) {
                            fullScreenView.show(FullScreenViewType.ErrorView)
                            recyclerView.gone()
                            // 设置重试按钮点击事件
                            fullScreenView.setOnClickListener { viewModel.retry() }
                        } else {
                            // 有数据时显示 Toast
                            Toast.makeText(
                                this@PaymentListActivity,
                                "加载失败: ${state.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is UiState.Empty -> {
                        fullScreenView.show(FullScreenViewType.ErrorView)
                        recyclerView.gone()
                        swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }
    
    // 观察本地数据变化（实时更新列表）
    private fun observePayments() {
        lifecycleScope.launch {
            viewModel.payments.collect { payments ->
                (recyclerView.adapter as? PaymentAdapter)?.submitList(payments)
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
}
```

**分层优势说明：**
- **易于测试**：每层都可以独立测试，使用 Fake 实现替换
- **易于替换**：DataSource 可以轻松切换（如从 Retrofit 切换到 GraphQL）
- **职责清晰**：每层只做自己的事情，不越界
- **易于扩展**：新增功能只需在对应层添加代码

---

#### 3.3 状态管理：StateFlow + Sealed Class 最佳实践

**示例：复杂场景的状态管理（列表 + 详情 + 搜索 + 筛选）**

```kotlin
// 1. 定义复合状态（多个状态组合）
data class PaymentListState(
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ErrorState? = null,
    val filter: PaymentFilter = PaymentFilter.ALL,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val hasMore: Boolean = true
) {
    val isEmpty: Boolean
        get() = payments.isEmpty() && !isLoading && error == null
    
    val showError: Boolean
        get() = error != null && payments.isEmpty()
}

data class ErrorState(
    val message: String,
    val type: ErrorType,
    val retryable: Boolean = true
)

enum class PaymentFilter {
    ALL, PENDING, SUCCESS, FAILED
}

// 2. ViewModel 中使用单一 StateFlow 管理所有状态
class PaymentListViewModel(
    private val repository: PaymentRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    
    private val _state = MutableStateFlow(PaymentListState())
    val state: StateFlow<PaymentListState> = _state.asStateFlow()
    
    // 只暴露 UI 需要的派生状态
    val isEmpty: StateFlow<Boolean> = _state
        .map { it.isEmpty }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val showError: StateFlow<Boolean> = _state
        .map { it.showError }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 加载数据
    fun loadPayments(isRefresh: Boolean = false) {
        val page = if (isRefresh) 1 else _state.value.currentPage
        
        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(isLoading = true, error = null) }
            
            repository.refreshPayments(page, _state.value.filter.name)
                .onSuccess { payments ->
                    _state.update {
                        it.copy(
                            payments = if (isRefresh) payments else it.payments + payments,
                            isLoading = false,
                            currentPage = page,
                            hasMore = payments.size >= 20,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorState(
                                message = error.message ?: "加载失败",
                                type = mapErrorType(error),
                                retryable = true
                            )
                        )
                    }
                }
        }
    }
    
    // 加载更多
    fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return
        
        viewModelScope.launch(dispatchers.io) {
            _state.update { it.copy(isLoadingMore = true) }
            
            repository.loadMorePayments(_state.value.currentPage + 1, _state.value.filter.name)
                .onSuccess { payments ->
                    _state.update {
                        it.copy(
                            payments = it.payments + payments,
                            isLoadingMore = false,
                            currentPage = it.currentPage + 1,
                            hasMore = payments.size >= 20
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            error = ErrorState(error.message ?: "加载失败", mapErrorType(error))
                        )
                    }
                }
        }
    }
    
    // 筛选
    fun setFilter(filter: PaymentFilter) {
        _state.update { it.copy(filter = filter, currentPage = 1) }
        loadPayments(isRefresh = true)
    }
    
    // 搜索（防抖）
    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        // 使用 Flow 实现防抖搜索（见下方）
    }
    
    private fun mapErrorType(error: Throwable): ErrorType {
        return when (error) {
            is SocketTimeoutException -> ErrorType.TIMEOUT
            is UnknownHostException -> ErrorType.NETWORK_ERROR
            is HttpException -> {
                when (error.code()) {
                    in 400..499 -> ErrorType.CLIENT_ERROR
                    in 500..599 -> ErrorType.SERVER_ERROR
                    else -> ErrorType.UNKNOWN_ERROR
                }
            }
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
}

// 3. UI 层观察状态
class PaymentListActivity : AppCompatActivity() {
    private val viewModel: PaymentListViewModel by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_list)
        
        observeState()
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // 更新列表
                updateList(state.payments)
                
                // 更新加载状态
                if (state.isLoading) {
                    fullScreenView.show(FullScreenViewType.LoadingView)
                } else {
                    fullScreenView.hide(FullScreenViewType.LoadingView)
                }
                
                // 更新错误状态
                state.error?.let { error ->
                    if (state.payments.isEmpty()) {
                        fullScreenView.show(FullScreenViewType.ErrorView)
                        // 设置重试
                        fullScreenView.setOnClickListener { viewModel.loadPayments(isRefresh = true) }
                    }
                } ?: run {
                    fullScreenView.hide(FullScreenViewType.ErrorView)
                }
                
                // 更新空状态
                if (state.isEmpty) {
                    showEmptyState()
                }
            }
        }
        
        // 观察派生状态
        lifecycleScope.launch {
            viewModel.isEmpty.collect { isEmpty ->
                emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        }
    }
}
```

---

#### 3.4 错误处理：分类处理与用户友好提示

**示例：完整的错误处理策略**

```kotlin
// 1. 错误分类与映射
sealed class AppError : Exception() {
    data class NetworkError(val cause: Throwable) : AppError()
    data class TimeoutError(val timeout: Long) : AppError()
    data class ServerError(val code: Int, val message: String) : AppError()
    data class ClientError(val code: Int, val message: String) : AppError()
    data class UnknownError(val cause: Throwable) : AppError()
}

// 2. Repository 层错误转换
class PaymentRepositoryImpl(
    private val remoteDataSource: PaymentRemoteDataSource,
    private val localDataSource: PaymentLocalDataSource,
    private val dispatchers: CoroutineDispatcherProvider
) : PaymentRepository {
    
    override suspend fun refreshPayments(page: Int, status: String?): Result<List<Payment>> =
        withContext(dispatchers.io) {
            runCatching {
                remoteDataSource.getPayments(page, status)
                    .getOrElse { throw mapToAppError(it) }
                    .map { it.toDomain() }
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(mapToAppError(it)) }
            )
        }
    
    private fun mapToAppError(error: Throwable): AppError {
        return when (error) {
            is SocketTimeoutException -> AppError.TimeoutError(10000)
            is UnknownHostException -> AppError.NetworkError(error)
            is HttpException -> {
                when (error.code()) {
                    in 400..499 -> AppError.ClientError(error.code(), error.message())
                    in 500..599 -> AppError.ServerError(error.code(), error.message())
                    else -> AppError.UnknownError(error)
                }
            }
            else -> AppError.UnknownError(error)
        }
    }
}

// 3. ViewModel 层错误处理与用户提示
class PaymentListViewModel(
    private val repository: PaymentRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<PagedData<Payment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<PagedData<Payment>>> = _state.asStateFlow()
    
    // 副作用：用于导航、显示 Toast 等一次性事件
    private val _sideEffect = Channel<SideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<SideEffect> = _sideEffect.receiveAsFlow()
    
    sealed class SideEffect {
        data class ShowToast(val message: String) : SideEffect()
        data class NavigateToDetail(val paymentId: String) : SideEffect()
    }
    
    fun loadPayments() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.value = UiState.Loading
            
            repository.refreshPayments(1, null)
                .onSuccess { payments ->
                    _uiState.value = if (payments.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(PagedData(payments, 1, true))
                    }
                }
                .onFailure { error ->
                    val (message, retryable) = when (error) {
                        is AppError.NetworkError -> {
                            "网络连接失败，请检查网络设置" to true
                        }
                        is AppError.TimeoutError -> {
                            "请求超时，请稍后重试" to true
                        }
                        is AppError.ServerError -> {
                            "服务器错误，请稍后重试" to true
                        }
                        is AppError.ClientError -> {
                            when (error.code) {
                                401 -> {
                                    _sideEffect.send(SideEffect.ShowToast("登录已过期，请重新登录"))
                                    "未授权访问" to false
                                }
                                403 -> "无权限访问" to false
                                404 -> "资源不存在" to false
                                else -> "请求失败" to true
                            }
                        }
                        else -> "未知错误" to true
                    }
                    
                    _uiState.value = UiState.Error(
                        message = message,
                        errorType = mapErrorType(error),
                        retryable = retryable
                    )
                }
        }
    }
    
    fun retry() {
        if (_uiState.value is UiState.Error && 
            (_uiState.value as UiState.Error).retryable) {
            loadPayments()
        }
    }
    
    private fun mapErrorType(error: Throwable): ErrorType {
        return when (error) {
            is AppError.NetworkError -> ErrorType.NETWORK_ERROR
            is AppError.TimeoutError -> ErrorType.TIMEOUT
            is AppError.ServerError -> ErrorType.SERVER_ERROR
            is AppError.ClientError -> ErrorType.CLIENT_ERROR
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
}

// 4. UI 层处理错误与副作用
class PaymentListActivity : AppCompatActivity() {
    private val viewModel: PaymentListViewModel by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_list)
        
        observeState()
        observeSideEffect()
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Error -> {
                        fullScreenView.show(FullScreenViewType.ErrorView)
                        errorText.text = state.message
                        
                        // 根据错误类型显示不同的重试按钮
                        if (state.retryable) {
                            retryButton.visible()
                            retryButton.setOnClickListener { viewModel.retry() }
                        } else {
                            retryButton.gone()
                        }
                    }
                    // ... 其他状态处理
                }
            }
        }
    }
    
    private fun observeSideEffect() {
        lifecycleScope.launch {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is SideEffect.ShowToast -> {
                        Toast.makeText(this@PaymentListActivity, effect.message, Toast.LENGTH_SHORT).show()
                    }
                    is SideEffect.NavigateToDetail -> {
                        startActivity(Intent(this@PaymentListActivity, PaymentDetailActivity::class.java).apply {
                            putExtra("payment_id", effect.paymentId)
                        })
                    }
                }
            }
        }
    }
}
```

---

#### 3.5 并发与取消：协程生命周期管理

**示例：正确处理并发、取消、重试**

```kotlin
class PaymentListViewModel(
    private val repository: PaymentRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {
    
    // 当前正在执行的加载任务
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    
    // 加载支付列表（支持取消）
    fun loadPayments() {
        // 取消之前的加载任务
        loadJob?.cancel()
        
        loadJob = viewModelScope.launch(dispatchers.io) {
            try {
                _uiState.value = UiState.Loading
                val payments = repository.refreshPayments(1, null).getOrThrow()
                _uiState.value = UiState.Success(PagedData(payments, 1, true))
            } catch (e: CancellationException) {
                // 协程被取消，不需要处理（这是正常的）
                throw e
            } catch (e: Exception) {
                // 其他错误
                if (isActive) { // 确保协程未被取消
                    _uiState.value = UiState.Error(e.message ?: "加载失败", ErrorType.UNKNOWN_ERROR)
                }
            }
        }
    }
    
    // 搜索（防抖 + 取消之前的搜索）
    fun search(query: String) {
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch(dispatchers.io) {
            delay(300) // 防抖：300ms
            
            if (!isActive) return@launch // 检查是否被取消
            
            try {
                val results = repository.searchPayments(query).getOrThrow()
                _uiState.value = UiState.Success(PagedData(results, 1, false))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = UiState.Error(e.message ?: "搜索失败", ErrorType.UNKNOWN_ERROR)
                }
            }
        }
    }
    
    // 并行加载多个数据源
    fun loadPaymentDetail(paymentId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                // 使用 async 并行加载
                val paymentDeferred = async { repository.getPaymentById(paymentId) }
                val relatedPaymentsDeferred = async { repository.getRelatedPayments(paymentId) }
                
                // 等待所有结果
                val payment = paymentDeferred.await().getOrThrow()
                val relatedPayments = relatedPaymentsDeferred.await().getOrThrow()
                
                _uiState.value = UiState.Success(
                    PaymentDetailData(payment, relatedPayments)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = UiState.Error(e.message ?: "加载失败", ErrorType.UNKNOWN_ERROR)
                }
            }
        }
    }
    
    // 带重试的加载
    fun loadWithRetry(maxRetries: Int = 3) {
        viewModelScope.launch(dispatchers.io) {
            var retryCount = 0
            var lastError: Throwable? = null
            
            while (retryCount < maxRetries && isActive) {
                try {
                    val payments = repository.refreshPayments(1, null).getOrThrow()
                    _uiState.value = UiState.Success(PagedData(payments, 1, true))
                    return@launch // 成功，退出循环
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    retryCount++
                    
                    if (retryCount < maxRetries) {
                        delay(1000 * retryCount) // 指数退避：1s, 2s, 3s
                    }
                }
            }
            
            // 所有重试都失败
            if (isActive) {
                _uiState.value = UiState.Error(
                    lastError?.message ?: "加载失败",
                    ErrorType.UNKNOWN_ERROR,
                    retryable = true
                )
            }
        }
    }
    
    // 在 ViewModel 清理时取消所有任务（viewModelScope 会自动处理）
    override fun onCleared() {
        super.onCleared()
        // viewModelScope 会自动取消所有协程，但也可以手动取消
        loadJob?.cancel()
        searchJob?.cancel()
    }
}

// Activity/Fragment 中使用 lifecycleScope（自动取消）
class PaymentListActivity : AppCompatActivity() {
    private val viewModel: PaymentListViewModel by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_list)
        
        // lifecycleScope 会在 Activity 销毁时自动取消
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                // 处理状态
            }
        }
        
        // 或者使用 repeatOnLifecycle（推荐，更精确的生命周期控制）
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // 只在 STARTED 状态时收集
                }
            }
        }
    }
}
```

**协程取消最佳实践：**
- 使用 `viewModelScope`：ViewModel 销毁时自动取消
- 使用 `lifecycleScope`：Activity/Fragment 销毁时自动取消
- 检查 `isActive`：在长时间运行的任务中定期检查
- 捕获 `CancellationException`：正确处理取消异常
- 使用 `repeatOnLifecycle`：精确控制收集时机

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
