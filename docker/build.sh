#!/bin/sh

docker rmi java-congressus
docker build -t java-congressus .
docker save -o javacongressus.img java-congressus
chmod a+w javacongressus.img
chmod a+r javacongressus.img
