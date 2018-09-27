package grace.datastore

import grace.app.GraceApp

class DBUtil {
    /**
     * 将属性名称编程数据库风格名称
     * @param propName
     * @return
     */
    static String toDbName(String propName) {
        propName = propName.uncapitalize()
        String result = ''
        propName.toCharArray().each {
            if (Character.isUpperCase(it)) {
                result += '_' + Character.toLowerCase(it)
            } else {
                result += it
            }
        }
        return result.toString()
    }

    /**
     * 将数据库风格命名转为属性命名
     * @param dataName
     * @return
     */
    static String toPropName(String dataName) {}

    /**
     * 处理参数，包括分页和排序
     * 貌似 pg 的 offset 是可以独立的，mysql 不可以。先以 mysql 为准。
     * @param params [offset:0,max:10,order:'id,desc']
     * @return
     */
    static String params(Map params) {
        if (!params) return ''
        return "${params.order ? 'order by ' + params.order : ''} ${params.limit ? 'limit ' + params.limit : ''} ${(params.limit && params.offset) ? 'offset ' + params.offset : ''}"
    }
}