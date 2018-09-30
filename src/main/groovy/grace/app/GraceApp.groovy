package grace.app

import com.alibaba.druid.filter.Filter
import com.alibaba.druid.filter.logging.Slf4jLogFilter
import com.alibaba.druid.filter.stat.StatFilter
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.sql.SQLUtils
import grace.controller.route.Routes
import groovy.util.logging.Slf4j
import io.undertow.servlet.api.DeploymentInfo
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.FileTemplateResolver
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
    //目录结构
    public static final String APP_DIR = 'grace-app'
    public static final String APP_DOMAINS = 'domains'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_VIEWS = 'views'
    public static final String APP_INTERCEPTORS = 'interceptors'
    public static final String APP_CONFIG = 'conf'
    public static final String APP_INIT = 'init'
    public static final String APP_ASSETS = 'assets'
    //env
    public static final String ENV_PROD = 'prod'
    public static final String ENV_DEV = 'dev'
    //instance
    private static GraceApp instance
    //config
    ConfigObject config
    String environment = 'dev' // dev,prod
    //datastore
    DataSource dataSource
    //engines for script,template,..
    GroovyScriptEngine scriptEngine
    TemplateEngine templateEngine
    //dirs
    boolean refreshing = false
    File root, appDir, domainsDir, controllersDir, viewsDir, interceptorsDir, configDir, initDir, assetDir, assetBuildDir
    List<File> allDirs

    /**
     * 构造并初始化
     * @param appRoot
     */
    GraceApp(File appRoot = null, String env = ENV_DEV) {
        //init dirs
        if (!appRoot) appRoot = new File('.')
        root = appRoot
        appDir = new File(appRoot, APP_DIR)
        domainsDir = new File(appDir, APP_DOMAINS)
        controllersDir = new File(appDir, APP_CONTROLLERS)
        viewsDir = new File(appDir, APP_VIEWS)
        interceptorsDir = new File(appDir, APP_INTERCEPTORS)
        configDir = new File(appDir, APP_CONFIG)
        initDir = new File(appDir, APP_INIT)
        assetDir = new File(appDir, APP_ASSETS)
        assetBuildDir = new File(root, 'build/assets')
        allDirs = [appDir, domainsDir, controllersDir, viewsDir, interceptorsDir, configDir, initDir, assetDir]
        //config
        environment = env
        config = new ConfigSlurper(environment).parse(new File(configDir, 'config.groovy').text)
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
     * @return
     */
    static getInstance() {
        if (instance) return instance
        instance = new GraceApp()
        return instance
    }

    /**
     * 是否开发环境
     */
    boolean isDev() {
        ENV_DEV == environment
    }

    /**
     * 初始化阶段整合其他 servlet 等。由 undertow 提供的类来实现。
     * @param deploymentInfo
     */
    void init(DeploymentInfo deploymentInfo) {
        def bootstrap = new File(initDir, 'BootStrap.groovy')
        if (bootstrap.exists()) {
            new GroovyClassLoader().parseClass(bootstrap).newInstance().init(deploymentInfo)
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
        scriptEngine = new GroovyScriptEngine(controllersDir.absolutePath, interceptorsDir.absolutePath)
        return scriptEngine
    }

    /**
     * template engine by thymeleaf
     * 默认是缓存的。
     * @return
     */
    TemplateEngine getTemplateEngine() {
        if (templateEngine) return templateEngine
        templateEngine = new TemplateEngine()
        FileTemplateResolver resolver = new FileTemplateResolver()
        resolver.setPrefix(viewsDir.absolutePath)
        resolver.setSuffix('.html')
        resolver.setCharacterEncoding('utf-8')
        resolver.setCacheable(false) //todo 开发期间不缓存
        templateEngine.setTemplateResolver(resolver)
        return templateEngine
    }

    /**
     * 初始化项目目录结构
     * @param root
     * @return
     */
    void initDirs() {
        log.info("init grace app dirs @ ${root.absolutePath}")
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
     * 检查是否是 grace app
     * @return
     */
    boolean isAppDir() {
        log.info("check grace app dirs @ ${root.absolutePath}")
        return !allDirs.find { !it.exists() }
    }

    /**
     * 刷新应用
     */
    synchronized void refresh(List<String> dirs) {
        refreshing = true
        if (!dirs) dirs = [APP_CONTROLLERS] //参数为空时，刷新

        //sleep(10000)
        log.info("refresh app for ${dirs}")

        if (dirs.contains(APP_CONTROLLERS) || dirs.contains(APP_INTERCEPTORS)) {
            //refresh routes
            Routes.clear()
            //控制器
            controllersDir.eachFileRecurse {
                if (it.name.endsWith('.groovy')) {
                    log.info("run controller script ${it.absolutePath}")
                    scriptEngine.run(it.absolutePath.substring(controllersDir.absolutePath.length() + 1), '')
                }
            }
            //拦截器
            interceptorsDir.eachFileRecurse {
                if (it.name.endsWith('.groovy')) {
                    log.info("run interceptor script ${it.absolutePath}")
                    scriptEngine.run(it.absolutePath.substring(interceptorsDir.absolutePath.length() + 1), '')
                }
            }
            //sort
            Routes.sort()
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
     * 判断是否是控制器
     * @param path
     * @return
     */
    boolean isController(String path) {
        return path.startsWith(controllersDir.absolutePath)
    }

    /**
     * 启动文件监控，变化更新
     */
    void startFileWatcher() {
        log.info("start watch ${appDir.absolutePath}")

        GroovyClassLoader loader = new GroovyClassLoader()
        WatchService watchService = FileSystems.getDefault().newWatchService()
        Paths.get(appDir.absolutePath).register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

        Thread watcher = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    WatchKey key = watchService.take()
                    try {
                        List<String> names = key.pollEvents()*.context()*.path
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
        watcher.setDaemon(true)
        watcher.start()
    }
}
