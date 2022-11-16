package grin.datastore

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

/**
 * DB 数据库
 */
@Slf4j
class DB {
    static DataSource dataSource // datasource,提供一个配置的入口，方便 grin 外部使用。

    /**
     * 获取 sql
     * @return
     */
    static Sql getSql() {
        new Sql(dataSource)
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
     * 用作闭包代理，便于 IDE 提示。
     */
    static class SQL {
        Sql sql
    }
}
