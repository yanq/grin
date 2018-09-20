package grace.controller

import grace.route.Routes

/**
 * 控制器脚本父类
 *
 * 这个方式是可以的，但是并未有好的效果：ide 并不能有效完成代码提示。放一放，留着备用。
 */
abstract class ControllerScript extends Script {
    ControllerScript(Binding binding) {
        super(binding)
    }

    /**
     * 通过 '/aaa'{} 模式添加路由
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, Object args) {
        if (args && args[0] instanceof Closure){
            Routes.req(name,args[0])
        } else {
            throw new NoSuchMethodException()
        }
    }
}
