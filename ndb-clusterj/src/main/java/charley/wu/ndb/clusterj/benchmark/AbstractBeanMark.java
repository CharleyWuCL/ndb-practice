package charley.wu.ndb.clusterj.benchmark;

import charley.wu.ndb.clusterj.NdbServer;
import com.mysql.clusterj.Session;
import com.mysql.clusterj.SessionFactory;
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
  protected static final SessionFactory factory = NdbServer.instance().getFactory();
  private ThreadLocal<Session> threadLocal = ThreadLocal.withInitial(() -> factory.getSession());

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    SampleResult sampleResult = new SampleResult();
    sampleResult.sampleStart();
    try {
      doTest(threadLocal.get());
      sampleResult.setSuccessful(true);
    } catch (Exception e) {
      e.printStackTrace();
      sampleResult.setSuccessful(false);
    }
    sampleResult.sampleEnd();
    return sampleResult;
  }

  abstract void doTest(Session session);
}
