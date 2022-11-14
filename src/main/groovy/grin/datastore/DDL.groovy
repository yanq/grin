package grin.datastore

import groovy.util.logging.Slf4j

import java.sql.Connection

/**
 * 数据定义
 */
@Slf4j
class DDL {
    /**
     * 数据库状态
     * 获取表和列的数据
     * @return
     */
    static Map<String, List<String>> dbStatus() {
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
                return "id char[32] primary key"
            } else {
                throw new Exception("id 必须是 整数或者字符串")
            }
        }

        def nullable = Utils.getEntityConstraintValue(entityClass, propertyName, 'Nullable')
        constraint += nullable ? '' : 'not null'

        if (cls == String) {
            def maxLength = Utils.getEntityConstraintValue(entityClass, propertyName, 'MaxLength')
            type = maxLength ? "varchar(${maxLength})" : 'varchar'
        }else {
            type = 'varchar'
        }

        log.debug("${entityClass.name} ${propertyName} ${columnName} - ${cls.name}")

        return "${columnName ?: EntityImpl.toDbName(propertyName)} ${type} ${constraint}"
    }
}
