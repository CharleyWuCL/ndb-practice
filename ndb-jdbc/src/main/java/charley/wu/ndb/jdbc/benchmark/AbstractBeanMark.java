package charley.wu.ndb.jdbc.benchmark;

import charley.wu.ndb.jdbc.DataSourceHolder;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public abstract class AbstractBeanMark extends AbstractJavaSamplerClient {

  protected static final AtomicLong ID = new AtomicLong(1);
  protected final HikariDataSource dataSource = DataSourceHolder.instance().getDataSource();

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    SampleResult sampleResult = new SampleResult();
    sampleResult.sampleStart();
    try (
        Connection conn = dataSource.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql());
    ) {
      doTest(statement);
      sampleResult.setSuccessful(true);
    } catch (Exception e) {
      e.printStackTrace();
      sampleResult.setSuccessful(false);
    }
    sampleResult.sampleEnd();
    return sampleResult;
  }

  abstract String sql();

  abstract void doTest(PreparedStatement statement) throws SQLException;
}
