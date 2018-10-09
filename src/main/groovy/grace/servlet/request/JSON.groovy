package grace.servlet.request

import groovy.json.StreamingJsonBuilder

/**
 * Json 支持
 */
trait JSON extends RequestBase {
    StreamingJsonBuilder json

    /**
     * json builder
     * @return
     */
    StreamingJsonBuilder getJson() {
        if (json) return json
        return new StreamingJsonBuilder(response.getWriter())
    }

    /**
     * 保持 json 的常规功能
     * @param object
     */
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
}