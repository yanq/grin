package grin.datastore

import groovy.sql.Sql

import java.lang.reflect.Modifier

/**
 * 实体类工具包
 */
class Utils {
    static List<String> CONSTRAINT_NAMES = ['Nullable', 'MaxLength', 'InList']

    /**
     * 执行 sql
     * @param sqlString
     */
    static void executeSql(String sqlString) {
        println("执行：\n${sqlString}")
        def start = System.currentTimeMillis()
        DB.withSql { Sql sql ->
            def r = sql.execute(sqlString)
            println("完成，${r ? '' : "影响了 ${sql.updateCount} 行，"}耗时 ${(System.currentTimeMillis() - start) / 1000000}ms")
        }
    }

    /**
     * 将属性名称编程数据库风格名称
     * @param propName
     * @return
     */
    static String toDBStyle(String propName) {
        propName = propName.uncapitalize()
        String result = ''
        propName.toCharArray().each {
            if (Character.isUpperCase(it)) {
                result += '_' + Character.toLowerCase(it)
            } else {
                result += it
            }
        }
        return result.toString()
    }

    /**
     * 获取表名
     * @param target
     * @return
     */
    static String findTableName(Class<Entity> target) {
        target[EntityImpl.MAPPING]?.table ?: toDBStyle(target.simpleName)
    }

    /**
     * 获取列名
     * todo 这里得缓存一下
     * @param target
     * @return
     */
    static String findColumnName(Class<Entity> target, String propertyName) {
        target[EntityImpl.MAPPING]?.columns?."${propertyName}" ?:
                "${toDBStyle(propertyName)}${target.getDeclaredField(propertyName).type.interfaces.contains(Entity) ? '_id' : ''}"
    }

    /**
     * 获取持久化属性列表
     * @param target
     * @return
     */
    static List<String> findPropertiesToPersist(Class target) {
        List fields = target.declaredFields.findAll { !Modifier.isStatic(it.modifiers) && !it.name.contains('$') }*.name
        fields - EntityImpl.excludeProperties - target[EntityImpl.TRANSIENTS] ?: []
    }

    /**
     * 转换成 Map
     * @return
     */
    static Map toMap(List<String> includes, Object entity) {
        def list = includes ?: Utils.findPropertiesToPersist(entity.class)
        def result = [:]
        list.each {
            def value = entity[it]
            if (value instanceof Entity) {
                def prefix = it + '.'
                def subExcludes = includes.findAll { it.startsWith(prefix) }.collect { it.replaceFirst(prefix, '') }
                value = value.toMap(subExcludes)
            }
            result.put(it, value)
        }
        return result
    }

    /**
     * 或者实体类的某个约束的
     * @param entityClass
     * @param propertyName
     * @param constraintName in
     * @return
     */
    static Object getEntityConstraintValue(Class<Entity> entityClass, String propertyName, String constraintName) {
        if (constraintName in CONSTRAINT_NAMES) {
            List<grin.datastore.validate.Validator> validatorList = entityClass.constraints[propertyName]
            return validatorList?.find { it.class.simpleName == constraintName }?.value
        } else {
            throw new Exception("不支持的约束名称 ${constraintName}，仅支持 ${CONSTRAINT_NAMES}")
        }
    }
}
