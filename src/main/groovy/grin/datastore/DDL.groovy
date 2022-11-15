package grin.datastore

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.sql.Connection

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
    static Map<String, List<String>> tables() {
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
        def fields = EntityImpl.findPropertiesToPersist(entityClass)
        def tableName = EntityImpl.findTableName(entityClass)
        def columnMap = EntityImpl.columnMap(entityClass)

        return """
create table ${tableName}(
${fields.collect { "        ${columnSql(entityClass, it, columnMap[it])}" }.join(',\n')}
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
        def type = ''
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

        def nullable = Utils.getEntityConstraintValue(entityClass, propertyName, 'Nullable')
        constraint += nullable ? 'default null' : 'not null'

        if (cls == String) {
            def maxLength = Utils.getEntityConstraintValue(entityClass, propertyName, 'MaxLength')
            type = maxLength ? "varchar(${maxLength})" : 'varchar'
        } else {
            type = 'varchar'
        }

        log.debug("${entityClass.name} ${propertyName} ${columnName} - ${cls.name}")

        return "${columnName ?: EntityImpl.toDbName(propertyName)} ${type} ${constraint}"
    }

    /**
     * 删除表
     * @param entityClass
     * @return
     */
    static String entityDropSql(Class<Entity> entityClass) {
        return "drop table if exists ${EntityImpl.findTableName(entityClass)} cascade"
    }

    /**
     * 执行 sql
     * @param sqlString
     */
    static void executeSql(String sqlString) {
        println("执行：\n${sqlString}")
        def start = System.currentTimeMillis()
        Sql sql = DB.sql
        def r = sql.execute(sqlString)
        println("完成，${r ? '': "影响了 ${sql.updateCount} 行，"}耗时 ${(System.currentTimeMillis() - start) / 1000000}ms")
    }
}
