package grace.datastore

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

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
        Sql sql = DB.sql
        String table = findTableName(target)
        def result = sql.firstRow("select * from ${table} where id=?", id)
        def entity = bindResultToEntity(result, target)
        sql.close()
        return entity
    }

    static list(Class target, Map params) {
        int offset = params?.offset ?: 0
        int max = params?.max ?: 100
        Sql sql = DB.sql
        List list = []
        List rows = sql.rows("select * from ${findTableName(target)} ${DBUtil.limitString(offset,max)}".toString())
        rows.each { row ->
            list << bindResultToEntity(row, target)
        }
        sql.close()
        return list
    }

    /**
     * save
     * @param entity
     */
    static save(Object entity) {
        Sql sql = DB.sql
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
            def sqlString = "update ${table} set ${sets} where id = ?".toString()
            def params = kvs.values().toList() << entity.id
            sql.executeUpdate(sqlString, params)
        } else { //to insert
            def sqlString = "insert into ${table} (${kvs.keySet().join(',')}) values (?${',?' * (kvs.size() - 1)})".toString()
            def result = sql.executeInsert(sqlString, kvs.values().toList())
            entity.id = result[0][0]
        }

        sql.close()
        return entity
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
