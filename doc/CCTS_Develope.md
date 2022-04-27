###### tags: `碩士` `pact`
# CCTS Develope
[TOC]



# Motivation
## 研究目標
Contract Test through services

Consumer-Driven Contract Test，為一確保服務間溝通順暢的一種測試流程。開發者先對服務的介面做出定義，並在往後依照這份合約進行測試。

在以event-driven 的微服務架構中，服務間的溝通皆是以事件作為往來的基礎單位。Contract Test的測試可以有效確保軟體在迭代的過程中，不會因為更版而發生錯誤。但是Contract僅保證了兩服務間溝通順暢，對於系統而言，在更多的使用者案例中，long-running transactions更能代表實際的使用狀況。由於長事務跨越了多個服務，在測試上的進行與複雜度的提升都會造成整體測試上的困難。



> 為了解決長事務的測試，開發以contract test為基礎的測試框架






## why we use rabbbit MQ ?
Event Driven架構作為以非同步設計基礎的的架構，在需要大量通訊的場景中，對比於傳統同步方式的呼叫，能夠有效的將服務間解耦，並提供更好的吞吐量與可靠度。在這類架構的設計中，Message broker被視為該種架構核心的實現。
目前市場上的Message框架有MQTT, RabbitMQ, kafka, Pulsar等。MQTT唯一輕量級的開源訊息protocol，多用於物聯網之中。Kafka為Apache基金會下的開源Message broker專案，專為高吞吐量高可靠性設計，而RabbitMQ為以AMQP為基礎的開源訊息方案，設計上較為彈性，相對易上手。而RabbitMQ所使用的AMQP為ISO/IEC 19464所定義的標準。




## kafka vs amqp 找文獻佐證

