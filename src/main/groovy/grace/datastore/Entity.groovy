package grace.datastore

import grace.app.GraceApp
import groovy.sql.Sql
import groovy.transform.CompileStatic

/**
 * 提供实体类的基本功能
 */
trait Entity<D> {
    //static Class<T> tClass = this.class
    static Map mapping = [table:'',clommons:[:]]
    Sql internalSql //一个示例默认持有一个 sql，懒加载

    /**
     * sql
     * @return
     */
    Sql getInternalSql(){
        if (internalSql) return sql
        internalSql = newSql()
        return internalSql
    }

    /**
     * new sql
     * @return
     */
    static Sql getSql() {
        return new Sql(GraceApp.instance.dataSource)
    }

    /**
     * get
     * todo 如果带着 D ，编译的时候异常，找不到类型 D，这不太科学。
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static get(Serializable id){
        println this.declaredFields
    }
}