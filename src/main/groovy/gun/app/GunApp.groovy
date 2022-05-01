package gun.app

import com.alibaba.druid.filter.Filter
import com.alibaba.druid.filter.logging.Slf4jLogFilter
import com.alibaba.druid.filter.stat.StatFilter
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.sql.SQLUtils
import groovy.json.JsonGenerator
import groovy.util.logging.Slf4j
import gun.datastore.DB
import gun.web.Controller
import gun.web.Controllers

import javax.sql.DataSource

/**
 * Gun App
 * 定义规约目录等。
 */
@Slf4j
class GunApp {
    //元数据
    public static final String VERSION = '0.1.1'
    //instance
    private static GunApp instance
    //env
    public static final String ENV_PROD = 'prod'
    public static final String ENV_DEV = 'dev'
    //目录结构
    public static final String APP_DIR = 'gun-app'
    public static final String APP_DOMAINS = 'domains'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_VIEWS = 'views'
    public static final String APP_CONFIG = 'conf'
    public static final String APP_INIT = 'init'
    public static final String APP_ASSETS = 'assets'
    public static final String APP_STATIC = 'static'
    public static final String APP_SCRIPTS = 'scripts'

    String environment = ENV_DEV // dev,prod
    ConfigObject config
    Controllers controllers = new Controllers()
    DataSource dataSource
    GroovyScriptEngine scriptEngine
    JsonGenerator jsonGenerator

    Class<Controller> errorControllerClass = Controller

    File projectDir, appDir, domainsDir, controllersDir, viewsDir, configDir, initDir, assetDir, assetBuildDir, staticDir, scriptDir
    List<File> allDirs


    /**
     * 构造并初始化
     * @param projectRoot
     */
    GunApp(File projectRoot = null, String env = ENV_DEV) {
        //init dirs
        if (!projectRoot) projectRoot = new File('.')
        projectDir = projectRoot
        log.info("start app @ ${projectDir.absolutePath} ...")
        appDir = new File(projectDir, APP_DIR)
        domainsDir = new File(appDir, APP_DOMAINS)
        controllersDir = new File(appDir, APP_CONTROLLERS)
        viewsDir = new File(appDir, APP_VIEWS)
        configDir = new File(appDir, APP_CONFIG)
        initDir = new File(appDir, APP_INIT)
        assetDir = new File(appDir, APP_ASSETS)
        assetBuildDir = new File(projectDir, 'build/assets')
        staticDir = new File(appDir, APP_STATIC)
        scriptDir = new File(appDir, APP_SCRIPTS)
        allDirs = [appDir, domainsDir, controllersDir, viewsDir, configDir, initDir, assetDir, staticDir, scriptDir]
        //config
        environment = env
        config = loadConfig()
        // 初始化数据库，控制器，错误处理
        DB.dataSource = getDataSource()
        if (config.dbSql) DB.executeSqlFile(new File(scriptDir, config.dbSql as String))
        controllers.load(controllersDir)
        controllers.loadURLMapping(config.urlMapping ?: [:])
        if (config.errorClass) errorControllerClass = config.errorClass
        log.info("started app @ ${environment}")
        log.info("urlMapping: ${config.urlMapping}")
    }

    /**
     * 设置根路径，并初始化。重复会有异常。
     * @param root
     * @return
     */
    static synchronized init(File root = null, String env = ENV_DEV) {
        if (instance) throw new Exception("Gun app has inited")
        instance = new GunApp(root, env)
    }

    /**
     * 获取单例
     * todo 这里同步的话，是不是会影响性能？
     * @return
     */
    static GunApp getInstance() {
        if (instance) return instance
        instance = new GunApp()
        return instance
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
        log.info("init gun app dirs @ ${projectDir.absolutePath}")
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
     * 检查目录结构
     * @return
     */
    void checkDirs() {
        log.info("check gun app dirs @ ${projectDir.absolutePath}")
        allDirs.each {
            if (!it.exists()) throw new Exception("目录不存在：${it.canonicalPath}")
        }
    }

    /**
     * 数据源
     * @return
     */
    DataSource getDataSource() {
        if (dataSource) return dataSource
        log.info("初始化 data source")
        dataSource = new DruidDataSource(config.dataSource)
        if (config.logSql) {
            Filter sqlLog = new Slf4jLogFilter(statementExecutableSqlLogEnable: true)
            sqlLog.setStatementSqlFormatOption(new SQLUtils.FormatOption(true, false))
            dataSource.setProxyFilters([sqlLog, new StatFilter()])
        }
        return dataSource
    }

    /**
     * GSE 延时加载
     */
    synchronized GroovyScriptEngine getScriptEngine() {
        if (scriptEngine) return scriptEngine
        log.info("初始化 GroovyScriptEngine")
        scriptEngine = new GroovyScriptEngine(domainsDir.absolutePath, controllersDir.absolutePath, scriptDir.absolutePath)
        return scriptEngine
    }

    /**
     * json generator
     * @return
     */
    synchronized JsonGenerator getJsonGenerator() {
        if (jsonGenerator) return jsonGenerator
        log.info("初始化 JsonGenerator")
        jsonGenerator = new groovy.json.JsonGenerator.Options()
                .addConverter(Date) { Date date ->
                    date.format(instance.config.json.dateFormat ?: 'yyyy-MM-dd HH:mm:ss')
                }
                .build()
        return jsonGenerator
    }

    /**
     * 启动服务
     * @param server
     */
    void startServer(GunServer server) {
        if (config.server.host) server.host = config.server.host
        if (config.server.http.port) server.port = config.server.http.port
        if (config.server.https.port) server.httpsPort = config.server.https.port
        if (config.server.https.jksPath) server.jksPath = config.server.https.jksPath
        if (config.server.https.jksPwd) server.jksPwd = config.server.https.jksPwd
        if (config.server.context) server.context = config.server.context
        if (config.server.uploadLocation) server.uploadLocation = config.server.uploadLocation
        if (config.server.maxFileSize) server.maxFileSize = config.server.maxFileSize
        if (config.server.maxRequestSize) server.maxRequestSize = config.server.maxRequestSize
        if (config.server.ioThreads) server.ioThreads = config.server.ioThreads
        if (config.server.workerThreads) server.workerThreads = config.server.workerThreads
        if (config.webSocketEntryList) server.webSocketEntryList = config.webSocketEntryList
        server.start()
    }
}
