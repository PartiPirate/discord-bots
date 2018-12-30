#!/bin/sh

#sudo ln -s ../java-project java-congressus
sudo docker rmi java-congressus
sudo docker build -t java-congressus .
#sudo rm docker/javacongressus.img
#sudo docker save -o docker/javacongressus.img java-congressus
#sudo chmod a+w docker/javacongressus.img
#sudo chmod a+r docker/javacongressus.img
#sudo rm java-congressus
