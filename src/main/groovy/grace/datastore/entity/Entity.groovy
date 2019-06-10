package grace.datastore.entity

import grace.common.Params

/**
 * 提供实体类的基本功能
 */
trait Entity<D> {
    List errorList = []

    /**
     * get
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static D get(Serializable id) {
        if (!id) return null
        EntityImpl.get(this, id)
    }

    /**
     * get all
     * 获取多条
     * @param ids
     * @return
     */
    static List<D> getAll(Serializable... ids) {
        if (!ids) return []
        new EntityImpl.Where(whereSql: "id in (${ids.collect { '?' }.join(',')})", params: ids.toList(), entityClass: this).list()
    }

    static List<D> getAll(List<Serializable> ids) {
        if (!ids) return []
        new EntityImpl.Where(whereSql: "id in (${ids.collect { '?' }.join(',')})", params: ids, entityClass: this).list()
    }

    /**
     * 列表
     * @param params
     * @return
     */
    static List<D> list(Map params = null) {
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
     * 刷新
     * @return
     */
    boolean refresh() {
        EntityImpl.refresh(this)
    }

    /**
     * where 自定义条件查询
     * @param sql
     */
    static EntityImpl.Where<D> where(String sql, List params = []) {
        new EntityImpl.Where(whereSql: sql, params: params ?: [], entityClass: this)
    }

    static EntityImpl.Where<D> where(String sql, Object... params) {
        new EntityImpl.Where(whereSql: sql, params: params.toList(), entityClass: this)
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
     * 错误表 (含提示)
     * @return
     */
    Map getErrors() {
        def errorMap = [:], constrains = constraintMap
        errorList.each {
            errorMap[it[0]] = constrains[it[0]]?.comment
        }
        return errorMap
    }

    /**
     * 属性列表
     * @return
     */
    static List<String> getProps() {
        EntityImpl.findPropertiesToPersist(this)
    }

    /**
     * 转换成 Map
     * @return
     */
    Map toMap(List<String> excludes) {
        EntityImpl.toMap(excludes,this)
    }

    /**
     * 绑定参数,实例
     * @param params
     * @return
     */
    D bind(Params params) {
        EntityImpl.bind(this, params)
    }

    /**
     * 从参数产生实例
     * @param params
     * @return
     */
    static from(Params params) {
        EntityImpl.bind(this, params)
    }
}