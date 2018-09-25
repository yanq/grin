package grace.datastore
/**
 * 提供实体类的基本功能
 */
trait Entity<T> {
    static Map mapping = [table: '', clommons: [:]]

    /**
     * get
     * todo 如果带着 D ，编译的时候异常，找不到类型 D，这不太科学。
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static get(Serializable id) {
        return DB.sql.firstRow("select * from book where id=${id}")
    }
}