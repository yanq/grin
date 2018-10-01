package grace.app

import grace.generate.Generator

/**
 * Grace 入口
 * 启动服务器，执行命令等。
 */
class GraceMain {
    //支持的命令
    static cmds = [
            'init': ['init grace dirs'],
            'run' : ['run [dev|prod]?', 'run grace server'],
            'create-domain': ['create domain class']
    ]
    /**
     * 用法提示
     * @return
     */
    static usage() {
        println 'Welcome to use Grace!'
        println 'command available:'
        cmds.each {
            println "$it.key ${it.value.join('  ')}"
        }
        println 'Enjoy it!'
    }

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {
        String cmd = args ? args[0] : ''

        //not support
        if (!cmds.containsKey(cmd)) {
            usage()
            return
        }

        //run
        if (cmd == 'run') {
            def server = new GraceServer()
            if (args.contains('prod')) {
                server.startApp(null, GraceApp.ENV_PROD)
            } else {
                server.startApp()
            }
        }

        //init
        if (cmd == 'init') {
            GraceApp.instance.initDirs()
        }

        //create domain
        if (cmd == 'create-domain'){
            Generator.createDomain(args[1])
        }
    }
}
