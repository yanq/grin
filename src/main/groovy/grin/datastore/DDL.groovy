package grin.datastore


import groovy.util.logging.Slf4j

import java.sql.Connection
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 数据定义
 */
@Slf4j
class DDL {
    /**
     * 数据库表信息
     * @return
     */
    static List tablesMetaData() {
        Connection connection = DB.dataSource.connection
        def tables = connection.metaData.getTables(connection.catalog, connection.schema, null, 'TABLE')
        def meta = tables.metaData
        def columnNames = []
        def rows = []

        meta.columnCount.times {
            def index = it + 1
            columnNames << meta.getColumnName(index)
        }

        while (tables.next()) {
            def row = [:]
            columnNames.each {
                row[it] = tables.getString(it)
            }
            rows << row
        }

        return rows
    }

    /**
     * 数据库列信息
     * @return
     */
    static List columnsMetaData() {
        Connection connection = DB.dataSource.connection
        def columns = connection.metaData.getColumns(connection.catalog, connection.schema, null, null)
        def meta = columns.metaData
        def names = []
        def rows = []

        meta.columnCount.times {
            def index = it + 1
            names << meta.getColumnName(index)
        }

        while (columns.next()) {
            def row = [:]
            names.each {
                row[it] = columns.getString(it)
            }
            rows << row
        }

        return rows
    }

    /**
     * 表信息，主要是表及其列名
     * @return
     */
    static Map<String, List<String>> tablesStatus() {
        Connection connection = DB.dataSource.connection
        def resultSet = connection.metaData.getColumns(connection.catalog, connection.schema, null, null)
        def metaData = resultSet.metaData
        def columnNames = []
        def result = [:]

        metaData.columnCount.times {
            def index = it + 1
            columnNames << metaData.getColumnName(index)
        }

        while (resultSet.next()) {
            def tableName = resultSet.getString('TABLE_NAME')
            def columnName = resultSet.getString('COLUMN_NAME')
            if (!result[tableName]) result[tableName] = []
            result[tableName] << columnName
        }

        return result
    }

    /**
     * 实体类创建表
     * @param entityClass
     * @return
     */
    static String entityCreateSql(Class<Entity> entityClass) {
        def fields = Utils.findPropertiesToPersist(entityClass)
        def tableName = Utils.findTableName(entityClass)

        return """
create table ${tableName}(
${fields.collect { "        ${columnSql(entityClass, it, Utils.findColumnName(entityClass,it))}" }.join(',\n')}
)
"""
    }

    /**
     * 列 sql
     * @param entityClass
     * @param propertyName
     * @param columnName
     * @return
     */
    static String columnSql(Class<Entity> entityClass, String propertyName, String columnName) {
        def cls = entityClass.getDeclaredField(propertyName).type
        def type = '未知类型'
        def constraint = ''

        // id
        if (propertyName == 'id') {
            if (cls in [int, Integer]) {
                return "id serial primary key"
            } else if (cls in [long, Long]) {
                return "id bigserial primary key"
            } else if (cls == String) {
                return "id varchar[32] primary key"
            } else {
                throw new Exception("id 必须是 整数或者字符串")
            }
        }

        // log.debug("列类型 ${entityClass.name} ${propertyName} ${columnName} - ${cls.name}")

        def nullable = Utils.getEntityConstraintValue(entityClass, propertyName, 'Nullable')
        constraint += nullable ? 'default null' : 'not null'

        if (cls == String) {
            def maxLength = Utils.getEntityConstraintValue(entityClass, propertyName, 'MaxLength')
            type = maxLength ? "varchar(${maxLength})" : 'varchar'
        } else if (cls in [boolean, Boolean]) {
            type = 'boolean'
        } else if (cls in [byte, Byte, short, Short, int, Integer]) {
            type = 'integer'
        } else if (cls in [long, Long]) {
            type = 'bigint'
        } else if (cls in [float, Float]) {
            type = 'real'
        } else if (cls in [double, Double]) {
            type = 'double precision'
        } else if (cls == BigDecimal) {
            type = 'decimal'
        } else if (cls == Date) {
            type = 'timestamp'
        } else if (cls == LocalDate) {
            type = 'date'
        } else if (cls == LocalTime) {
            type = 'time'
        } else if (cls == LocalDateTime) {
            type = 'timestamp'
        } else if (cls in [List, Map]) {
            type = 'varchar'
        } else if (cls.interfaces.contains(Entity)) {
            def c = cls.getDeclaredField('id').type
            if (c in [int, Integer]) {
                type = 'integer'
            } else if (c in [long, Long]) {
                type = 'bigint'
            } else if (c == String) {
                type = 'varchar(32)'
            } else {
                throw new Exception("${propertyName} 的 id 必须是 整数或者字符串")
            }
        } else {
            throw new Exception("未支持的 Java 类型 ${cls.name}")
        }


        return "${columnName} ${type} ${constraint}"
    }

