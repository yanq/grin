package grace.datastore

import groovy.sql.Sql

trait Entity {
    Sql sql

    Sql getSql(){
        new Sql()
    }



}