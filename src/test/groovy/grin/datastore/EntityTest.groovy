package grin.datastore

import groovy.sql.Sql
import org.h2.jdbcx.JdbcDataSource

import java.time.LocalDateTime

import static grin.datastore.validate.Validators.*

class EntityTest extends GroovyTestCase {

    /**
     * 书
     */
    class Book implements Entity<Book> {
        Long id
        String title
        String author = 'Yan'
        String description = ''
        double price
        String forPeople = '青少年'
        Date publishAt = new Date()
        LocalDateTime dateCreated
        LocalDateTime lastUpdated

        static transients = []
        static constraints = [
                title      : [grin.datastore.validate.Validators.nullable(), grin.datastore.validate.Validators.blank(),],
                author     : [minLength(3), maxLength(5, '太长了'), matches('Y.{2}')],
                description: [grin.datastore.validate.Validators.nullable(false), grin.datastore.validate.Validators.blank(false),],
                price      : [max(5.5), min(1.0),
                              validator('就是不通') { String fieldName, Object fieldValue, Entity<?> entity ->
                                  return false
                              }],
                forPeople  : [inList(['儿童', '青少年', '成年人'])]
        ]
    }

    void testValidator() {
        Book book = new Book(price: 3)
        book.validate()
        println(book.errors)
    }

    void testGetConstraints() {
        println(Utils.getEntityConstraintValue(Book, 'title', 'Nullable'))
        println(Utils.getEntityConstraintValue(Book, 'author', 'MaxLength'))
        println(Utils.getEntityConstraintValue(Book, 'forPeople', 'InList'))
    }

    void testDDL() {
        DB.dataSource = new JdbcDataSource(url: "jdbc:h2:~/h2db/test;MODE=PostgreSQL", user: 'sa', password: '')

        println("Tables")
        DDL.tablesMetaData().each { println(it) }
        println("Columns")
        DDL.columnsMetaData().each {println(it)}

        // println DDL.dbTables()
        // def s = DDL.entityCreateSql(Book)
        // DB.withSql { Sql sql ->
        //     sql.executeUpdate(s)
        // }
    }
}
