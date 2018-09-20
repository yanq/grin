package grace.app

import grace.route.Routes
import groovy.util.logging.Slf4j
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.FileTemplateResolver

import java.nio.file.*
import static java.nio.file.StandardWatchEventKinds.*

/**
 * Grace App
 * 定义规约目录等。
 */
@Slf4j
class GraceApp {
    //目录结构
    public static final String APP_DIR = 'grace-app'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_VIEWS = 'views'
    public static final String APP_INTERCEPTORS = 'interceptors'
    //instance
    private static GraceApp instance
    //other
    GroovyScriptEngine scriptEngine
    TemplateEngine templateEngine
    boolean refreshing = false
    File root, appDir, controllersDir, viewsDir, interceptorsDir
    List<File> allDirs

    /**
     * 构造并初始化
     * @param appRoot
     */
    private GraceApp(File appRoot) {
        if (!appRoot) appRoot = new File('.')
        root = appRoot
        appDir = new File(appRoot, APP_DIR)
        controllersDir = new File(appDir, APP_CONTROLLERS)
        viewsDir = new File(appDir, APP_VIEWS)
        interceptorsDir = new File(appDir, APP_INTERCEPTORS)
        allDirs = [appDir, controllersDir, viewsDir, interceptorsDir]
    }

    /**
     * GSE 延时加载
     */
    GroovyScriptEngine getScriptEngine() {
        if (scriptEngine) return scriptEngine
        scriptEngine = new GroovyScriptEngine(controllersDir.absolutePath,interceptorsDir.absolutePath)
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
     * 设置根路径，并初始化。重复会有异常。
     * @param root
     * @return
     */
    static setRoot(File root) {
        if (instance) throw new Exception("Grace app has inited")
        instance = new GraceApp(root)
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
     * 初始化项目目录结构
     * @param root
     * @return
     */
    void initDirs() {
        log.info("init grace app dirs @ ${appRoot.absolutePath}")
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
