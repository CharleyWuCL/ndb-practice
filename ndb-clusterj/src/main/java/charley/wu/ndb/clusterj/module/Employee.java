package charley.wu.ndb.clusterj.module;

import com.mysql.clusterj.annotation.Column;
import com.mysql.clusterj.annotation.Index;
import com.mysql.clusterj.annotation.PersistenceCapable;
import com.mysql.clusterj.annotation.PrimaryKey;
import java.util.Date;

/**
 * Desc...
 *
 * @author Charley Wu
 */
@PersistenceCapable(table = "employee")
@Index(name = "PRIMARY")
public interface Employee {

  @PrimaryKey
  long getId();

  void setId(long id);

  String getFirst();

  void setFirst(String first);

  String getLast();

  void setLast(String last);

  @Column(name = "municipality")
  @Index(name = "idx_municipality")
  String getCity();

  void setCity(String city);

  Date getStarted();

  void setStarted(Date date);

  Date getEnded();

  void setEnded(Date date);

  Integer getDepartment();

  void setDepartment(Integer department);
}
