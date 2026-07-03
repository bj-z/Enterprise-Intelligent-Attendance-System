package com.attendance.util;

/**
 * 达梦数据库适配工具类
 * <p>
 * 当从MySQL迁移到达梦8数据库时，需要处理以下兼容性问题：
 * <ol>
 *   <li>DDL差异：AUTO_INCREMENT → IDENTITY(1,1)</li>
 *   <li>数据类型差异：TINYINT → SMALLINT, DATETIME → TIMESTAMP</li>
 *   <li>函数差异：GROUP_CONCAT → WM_CONCAT, IFNULL → NVL</li>
 *   <li>分页差异：LIMIT offset,size → LIMIT size OFFSET offset (达梦也可用标准写法)</li>
 *   <li>外键语法：MySQL CASCADE → 达梦兼容，但需注意约束名长度</li>
 *   <li>VARCHAR长度：达梦上限4000，MySQL上限65535</li>
 *   <li>日期函数：NOW() → SYSDATE, CURRENT_TIMESTAMP → SYSTIMESTAMP</li>
 * </ol>
 * <p>
 * 适配步骤：
 * <ol>
 *   <li>复制init.sql为init_dameng.sql，修改DDL语法</li>
 *   <li>在application-dameng.yml中配置达梦数据源</li>
 *   <li>修改Hibernate方言为org.hibernate.dialect.DmDialect（需达梦官方驱动）</li>
 *   <li>替换pom.xml中MySQL驱动为达梦驱动</li>
 *   <li>检查所有Repository中自定义@Query的SQL语法</li>
 * </ol>
 * <p>
 * 当前项目已做好的达梦适配准备：
 * <ul>
 *   <li>实体类使用JPA注解，不直接写SQL建表</li>
 *   <li>Entity中使用@Column(length)而非依赖数据库默认值</li>
 *   <li>Repository使用Spring Data JPA方法命名查询，减少原生SQL</li>
 *   <li>application-dameng.yml已预留达梦数据库配置模板</li>
 * </ul>
 *
 * @author attendance-team
 */
public class DamengDialectHelper {

    private DamengDialectHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 获取达梦数据库对应的SQL方言类名
     */
    public static final String DAMENG_DIALECT = "org.hibernate.dialect.DmDialect";

    /**
     * 获取达梦数据库对应的驱动类名
     */
    public static final String DAMENG_DRIVER = "dm.jdbc.driver.DmDriver";

    /**
     * 达梦JDBC URL模板
     * jdbc:dm://{host}:{port}/{database}?schema={schema}
     */
    public static String buildDamengUrl(String host, int port, String database) {
        return String.format("jdbc:dm://%s:%d/%s", host, port, database);
    }

    /**
     * MySQL → 达梦 SQL 转换参考
     * 以下为常见需要修改的SQL片段：
     *
     * MySQL                     → 达梦
     * ─────────                → ─────────
     * AUTO_INCREMENT           → IDENTITY(1,1)
     * TINYINT(1)               → SMALLINT
     * DATETIME                  → TIMESTAMP
     * `表名`                    → "表名" (或去掉反引号)
     * ENGINE=InnoDB             → 删除
     * DEFAULT CHARSET=utf8mb4  → 删除
     * ON UPDATE CURRENT_TIMESTAMP → 使用触发器替代
     * VARCHAR(500)              → VARCHAR2(500)
     */
}
