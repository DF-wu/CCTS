#!/bin/bash


echo "stop and remove exist container."
sudo docker compose down

echo "stop and remove exist container. Done."

echo "start to build."
sh ./build.sh

echo "start container."
sudo docker compose up -d
echo "Container is up now !"