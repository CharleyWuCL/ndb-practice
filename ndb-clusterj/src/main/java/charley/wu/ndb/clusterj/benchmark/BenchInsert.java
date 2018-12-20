package charley.wu.ndb.clusterj.benchmark;

import charley.wu.ndb.clusterj.module.Employee;
import com.mysql.clusterj.Session;
import java.util.Date;

/**
 * Desc...
 *
 * @author Charley Wu
 */
public class BenchInsert extends AbstractBeanMark {

  @Override
  void doTest(Session session) {
    long id = ID.getAndIncrement();
    Employee employee = session.newInstance(Employee.class);
    employee.setId(id);
    employee.setFirst("Inserted");
    employee.setLast("Inserted");
    employee.setCity("municipality");
    employee.setStarted(new Date());
    employee.setEnded(new Date());
    employee.setDepartment(2);
    session.persist(employee);
  }
}
