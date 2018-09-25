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
}
