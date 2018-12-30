#!/bin/sh

sudo ln -s ../java-project java-congressus
sudo docker rmi java-congressus
sudo docker build -t java-congressus .
sudo docker save -o javacongressus.img java-congressus
chmod a+w javacongressus.img
chmod a+r javacongressus.img
sudo rm java-congressus
