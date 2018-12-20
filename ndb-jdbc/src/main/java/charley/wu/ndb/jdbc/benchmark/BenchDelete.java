package charley.wu.ndb.jdbc.benchmark;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchDelete extends AbstractBeanMark {

  @Override
  String sql() {
    return "Delete from employee where id = ?;";
  }

  @Override
  void doTest(PreparedStatement statement) throws SQLException {
    statement.setLong(1, ID.getAndIncrement());
    statement.executeUpdate();
  }
}
