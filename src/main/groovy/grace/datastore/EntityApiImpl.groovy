package grace.datastore

import groovy.sql.GroovyRowResult

/**
 * 实体 api 实现
 * 实现具体的操作，get save，解析实体类等。
 */
class EntityApiImpl {
    public static final String MAPPING = 'mapping' //mapping 定义实体类与表之间的映射关系,table,columns
    public static final String TRANSIENTS = 'transients' //不持久化的类属性
    // 系统级忽略的内容
    public static final List<String> excludeProperties = [MAPPING, TRANSIENTS, '$staticClassInfo', '__$stMC', 'metaClass', '$staticClassInfo$', '$callSiteArray']

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
     * save
     * @param entity
     */
    static save(Object entity) {
        //kvs
        Map columnMap = columnMap(entity.class)
        List ps = findPropertiesToPersist(entity.class)
        Map kvs = [:]
        ps.each {
            String name = it
            if (columnMap.containsKey(it)) {
                kvs << [(columnMap[it]): entity[it]]
            } else {
                kvs << [(DBUtil.toDbName(it)): entity[name]]
            }
        }
        kvs.remove('id')

        String table = findTableName(entity.class)
        if (entity.hasProperty('id') && entity['id']) { //to update
            def sets = kvs.keySet().collect { "${it} = ?" }.join(',').toString()
            def sql = "update ${table} set ${sets} where id = ?".toString()
            def params = kvs.values().toList() << entity.id
            DB.sql.executeUpdate(sql, params)
            return entity
        } else { //to insert
            def sql = "insert into ${table} (${kvs.keySet().join(',')}) values (?${',?' * (kvs.size() - 1)})".toString()
            def result = DB.sql.executeInsert(sql, kvs.values().toList())
            entity.id = result[0][0]
            return entity
        }
    }

    static List<String> findPropertiesToPersist(Class target) {
        target.declaredFields*.name - excludeProperties - (target[TRANSIENTS] ?: [])
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
