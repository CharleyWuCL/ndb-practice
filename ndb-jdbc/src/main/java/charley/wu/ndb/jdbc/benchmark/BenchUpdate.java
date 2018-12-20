package charley.wu.ndb.jdbc.benchmark;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchUpdate extends AbstractBeanMark {

  @Override
  String sql() {
    return "update employee set first = 'Updated', last = 'Updated' where id = ?;";
  }

  @Override
  void doTest(PreparedStatement statement) throws SQLException {
    statement.setLong(1, ID.getAndIncrement());
    statement.executeUpdate();
  }
}
