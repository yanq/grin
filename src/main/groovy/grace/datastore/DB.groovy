package grace.datastore

import grace.app.GraceApp
import groovy.sql.Sql

/**
 * DB 数据库
 */
class DB {

    /**
     * 获取 sql
     * @return
     */
    static Sql getSql(){
        return new Sql(GraceApp.instance.dataSource)
    }

    static withSql(@DelegatesTo(SQL) Closure closure){
        Sql sql = getSql()
        def result = closure.call(sql)
        sql.close()
        return result
    }

    /**
     * 用作闭包代理，便于 ide 提示。
     */
    static class SQL{
        Sql sql
    }
}
