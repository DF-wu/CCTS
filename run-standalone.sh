mvn -T 1C clean install -Dmaven.test.skip=true
cp ./target/CCTS-0.0.1.jar app.jar
java -jar app.jar