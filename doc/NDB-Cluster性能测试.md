## NDB-Cluster性能测试

本次性能测试只比较同等条件下，Mysql Cluster NDB的两种Java访问方式的性能差异，并不涉及NDB与单Mysql实例的性能对比。

### 集群概要

**拓扑结构**

![ndb](https://dev.mysql.com/doc/refman/5.7/en/images/multi-comp-1.png)

**NDB集群主机配置**

| 项目     | 参数           |
| -------- | -------------- |
| 主机类型 | 虚拟机         |
| 操作系统 | Oracle Linux 7 |
| CPU      | 4核            |
| 内存     | 12G            |
| 磁盘     | 10G            |

**测试程序主机配置**

| 项目     | 参数           |
| -------- | -------------- |
| 主机类型 | 虚拟机         |
| 操作系统 | Oracle Linux 7 |
| CPU      | 4核            |
| 内存     | 8G             |
| 磁盘     | 10G            |



### 测试背景

为检测NDB的`Java`两种访问方式的效率，故通过`Jmeter`工具分别对`ClusterJ`、`JDBC`进行性能测试对比。为保证两种方式的对等性，`ClusterJ`只连一个API Node节点，并设置`Session`最大事务数为64；JDBC只连一个`Mysqld`服务端，并设置连接池大小为64.

**系统架构图**

ClusterJ是通过JNI接口直接与NDB的数据节点交互，而JDBC则需要通过Mysqld服务进行数据操作。因此理论上ClusterJ的效率要好与JDBC。

![](/arch/arch.png)

**ClusterJ测试工程**

参数配置：

```java
Map<String, String> props = new HashMap<>();
props.put("com.mysql.clusterj.connectstring", "127.0.0.1:1186");
props.put("com.mysql.clusterj.database", "ndbtest");
props.put("com.mysql.clusterj.connection.pool.nodeids", "53");
props.put("com.mysql.clusterj.max.transactions", "64");
```

核心代码：

```java
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
```

**JDBC测试工程**

参数配置：

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/ndbtest");
config.setUsername("root");
config.setPassword("root");
config.setAutoCommit(true);
config.setMaximumPoolSize(64);
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
```

核心代码：

```java
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
```

### 测试结果

**基础数据**

ClusterJ访问方式CRUD随线程增加TPS变化数据

| Thread Nums | 1      | 2      | 4       | 8       | 16      | 32      | 64      |
| ----------- | ------ | ------ | ------- | ------- | ------- | ------- | ------- |
| Insert      | 785.5  | 1910.4 | 4379.1  | 8067.3  | 14347.7 | 23840.8 | 32547.4 |
| Select      | 2513.3 | 5834.3 | 10168.1 | 19461.6 | 31622.1 | 39215.3 | 42834.8 |
| Update      | 522.4  | 1294.4 | 3105.4  | 5512.4  | 9937.7  | 16443.7 | 24056.1 |
| Delete      | 572.5  | 1436.9 | 3284.2  | 5940.7  | 10827.8 | 17632.7 | 25755.6 |

ClusterJ访问方式CRUD随线程增加CUP Load变化数据

| Thread   Nums | 1    | 2    | 4    | 8    | 16   | 32   | 64   |
| ------------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| Insert        | 5.5  | 9.5  | 18.5 | 24.3 | 28   | 32   | 35   |
| Select        | 4    | 7    | 10   | 15   | 18.3 | 19   | 16.3 |
| Update        | 8.5  | 16   | 18   | 24.5 | 28.8 | 33.8 | 37.5 |
| Delete        | 5.5  | 11.3 | 16   | 23.5 | 27   | 31.3 | 38.3 |

JDBC访问方式CRUD随线程增加TPS变化数据

| Thread Nums | 1      | 2      | 4      | 8       | 16      | 32      | 64      |
| ----------- | ------ | ------ | ------ | ------- | ------- | ------- | ------- |
| Insert      | 443.2  | 1051.3 | 2546.3 | 5187.9  | 9304.8  | 14925.8 | 21054.6 |
| Select      | 1013.1 | 2345.4 | 5555.3 | 10550.4 | 16860.4 | 21452.9 | 23496.7 |
| Update      | 521.3  | 1193.1 | 2717.2 | 5618.7  | 10418   | 16751   | 23039.1 |
| Delete      | 551.3  | 1223.6 | 2851.6 | 5991.4  | 10945   | 17887.7 | 24930.1 |

JDBC访问方式CRUD随线程增加CUP Load变化数据

| Thread   Nums | 1    | 2    | 4    | 8    | 16   | 32   | 64   |
| ------------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| Insert        | 3.8  | 7    | 12.5 | 20   | 25   | 28.8 | 40   |
| Select        | 8.5  | 14.5 | 26.3 | 45   | 67.5 | 80   | 85   |
| Update        | 3    | 6    | 12.5 | 23.8 | 40   | 65   | 77.5 |
| Delete        | 2.8  | 6    | 12.5 | 23.3 | 37.3 | 57.5 | 70   |

**TPS趋势对比图**

Insert操作

![](/bench/benckmark-insert.png)

Select操作

![](/bench/benckmark-select.png)

Update操作

![](/bench/benckmark-update.png)

Delete操作

![](/bench/benckmark-delete.png)



**CPU Load趋势图**

ClusterJ CRUD操作

![](/bench/clusterj-cpu.png)

JDBC CRUD操作

![](/bench/jdbc-cpu.png)

### 结论分析

由上述几张图可知，Select操作ClusterJ方式要比JDBC方式快两倍左右，Insert操作快1.5倍左右，而Update、Delete操作性能基本相当。

ClusterJ访问方式基于JNI接口直接访问数据节点NDB Node，因此比JDBC方式效率要高很多。对于Update、Delete操作，ClusterJ需要通过Session先查询出来，然后再对对象进行下一步操作，实际上做了两步操作，因此在效率上比没有出现预想的比JDBC高。

在测试集群中，NDB Cluster的数据节点有两个，MySQL服务端节点一个。使用ClusterJ是直连数据节点，压力是均匀分布在两台机器上；而通过JDBC连接时，压力全部都在MySQL节点上。因此，使用ClusterJ要比JDBC方式对数据库主机的压力要小得多。

但是ClusterJ同时只能操作一个表对象，不能进行多张表之间的Join操作，因此局限性也是显而易见的。