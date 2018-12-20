package charley.wu.ndb.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class DataSourceHolder {

  private static DataSourceHolder instance = new DataSourceHolder();

  private HikariDataSource dataSource;

  private DataSourceHolder() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/ndbtest");
    config.setUsername("root");
    config.setPassword("root");
    config.setAutoCommit(true);
    config.setMaximumPoolSize(64);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    dataSource = new HikariDataSource(config);
  }

  public static DataSourceHolder instance() {
    return instance;
  }

  public HikariDataSource getDataSource() {
    return dataSource;
  }
}