    /**
     * 删除表
     * @param entityClass
     * @return
     */
    static String entityDropSql(Class<Entity> entityClass) {
        return "drop table if exists ${Utils.findTableName(entityClass)} cascade"
    }

    /**
     * 外键
     * @param entityClassList
     * @return
     */
    static checkForeignKey(List<Class<Entity>> entityClassList) {
        entityClassList.each {
            def entity = it
            Utils.findPropertiesToPersist(entity).each {
                def propertyType = entity.getDeclaredField(it).type
                if (propertyType.interfaces.contains(Entity)) {
                    Utils.executeSql("alter table ${Utils.findTableName(entity)} add foreign key (${Utils.findColumnName(entity, it)}) " +
                            "references ${Utils.findTableName(propertyType)}")
                }
            }
        }
    }

    /**
     * 表的创建与删除
     * @param entityClass
     */
    static createTable(Class<Entity> entityClass) {
        Utils.executeSql(entityCreateSql(entityClass))
    }

    static createTables(List<Class<Entity>> entityClassList) {
        entityClassList.each {
            Utils.executeSql(entityCreateSql(it))
        }
        checkForeignKey(entityClassList)
    }

    static dropTable(Class<Entity> entityClass) {
        Utils.executeSql(entityDropSql(entityClass))
    }

    static dropTables(List<Class<Entity>> entityClassList) {
        entityClassList.each {
            Utils.executeSql(entityDropSql(it))
        }
    }

    static dropAndCreateTables(List<Class<Entity>> entityClassList){
        dropTables(entityClassList)
        createTables(entityClassList)
    }

    /**
     * 更新表
     * 缺少的表或者列，补齐，并不删除内容，只提醒。
     * @param entityClassList
     * @return
     */
    static updateTable(Class<Entity> entity, Map<String, List<String>> tables = []) {
        def tableName = Utils.findTableName(entity)
        log.info("update table ${tableName}")
        if (tables.containsKey(tableName.toUpperCase())) {
            def columnsNow = tables[tableName.toUpperCase()].collect { it.toLowerCase() }
            def properties = Utils.findPropertiesToPersist(entity)
            def columnsWill = properties.collect { Utils.findColumnName(entity, it) }
            if (columnsNow - columnsWill) log.warn("多余的列 ${columnsNow - columnsWill}")
            properties.each {
                def columnName = Utils.findColumnName(entity, it)
                if (!(columnName in columnsNow)) {
                    Utils.executeSql("alter table ${tableName} add column ${columnSql(entity, it, columnName)}")
                }
            }
        } else {
            createTable(entity)
        }
    }

    static updateTables(List<Class<Entity>> entityClassList) {
        def tablesMeta = tablesStatus()
        def tablesNow = tablesMeta.keySet().collect { it.toLowerCase() }
        def tablesWill = entityClassList.collect { Utils.findTableName(it) }
        if (tablesNow - tablesWill) log.warn("多余的表 ${tablesNow - tablesWill}")
        entityClassList.each { updateTable(it, tablesMeta) }
        checkForeignKey(entityClassList)
    }
}
