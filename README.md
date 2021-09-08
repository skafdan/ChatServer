# ChatServer
A simple terminal chat-server and terminal client written in Java that uses TCP 
and TLS encryption.

## Dependencies 

- `JDK 16` - Written using `openjdk-jdk-16`, tested `JDk-8` also works.
- `mariadb-JDBC 2.7.3` this can be downloaded using your package manager or directly from the [website](https://downloads.mariadb.com/Connectors/java/connector-java-2.7.3/)
- `keytool` for generating TLS certificate
- `Web sever` - tested with `apache` and `nginx`
- `make`

## Installation 

Install all the required dependencies and run this command to link the mariadb-jdbc connection to your java runtime.
```
# ln -s /usr/share/java/mariadb-jdbc/mariadb-java-client.jar /usr/lib/jvm/default-runtime/jre/lib/ext
```
If you downloaded the `mariadb-jdbc` connecter manually place it into directory first.
```
/usr/share/java/mariadb-jdbc/mysql-connector-java-bin-2.7.3.jar
```

Git clone the repository and run the make command.
```
$ git clone https://github.com/skafdan/ChatServer

$ cd /path/to/ChatServer

$ make
```

## Usage
Run the installation script `startServer.sh` as root.
```
/path/to/ChatServer/ # ./startServer.sh
```
It can also be run with an optional port number, however it will use the port `7777`  if not provided.

```
/path/to/ChatServer/ # ./startServer.sh [port]
```

The script generates the TLS certificate and copies it the to default root of the 
webserver `/var/www/html/` to be downloaded by the client when they connect.

### data.properties
The authentication and address for the database are imported using java properties library, as such
a `data.properties` file with 3 key-value pairs needs to be in the root directory of the project
```
host=jdbc::mysql://<address>/chatserver
username=<username>
password=<password>
```