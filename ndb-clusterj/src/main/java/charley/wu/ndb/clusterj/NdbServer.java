package charley.wu.ndb.clusterj;

import com.mysql.clusterj.ClusterJHelper;
import com.mysql.clusterj.SessionFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class NdbServer {

  private static NdbServer instance = new NdbServer();
  private SessionFactory factory;

  private NdbServer() {
    Map<String, String> props = new HashMap<>();
    props.put("com.mysql.clusterj.connectstring", "127.0.0.1:1186");
    props.put("com.mysql.clusterj.database", "ndbtest");
    props.put("com.mysql.clusterj.connection.pool.nodeids", "53");
    props.put("com.mysql.clusterj.max.transactions", "64");
//    props.put("com.mysql.clusterj.connection.pool.size", "4");
    factory = ClusterJHelper.getSessionFactory(props);
  }

  public static NdbServer instance() {
    return instance;
  }

  public SessionFactory getFactory() {
    return factory;
  }
}
