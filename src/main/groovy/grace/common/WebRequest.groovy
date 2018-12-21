package grace.common

import com.sun.net.httpserver.HttpExchange
import grace.app.GraceApp
import groovy.json.StreamingJsonBuilder
import groovy.xml.MarkupBuilder
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * 请求约定
 * 作为闭包的代理，用于 IDE 提示。
 * 实际情况不一定全部实现。
 */
abstract class WebRequest {
    String controllerName
    //参数
    Params params
    GraceApp app
    //g
    GraceExpression g
    //servlet
    HttpServletRequest request
    HttpServletResponse response
    HttpSession session
    ServletContext context
    Map<String, String> headers
    // http server exchange
    HttpExchange exchange

    //flash
    abstract FlashScope.Flash getFlash()

    abstract Map toMap() // for template binding , above
    abstract void forward(String path)
    abstract void redirect(String location)


    //usually
    abstract remoteIP()
    abstract int status()
    //send message
    abstract void sendMessage(int status,String message)
    abstract void notFound()
    abstract void error(Exception e)

    // renders
    abstract void render(Object object) // for anything others : list map ...
    abstract void render(String string)
    abstract void render(String view,Map model) // for template
    //文件处理
    abstract void render(byte[] bytes)
    abstract void render(File file)
    abstract void render(File file,int cacheTime)
    abstract void asset()
    abstract void files()
    //上传下载
    abstract List upload()
    abstract void download()

    //json
    abstract StreamingJsonBuilder getJson()
    void json(Map m){getJson()(m)}
    void json(String name){getJson()(name)}
    void json(List l){getJson()(l)}
    void json(Object... args){getJson()(args)}
    void json(Iterable coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(coll,c)}
    void json(Collection coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(coll,c)}
    void json(@DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(c)}
    void json(String name, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(name,c)}
    void json(String name, Iterable coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(name,coll,c)}
    void json(String name, Collection coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c){getJson()(name,coll,c)}
    void json(String name, Map map, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure callable){getJson()(name,map,callable)}

    //html
    abstract MarkupBuilder getHtml()
}
