#!/bin/bash

#Ensure ClassPATH set
if [ -z "$CLASSPATH" ]
then
    export CLASSPATH="./:/usr/share/java/mariadb-jdbc/mariadb-java-client.jar"
fi

#Starting the server
if [ -z "$1" ]
then 
    java -Djavax.net.ssl.keyStore=yourKEYSTORE -Djavax.net.ssl.keyStorePassword=quack1nce4^ -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol ChatServer 7777
else 
    java -Djavax.net.ssl.keyStore=yourKEYSTORE -Djavax.net.ssl.keyStorePassword=quack1nce4^ -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol ChatServer $1
fi
