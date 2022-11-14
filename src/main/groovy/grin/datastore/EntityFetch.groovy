package grin.datastore
/**
 * 实体工具类
 */
class EntityFetch {
    /**
     * 获取实体的关联实体
     * @param entity
     * @param names
     */
    static void fetch(Entity entity, String... names) {
        fetchList([entity], names)
    }

    /**
     * 获取列表的关联实体
     * 避免 n+1 现象
     * @param list
     * @param names
     */
    static void fetchList(List list, String... names) {
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