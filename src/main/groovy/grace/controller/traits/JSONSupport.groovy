package grace.controller.traits

import groovy.json.StreamingJsonBuilder

/**
 * Json 支持
 */
trait JSONSupport extends RequestBase {
    StreamingJsonBuilder json

    /**
     * json builder
     * @return
     */
    StreamingJsonBuilder getJson() {
        if (json) return json
        return new StreamingJsonBuilder(response.getWriter())
    }

    // todo do something
//    void json(Object object){
//        getJson()(object)
//    }
}