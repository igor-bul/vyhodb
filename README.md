# Welcome to vyhodb

**vyhodb** database is the right choice when you have complex data model with sophisticated business logic, 
which have to be executed in ACID transactions with SERIALIZABLE isolation level. In other words, it is best for 
enterprise level applications.

**vyhodb** is written on Java and currently can be used by Java applications only.

**vyhodb** database has the following features:

  * [Network model](https://en.wikipedia.org/wiki/Network_model) with schemaless approach
  * [ACID](https://en.wikipedia.org/wiki/ACID) transactions with SERIALIZABLE isolation level
  * Three running modes (stand-alone, embedded, local)
  * Stored procedures (written in Java)
  * **Indexes** for fast searching child records
  * **Functions API** for traversing over data model
  * **ONM API** for mapping Java objects to vyhodb data model

**vyhodb** is also easy to use:

  * Simple configuring (one property file)
  * Auto expanding storage
  * “Hot” backups
  * Master/Slave replication
  * Load balancer and non-fault-tolerant cluster
  * Small footprint (1,4 mb)
 
