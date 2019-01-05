#!/bin/sh

#sudo ln -s ../java-project java-congressus

echo "Remove docker image"

sudo docker rmi java-congressus

echo "Start build image"

sudo docker build -t java-congressus .

echo "Image built"

#sudo rm docker/javacongressus.img
#sudo docker save -o docker/javacongressus.img java-congressus
#sudo chmod a+w docker/javacongressus.img
#sudo chmod a+r docker/javacongressus.img
#sudo rm java-congressus
