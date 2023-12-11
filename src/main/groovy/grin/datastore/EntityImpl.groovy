package grin.datastore

import grin.datastore.validate.Blank
import grin.datastore.validate.Nullable
import grin.datastore.validate.Validator
import grin.datastore.validate.Validators
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * 实体 api 实现
 * 实现具体的操作，get save，解析实体类等。
 */
@Slf4j
class EntityImpl {
    // 保留变量
    public static final String MAPPING = 'mapping' // mapping 定义实体类与表之间的映射关系,table,columns
    public static final String TRANSIENTS = 'transients' // 不持久化的类属性
    public static final String CONSTRAINTS = 'constraints' // 约束
    // 系统级忽略的内容
    public static final List<String> excludeProperties = ['metaClass', 'grin_datastore_Entity__errors']

    /**
     * save
     * 插入或更新
     * @param entity
     */
    static save(Object entity) {
        DB.withSql { Sql sql ->
            // kvs
            Map columnMap = columnMap(entity.class)
            List ps = Utils.findPropertiesToPersist(entity.class)
            Map kvs = [:]
            ps.each {
                def propertyValue = entity[it]
                def propertyClass = entity.class.getDeclaredField(it).type
                // 如果是 Date，转换成 LocalDateTime，pg 当前的驱动不支持 Date 了。mysql 无影响。
                if (propertyValue instanceof Date) propertyValue = java.time.LocalDateTime.ofInstant(propertyValue.toInstant(), ZoneId.systemDefault())
                if (propertyValue instanceof List) propertyValue = JsonOutput.toJson(propertyValue)
                if (propertyValue instanceof Map) propertyValue = JsonOutput.toJson(propertyValue)
                if (columnMap.containsKey(it)) {
                    kvs << [(columnMap[it]): propertyValue]
                } else {
                    if (propertyClass.interfaces.contains(Entity)) {
                        kvs << [(Utils.toDBStyle(it) + '_id'): propertyValue?.id]
                    } else {
                        kvs << [(Utils.toDBStyle(it)): propertyValue]
                    }
                }
            }
            kvs.remove('id')

            String table = Utils.findTableName(entity.class)
            if (entity.hasProperty('id') && entity['id']) {
                // to update
                def sets = kvs.keySet().collect { "${it} = ?" }.join(',').toString()
                def sqlString = "update ${table} set ${sets} where id = ?".toString()
                def params = kvs.values().toList() << entity.id
                sql.executeUpdate(sqlString, params)
            } else {
                // to insert
                def sqlString = "insert into ${table} (${kvs.keySet().join(',')}) values (?${',?' * (kvs.size() - 1)})".toString()
                def result = sql.executeInsert(sqlString, kvs.values().toList())
                entity.id = result[0][0]
            }

            return entity
        }
    }

    /**
     * delete
     * 删除
     * @param entity
     * @return
     */
    static boolean delete(Object entity) {
        return DB.withSql { Sql sql -> sql.execute "delete from ${Utils.findTableName(entity.class)} where id = ?".toString(), [entity.id] }
    }

    /**
     * refresh
     * @param entity
     * @return
     */
    static refresh(Object entity) {
        DB.withSql { Sql sql ->
            def row = sql.firstRow "select * from ${Utils.findTableName(entity.class)} where id = ?".toString(), [entity.id]
            bindResultToEntityInstance(row, entity)
        }
    }

    /**
     * 验证约束
     * @param entity
     * @return
     */
    static boolean validate(Entity entity) {
        if (!entity.hasProperty('id')) throw new Exception("实体类缺少 id 属性")
        entity.errors.clear()
        Utils.findPropertiesToPersist(entity.class).each { String name ->
            if (name == 'id') return
            Object value = entity[name]
            List<Validator> constraints = entity.constraints[name] ?: []

            if (value == null) {
                Validator nullable = constraints.find { it instanceof Nullable } ?: Validators.nullable(false)
                if (!nullable.value) entity.errors[(name)] = nullable.message
                return
            }

            if (value instanceof String && value.trim() == '') {
                Blank blank = constraints.find { it instanceof Blank } ?: Validators.blank(false)
                if (!blank.value) entity.errors[(name)] = blank.message
                return
            }

            def v = constraints.find { !it.validate(name, value, entity) }
            if (v) entity.errors[(name)] = v.message
        }

        entity.errors ? false : true
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
        return bindResultToEntityInstance(result, entity)
    }

