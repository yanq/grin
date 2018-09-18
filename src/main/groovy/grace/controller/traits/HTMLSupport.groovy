package grace.controller.traits

import groovy.xml.MarkupBuilder

/**
 * html 支持
 */
trait HTMLSupport {
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