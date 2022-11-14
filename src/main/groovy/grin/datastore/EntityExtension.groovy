package grin.datastore
/**
 * Grin Entity 扩展类的实例方法
 */
class EntityExtension {
    /**
     * 实体列表获取关联实体
     * @param list
     * @param names
     */
    public static void fetch(List<? extends Entity> list, String... names) {
        EntityFetch.fetchList(list, names)
    }
}
