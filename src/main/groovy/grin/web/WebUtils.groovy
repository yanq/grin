package grin.web

import grin.datastore.Entity
import groovy.util.logging.Slf4j

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 控制器工具类
 * 加载控制器，全局的拦截器，执行操作等。
 */
@Slf4j
class WebUtils {
    /**
     * 实体类列表
     * @param dir
     */
    static List<Class<Entity>> loadEntities(File dir) {
        List<Class> list = []
        if (dir.exists())
            dir.eachFileRecurse {
                String className = fileToClassName(it, dir, '.groovy')
                if (className) list.add(Class.forName(className))
            }
        return list
    }

    /**
     * 加载所有的控制器
     * @param dir
     */
    static Map<String, String> loadControllers(File dir) {
        Map<String, String> controllers = [:]
        dir.eachFileRecurse {
            if (it.name.endsWith('Controller.groovy')) {
                String className = fileToClassName(it, dir, '.groovy')
                if (className) {
                    String name = className.split('\\.')[-1]
                    name = name.substring(0, name.length() - 'Controller'.length()).uncapitalize()
                    controllers.put(name, className)
                }
            }
        }
        return controllers
    }

    /**
     * 加载 actions
     * @param controllers
     * @return
     */
    static Map<String, Method> loadActions(Map<String, String> controllers) {
        Map<String, Method> actions = [:]
        controllers.each { name, className ->
            Class clazz = Class.forName(className)
            if (!Controller.isAssignableFrom(clazz)) throw new Exception("控制器 ${clazz} 不是 ${Controller.class}")
            Class.forName(className).getDeclaredMethods()
                    .findAll {
                        def methodName = it.name
                        def modifiers = it.modifiers
                        !['$', 'super$'].find { methodName.startsWith(it) } && Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    }
                    .each {
                        actions.put("${name}-${it.name.uncapitalize()}", it)
                    }
        }
        return actions
    }


    /**
     * 寻找拦截器
     * @param dir
     * @return
     */
    static Interceptor findInterceptor(File dir) {
        Interceptor interceptor
        dir.eachFileRecurse {
            if (it.name.matches('.+Interceptor.groovy')) {
                if (interceptor) throw new Exception("已经存在拦截器 ${interceptor.class.name}, 又有：${it.name}")
                String className = fileToClassName(it, dir, '.groovy')
                Class clazz = Class.forName(className)
                if (!Interceptor.isAssignableFrom(clazz)) throw new Exception("拦截器 ${clazz} 不是 ${Interceptor.class}")
                interceptor = clazz.newInstance()
            }
        }
        return interceptor
    }

    /**
     * 加载路由定义
     * @param urlMapping
     */
    static List<Route> loadRoutes(Map<String, String> urlMapping) {
        List<Route> routes = []
        urlMapping.each {
            routes.add(new Route(it.key, it.value))
        }
        if (!routes) throw new Exception("缺少必要的路由配置，至少有一个默认的 '/@controllerName/?@actionName?/@id?'")
        return routes
    }

    /**
     * 加载 websocket
     * @param dir
     */
    static List<Class> loadWebsockets(File dir) {
        List<Class> websockets = []
        if (dir.exists())
            dir.eachFileRecurse {
                String className = fileToClassName(it, dir, '.groovy')
                if (className) websockets.add(Class.forName(className))
            }
        return websockets
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
