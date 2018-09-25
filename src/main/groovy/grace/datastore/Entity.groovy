package grace.datastore
/**
 * 提供实体类的基本功能
 */
trait Entity<D> {

    /**
     * get
     * todo 如果带着 D ，编译的时候异常，找不到类型 D，这不太科学。
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static get(Serializable id) {
        EntityApiImpl.get(this, id)
    }

    /**
     * 表名
     * @return
     */
    static String getTableName() {
        hasProperty('mapping') ? this['mapping'].table : DBUtil.toDbName(this.simpleName)
    }

    /**
     * 绑定数据
     * 从普通的 map 或者 result
     * @param data
     */
    static bindData(Map data) {}
}