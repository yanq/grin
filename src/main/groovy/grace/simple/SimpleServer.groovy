package grace.simple

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import grace.route.Processor
import grace.util.RegexUtil
import groovy.util.logging.Slf4j

import java.util.concurrent.Executors

/**
 * 最简服务器
 * 使用 HttpServer 实现服务器，用于单控制器文件，简单交互，低资源的场景
 */
@Slf4j
class SimpleServer {
    int port
    String context
    int threadCount = 1

    /**
     * 构造器，默认
     */
    SimpleServer() {
        this(8080, '/', 1)
    }

    /**
     * 构造器，带参数
     * @param port
     * @param context
     * @param threadCount
     */
    SimpleServer(int port, String context, int threadCount) {
        this.port = port
        this.context = context
        this.threadCount = threadCount
    }

    /**
     * 启动服务
     */
    void start() {
        log.info("start server @ http://localhost:${port}")
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0); //backlog 使用系统默认,最大等待处理 socket 数量
        if (threadCount > 1) server.setExecutor(Executors.newFixedThreadPool(threadCount));
        server.createContext("/", new GraceHandler());
        server.start();
    }

    /**
     * Handler
     */
    @Slf4j
    static class GraceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clearedURI = RegexUtil.toURI(exchange.getRequestURI().path,exchange.httpContext.path)
            SimpleRequest request = new SimpleRequest(exchange)

            try {
                Processor.processRequest(clearedURI, request)
                if (!request.processed) { //如果没有处理过
                    byte[] bytes = request.writer.toString().getBytes('utf-8')
                    exchange.sendResponseHeaders(200, bytes.length)
                    exchange.getResponseBody().write(bytes)
                    exchange.getResponseBody().close()
                }
            } catch (Exception e) {
                e.printStackTrace()
                request.error(e)
            }
        }
    }
}
