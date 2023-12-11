package grin.app

import com.alibaba.druid.filter.Filter
import com.alibaba.druid.filter.logging.Slf4jLogFilter
import com.alibaba.druid.filter.stat.StatFilter
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.sql.SQLUtils
import grin.datastore.DB
import grin.datastore.DDL
import grin.datastore.Entity
import grin.web.Interceptor
import grin.web.Route
import grin.web.Template
import grin.web.WebUtils
import groovy.json.JsonGenerator
import groovy.json.StreamingJsonBuilder
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Method

/**
 * Grin App
 * 定义规约目录等。
 */
@Slf4j
class App {
    // env
    public static final String ENV_PROD = 'prod'
    public static final String ENV_DEV = 'dev'
    public static final String GRIN_ENV_NAME = 'GRIN_ENV'
    public static final List<String> GRIN_ENV_LIST = [ENV_DEV, ENV_PROD]
    // 目录结构
    public static final String APP_DIR = 'grin-app'
    public static final String APP_DOMAINS = 'domains'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_WEBSOCKETS = 'websockets'
    public static final String APP_VIEWS = 'views'
    public static final String APP_CONFIG = 'conf'
    public static final String APP_INIT = 'init'
    public static final String APP_ASSETS = 'assets'
    public static final String APP_STATIC = 'static'
    public static final String APP_SCRIPTS = 'scripts'

    String environment
    File projectDir, appDir, domainsDir, controllersDir, websocketsDir, viewsDir, configDir, initDir, assetDir, staticDir, scriptDir
    List<File> allDirs

    ConfigObject config

    // 一些延迟初始化的属性
    private static App _instance
    private GroovyScriptEngine _scriptEngine
    private JsonGenerator _jsonGenerator
    private Template _template

    // web 组件
    Map<String, String> controllers = [:]
    Map<String, Method> actions = [:]
    Interceptor interceptor
    List<Class> websockets = []
    List<Route> routes = []

    /**
     * 构造并初始化
     * @param projectRoot
     */
    private App(File projectRoot = null, String env = null) {
        // init dirs
        projectDir = projectRoot ?: new File('.').getCanonicalFile()
        appDir = new File(projectDir, APP_DIR)
        domainsDir = new File(appDir, APP_DOMAINS)
        controllersDir = new File(appDir, APP_CONTROLLERS)
        websocketsDir = new File(appDir, APP_WEBSOCKETS)
        viewsDir = new File(appDir, APP_VIEWS)
        configDir = new File(appDir, APP_CONFIG)
        initDir = new File(appDir, APP_INIT)
        assetDir = new File(appDir, APP_ASSETS)
        staticDir = new File(appDir, APP_STATIC)
        scriptDir = new File(appDir, APP_SCRIPTS)
        allDirs = [appDir, domainsDir, controllersDir, websocketsDir, viewsDir, configDir, initDir, assetDir, staticDir, scriptDir]
        // env
        environment = (env ?: System.getenv(GRIN_ENV_NAME)) ?: ENV_DEV
        if (!(environment in GRIN_ENV_LIST)) throw new Exception("错误的运行环境值：${environment}，值必须是 ${GRIN_ENV_LIST} 之一。")
        // config
        config = loadConfig()
        log.info("start app @ ${projectDir.absolutePath} ${environment} ...")
    }

    /**
     * 设置根路径，并初始化。重复会有异常。
     * @param root
     * @return
     */
    static init(File root = null, String env = null) {
        if (_instance) throw new Exception("Grin app has inited")
        _instance = new App(root, env)
    }

    /**
     * 获取单例
     * @return
     */
    synchronized static App getInstance() {
        if (_instance) return _instance
        _instance = new App()
        return _instance
    }

    /**
     * 获取配置
     * @return
     */
    ConfigObject loadConfig() {
        def configFile = new File(configDir, 'config.groovy')
        if (configFile.exists()) {
            return new ConfigSlurper(environment).parse(configFile.text)
        } else {
            throw new Exception("配置文件不存在")
        }
    }

    /**
     * 是否开发环境
     */
    boolean isDev() {
        ENV_DEV == environment
    }

    /**
     * 初始化项目目录结构
     * @param root
     * @return
     */
    void initDirs() {
        log.info("init grin app dirs @ ${projectDir.absolutePath}")
        allDirs.each {
            if (it.exists()) {
                log.info("${it.name} exists")
            } else {
                it.mkdirs()
                log.info("${it.name} mkdirs")
            }
        }
    }

    /**
     * 初始化数据库
     * 这个需要手动调用，在必要的地方。因为有时候初始化 APP 并不需要数据库，如运行创建领域类这样的命令。
     * @return
     */
    void initializeDB() {
        log.info("初始化数据库")
        def dataSource = new DruidDataSource(config.dataSource)
        if (config.logSql) {
            Filter sqlLog = new Slf4jLogFilter(statementExecutableSqlLogEnable: true)
            sqlLog.setStatementSqlFormatOption(new SQLUtils.FormatOption(true, false))
            dataSource.setProxyFilters([sqlLog, new StatFilter()])
        }
        DB.dataSource = dataSource
        if (config.dbCreate == 'create-drop') DDL.dropAndCreateTables(WebUtils.loadEntities(domainsDir))
        if (config.dbCreate == 'update') DDL.updateTables(WebUtils.loadEntities(domainsDir))
        if (config.dbSql) DDL.executeSqlFile(new File(scriptDir, config.dbSql as String))
        log.info("Tables：${DDL.tableColumns().keySet()}")
    }

    /**
     * 初始化 web 组件
     * 如上面，这个也需要手动调用
     */
    void initializeWeb() {
        routes = WebUtils.loadRoutes(config.urlMapping)
        controllers = WebUtils.loadControllers(controllersDir)
        actions = WebUtils.loadActions(controllers)
        interceptor = WebUtils.findInterceptor(controllersDir) ?: new Interceptor()
        websockets = WebUtils.loadWebsockets(websocketsDir)
        log.info("初始化 web\nroutes:${routes}\ncontrollers:${controllers}\nactions:${actions}\nintercepter:${interceptor?.class}\nwebsockets:${websockets}")
    }

    /**
     * GSE 延时加载
     */
    synchronized GroovyScriptEngine getScriptEngine() {
        if (_scriptEngine) return _scriptEngine
        _scriptEngine = new GroovyScriptEngine(domainsDir.absolutePath, controllersDir.absolutePath, websocketsDir.absolutePath, scriptDir.absolutePath)
        return _scriptEngine
    }

    /**
     * json generator
     * @return
     */
    synchronized JsonGenerator getJsonGenerator() {
        if (_jsonGenerator) return _jsonGenerator
        _jsonGenerator = new groovy.json.JsonGenerator.Options()
                .addConverter(Date) { Date date ->
                    date.format(instance.config.json.dateFormat ?: 'yyyy-MM-dd HH:mm:ss')
                }
                .addConverter(Entity) { it.toMap() }
                .build()
        return _jsonGenerator
    }

    StreamingJsonBuilder getJson(HttpServletResponse response) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        return new StreamingJsonBuilder(response.getWriter(), getJsonGenerator())
    }

    /**
     * 模板引擎
     * @return
     */
    synchronized Template getTemplate() {
        if (_template) return _template
        _template = new Template(this)
        return _template
    }
}
