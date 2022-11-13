package gun.datastore


import org.h2.jdbcx.JdbcDataSource

import java.sql.Connection

// String url = "jdbc:h2:mem:test" // 内存模式下，每次连接，都是一个新的数据库，所以无法测试到创建表的情况
String url = "jdbc:h2:./test"
DB.dataSource = new JdbcDataSource(url: url, user: 'sa', password: '')

// 创建表
// DB.sql.executeUpdate("""
//          create table book(id int,title varchar(50));
//          create table author(id int,name varchar(50));
// """)

// 查询表
// Connection connection = DB.dataSource.connection
// def tables = connection.metaData.getTables(connection.catalog, connection.schema, null, 'TABLE')
// def meta = tables.metaData
// def columnNames = []
// def rows = []
//
// meta.columnCount.times {
//     def index = it + 1
//     columnNames << meta.getColumnName(index)
// }
//
// while (tables.next()) {
//     def row = [:]
//     columnNames.each {
//         row[it] = tables.getString(it)
//     }
//     rows << row
// }
//
// println "共 ${rows.size()} 行"
// rows.each {
//     println it
// }

// 查询列
Connection connection = DB.dataSource.connection
def columns = connection.metaData.getColumns(connection.catalog, connection.schema, null, null)
def meta = columns.metaData
def names = []
def rows = []

meta.columnCount.times {
    def index = it + 1
    names << meta.getColumnName(index)
}

while (columns.next()) {
    def row = [:]
    names.each {
        row[it] = columns.getString(it)
    }
    rows << row
}

println "共 ${rows.size()} 行"
rows.each {
    println it
    // println JsonOutput.prettyPrint(JsonOutput.toJson(it))
}