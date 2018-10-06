package grace.datastore.entity

import grace.controller.request.Params

/**
 * 提供实体类的基本功能
 */
trait Entity<D> {
    List errors = []

    /**
     * get
     * todo 如果带着 D ，编译的时候异常，找不到类型 D，这不太科学。
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static get(Serializable id) {
        EntityImpl.get(this, id)
    }

    /**
     * 列表
     * @param params
     * @return
     */
    static list(Map params = null) {
        EntityImpl.list(this, params)
    }

    /**
     * 计数
     * @return
     */
    static int count() {
        EntityImpl.count(this)
    }

    /**
     * 保存，更新或者插入
     * @return
     */
    D save() {
        EntityImpl.save(this)
    }

    /**
     * 删除
     * @return
     */
    boolean delete() {
        EntityImpl.delete(this)
    }

    /**
     * where 自定义条件查询
     * @param sql
     */
    static EntityImpl.Where where(String sql, List params = []) {
        new EntityImpl.Where(whereSql: sql, params: params, entityClass: this)
    }

    /**
     * 约束 Map
     * @return
     */
    static Map getConstraintMap() {
        EntityImpl.getConstraintMap(this)
    }

    /**
     * 验证约束
     * @return
     */
    boolean validate() {
        EntityImpl.validate(this)
    }

    /**
     * 属性列表
     * @return
     */
    static List<String> getProps(){
        EntityImpl.findPropertiesToPersist(this)
    }

    /**
     * 绑定参数
     * @param params
     * @return
     */
    static bind(Params params){
        EntityImpl.bind(this,params)
    }
}