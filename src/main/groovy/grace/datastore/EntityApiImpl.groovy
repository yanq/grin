package grace.datastore

import groovy.sql.GroovyRowResult

/**
 * 实体 api 实现
 * 实现具体的操作，get save，解析实体类等。
 */
class EntityApiImpl {
    public static final String MAPPING = 'mapping' //mapping 定义实体类与表之间的映射关系,table,columns
    public static final List<String> excludeProperties = ['$staticClassInfo', '__$stMC', 'metaClass', '$staticClassInfo$', '$callSiteArray']

    /**
     * get
     * 神一样的 get
     * @param target
     * @param id
     * @return
     */
    static get(Class target, Serializable id) {
        String table = findTableName(target)
        def result = DB.sql.firstRow("select * from ${table} where id=?", id)
        return bindResultToEntity(result, target)
    }

    /**
     * 获取表名
     * @param target
     * @return
     */
    static String findTableName(Class target) {
        target[MAPPING]?.table ?: DBUtil.toDbName(target.simpleName)
    }

    /**
     * 获取列映射
     * @param target
     * @return
     */
    static Map columnMap(Class target) {
        target[MAPPING]?.columns ?: [:]
    }

    /**
     * 绑定 result 到 实体类
     * @param result
     * @param target
     * @return
     */
    static bindResultToEntity(GroovyRowResult result, Class target) {
        if (!result) return null

        def entity = target.newInstance()
        def columnMap = columnMap(target)
        List<String> properties = target.declaredFields*.name - excludeProperties
        properties.each {
            String key = it
            if (columnMap.containsKey(key)) {
                key = columnMap[key]
            } else {
                key = DBUtil.toDbName(key)
            }
            if (result.containsKey(key)) entity[(it)] = result[key]
        }
        return entity
    }
}
