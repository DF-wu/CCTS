<img src="/imgs/logo.png" width="480">

# *Composite Contract Testing Service* Handbook
> A tool to help Event-driven asynchronized microservice system do intergrated. 

## Architecture
The image below shows how CCTS work with ralated services and their dependencies.
<img src="/imgs/architecture.svg" width="480">

## Functionality
CCTS system combines contract test and log from end-2-end test to ensure the system behavior qualified your desire.

The details of the document can be found at [here](/doc/Composite%20Contract%20Testing%20Service%20Handbook.md) or [here on hackmd](https://hackmd.io/HRG1J7HkREqNM2qw0PxB0g).




### How to use CCTS 
The process flow below shows the difference stage when using CCTS.

<img src="/imgs/user_process.svg" width="240">


### API endpoint
The marix below shows the http api endpoint that how we act with CCTS service.

| path         | method | note                           |
| ------------ | ------ |:------------------------------ |
| `/conductCCTS` | POST   | To conduct and get CCTS report |
| `/cleanDB`   | POST   | clean DB data                  |
### CCTS Document and CCTS Message Specification  
CCTS Document is a file and structure to describe a SAGA flow for a system that are going to test .

CCTS Message Specification is a structure to describe certain events that will be recognized by CCTS service.


## Deployment
Video Guide: https://youtu.be/X212bWHwBY0

### Pre-requirment
+ Make sure `docker` is available in your system.
+ Make sure `maven` is available in your system.
+ Make sure `git` is available in your system.
+ Linux distro is recommanded for a enviroment
+ CCTS is developed by *SpringBoot framework* release 2.6.7
+ CCTS language level is setting in **17**
    + In others word, JDK-17 or compatible release is essential.
    + I use `azul-17` A.K.A. `zulu` JDK for a JDK-17 implement.
+ A brave heart that never gives up in seeking higher goals.
    + ![](https://media.giphy.com/media/lgcUUCXgC8mEo/giphy.gif)

### Related repository.
+ CCTS 本體$\rightarrow$  https://github.com/DF-wu/CCTS
+ CCTS 開發時使用的 pact broker $\rightarrow$  https://github.com/DF-wu/ContractTestingBoilerplate/tree/master

+ CCTS 開發時使用的rabbitmq範本  $\rightarrow$ https://github.com/DF-wu/RabbitMQ_server

+ CCTS使用的儲值範例系統，修改自PDVPS專案。
    + orchestrator $\rightarrow$ https://github.com/DF-wu/CCTS_poc_orchestrator
    + point service $\rightarrow$ https://github.com/DF-wu/CCTS_poc_points
    + payment service $\rightarrow$ https://github.com/DF-wu/CCTS_poc_payment
    + logging service $\rightarrow$ https://github.com/DF-wu/CCTS_poc_logging

### How to deploy
#### Container Version
+ I containerlized whole CCTS to an single docker compose. Should be work well. The steps of deployment:
    1. Determine a destination for deploying CCTS. Docker is requirment. I recommand most popular Linux distro. The demonstraion is using arch-based manjaro Linux
    2. `sh start-CCTS.sh`
    3. Done. Container YYDS.


#### Monolith 部屬
1. Clone all related project which was mentioned above.
2. First for Pact broker.
    1. Get into the folder，chage the configuration by your requirement in `docker-compose.yml`。The default exposed port is `8282`
    3. `docker compose up -d`. Done。
    4. Yes, this service has been containerlized
3. Deploy RabbitMQ.
    1. Get into the folder.
    2. change the configuration in `docker-compose.yml`. The definition of enviroment variables should be found in official docker image release in docker hub. Below for current setting:
        +  rabbitmq outside port: `10109`
        +  rabbitmq management port: `10110`
        +  RabbitMQ management username: `soselab`
        +  RabbitMQ management password: `soselab401`
    3. `docker compose up -d`.
    4. Done. Container YYDS again.
4. Deploy Database. CCTS default use mongoDB .
    + You can find thousand of guidence for deploy a mongodb container. Go and select what you like.
    + I use Mongo Atlas while developing.
    + The essential setting for CCTS list as below
        + username: `soselab`
        + password: `soselab401`
        + DB name: `CCTS`
5. Deploy CCTS.
    1. Get into the folder.
    2. Change related setting if you changed it before.
    3. First for rabbitMQ.
        1. This is for RabbitMQ itself configuration. Configuration file is located in `src/main/java/tw/dfder/ccts/configuration/RabbitmqConfig.java`. 
        2. For Spring Rabbitmq lib.This is Springboot Configuration. Connection setting is located in `src/main/resources/application.yml`. The part would be seen like this.
             replace `${VARIABLE}` align your enviroment.
            ```yaml
              rabbitmq:
                username: ${USERNAME}
                password: ${PASSOWORD}
                host: ${RABBITMQ_HOST}
                port: ${RABBITMQ_PORT}
                virtual-host: /
                listener:
                  simple:
                    # 手動ack, 確保訊息完整處理
                    acknowledge-mode: manual
            ```
    4. Set the database.
        1. The database configuration is defined in sprigboot configuration as known as `application.properties`. CCTS use `yaml` format as `application.yaml` to replace it. The part would be seen like this.
            replace `${VARIABLE}` align your enviroment.
            ```yaml
            spring:
              data:
                mongodb:
                  uri: mongodb+srv://${USERNAME}:${PASSWORD}@${DATABASE_ADDRESS}/CCTS?retryWrites=true&w=majority
            ```
    5. Set up for Pact broker
        1. The configuration is defined in springboot configuration AKA `application.properties` or `application.yaml`.
        2. This part of setting in configuration file would be seen like:
            ```yaml
            CCTS:
              pact_broker: http://${BROKER_ADDRESS}:${BROKER_PORT}
            ```
            replace `${VARIABLE}` align your enviroment.
    6. Set up anything else you need to connect to CCTS.
    7. Deploy CCTS
        + Thers is serveral way to deploy a SpringBoot programe. Go and choose what you want.
        + I recommand to pack whole programe as a `jar` file or a `container`for deployment.
    8. Deploy PDVPS (Optional)
        1. Specify a folder and get into it.
        2. Clone corresponding repo.
        3. Change Rabbitmq connection setting in Springboot configuration file for each project if needed.
        4. Change database(MongoDB) connection in Springboot configuration file for each project if needed.
        5. Make sure  RabbitMQ configuration about queue, exchange, top or etc. is corrosponding to right assignment.
        > Refer to the previous steps
        6. run it as a `jar` file or run it in IDE.
    9. Done.

