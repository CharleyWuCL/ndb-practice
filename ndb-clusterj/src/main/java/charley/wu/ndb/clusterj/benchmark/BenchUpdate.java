package charley.wu.ndb.clusterj.benchmark;

import charley.wu.ndb.clusterj.module.Employee;
import com.mysql.clusterj.Session;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchUpdate extends AbstractBeanMark {

  @Override
  void doTest(Session session) {
    Employee employee = session.find(Employee.class, ID.getAndIncrement());
    employee.setFirst("Updated");
    employee.setLast("Updated");
    session.updatePersistent(employee);
  }
}
