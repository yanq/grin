package grin.app

import grin.generate.Generator

/**
 * Gun 入口
 * 启动服务器，执行命令等。
 */
class GunMain {
    // 支持的命令
    static cmds = [
            'init'             : ['init gun dirs'],
            'create-domain'    : ['create domain class'],
            'create-controller': ['create controller class'],
            'generate-all'     : ['generate controller and views from domain class'],
            'generate-service' : ['generate a service controller from domain class'],
            'run-script'       : ['run script']
    ]

    /**
     * 用法提示
     * @return
     */
    static usage() {
        println 'Welcome to use Gun!'
        println 'command available:'
        cmds.each {
            println "$it.key ${it.value.join('  ')}"
        }
        println 'Enjoy it!'
    }

    /**
     * 满足条件，否则退出
     * @param expression
     * @param title
     * @return
     */
    static mustOrFail(boolean expression, String title) {
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

        if (!args) {
            Scanner scanner = new Scanner(System.in)
            println('input command :')
            String input = scanner.nextLine()
            println('input: ' + input)
            args = input.split()
        }

        String cmd = args ? args[0] : ''

        //not support
        if (!cmds.containsKey(cmd)) {
            println("command error : ${args}")
            usage()
            return
        }

        //init
        if (cmd == 'init') {
            GunApp.instance.initDirs()
        }

        //create domain
        if (cmd == 'create-domain') {
            mustOrFail args.size() > 1, "缺少类名"
            Generator.createDomain(args[1])
        }

        //create controller
        if (cmd == 'create-controller') {
            mustOrFail args.size() > 1, "缺少类名"
            Generator.createController(args[1])
        }

        //generate all
        if (cmd == 'generate-all') {
            mustOrFail args.size() > 1, "缺少类名"
            Generator.generateAll(args[1])
        }

        //generate all
        if (cmd == 'generate-service') {
            mustOrFail args.size() > 1, "缺少类名"
            Generator.generateService(args[1])
        }

        //run script
        if (cmd == 'run-script') {
            mustOrFail args.size() > 1, "缺少类名"
            String script = args[1].endsWith('.groovy') ? args[1] : args[1] + '.groovy'
            def start = System.currentTimeMillis()
            GunApp.instance.scriptEngine.run(script, '')
            println("Run script ${script},use time ${(System.currentTimeMillis() - start) / 1000}s.")
        }
    }
}
