package gun.datastore

import gun.datastore.validate.Validators

import static gun.datastore.validate.Validators.*

class ValidatorsTest extends GroovyTestCase {

    /**
     * 书
     */
    class Book implements Entity<Book> {
        Long id
        String title
        String author = 'Yan'
        String description = ''
        double price

        static transients = []
        static constraints = [
                title      : [Validators.nullable(), Validators.blank(),],
                author     : [minLength(3), maxLength(5, '太长了'), matches('Y.{2}')],
                description: [Validators.nullable(false), Validators.blank(false),],
                price      : [max(5.5), min(1.0),
                              validator('就是不通') { String fieldName, Object fieldValue, Entity<?> entity ->
                                  return false
                              }]
        ]
    }

    void testMin() {
        Book book = new Book(price: 3)
        book.validate()
        print(book.errors)
    }

    void testMax() {
        assert true
    }


    void testValidator() {
    }
}
