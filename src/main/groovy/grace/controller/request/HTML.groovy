package grace.controller.request

import groovy.xml.MarkupBuilder

/**
 * html 支持
 */
trait HTML {
    MarkupBuilder html

    /**
     * html builder
     * @return
     */
    MarkupBuilder getHtml() {
        if (html) return html
        return new MarkupBuilder(response.getWriter())
    }
}