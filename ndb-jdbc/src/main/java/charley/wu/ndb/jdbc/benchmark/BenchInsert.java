package charley.wu.ndb.jdbc.benchmark;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchInsert extends AbstractBeanMark {

  @Override
  String sql() {
    return "insert into employee (id, first, last, municipality) values (?, 'Inserted', 'Inserted', 'municipality');";
  }

  @Override
  void doTest(PreparedStatement statement) throws SQLException {
    statement.setLong(1, ID.getAndIncrement());
    statement.executeUpdate();
  }
}
