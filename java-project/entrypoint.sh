#!/bin/sh
echo $configuration

cat hosts >> /etc/hosts

echo additional hosts added

java -Dfile.encoding=UTF-8 -classpath bin:lib/JDA-5.0.0-beta.10-withDependencies.jar:lib/json-20220924.0.0.jar:lib/slf4j-api-1.7.25.jar:lib/gradle-wrapper.jar:lib/unirest-java-1.3.0.jar:lib/org-apache-commons-logging.jar:lib/httpcore-4.4.7.jar:lib/jackson-annotations-2.9.1.jar:lib/base64-2.3.9.jar:lib/commons-io-2.6.jar:lib/httpclient-4.5.6.jar:lib/jackson-core-2.9.8.jar:lib/jackson-databind-2.9.8.jar:lib/jsoup-1.11.3.jar:lib/lava-common-1.1.0.jar:lib/lavaplayer-natives-1.3.13.jar:lib/lavaplayer-1.3.19.jar:lib/commons-text-1.8.jar:lib/commons-lang3-3.9.jar fr.partipirate.discord.bots.congressus.CongressusBot $configuration
