package gun.datastore.entity
/**
 * 实体工具类
 */
class EntityUtil {
    /**
     * 将属性名称编程数据库风格名称
     * @param propName
     * @return
     */
    static String toDbName(String propName) {
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
     * 将数据库风格命名转为属性命名
     * @param dataName
     * @return
     */
    static String toPropName(String dataName) {}

    /**
     * 处理参数，包括分页和排序
     * 貌似 pg 的 offset 是可以独立的，mysql 不可以。先以 mysql 为准。
     * @param params [offset:0,limit:10,order:'id desc']
     * @return
     */
    static String params(Map params) {
        if (!params) return ''
        return "${params.order ? 'order by ' + params.order : ''} ${params.limit ? 'limit ' + params.limit : ''} ${(params.limit && params.offset) ? 'offset ' + params.offset : ''}"
    }

    /**
     * 获取关联实体
     * 避免 n+1 现象
     * @param list
     * @param names
     */
    static void fetch(List list, String... names) {
        if (!list) return
        def entityClass = list[0]?.class
        if (!entityClass || !entityClass.interfaces.contains(Entity)) return //非 entity ，不作处理

        if (!names) {
            names = entityClass.declaredFields.findAll { it.type.interfaces.contains(Entity) }.name
        }
        names.each {
            def name = it
            def propertyClass = entityClass.getDeclaredField(name).type
            def ids = list.collect { it[name] }.id.unique()

            if (!ids) return

            def propertyList = propertyClass.getAll(ids)
            list.each {
                def entity = it
                def p = propertyList.find {
                    it.id == entity[name]?.id
                }
                if (p) entity[name] = p
            }
        }
    }
}