package grace.common

import com.sun.net.httpserver.HttpExchange
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
abstract class Request {
    String controllerName
    //参数
    Params params
    //g
    GraceExpression g
    //servlet
    HttpServletRequest request
    HttpServletResponse response
    HttpSession session
    ServletContext context
    Map<String, String> headers
    abstract Map toMap() // for template binding , above
    abstract void forward(String path)
    abstract void redirect(String location)

    // exchange
    HttpExchange exchange

    //usually
    abstract void notFound()

    // renders
    abstract void render(Object object) // for anything others : list map ...
    abstract void render(String string)
    abstract void render(String view,Map model) // for template
    //文件处理
    abstract void render(byte[] bytes)
    abstract void render(File file)
    abstract void render(File file,int cacheTime)
    abstract void asset()
    abstract void upload()
    abstract void download()
    abstract void files()

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
