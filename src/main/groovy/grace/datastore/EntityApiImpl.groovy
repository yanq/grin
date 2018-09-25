package grace.datastore

import groovy.sql.GroovyRowResult

class EntityApiImpl {
    public static final String MAPPING = 'mapping' //mapping 定义实体类与表之间的映射关系
    public static final List<String> excludeProperties = ['$staticClassInfo', '__$stMC', 'metaClass', '$staticClassInfo$', '$callSiteArray']

    static get(Class target, Serializable id) {
        String table = findTableName(target)
        def result = DB.sql.firstRow("select * from ${table} where id=?", id)
        return bindEntity(target, result)
    }

    static String findTableName(Class target) {
        target[MAPPING]?.table ?: DBUtil.toDbName(target.simpleName)
    }

    static Map columnMap(Class target) {
        target[MAPPING]?.columns ?: [:]
    }

    static bindEntity(Class target, GroovyRowResult result) {
        def entity = target.newInstance()
        //List<String> properties = target.declaredFields*.name - excludeProperties
        def columnMap = columnMap(target)
        result.each {
            def key = it.key
            def mappedProperty = columnMap.find { it.value == key }
            if (mappedProperty) {
                if (entity.hasProperty(mappedProperty.key)) entity[(mappedProperty.key)] = it.value
            } else {
                if (entity.hasProperty(it.key)) entity[(it.key)] = it.value
            }
        }
        return entity
    }
}
