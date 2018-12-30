#!/bin/sh
echo $configuration

cat hosts >> /etc/hosts

echo additional hosts added

java -Dfile.encoding=UTF-8 -classpath bin:lib/JDA-3.3.0_260-withDependencies.jar:lib/slf4j-api-1.7.25.jar:lib/gradle-wrapper.jar:lib/commons-io-2.5.jar:lib/jsoup-1.10.3.jar:lib/lavaplayer-1.2.47.jar:lib/unirest-java-1.3.0.jar:lib/org-apache-commons-logging.jar:lib/httpclient-4.5.3.jar:lib/httpcore-4.4.7.jar:lib/jackson-core-2.9.1.jar:lib/jackson-databind-2.9.1.jar:lib/jackson-annotations-2.9.1.jar fr.partipirate.discord.bots.congressus.CongressusBot $configuration
