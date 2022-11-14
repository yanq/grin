package grin.web

import grin.datastore.Transformer
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 参数
 */
@CompileStatic
class Params extends HashMap<String, Object> {

    /**
     * 解析 Date
     * @param key
     * @param format
     * @return
     */
    Date date(String key, String format = null) {
        return Transformer.toDate(super.get(key), format ? [format] : []) as Date
    }

    /**
     * 解析 LocalDate
     * @param key
     * @param format
     * @return
     */
    LocalDate localDate(String key, String format = null) {
        return Transformer.toLoaclDate(super.get(key), format ? [format] : []) as LocalDate
    }

    /**
     * 解析 LocalDateTime
     * @param key
     * @param format
     * @return
     */
    LocalDateTime localDateTime(String key, String format = null) {
        return Transformer.toLocalDateTime(super.get(key), format ? [format] : []) as LocalDateTime
    }
}