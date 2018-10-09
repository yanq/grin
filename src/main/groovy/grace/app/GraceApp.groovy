package grace.app

import com.alibaba.druid.filter.Filter
import com.alibaba.druid.filter.logging.Slf4jLogFilter
import com.alibaba.druid.filter.stat.StatFilter
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.sql.SQLUtils
import grace.controller.route.Routes
import groovy.util.logging.Slf4j
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
    public static final String APP_STATIC = 'static'
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
    File projectDir, appDir, domainsDir, controllersDir, viewsDir, interceptorsDir, configDir, initDir, assetDir, assetBuildDir,staticDir
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
        interceptorsDir = new File(appDir, APP_INTERCEPTORS)
        configDir = new File(appDir, APP_CONFIG)
        initDir = new File(appDir, APP_INIT)
        assetDir = new File(appDir, APP_ASSETS)
        assetBuildDir = new File(projectDir, 'build/assets')
        staticDir = new File(appDir, APP_STATIC)
        allDirs = [appDir, domainsDir, controllersDir, viewsDir, interceptorsDir, configDir, initDir, assetDir,staticDir]
        //config
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
            if (!it.exists() ) throw new Exception("目录不存在：${it.canonicalPath}")
        }
    }

    /**
     * 运行 Bootstrap
     * 方便应用在服务启动器，初始化自己的内容。
     * @param context 根据需要传递参数，如 undertow 传递 DeploymentInfo；后续会支持其他模式。
     */
    void init(Object context) {
        def bootstrap = new File(initDir, 'BootStrap.groovy')
        if (bootstrap.exists()) {
            new GroovyClassLoader().parseClass(bootstrap).newInstance().init(context)
        }
    }

    /**
     * 刷新应用
     */
    synchronized void refresh(List<String> dirs = null) {
        refreshing = true
        log.info("refresh request @ ${dirs ?: 'start'}")



        //重载控制器，拦截器
        if (dirs == null || dirs?.find {it.endsWith('.groovy')}) {
            //refresh routes
            Routes.clear()

            //重载配置
            config = new ConfigSlurper(environment).parse(new File(configDir, 'config.groovy').text)
            if (config.fileUpload.upload) Routes.post(config.fileUpload.upload){upload()}
            if (config.fileUpload.download) Routes.get(config.fileUpload.download+'/@file'){download()}
            if (config.assets.uri) Routes.get(config.assets.uri+'/@file'){asset()}
            if (config.files.uri) Routes.get(config.files.uri+'/@file'){files()}

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
        resolver.setPrefix(viewsDir.canonicalPath)
        resolver.setSuffix('.html')
        resolver.setCharacterEncoding('utf-8')
        resolver.setCacheable(false) //todo 开发期间不缓存
        templateEngine.setTemplateResolver(resolver)
        return templateEngine
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
        watcher.setName('GraceApp file watch service')
        watcher.setDaemon(true)
        watcher.start()
    }
}
