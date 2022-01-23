package gun.app

import com.alibaba.druid.filter.Filter
import com.alibaba.druid.filter.logging.Slf4jLogFilter
import com.alibaba.druid.filter.stat.StatFilter
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.sql.SQLUtils
import groovy.json.JsonGenerator
import groovy.util.logging.Slf4j
import gun.web.Controller
import gun.web.Controllers

import javax.sql.DataSource
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.WatchKey
import java.nio.file.WatchService

import static java.nio.file.StandardWatchEventKinds.*

/**
 * Grace App
 * 定义规约目录等。
 */
@Slf4j
class GraceApp {
    //元数据
    public static final String VERSION = '0.1.1'
    //instance
    private static GraceApp instance
    //env
    public static final String ENV_PROD = 'prod'
    public static final String ENV_DEV = 'dev'
    //目录结构
    public static final String APP_DIR = 'grace-app'
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
    JsonGenerator jsonGenerator;
    boolean refreshing = false

    Class<Controller> errorControllerClass = Controller

    File projectDir, appDir, domainsDir, controllersDir, viewsDir, configDir, initDir, assetDir, assetBuildDir, staticDir, scriptDir
    List<File> allDirs


    /**
     * 构造并初始化
     * @param projectRoot
     */
    GraceApp(File projectRoot = null, String env = ENV_DEV) {
        //init dirs
        if (!projectRoot) projectRoot = new File('.')
        projectDir = projectRoot
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
        config = config()
        environment = env
    }

    /**
     * 设置根路径，并初始化。重复会有异常。
     * @param root
     * @return
     */
    static synchronized setRootAndEnv(File root, String env = ENV_DEV) {
        if (instance) throw new Exception("Grace app has inited")
        instance = new GraceApp(root, env)
    }

    /**
     * 获取单例
     * todo 这里同步的话，是不是会影响性能？
     * @return
     */
    static GraceApp getInstance() {
        if (instance) return instance
        instance = new GraceApp()
        return instance
    }

    /**
     * 获取配置
     * @return
     */
    ConfigObject config() {
        def configFile = new File(configDir, 'config.groovy')
        if (configFile.exists()) {
            return new ConfigSlurper(environment).parse(configFile.text)
        } else {
            log.warn("No config file found!")
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
        log.info("init grace app dirs @ ${projectDir.absolutePath}")
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
        log.info("check grace app dirs @ ${projectDir.absolutePath}")
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
    GroovyScriptEngine getScriptEngine() {
        if (scriptEngine) return scriptEngine
        scriptEngine = new GroovyScriptEngine(controllersDir.absolutePath, scriptDir.absolutePath)
        return scriptEngine
    }

    /**
     * json generator
     * @return
     */
    JsonGenerator getJsonGenerator() {
        if (jsonGenerator) return jsonGenerator

        jsonGenerator = new groovy.json.JsonGenerator.Options()
                .addConverter(Date) { Date date ->
                    date.format(instance.config.json.dateFormat ?: 'yyyy-MM-dd HH:mm:ss')
                }
                .build()
        return jsonGenerator
    }

    /**
     * 刷新应用
     */
    synchronized void refresh(List<String> dirs = null) {
        refreshing = true
        log.info("refresh app @ ${dirs ?: 'start'}")
        //重载控制器，拦截器
        if (dirs == null || dirs?.find { it.endsWith('.groovy') }) {
            config = config()
            controllers.reload(controllersDir)

            if (config.errorClass) errorControllerClass = config.errorClass
        }
        refreshing = false
    }

    /**
     * 等待刷新完成
     * 用于 servlet 中，避免更新过程中可能出现的不确定性
     */
    void waitingForRefresh() {
        if (!refreshing) return
        while (true) {
            sleep(100)
            if (!refreshing) return
        }
    }

    /**
     * 启动文件监控，变化更新
     */
    void startFileWatcher() {
        log.info("start watch ${appDir.absolutePath}")

        GroovyClassLoader loader = new GroovyClassLoader()
        WatchService watchService = FileSystems.getDefault().newWatchService()

        //目录极其子目录，监控只有一级
        Paths.get(appDir.absolutePath).register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        appDir.absoluteFile.eachFileRecurse {
            if (it.isDirectory()) Paths.get(it.absolutePath).register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        }

        Thread watcher = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    WatchKey key = watchService.take()
                    try {
                        List<String> names = key.pollEvents()*.context()*.toString()
                        refresh(names)
                    } catch (Exception e) {
                        log.warn("file change deal fail")
                        e.printStackTrace()
                    } finally {
                        key.reset()
                    }
                }
            }
        })
        watcher.setName('GraceApp file watch service')
        watcher.setDaemon(true)
        watcher.start()
    }
}
