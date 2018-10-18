package grace.datastore

import grace.app.GraceApp
import groovy.sql.Sql
import javax.sql.DataSource

/**
 * DB 数据库
 */
class DB {

    static DataSource dataSource //datasource,提供一个配置的入口，方便 grace 外部使用。

    /**
     * 获取 sql
     * @return
     */
    static Sql getSql() {
        return new Sql(dataSource ?: GraceApp.instance.dataSource)
    }

    /**
     * with sql
     * @param closure
     * @return
     */
    static withSql(@DelegatesTo(SQL) Closure closure) {
        Sql sql = getSql()
        def result = closure.call(sql)
        sql.close()
        return result
    }

    /**
     * with transaction
     * @param closure
     * @return
     */
    static withTransaction(@DelegatesTo(SQL) Closure closure) {
        Sql sql = getSql()
        def result
        sql.withTransaction {
            result = closure.call(sql)
        }
        sql.close()
        return result
    }

    /**
     * 用作闭包代理，便于 ide 提示。
     */
    static class SQL {
        Sql sql
    }
}
