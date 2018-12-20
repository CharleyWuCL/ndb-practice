package charley.wu.ndb.jdbc.benchmark;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchSelect extends AbstractBeanMark {


  @Override
  String sql() {
    return "select id, first, last, municipality, started, ended, department from employee where id = ?;";
  }

  @Override
  void doTest(PreparedStatement statement) throws SQLException {
    statement.setLong(1, ID.getAndIncrement());
    statement.executeQuery();
  }
}
