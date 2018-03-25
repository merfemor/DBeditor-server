# DBeditor-server
API server of DBeditor written on Play Framework.

# Install

You need `sbt` to build project and `rabbitmq`.

## PostgreSQL configuration

Install PostgreSQL (tested on 9.5.1 version downloaded [here](https://www.enterprisedb.com/thank-you?anid=209615)).

Add PostgreSQL to PATH:
```
echo 'export PATH=/opt/PostgreSQL/9.5/bin/:$PATH' >> ~/.profile
```
Create database and orm user:
```
$ sudo -u postgres psql
postgres=# create database dbeditor;
postgres=# create user orm with password '123456';
postgres=# grant all on database dbeditor to orm;
```

# Run

At first you need to run RabbitMQ server:
```
# rabbitmq-server
```

Run Play server:
```
sbt run
```
