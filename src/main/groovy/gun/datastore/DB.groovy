package gun.datastore

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

/**
 * DB 数据库
 * todo 验证完善，表更新 等
 */
@Slf4j
class DB {
    static DataSource dataSource //datasource,提供一个配置的入口，方便 gun 外部使用。
    static ThreadLocal<Sql> localSql = new ThreadLocal<>() // 线程内的 sql，主要用来控制事务

    /**
     * 获取 sql
     * @return
     */
    static Sql getSql() {
        Sql sql = localSql.get()
        if (sql) {
            log.debug('use local sql')
        } else {
            log.debug('create new sql')
            sql = new Sql(dataSource)
        }
        return sql
    }

    /**
     * with sql
     * @param closure
     * @return
     */
    static withSql(@DelegatesTo(SQL) Closure closure) {
        Sql sql = getSql()
        def result = closure.call(sql)
        if (sql.connection?.autoCommit) sql.close() //自动提交的时候才关闭，如事务中，单独的执行不能关闭 sql
        return result
    }

    /**
     * with transaction
     * @param closure
     * @return
     */
    static withTransaction(@DelegatesTo(SQL) Closure closure) {
        Sql sql = getSql()
        localSql.set(sql)
        try {
            def result
            sql.withTransaction {
                result = closure.call(sql)
            }
            return result
        } finally {
            localSql.remove()
            sql.close()
        }
    }

    /**
     * 用作闭包代理，便于 IDE 提示。
     */
    static class SQL {
        Sql sql
    }

    /**
     * 执行 sql 文件
     * 创建及修改表，不会返回任何有用的结果，都是 false 0。
     * @param sql
     */
    static void executeSqlFile(File sql) {
        log.info("exec sql file ${sql.name}")
        getSql().execute(sql.text)
    }
}
