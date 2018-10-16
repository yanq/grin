package grace.app

import grace.generate.Generator

/**
 * Grace 入口
 * 启动服务器，执行命令等。
 */
class GraceMain {
    //支持的命令
    static cmds = [
            'init'             : ['init grace dirs'],
            'run'              : ['run [dev|prod]?', 'run grace server'],
            'create-domain'    : ['create domain class'],
            'create-controller': ['create controller class'],
            'generate-all'     : ['generate controller and views from domain class']
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

    static warmAndExit(boolean expression, String title) {
        if (!expression) {
            println(title)
            System.exit(0)
        }
    }

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {

        if (!args){
            Scanner scanner = new Scanner(System.in)
            println('input command :')
            String input = scanner.nextLine()
            println('input: '+input)
            args = input.split()
        }

        String cmd = args ? args[0] : ''

        //not support
        if (!cmds.containsKey(cmd)) {
            println("command error : ${args}")
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
        if (cmd == 'create-domain') {
            warmAndExit args.size() > 1, "缺少类名"
            Generator.createDomain(args[1])
        }

        //create controller
        if (cmd == 'create-controller') {
            warmAndExit args.size() > 1, "缺少类名"
            Generator.createController(args[1])
        }

        //generate all
        if (cmd == 'generate-all') {
            warmAndExit args.size() > 1, "缺少类名"
            Generator.generateAll(args[1])
        }
    }
}
