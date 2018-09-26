package grace.datastore

import grace.app.GraceApp

class DBUtil {
    /**
     * 将属性名称编程数据库风格名称
     * @param propName
     * @return
     */
    static String toDbName(String propName) {
        propName = propName.uncapitalize()
        String result = ''
        propName.toCharArray().each {
            if (Character.isUpperCase(it)) {
                result += '_' + Character.toLowerCase(it)
            } else {
                result += it
            }
        }
        return result.toString()
    }

    /**
     * 将数据库风格命名转为属性命名
     * @param dataName
     * @return
     */
    static String toPropName(String dataName) {}

    static String limitString(int offset, int max) {
        String driver = GraceApp.instance.config.dataSource.driverClassName
        if (driver.contains('mysql')) return "limit ${offset},${max}"
        if (driver.contains('postgresql')) return "limit ${offset},${max}"
        throw new Exception("limit not support for ${driver}")
    }
}