#!/bin/bash


echo "stop and remove exist container."
sh docker compose down

echo "stop and remove exist container. Done."

echo "start to build."
sh ./build.sh

echo "start container."
sh docker compose up -d
echo "Container is up now !"