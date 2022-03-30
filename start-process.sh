#!/bin/bash

echo "start to build."
sh ./build.sh
echo "build finished. start to publish contract"
sh ./publish-contract.sh