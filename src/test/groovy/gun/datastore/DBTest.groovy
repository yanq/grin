package gun.datastore

import org.h2.jdbcx.JdbcDataSource

String url = "jdbc:h2:mem:test"
// String url = "jdbc:h2:~/test"
DB.dataSource = new JdbcDataSource(url: url, user: 'sa', password: '')

println(DB.sql.dataSource.connection.catalog)
