package grace.servlet.request

import groovy.xml.MarkupBuilder

/**
 * html 支持
 */
trait HTML extends RequestBase {
    MarkupBuilder html

    /**
     * html builder
     * @return
     */
    MarkupBuilder getHtml() {
        if (html) return html
        html = new MarkupBuilder(response.getWriter())
        return html
    }
}