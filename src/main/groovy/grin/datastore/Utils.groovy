package grin.datastore
/**
 * 实体类工具包
 */
class Utils {
    static List<String> CONSTRAINT_NAMES = ['Nullable', 'MaxLength', 'InList']

    /**
     * 或者实体类的某个约束的
     * @param entityClass
     * @param propertyName
     * @param constraintName in
     * @return
     */
    static Object getEntityConstraintValue(Class<Entity> entityClass, String propertyName, String constraintName) {
        if (constraintName in CONSTRAINT_NAMES) {
            List<grin.datastore.validate.Validator> validatorList = entityClass.constraints[propertyName]
            return validatorList?.find { it.class.simpleName == constraintName }?.value
        } else {
            throw new Exception("不支持的约束名称 ${constraintName}，仅支持 ${CONSTRAINT_NAMES}")
        }
    }
}
