package gun.web

import groovy.util.logging.Slf4j

import java.lang.reflect.Method

/**
 * 控制器工具类
 * 加载控制器，全局的拦截器，执行操作等。
 */
@Slf4j
class Controllers {

    Map<String, String> controllerMap = [:]
    Map<String, Method> methodMap = [:]
    Interceptor interceptor
    List<Class> websockets = []
    List<Route> routeList = []

    /**
     * 加载所有的控制器
     * @param dir
     */
    synchronized void load(File dir) {
        controllerMap.clear()
        methodMap.clear()
        interceptor = null

        dir.eachFileRecurse {
            if (!it.name.endsWith('.groovy')) return //忽略非目标文件
            def name = it.name.split('\\.')[0].uncapitalize()
                    .replaceAll("Controller", '')
                    .replaceAll('Interceptor', '')
            def className = it.canonicalPath.replace(dir.canonicalPath, '').substring(1)
            className = className.substring(0, className.lastIndexOf('.')).replace('/', '.')
            if (it.name.matches('.+Controller.groovy')) {
                if (controllerMap.containsKey(name)) throw new Exception("控制器 ${name} 已经存在: ${controllerMap.get(name)},新的 ${className}")
                controllerMap.put(name, className)
            }
            if (it.name.matches('.+Interceptor.groovy')) {
                if (interceptor) throw new Exception("已经存在拦截器 ${interceptor.class.name}, 又有：${it.name}")
                Class clazz = Class.forName(className)
                if (!Interceptor.isAssignableFrom(clazz)) throw new Exception("拦截器 ${clazz} 不是 ${Interceptor.class}")
                interceptor = clazz.newInstance()
            }
        }

        if (!interceptor) interceptor = new Interceptor()

        controllerMap.each { name, className ->
            Class clazz = Class.forName(className)
            if (!Controller.isAssignableFrom(clazz)) throw new Exception("控制器 ${clazz} 不是 ${Controller.class}")
            Class.forName(className).getDeclaredMethods()
                    .findAll {
                        def methodName = it.name
                        !['$', 'super$'].find { methodName.startsWith(it) }
                    }
                    .each {
                        methodMap.put("${name}-${it.name.uncapitalize()}", it)
                    }
        }

        log.info("load interceptor ${interceptor.class.simpleName} and controllers ${controllerMap.keySet()}")
    }

    /**
     * 加载路由定义
     * @param urlMapping
     */
    void loadURLMapping(Map<String, String> urlMapping) {
        routeList.clear()
        urlMapping.each {
            routeList.add(new Route(it.key, it.value))
        }
        if (!routeList) throw new Exception("缺少必要的路由配置，至少有一个默认的 '/@controllerName/?@actionName?/@id?'")
        log.info("load routes: ${routeList}")
    }

    /**
     * 加载 websocket
     * @param dir
     */
    void loadWebsockets(File dir) {
        websockets.clear()
        if (dir.exists())
            dir.eachFileRecurse {
                String className = fileToClassName(it, dir, '.groovy')
                if (className) websockets.add(Class.forName(className))
            }
        log.info("load websockets: ${websockets}")
    }

    /**
     * 从文件解析类名
     * @param file
     * @param dir
     * @param suffix
     * @return
     */
    static String fileToClassName(File file, File dir, String suffix) {
        if (file.name.endsWith(suffix)) {
            String name = file.canonicalPath.replace(dir.canonicalPath, '').substring(1)
            String className = name.substring(0, name.length() - suffix.length()).replaceAll('/', '.')
            return className
        }
    }
}