    /**
     * 绑定 row 到 实体实例
     * @param result
     * @param entity
     * @return
     */
    static bindResultToEntityInstance(GroovyRowResult result, Object entity) {
        if (!result) return null

        def clasz = entity.class
        def columnMap = columnMap(clasz)
        List<String> properties = Utils.findPropertiesToPersist(clasz)
        properties.each {
            String key = it
            Class propClass = clasz.getDeclaredField(it)?.type
            // 处理属性对表列的映射
            if (columnMap.containsKey(key)) {
                key = columnMap[key]
            } else {
                key = Utils.toDBStyle(key)
                if (propClass.interfaces.contains(Entity)) {
                    key = key + "_id"
                }
            }
            // 从 result 中取值并赋值给实体
            if (result.containsKey(key)) {
                switch (propClass) {
                    case LocalDate:
                        entity[(it)] = result[key].toLocalDate()
                        break
                    case LocalTime:
                        entity[(it)] = result[key].toLocalTime()
                        break
                    case LocalDateTime:
                        entity[(it)] = result[key].toLocalDateTime()
                        break
                    case List:
                        entity[(it)] = new JsonSlurper().parseText(result[key] ?: '[]')
                        break
                    case Map:
                        entity[(it)] = new JsonSlurper().parseText(result[key] ?: '{}')
                        break
                    case Entity:
                        if (result[key]) {
                            entity[(it)] = propClass.newInstance()
                            entity[(it)]['id'] = result[key]
                        }
                        break
                    default:
                        entity[(it)] = result[key]
                }
            }
        }
        return entity
    }

    /**
     * 绑定参数到实体
     * @param entityClass
     * @param params
     * @return
     */
    static bind(Class entityClass, Map params) {
        def entity = entityClass.newInstance()
        bind(entity, params)
        return entity
    }

    static bind(Entity entity, Map params) {
        Class entityClass = entity.class
        List props = Utils.findPropertiesToPersist(entityClass) - 'id'
        props.each {
            def propClass = entity.class.getDeclaredField(it).type
            def key = it
            if (propClass.interfaces.contains(Entity)) key = key + "Id"
            def keys = [key, Utils.toDBStyle(key)]
            def value = params.find { it.key in keys }?.value
            try {
                if (params.keySet().intersect(keys)) {
                    // 绑定实体和其他是不一样的
                    if (propClass.interfaces.contains(Entity)) {
                        if (value) {
                            entity[(it)] = propClass.newInstance()
                            entity[(it)]['id'] = Transformer.toType(propClass, 'id', value)
                        } else {
                            entity[(it)] = null
                        }
                    } else {
                        entity[it] = Transformer.toType(entityClass, it, value)
                    }
                }
            } catch (Exception exception) {
                entity.errors[it] = "数据格式错误，无法正确转换为适当类型"
                log.warn("转换数据错误(${it},${value})，${exception.getMessage()}")
                // exception.printStackTrace()
            }
        }
        return entity
    }

    /**
     * 获取列映射
     * @param target
     * @return
     */
    static Map columnMap(Class<Entity> target) {
        target[EntityImpl.MAPPING]?.columns ?: [:]
    }


    /**
     * where 查询
     */
    static class Where<D> {
        String whereSql
        List params
        Class entityClass

        D get(List<String> selects = []) {
            List list = list([offset: 0, max: 1], selects)
            if (list) return list[0]
            return null
        }

        List<D> list(Map pageParams = [:], List<String> selects = []) {
            preDealParams()
            return DB.withSql { Sql sql ->
                List list = []
                List rows = sql.rows("select ${dealSelects(selects)} from ${Utils.findTableName(entityClass)} ${whereSql ? 'where ' + whereSql : ''} ${dealParams(pageParams)}".toString(), params)
                rows.each { row ->
                    list << bindResultToEntity(row, entityClass)
                }
                sql.close()
                return list
            }
        }

        int count(List<String> selects = []) {
            preDealParams()
            DB.withSql { Sql sql -> sql.firstRow("select count(${dealSelects(selects)}) as num from ${Utils.findTableName(entityClass)} ${whereSql ? 'where ' + whereSql : ''}".toString(), params).num }
        }

        int countDistinct(List<String> selects = []) {
            preDealParams()
            DB.withSql { Sql sql -> sql.firstRow("select count(distinct ${dealSelects(selects)}) as num from ${Utils.findTableName(entityClass)} ${whereSql ? 'where ' + whereSql : ''}".toString(), params).num }
        }

        private preDealParams() {
            for (int i = 0; i < params.size(); i++) {
                if (params[i] instanceof Date) params[i] = java.time.LocalDateTime.ofInstant(params[i].toInstant(), ZoneId.systemDefault())
            }
        }

        private dealSelects(List<String> selects = []) {
            if (!selects) return '*'
            selects.collect { Utils.findColumnName(entityClass, it) }.join(',')
        }


        /**
         * 处理参数，包括分页和排序
         * 貌似 pg 的 offset 是可以独立的，mysql 不可以。先以 mysql 为准。
         * @param params [offset:0,limit:10,order:'id desc,name asc']
         * @return
         */
        private String dealParams(Map params) {
            if (!params) return ''
            String order = params.order
            if (order) {
                order = order.split(',').collect {
                    def list = it.split(' ')
                    list[0] = Utils.findColumnName(entityClass, list[0])
                    return list.join(' ')
                }.join(',')
            }
            return "${order ? 'order by ' + order : ''} ${params.limit ? 'limit ' + params.limit : ''} ${(params.limit && params.offset) ? 'offset ' + params.offset : ''}"
        }
    }
}