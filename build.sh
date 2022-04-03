#!/bin/bash
# 不要在container裡面用maven build ， 不然會很慢
# 不要在container裡面用maven build ， 不然會很慢
# 不要在container裡面用maven build ， 不然會很慢
# by stanley2058@yahoo.com.tw
# 所以這是先在外面build ，再把build好的檔案 copy到外面的working dir

mvn -T 1C clean install -Dmaven.test.skip=true
echo "build.sh : maven build successfully"

cp ./target/CCTS-0.0.1.jar app.jar
echo "build.sh : cp apps from target directory to root directory"

docker build . -t CCTS-image
echo "build.sh : docker build successfully"



# running in deploy machine.
#docker stop pdvps-payment || true
#docker rm pdvps-payment || true

#docker run  -p 10107:8888 -d --name pdvps-payment pdvps-payment-image:latest