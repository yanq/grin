package grace.datastore

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import java.lang.reflect.Modifier

/**
 * 实体 api 实现
 * 实现具体的操作，get save，解析实体类等。
 */
@Slf4j
class EntityApiImpl {
    //保留变量
    public static final String MAPPING = 'mapping' //mapping 定义实体类与表之间的映射关系,table,columns
    public static final String TRANSIENTS = 'transients' //不持久化的类属性
    public static final String CONSTRAINTS = 'constraints' //不持久化的类属性
    public static final String CONSTRAINT_MAP = 'constraintMap' //不持久化的类属性
    // 系统级忽略的内容
    public static final List<String> excludeProperties = ['metaClass']
    //other
    public static final int DEFAULT_MAX_ROWS = 100

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
        Sql sql = DB.sql
        List list = []
        List rows = sql.rows("select * from ${findTableName(target)} ${DBUtil.params(params)}".toString())
        rows.each { row ->
            list << bindResultToEntity(row, target)
        }
        sql.close()
        return list
    }

    static int count(Class target) {
        return DB.withSql { sql -> sql.firstRow("select count(*) as num from ${findTableName(target)}".toString()).num }
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
        if (entity.hasProperty('id') && entity['id']) {
            //to update
            def sets = kvs.keySet().collect { "${it} = ?" }.join(',').toString()
            def sqlString = "update ${table} set ${sets} where id = ?".toString()
            def params = kvs.values().toList() << entity.id
            sql.executeUpdate(sqlString, params)
        } else {
            //to insert
            def sqlString = "insert into ${table} (${kvs.keySet().join(',')}) values (?${',?' * (kvs.size() - 1)})".toString()
            def result = sql.executeInsert(sqlString, kvs.values().toList())
            entity.id = result[0][0]
        }

        sql.close()
        return entity
    }


    static boolean delete(Object entity) {
        return DB.withSql { sql -> sql.execute "delete from ${findTableName(entity.class)} where id = ?".toString(), [entity.id] }
    }

    /**
     * 获取持久化属性列表
     * @param target
     * @return
     */
    static List<String> findPropertiesToPersist(Class target) {
        List fields = target.declaredFields.findAll { !Modifier.isStatic(it.modifiers) }*.name
        fields - excludeProperties - (target.hasProperty(TRANSIENTS) ? target[TRANSIENTS] : [])
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
        List<String> properties = findPropertiesToPersist(target)
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

    /**
     * where 查询
     */
    static class Where {
        String whereSql
        List params
        Class entityClass

        def get() {
            List list = list([offset: 0, max: 1])
            if (list) return list[0]
            return null
        }

        List list(Map pageParams) {
            return DB.withSql { Sql sql ->
                List list = []
                List rows = sql.rows("select * from ${findTableName(entityClass)} ${whereSql ? 'where ' + whereSql : ''} ${DBUtil.params(pageParams)}".toString())
                rows.each { row ->
                    list << bindResultToEntity(row, entityClass)
                }
                sql.close()
                return list
            }
        }

        int count() {
            DB.withSql { sql -> sql.firstRow("select count(*) as num from ${findTableName(entityClass)} ${whereSql ? 'where ' + whereSql : ''}".toString(), params).num }
        }
    }

    /**
     * 验证约束
     * @param entity
     * @return
     */
    static boolean validate(Entity entity) {
        if (!entity.hasProperty('id')) throw new Exception("该类没有 id 属性，差评")

        entity.errors = [] //置空
        getConstraintMap(entity.class).each {
            def propertyConstraints = it
            def value = entity[propertyConstraints.key]
            Map constraintsToValidate = propertyConstraints.value

            //comment
            constraintsToValidate.remove('comment')

            //null 处理
            if (null == value) {
                if (!constraintsToValidate.nullable) entity.errors << [propertyConstraints.key, 'nullable']
                return
            }
            constraintsToValidate.remove('nullable')

            //blank
            if ('' == value) {
                if (!constraintsToValidate.blank) entity.errors << [propertyConstraints.key, 'blank']
                return
            }
            constraintsToValidate.remove('blank')

            constraintsToValidate.each {
                if (!Validator.validate(value, it)) {
                    entity.errors << [propertyConstraints.key, it.key]
                }
            }
        }

        entity.errors ? false : true
    }

    /**
     * 获取约束表
     * todo 缓存一下更好
     * @param entityClass
     * @return
     */
    static Map<String, Map> getConstraintMap(Class entityClass) {
        ConstraintsBuilder.buildFromEntityClass(entityClass)
    }

}
