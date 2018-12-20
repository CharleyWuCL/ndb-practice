

## MySQL NDB Cluster性能测试

### NDB介绍

MySQL集群是一种在无共享架构（SNA，Share Nothing Architecture）系统里应用内存数据库集群的技术。这种无共享的架构可以使得系统使用低廉的硬件获取高的可扩展性。
MySQL集群是一种分布式设计，目标是要达到没有任何单点故障点。因此，任何组成部分都应该拥有自己的内存和磁盘。任何共享存储方案如网络共享，网络文件系统和SAN设备是不推荐或不支持的。通过这种冗余设计，MySQL声称数据的可用度可以达到99.999%。
实际上，MySQL集群是把一个叫做NDB的内存集群存储引擎集成与标准的MySQL服务器集成。它包含一组计算机，每个都跑一个或者多个进程，这可能包括一个MySQL服务器，一个数据节点，一个管理服务器和一个专有的一个数据访问程序。



#### 成员角色

- **管理节点（MGM）**
  用来实现整个集群的管理，理论上一般只启动一个，而且宕机也不影响 cluster 的服务，这个进程只在cluster 启动以及节点加入集群时起作用， 所以这个节点不是很需要冗余，理论上通过一台服务器提供服务就可以了。通过 ndb_mgmd 命令启动，使用 config.ini 配置文件。

- **数据节点（NDB）**
  用来存储数据，可以和管理节点(MGM)、 用户端节点(API)处在不同的机器上，也可以在同一个机器上面，集群中至少要有一个DB节点，2个以上 时就能实现集群的高可用保证，DB节点增加时，集群的处理速度会变慢。通过 ndbd 命令启动，第一次创建好cluster DB 节点时，需要使用 –init参数初始化。

- **客户端节点（API）**
  通过他实现 Cluster DB 的访问，这个节点也可以是普通的 mysqld 进程，也可以是空节点，提供给外界连接，如JAVA程序。 需要在配置文件中配置ndbcluster 指令打开 NDB Cluster storage engine 存储引擎，增加 API 节点会提高整个集群的并发访问速度和整体的吞吐量，该节点 可以部署在Web应用服务器上，也可以部署在专用的服务器上，也开以和DB部署在 同一台服务器上。


#### 网络拓扑

