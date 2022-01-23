package gun.util

import gun.datastore.entity.Entity
import gun.datastore.entity.EntityUtil

import java.text.SimpleDateFormat

/**
 * Grace 扩展类的实例方法
 */
class GraceExtension {
    /**
     * 实体列表获取关联实体
     * @param list
     * @param names
     */
    public static void fetch(List list,String... names){
        EntityUtil.fetch(list,names)
    }

    public static void fetch(Entity entity, String... names){
        EntityUtil.fetch([entity],names)
    }

    /**
     * Date format 方法
     * groovy 2.5 以后，就不支持了，这不科学
     * @param date
     * @param dateFormat
     * @return
     */
    public static String format(Date date,String dateFormat = 'yyyy-MM-dd HH:mm'){
        new SimpleDateFormat(dateFormat).format(date)
    }
}
