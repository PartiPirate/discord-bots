#!/bin/sh

echo "Start compilation"

sudo rm -rf "$PWD"/java-project/bin/*
sudo docker run --rm -v "$PWD"/java-project/:/usr/src/myapp/ -w /usr/src/myapp openjdk:8 javac -classpath lib/commons-io-2.5.jar:lib/gradle-wrapper.jar:lib/httpclient-4.5.3.jar:lib/httpcore-4.4.7.jar:lib/jackson-annotations-2.9.1.jar:lib/jackson-core-2.9.1.jar:lib/jackson-databind-2.9.1.jar:lib/JDA-3.3.0_260-withDependencies.jar:lib/jsoup-1.10.3.jar:lib/lavaplayer-1.2.39.jar:lib/lavaplayer-1.2.47.jar:lib/org-apache-commons-logging.jar:lib/slf4j-api-1.7.25.jar:lib/unirest-java-1.3.0.jar -sourcepath src -d bin/ $(find */src/* | grep .java | sed -e 's/java-project\/src/src/')

echo "End compilation"