![ndb](https://dev.mysql.com/doc/refman/5.7/en/images/multi-comp-1.png)



### NDB安装

这里使用`Auto-Installer`安装工具安装，自带GUI操作界面，比较方便。



**下载地址**

请根据MySQL版本下载对应的NDB安装包，目前

地址：https://dev.mysql.com/downloads/cluster/

包：mysql-cluster-community-7.6.8-1.el7.x86_64.rpm-bundle.tar，mysql-cluster-gpl-7.6.8-el7-x86_64.tar.gz



**安装步骤**

- 解压两个包

  ```shell
  $ tar -xf mysql-cluster-community-7.6.8-1.el7.x86_64.rpm-bundle.tar
  $ tar -zxf mysql-cluster-gpl-7.6.8-el7-x86_64.tar.gz
  ```

- 删除已有的mysql安装包

  ```shell
  # 停止已有的MySQL服务
  $ service mysql stop
  # 查找rpm包并删除
  $ rpm -qa | grep -i mysql
  $ rpm -ev <包名> --nodeps
  # 查找遗留文件并删除
  $ whereis mysql
  $ find / -name mysql
  ```

- 安装Auto Installer工具并启动，安装过程中会需要其他依赖包，按提示安装即可

  ```shell
  $ yum install mysql-cluster-community-common-7.6.8-1.el7.x86_64.rpm
  $ yum install mysql-cluster-community-libs-7.6.8-1.el7.x86_64.rpm
  $ yum install mysql-cluster-community-client-7.6.8-1.el7.x86_64.rpm
  $ yum install mysql-cluster-community-server-7.6.8-1.el7.x86_64.rpm
  $ yum install mysql-cluster-community-auto-installer-7.6.8-1.el7.x86_64.rpm
  ```

- 将必要的文件拷到相应目录下

  ```shell
  $ cd /mysql-cluster-gpl-7.5.12-el7-x86_64
  $ cp bin/ndb_mgm* /usr/local/bin
  $ cp bin/ndbmtd  /usr/local/bin
  $ cp bin/mysqld  /usr/local/bin
  $ cd /usr/local/bin
  $ chmod +x ndb_mgm*
  $ chmod +x ndbmtd
  $ chmod +x mysqld
  ```

- 创建`mysql`用户及组

  ```shell
  $ groupadd mysql
  $ useradd -g mysql mysql
  # 设置密码，mysql/mysql
  $ passwd mysql
  ```

- 启动`auto-installer`安装工具

  ```shell
  $ ./usr/bin/ndb_setup.py 
  ```



**Auto-Installer图形化安装步骤**

- 第一次登陆输入一个密码，这里随便填，并且需要填写一个配置文件名，随便填，如test。

  ![](/install/auto-installer-1.png)

- 填写集群内机器IP及登陆方式，其他默认即可。

  ![](/install/auto-installer-2.png)

- 连接主机成功后会显示主机信息。

  ![](/install/auto-installer-3.png)

- 节点管理，可以创建或删除节点。

  ![](/install/auto-installer-4.png)

- 创建好之后查看节点分布情况以及管理端口目录等等。

  ![](/install/auto-installer-5.png)

- 部署启动或停止集群。

  ![](/install/auto-installer-6.png)

- 各组件运行命令行

  ```shell
  # ndb_mgm
  $ ndb_mgmd --initial --ndb-nodeid=49 --config-dir=/home/mysql/MySQL_Cluster/49/ --config-file=/home/mysql/MySQL_Cluster/49/config.ini
  # ndbd
  $ ndbmtd --ndb-nodeid=1 --ndb-connectstring=10.182.172.148:1186
  # mysqld
  $ mysqld --defaults-file=/home/mysql/MySQL_Cluster/54/my.cnf
  ```

- 使用命令行查看集群状态

  ```shell
  $ ndb_mgm
  -- NDB Cluster -- Management Client --
  ndb_mgm> show
  Connected to Management Server at: localhost:1186
  Cluster Configuration
  ---------------------
  [ndbd(NDB)]     1 node(s)
  id=1    @10.182.173.158  (mysql-5.7.24 ndb-7.6.8, Nodegroup: 0, *)
  
  [ndb_mgmd(MGM)] 1 node(s)
  id=49   @10.182.173.158  (mysql-5.7.24 ndb-7.6.8)
  
  [mysqld(API)]   3 node(s)
  id=53   @10.182.173.158  (mysql-5.7.24 ndb-7.6.8)
  id=231 (not connected, accepting connect from 10.182.173.158)
  id=233 (not connected, accepting connect from 10.182.173.158)
  ```



**连接Mysql客户端及建库建表**

```shell
# root用户默认没有密码
$ mysql -uroot -p -h 127.0.0.1
# 设置root用户密码，密码为root，并开启远程访问权限
mysql> update user set password=PASSWORD('root') where User='root';
mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root';
mysql> flush privileges;
```

```sql
-- 设置全局存储引擎
mysql> set default_storage_engine= ndbcluster;
mysql> show variables like '%default_storage_engine%';
+------------------------+------------+
| Variable_name          | Value      |
+------------------------+------------+
| default_storage_engine | ndbcluster |
+------------------------+------------+

-- 建库
CREATE SCHEMA `ndbtest` DEFAULT CHARACTER SET utf8 ;
-- 建表
CREATE TABLE `ndbtest`.`employee` (
    `id` BIGINT(20) NOT NULL PRIMARY KEY,
    `first` VARCHAR(64) DEFAULT NULL,
    `last` VARCHAR(64) DEFAULT NULL,
    `municipality` VARCHAR(64) DEFAULT NULL,
    `started` DATE DEFAULT NULL,
    `ended` DATE DEFAULT NULL,
    `department` INT NOT NULL DEFAULT 1,
    -- UNIQUE KEY idx_u_hash (`last`,`first`) USING HASH,
    KEY idx_municipality (`municipality`)
) ENGINE=NDBCLUSTER;
```



### ClusterJ API 使用

**概念介绍**

详见官方文档：[The ClusterJ API and Data Object Model](https://dev.mysql.com/doc/ndbapi/en/mccj-overview-clusterj-object-models.html)

**接口定义**

定义一个数据库对象接口，不用的字段可以不映射。

```java
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
```

**基础用法**

```java
public class Example {
    public static void main(String[] args) {
        //初始化Session Factory
        Map<String, String> props = new HashMap<>();
        props.put("com.mysql.clusterj.connectstring", "127.0.0.1:1186");
        props.put("com.mysql.clusterj.database", "ndbtest");
        //API Node Id
        props.put("com.mysql.clusterj.connection.pool.nodeids", "53");
        SessionFactory factory = ClusterJHelper.getSessionFactory(props);
        Session session = factory.getSession();
        
        //Insert
        Employee newEmployee = session.newInstance(Employee.class);
        newEmployee.setId(1);
        newEmployee.setFirst("John");
    	newEmployee.setLast("Jones");
        newEmployee.setCity("municipality");
        session.persist(employee);
        
        //Select
        Employee employee = session.find(Employee.class, 1);
    	employee.getFirst();
        
        //Update
        Employee employee = session.find(Employee.class, 1);
        employee.setFirst("Updated");
        employee.setLast("Updated");
        session.updatePersistent(employee);
        
        //Delete
        Employee employee = session.find(Employee.class, 1);
        session.deletePersistent(employee);
    }
}
```

**高级使用**

可以做批量插入、删除、修改、`QueryBuilder`等操作。

详见官方文档：[Usage of ClusterJ](https://dev.mysql.com/doc/ndbapi/en/mccj-using-clusterj.html)



### JDBC使用

因为集群提供了`Mysqld`服务，所以我们可以通过传统JDBC方式接入NDB集群。

```java
public class Example {
    private static final SQL = "select * from employee where id = ?;"；
    
    public static void main(String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/ndbtest");
        config.setUsername("root");
        config.setPassword("root");
        config.setAutoCommit(true);
        config.setMaximumPoolSize(64);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource dataSource = new HikariDataSource(config);
        
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement statement = conn.prepareStatement(SQL);
        ) {
          statement.setLong(1, 12);
          statement.executeQuery();
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }
}
```