+ [參考confluent針對rabbitmq, kafka等message broker的效能測試](https://www.confluent.io/blog/kafka-fastest-messaging-system/)

https://www.slintel.com/tech/queueing-messaging-and-background-processing/apachekafka-vs-rabbitmq
$\uparrow$ 市佔率
amqp是iso標準


+ 結論
    + kafka的綜合性能更強，吞吐量為最高
    + rabbitMQ在低吞吐量時延遲更低，但是在CPU的開銷上更大，無法乘載過高的流量。


## 服務間溝通小資料 大資料 latency？
https://www.confluent.io/blog/kafka-fastest-messaging-system/
+ why amqp but no kafka?
    + idk sad.....X












# Requirment
## Instruction
本系統為一以event-driven microservice為服務對象的測試框架，協助開發人員與測試人員維持系統開發的品質。

1. 能夠以CDC為基礎，完成跨服務的測試
2. 會有狀態機的設計，以應對測試成功或測試失敗的狀況
3. 需要能夠在整合測試階段收集各service發出的message，並對照contract文件(pact)以檢核是否完成該環節。
4. 輔助開發人員與測試人員維持對於EDA的microservice系統的品質
## Operate Concept
在開發階段時，開發人員依據CCTS Message Spec在服務間的message中埋入特定的訊息，並且執行契約測試，將訊息同時轉送一份至特定EventLog儲存。

在整合測試階段時，測試人員將CCTS連同待測系統一同部署，並且依據格式撰寫CCTS測試案例(Composite Contract)。執行測試後CCTS會依據Composite Contract的內容檢核是否存在EventLog中，並且檢查是否執行過契約測試。




---

:::info
流程概念再精簡
後再補詳細設定

:::

Jack是一個軟體的產品經理，今天接到一個開發專案，需要以microservice為基礎並且使用event-driven的溝通方式以提高效能，為了保證軟體的品質，Jack決定使用CCTS來對系統做測試。 完成設計後，Jack將各服務切分開並交付給不同團隊做開發，並且規定message header需要依照CCTS message spec做設計，由於CCTS要求在開發階段對需要測試的message做案例編號，Jack在API文件中統整了一份message testCaseId資訊，以保證系統內沒有重複的testCaseId。


Frank是其中一個服務的開發者，依照規範使用Message Pact作為契約測試的框架，制定服務的合約時(Consumer-Driven Contract)，依據message testCaseId分開撰寫測試案例以及合約規格。在送出的message則依據Consumer端制定的testCaseId撰寫測試案例。執行契約測試時，亦須設定pact broker的位置事先配置好。 完成測試以後將相關的契約文件以及結果上傳至pact broker。

系統開發完成以後，Jack將API文件以及設計文件提交給測試團對進行整合測試。

> 由於CCTS需要收集測試期間產生的所有message資料，因此事前需要一定配置以利測試進行(將所有訊息收集至特定Queue中)，但是Jack使用了與CCTS預設相同的message broker平台，因此CCTS會自動化地完成這部分操作。
:::info
抽出來寫
:::

Raja是測試團隊的執行人，Raja依據開發團隊提供的API文件以及設計文件依據，設計出不同需要測試的測試案例，並依照CCTS document spec撰寫成各個不同的CCTS document。


在執行完整合測試的測試案例以後，Raja將撰寫好的CCTS document放置於CCTS的指定位置並啟動了CCTS，CCTS會在背景解析這些document與事前收集的EventLog。


啟動完成後raja開始執行Composite Contract test，於是CCTS先確認需要的相關要素是否完備、EventLog是否完成解析、CCTS document 是否合乎格式設定並完成解析，pact broker是否存在且含有契約測試結果。

CCTS將document作為測試基準，先將搜尋eventlog是否滿足所有的待測路徑，通過以後再行驗證合約是否存在，是否執行過契約測試。最後輸出報表


分stage說明:




## Functional Requirment
### 簡單紀錄一下
+ [x] 讀ccts document檔案作為stream
+ [x] 解析ccts成 POJO
+ [x] 支援多個ccts document 解析
+ [x] 找到應該要驗證的路徑有哪些
+ [x] 從eventlog queue抓message下來
+ [x] 接收contract
+ [x] 接收contract test的結果
+ [x] 對照message與待驗證的路徑
+ [x] 對照contract是否吻合
+ [ ] 生不同的測試案例說明可能會發生的測試不通過的狀況
+

### Develope Stage
|    編號     |                  名稱                   |                  說明                   |
|:-----------:|:---------------------------------------:|:---------------------------------------:|
| CCTS-R-D-01 | Upload Contract && Contract Test Result | 可供上傳contract以及contract test的結果 |

### Intergrate Stage

| 編號      |                 名稱                  |                          說明                          |
|:--------- |:-------------------------------------:|:------------------------------------------------------:|
| CCTS-R-I-01 |       Import Composite Contract       |                   讀取並解析測試案例                   |
| CCTS-R-I-02 |     Import Message from EventLog      |             從EventLog Queue中讀取message              |
| CCTS-R-I-03 |    Build message transaction list     | 跟據Composite Contract 建立檢查清單(預期的message清單) |
| CCTS-R-I-04 |          Inspect transaction          |         檢查各transaction對應的message是否存在         |
| CCTS-R-I-05 | Check correspond contract test result |           檢查該transaction是否完成契約測試            |


## non-Funtional Requirment
+ 盡量一般化描述測試案例







# Design



# Design Issue
## CCTS可以只靠pact broker來獲取所需要的所有pact以及CDC測試結果嗎?
+ https://docs.pact.io/pact_broker/publishing_and_retrieving_pacts
  感謝~~廢到笑得pact doc~~神奇的pact，有提供一些api來獲取pact文件。


## 有哪些可能的錯誤狀況
EventLog, CCTS document, Pact && test result
1. 有path，但是沒有相同producer consumer的event
2. 有path，有相同producer consumer的events，但是沒有相同的testCaseId
3. 有path，但是對應的contract中沒有對應的testCaseId
4. 有path，但是沒有做contract test(找不到verification history)
5.



## CCTS document validator
+ json schema should be work but DF hasn't implement
+ TODO

## 如果同樣path的masseage有負數個應該如何處理
+ 意即provider consumer testcaseid都一樣的message
+ 由於message的意義僅驗證path是否有效，邏輯上已存在與否做判斷


## pact broker 整合進ci流程的建議
+ pending



## eventLog能否放置在獨立私有的messageBroker
+ 理論上可行，但是開發端可能會負擔加重


## 報表的輸出格式
+ 目前是仿yaml格式輸出

## 驗證流程
+ 設計流程
    1. 解析所有document
    2. 從document抽取所有欲驗證的path
    4. 驗證eventlog是否滿足path(每一條path都有對應的event產出)
    5. 驗證pact是否滿足document(path的testCaseId是否存在於對應的合約中)
    6. 驗證參與的所有服務(paricipant)在pact broker中是否有通過的契約測試紀錄
    7. return 結果

## time sequence label
+ 以這個PDVPS topup說明，point update fail 與 payment fail兩條案例的eventlog產生先後順序並不固定。
  ![](https://i.imgur.com/selYSJv.png)

+ 解:
    + 在CCTS document 新增一欄位給予測試人員輸入先後順序如
        + 1 ,5 ,6, 9, 10
        + 1 ,9 , 10 ,11 ,12









---

## 在整合測試階段的verification狀態機設計
### 狀態機描述文件規格設計
https://hackmd.io/YPazaXezQ1a_8VI6VDmn9w
### 狀態機流程
+ 以PDVPS儲值流程的狀態機設計
    + https://drive.google.com/file/d/1R02-U71dM3OAF7uDL_mdxwfRzmCyYFCE/view?usp=sharing

## 使用流程設計
### Develope Stage
+ 開發人員執行contract test
+ 開發人員須將服務間的Message依照CCTS message spec格式加入必要的屬性

### Integration Testing Stage
#### CCTS 作為一服務在整合測試階段部署
+ 測試人員需撰寫
    + CCTS Test document
        + saga中狀態遷移的狀況
        + 需知道服務的message communcation 狀況(誰給誰什麼東西)








## dynamic layer vs static layer

1. run完測試後 收集event對照contract-based state machine 是否符合

2. 如何描述狀態機
3. 將各個transaction contract 整合成composite contract(描述狀態機內各transaction)


# Issue
## 應該要如何表示各pact所屬之Composite Contract或是某特定測試流程。(能不能在contract 塞自己的label)
+ 在pact塞tag
    + tag位置?
        1. 在pact的expectRecive(類似title的那個地方)置入需要的東西，概念上可行
        2. 直接在pact增加欄位
            + 經過測試，可行!!
+ 使用message pact的metadata功能
    + https://docs.pact.io/implementation_guides/jvm/consumer/junit#consumer-test-for-a-message-consumer

## state machine 描述語言有哪些
1. amazon有一個state machine language
    + EC2服務用的 可以參考
    + https://docs.aws.amazon.com/step-functions/latest/dg/amazon-states-language-state-machine-structure.html


## 如何將實際產生的message對應到coposite contract的測試流程 (dynamic layer)



## 如何將產生出來的contract與其定義的provider and consumer 配對

+ https://docs.pact.io/implementation_guides/jvm/provider/maven#verifying-a-message-provider


## 開發階段的契約測試 vs 整合階段的測試

## Static layer && dynamic layer 如何將流程與訊息對應起來
相關研究參考[Research](#Research)章節。
+ [以PDVPS msg flow chart為例](https://app.diagrams.net/#G18Y2tEUwWQAmAGiQb9vUoEBYCRmUTJMxU)，本案例可以提取出兩個Contract(Pact)。
    1. (Stage 1)payment service收到confirm hook以後會送出update point的message至*QUEUE_PDVPS_UPDATEPOINT*，並由point service消費該message。
    2. (Stage 2)point成功更新點數以後，送出一message至*QUEUE_PDVPS_PAYMENT*，由Logging Service消費該message。

上述的case以Composite Contract的格式包裝後如下
```yaml=
Composite Contract title: CC1
pacts:
 - payment update point
 - update point log
```


在Stage 1，payment Service與point service間互相傳送的訊息應該要符合以下規定
```yaml=
metadata:
# 放置這份message所屬的composite contract title
 - Conposite Contract: CC1 
# 本訊息所屬的pact合約
 - process stage: Payment and update points
payload: 訊息內容
```


而約定的成的契約pact可能如下(內容無視，簡單假資料而已)
:::spoiler 點選顯示code

```json=
  {
  "consumer": {
    "name": "point service"
  },
  "messages": [
    {
      "contents": {
        "isLeapYear": true,
        "localDate": "2000-01-31"
      },
      "description": "Payment and update points",
      "generators": {
        "body": {
          "$.localDate": {
            "expression": "^\\d{4}-\\d{2}-\\d{2}$",
            "format": "yyyy-MM-dd",
            "type": "Date"
          }
        }
      },
      "matchingRules": {
        "body": {
          "$.isLeapYear": {
            "combine": "AND",
            "matchers": [
              {
                "match": "type"
              }
            ]
          },
          "$.localDate": {
            "combine": "AND",
            "matchers": [
              {
                "date": "yyyy-MM-dd",
                "match": "date"
              }
            ]
          }
        }
      },
      "metaData": {
        "Composite Contract": "CC1",
        "contentType": "application/json",
        "process stage": "Payment and update points"
      }
    }
  ],
  "metadata": {
    "pact-jvm": {
      "version": "4.2.10"
    },
    "pactSpecification": {
      "version": "3.0.0"
    }
  },
  "provider": {
    "name": "payment service"
  }
}

```
:::

在 Stage 2 時，訊息的spec會長得像
```yaml=
metadata:
# 放置這份message所屬的composite contract title
 - Conposite Contract: CC1 
# 本訊息所屬的pact合約
 - process stage: update point log
payload: 訊息內容
```
而 logging與point倆服務間的pact可能會長這樣
:::spoiler 點擊顯示code

```json=
 {
  "consumer": {
    "name": "logging service"
  },
  "messages": [
    {
      "contents": {
        "isLeapYear": true,
        "localDate": "2000-01-31"
      },
      "description": "logging",
      "generators": {
        "body": {
          "$.localDate": {
            "expression": "^\\d{4}-\\d{2}-\\d{2}$",
            "format": "yyyy-MM-dd",
            "type": "Date"
          }
        }
      },
      "matchingRules": {
        "body": {
          "$.isLeapYear": {
            "combine": "AND",
            "matchers": [
              {
                "match": "type"
              }
            ]
          },
          "$.localDate": {
            "combine": "AND",
            "matchers": [
              {
                "date": "yyyy-MM-dd",
                "match": "date"
              }
            ]
          }
        }
      },
      "metaData": {
        "Composite Contract": "CC1",
        "contentType": "application/json",
        "process stage": "update point log"
      }
    }
  ],
  "metadata": {
    "pact-jvm": {
      "version": "4.2.10"
    },
    "pactSpecification": {
      "version": "3.0.0"
    }
  },
  "provider": {
    "name": "point service"
  }
}
```

:::

## 當restful controller 接到request並需要回復非同步內容時，會有點小問題。
由於Saga pattern 中，data persistant的地方在各個microservices中。

如果在前端需要透過Saga client存取資料時，需要特別設計一下。

目前是EDA flow跟 Synchronize flow分開處理






# Research
## message metadata field
在 pact-jvm下的messagePact有支援metadata的欄位可以自行填寫資訊並驗證，為key-value的形式。

e.g.
*consumer*
:::spoiler 點我顯示code

```java=
public MessagePact validateMsgFromRabbitmqProvider(MessagePactBuilder builder) {
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("msgSource","rabbitMQ_consumer");
    metadata.put("compositeContract","test CC1");
    return builder
    .expectsToReceive("This is async EDA msg using pact with metadata")
    .withMetadata(metadata)
    .withContent(LambdaDsl.newJsonBody(lambdaDslJsonBody -> {
        lambdaDslJsonBody.dateExpression("localDate", "^\\d{4}-\\d{2}-\\d{2}$", "yyyy-MM-dd");
        lambdaDslJsonBody.booleanType("isLeapYear", Boolean.TRUE);
    }).build())
    .toPact();        
```    
:::

會自動塞一個contentype=json的設定

```json=
 "metaData": {
    "compositeContract": "test CC1",
    "contentType": "application/json",
    "msgSource": "rabbitMQ_consumer"
  }
```
---
*producer*
:::spoiler 點我顯示code

```java=
@PactVerifyProvider("This is async EDA msg using pact with metadata")
public MessageAndMetadata verifyMessageForOrder() {
    Gson gson = new Gson();
    DateInfo dateinfo = new DateInfo();
    dateinfo.setLeapYear(true);

    DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    dateinfo.setLocalDate(LocalDate.parse(LocalDate.now().toString(), dateformatter).toString());
//   設定metadata  像是header
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("msgSource","rabbitMQ_consumer");
    metadata.put("compositeContract","test CC1");
    return generateMessageAndMetadata(gson.toJson(dateinfo), metadata);
}
// 產生pact接受的message格式
private MessageAndMetadata generateMessageAndMetadata(String payload, Map<String, String> meta) {
    HashMap<String, Object> metadata = new HashMap<String, Object>();
    meta.forEach((k, v) -> metadata.put(k, v));

    return new MessageAndMetadata(payload.getBytes(), metadata);
}
```
:::


## Composite Contract格式設計

假設一個Composite Contract代表一個類似e2e測試的 test Case，包含了測試中會驗證的多個contract。那可以先假設一個Composite Contract的格式會類似這樣。
```yaml
Composite Contract Title: PaymentAndUpdatePointAndLogging
  pacts:
   - Payment and update points
   - logging
   
```
+ **Composite Contract Title** 為這一份contract的標題
+ **pacts** 為本contract會使用到的pact，為陣列的形式


## message spec設計

根據[這個部分的資料](https://hackmd.io/TAP-rALvSS6Atx_1m24yUA?both#message-metadata-field)，在MessagePact中可以附帶metadata資訊。結構會如同[Spring Message](https://docs.spring.io/spring-integration/reference/html/message.html)的格式，在AMQP與kafka的格式中也有類似的設計。
```java=
public interface Message<T> {
    T getPayload();
    MessageHeaders getHeaders();
}
```

假設以上述的headers做為message傳送時附帶的標籤，為了在之後可以收集訊息並對照Composite Contract中定義的內容，可以訂出一份message在傳輸時應該遵守的spec。以yaml格式來表示的話會類似下面所示。 [以PDVPS msg flow chart為例](https://app.diagrams.net/#G18Y2tEUwWQAmAGiQb9vUoEBYCRmUTJMxU)
```yaml=
metadata:
# 放置這份message所屬的composite contract title
 - Conposite Contract: PaymentAndUpdatePointAndLogging 
# 本訊息所屬的pact合約
 - process stage: Payment and update points
payload: message content
```


## 一份pact合約可以包含複數個測試案例
一份pact所表示的意義是兩個服務間有約定的介面規格等。

目前由小到大的依序是
pact內的不同的介面合約 (比如說API A, B, C) → pact (CDC所描述的階段)→ Composite Contract (E2E test case)

## 狀態機可行性
saga 狀態機 相關研究 https://dzone.com/articles/modelling-saga-as-a-state-machine
https://www.baeldung.com/cs/saga-pattern-microservices
https://developer.ibm.com/articles/use-saga-to-solve-distributed-transaction-management-problems-in-a-microservices-architecture/
https://ithelp.ithome.com.tw/articles/10236124
https://medium.com/skyler-record/%E5%BE%AE%E6%9C%8D%E5%8B%99%E6%9E%B6%E7%9A%84%E8%B3%87%E6%96%99%E4%B8%80%E8%87%B4%E6%80%A7-1-saga-pattern-cf05aed1307b



以一個飛機訂票系統為例
如果以Orchestrator的方式設計：
Orchestrator 負責程序上的把關，如
收到何種event就做何種操作
![](https://dz2cdn1.dzone.com/storage/temp/14973567-1626499734487.png)

但是如果在某個環節發生問題或操作失敗的話，Ochestrator無法處理。針對這樣order、seat、payment有狀態的部分可以設計出如下的狀態機。
![](https://dz2cdn1.dzone.com/storage/temp/14973568-1626499769195.png)

將流程與狀態機結合後如下
![](https://dz2cdn1.dzone.com/storage/temp/14973570-1626499790926.png)
概念上而言，transaction的狀態管理由 Saga State Machine管理，由Orchestrator負責執行對應的操作，責任比較明確。



## Composite Contract的概念是否與BDD有關係
ref: https://tw.alphacamp.co/blog/bdd-tdd-cucumber-behaviour-driven-development

TDD要求為在撰寫API前先進行測試，之後再修正API以符合測試要求避免方向錯誤。

缺點為多數皆由開發人員完成，不易直接與非相關人員協調。

BDD為先行定義測試規格→由測試規格生成測試案例→撰寫API程式
利用較接近自然語言的方式描述測試規格，以便與相關人員討論。再依據規格進行測試案例的生成。



## 參考 AWS State machine structure的語法
[設計參見這裡](#在整合測試階段的verification狀態機設計)
+ AWS State machine structure 是為了 AWS step function 服務誕生的東西(AWS lamda裡面的)
    + 參考該語法做saga的state machine case

+ 範例： ![](https://i.imgur.com/Kevd17V.jpg)





# QA and Discuss

## 在測試案例的分類上應該如何設計比較好
參考[一份pact合約可以包含複數個測試案例](#一份pact合約可以包含複數個測試案例)
比如說A到B服務間有兩種功能與其對應的傳遞分別為 FuncA: msgA, FuncB: msgB，都會在同一分pact文件下。
如果有一份Composite Contract需要測試，其中包含了FuncA。依照目前Composite Contract spec設計的話，在呼叫該pact 執行Contract Test的時候也會一併測試到不相關的FuncB。


## 對於自動化的程度需要做到什麼程度
在開發階段，開發人員會執行contract test，產生出來的pact，會在之後的整合測試階段拿來對照eventstore中的資料是否吻合contract中的訊息

1. ccts是否需要在integration testing 中驗證eventstore中儲存的訊息是否吻合Pact所定義的內容? 及僅需驗證message的存在(確認兩服務間有通訊 不管內容)或是連內容都需要驗證。
2. 測試人員是否需要手動填寫服務間互動的相關資訊(contract, msg source, dest etc.)
3. CCTS spec制定時，是否需要假定微服務系統的架構，比如orchestrator與choreography在撰寫ccts測試案例時會有所差異(需要給予的資訊不同)，狀態圖也需要更詳細。





# Note

## 整合測試階段，收集Message拼拼圖對照Pact
在message中，需要附帶pact資訊，才能對照回contract。

phase 2


## 整合測試階段的測試，要以一個contract的狀態為設計或是更高層面的測試案例?

會有哪些可能的狀態?
verifier 狀態機設計 https://drive.google.com/file/d/1R02-U71dM3OAF7uDL_mdxwfRzmCyYFCE/view?usp=sharing

## verifier 是否需要再執行contract test檢查msg內容是否正確?
困難點: 需要一服務專門將msg對應檢查的contract

## 要解的問題是long-runing transaction的測試?


## 簡單生一個orchestrator的範例系統
看看寫orchestrator的時候會撞到什麼牆 可以用pdvps的案例

## a saga = a case
一個微服務系統內可能有多個saga pattern, 多個orchestrator。專orchestrator 專供該pattern使用


## pdvps改寫orchestrator看看 （wait）


## 看一下人家的方法
https://www.vinsguru.com/orchestration-saga-pattern-with-spring-boot/
https://www.baeldung.com/cs/saga-pattern-microservices

## state machine需要能夠辨認所屬服務（source , destination）


## 找看看saga 的實做長什麼樣子。 orchestrator又是怎麼樣的設計。
https://github.com/lucasdeabreu/saga-pattern-example

https://microservices.io/patterns/data/saga.html

## 2.24

測試時會發生什麼可能的錯誤
state machine =>  a saga case

1. 有測到相關的event但規格沒有 : 規格(state machine)不夠完整
    +
2. 規格有但實際上沒有的流程 : E2E測試不夠完整
    +
3. 實際運作順序錯誤
    + in what case
    + 要預防邏輯錯誤
        + 非同步的方式下要如何預防@@
4. contract test的結果是否能對映到state machine的案例
    + pact define in state machine case?


## 3.3
+ yaml schema?
+ state 的描述是否有特定的狀況需要特別的語法或機制處理
    + like compensation
+ 狀態轉換間的event互動要描述
+ 要想*測試時會發生什麼可能的錯誤*
    + 設計實際案例
+ 能否平行測試（later）
+ state machine spec
    + https://www.uml-diagrams.org/state-machine-diagrams.html
+ 能否自動gen saga test case and state machine (later)


## 3.11
用標準的uml格式表示圖
用contract分辨兩服務間不同的msg
如何取得contract test結果 -> 後面階段要看前面是否有做過測試
+ pact broker or 土砲（不得已）

*平行* 先不考慮
整理 功能需求 補清楚設計流程
統整CCTS應遵循的規則

圖上transaition的線該如何漂亮的表示
+ 人家saga畫得很漂亮就學起來 不要自己做那種爛東西
+	```yaml
	request path:
	  source: xxx
	  detination: xxx
	response path:
	  source: xxx
	  destination: xxx
	```
## 3.18
> json schema研究中

設計建議的開發流程
Choice -> option
取消booleanequal
買startUML？
upload contract
funcitonal req. 分階段&&角色撰寫
**文獻回顧**
message broker 的訊息傳送錯誤控制有哪些
+ https://www.baeldung.com/spring-amqp-error-handling


## 3.24
> functional requirement的階段 角色
> starUML買 https://staruml.io/order
> yabai 不同test case 會在同一份contract 裡面
> 要重改message spec
> document spec也要改成符合json naming

CCTS使用方式要說明如果發生狀況，會需要做什麼處理（error handling）


parser 判斷規則要說明


## 3.31
+ pact broker的加入與否 -> automation
    1. 用pact broker -> 設計專門跟broker互動的機制
    2. 不用pact -> 工人智慧，自己合約做完跟相關服務的人約定好就好，土炮回報測試狀況、contract檔案

+ 在post CDC result 的手段上有什麼想法
    1. 開發時測完，工人智慧
    2. CCTS包山包海全自動，自己到broker撈資料


+ 改名 CCTS沒有母音 口年
    + **Edamcits**: event driven architecture microservices contract integrated test system
    + **Aedmcits**: Asynchronized Event driven microservices contract intergrated test system
    + [x] **CCTS**: Composite Contract Test Service


## 4.7
> + pact broker 在管理contract上有matrix的概念: 以application版本為基礎的測試結果圖。 CCTS先假設latest = what test case use
> + ccts message spec 中 似乎不需要規定太多額外的描述，只要能保證整個測試案例中不會出現重複即可
> + message term -> testcaseid?
> + 測試結果 -> can i deploy?

撰寫操作概念


## 4.14
> debug中 說有那些bug
> 在測試測試案例中
> 操作概念寫好了

說明實驗方法 前提與限制

如果有文獻出處或是參考必須補上

4月底要有成果  5月丟TCSE


## 4.21
+ 能夠完整照流程demo一次
+ CCTS document linter
+ **先後順序的驗證** 塞timestamp and time squence lable
+ report fix: errors -> *failure*
+ path -> delievery
+ output -> markdown
+ global repo for share CCTS spec document and related script
+ 整理一份漂亮的文件出來給老師 