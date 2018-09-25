package grace.datastore

import groovy.sql.GroovyRowResult

class EntityApiImpl {
    public static final String MAPPING = 'mapping' //mapping 定义实体类与表之间的映射关系

    static get(Class target, Serializable id) {
        String table = findTableName(target)
        def result = DB.sql.firstRow("select * from ${table} where id=?", id)
        return bindEntity(target,result)
    }

    static String findTableName(Class target) {
        target[MAPPING]?.table ?: DBUtil.toDbName(target.simpleName)
    }

    static bindEntity(Class target, GroovyRowResult result){
        def entity = target.newInstance()
        result.each {
            if (entity.hasProperty(it.key)) entity[(it.key)] = it.value
        }
        return entity
    }
}
