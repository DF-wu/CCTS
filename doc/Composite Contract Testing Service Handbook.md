###### tags: `碩士`

# *Composite Contract Testing Service* Handbook
> A tool to help Event-driven asynchronized microservice system do intergrated .


[TOC]

## Guidance 服用姿勢
+ 建議的開發流程：
    1. PO事前將服務所需的端點規格訂出以利進行契約測試，並指派人手進行開發。
    2. 開發人員依據先前訂出的契約規格完成服務，在服務間溝通的message依據[*CCTS message spec*](##CCTS-Message-spec)的規格埋入metadata。
    3. 開發人員執行契約測試並通過之後，將契約內容以及測試結果上傳至CCTS服務中。
    4. 在測試階段，測試人員需要取得微服務中各個saga的案例，並依據[*CCTS Document spec*](##CCTS-Document-spec)的規定將其撰寫為狀態圖的描述文件。
    5. 在整合測試以前，測試人員需要設定message broker，需要有一個專屬的queue負責收集測試中所有的訊息
    6. 執行整合測試後，至CCTS執行Composite Test，檢核訊息是否有遺漏
    7. (?) 產生測試結果報告


## CCTS Message spec v2
+ CCTS 預設以rabbitmq作為message broker
    + 在一個message被produce時，必須在message header中埋入以下表格對應訊息，用以判斷本訊息。
    + **注意**，在整個系統中，以下三個欄位對應的值不應該重複。

| Key             | Value                      |
| --------------- |:-------------------------- |
| `provider`     | 發送者的service Name       |
| `consumer`    | 接收者的service Name       |
| `testCaseID`                |  本次通訊對應Contract的項目|



## CCTS test case description spec
+ In Pact Contract test case:
    + 在`expectToRecieve()`中，需要按照以下格式設定。 (The description of test case)
+ Should be unique in its contract.
```yaml=
testCaseId: 
```


## CCTS Document spec
+ build a ccts profile:
    + [The Last version](###v0.9)



## Design
### Develope Stage
#### Message metadata spec

### Intergrated Stage





---
## 參考資訊
+ 畫的圖；https://drive.google.com/file/d/1R02-U71dM3OAF7uDL_mdxwfRzmCyYFCE/view?usp=sharing
+ 範例狀態變化圖
    + ![](https://i.imgur.com/Kevd17V.jpg)
    + ![](https://i.imgur.com/l7rjCNe.jpg)
    + ![](https://i.imgur.com/Kg5nxa4.jpg)

### testCaseId table


| testCaseId       | provider       | consumer       | note              |
|:---------------- |:-------------- | -------------- |:----------------- |
| t-orc-payment-01 | orchestrator   | paymentService | req               |
| t-payment-orc-01 | paymentService | orchestrator   | res               |
| t-orc-point-01   | orchestrator   | pointService   | req               |
| t-point-orc-01   | pointService   | orchestrator   | res               |
| t-orc-logging-01 | orchestrator   | loggingService | req               |
| t-logging-orc-01 | loggingService | orchestrator   | res               |
| t-payment-orc-02 | paymentService | orchestrator   | req time label 7  |
| t-orc-payment-02 | orchestrator   | paymentService | res time label 8  |
| t-point-orc-02   | pointService   | orchestrator   | req time label 9  |
| t-orc-point-02   | orchestrator   | pointService   | res time label 10 |
| t-point-orc-03   | pointService   | orchestrator   | req time label 11 |
| t-payment-orc-03 | paymentService | orchestrator   | res time label 12 |
| t-payment-orc-04 | paymentService | orchestrator   | time label 13     |



### 報告輸出格式
+ todo

## 版本紀錄
### v0.9
#### 變更紀錄
+ 為解決不同分支的先後順序問題，加入`timeSequences`
    + 以陣列表示，個元素以逗號分隔，元素為`int32`整數
    + 各元素必須由小到大
    + 不得為空
+ simpleState的`End`屬性改名為`isEnd`
#### spec
+ `CCTSversion`: 對應CCTS版本
+ `title`: 本測試名稱
+ `startAt`: 初始狀態
+ `timeSequences`
    - 1,2,3,5
    - 1,2,5,6,7
+ `states`: 狀態圖 hashMap
    + `狀態名稱`:
        + `comment`: 狀態說明
        + `nextState`: (下一個狀態)
            + `stateName`: 下一個狀態的名稱，需定義在`states中`
            + `testCaseId` : 對應的Contract Term(description in pact)
            + `provider` :
            + `consumer` :
            + `timeSequenceLable` :
        + `options`: 狀態分歧
            + `狀態名稱`:
                + `stateName`  : 狀態名稱
                + `testCaseId` : 對應的合約項目(pact message description)
                + `provider` :
                + `consumer` :
                + `timeSequenceLable` :
            + `狀態名`
        + `end`: 結束狀態 *True* or *False*



---
### v0.8
#### 變更紀錄
+ 為了支援CCTS案例順序驗證，增加一欄位 `timeSequenceLable`
#### spec
+ `CCTSversion`: 對應CCTS版本
+ `title`: 本測試名稱
+ `startAt`: 初始狀態
+ `states`: 狀態圖 hashMap
    + `狀態名稱`:
        + `comment`: 狀態說明
        + `nextState`: (下一個狀態)
            + `stateName`: 下一個狀態的名稱，需定義在`states中`
            + `testCaseId` : 對應的Contract Term(description in pact)
            + `provider` :
            + `consumer` :
            + `timeSequenceLable` :
        + `options`: 狀態分歧
            + `狀態名稱`:
                + `stateName`  : 狀態名稱
                + `testCaseId` : 對應的合約項目(pact message description)
                + `provider` :
                + `consumer` :
                + `timeSequenceLable` :
            + `狀態名`
        + `end`: 結束狀態 *True* or *False*



---
### v0.7
#### 變更紀錄
+ 為了撰寫上的一致性 將`options`的屬性改為與nextState相同

#### spec
+ `CCTSversion`: 對應CCTS版本
+ `title`: 本測試名稱
+ `startAt`: 初始狀態
+ `states`: 狀態圖 hashMap
    + `狀態名稱`:
        + `comment`: 狀態說明
        + `nextState`: (下一個狀態)
            + `stateName`: 下一個狀態的名稱，需定義在`states中`
            + `testCaseId`: 對應的Contract Term(description in pact)
            + `provider`
            + `consumer`
        + `options`: 狀態分歧
            + `狀態名稱`:
                + `stateName` : 狀態名稱
                + `testCaseId`: 對應的合約項目(pact message description)
                + `provider`:
                + `consumer`:
            + `狀態名`
        + `end`: 結束狀態 *True* or *False*


:::spoiler 範例v0.7
```yaml=
CCTSversion: "0.6"
title: CCTS saga test case1
startAt: Create top-up event
states:
  Create top-up event:
    comment: initial state
    end: False
    nextState:
      stateName: payment process
      testCaseId: t-orc-payment-01
      provider: orchestrator
      consumer: paymentService
  Payment process:
    comment: make payment
    end: False
    options:
      Prepare update point:
        stateName: Prepare update point
        testCaseId: t-payment-orc-01
        provider: paymentService
        consumer: orchestrator
      Payment Fail:
        stateName: Payment Fail
        testCaseId: poc-03
        provider: paymentService
        consumer: orchestrator
  Payment Fail:
    comment: do payment fail compensation
    end: False
    nextState:
      testCaseId: poc-04
      stateName: Payment reversed
      provider: orchestrator
      consumer: paymentService
  Payment reversed:
    comment: reverse Payment
    end: False
    nextState:
      testCaseId: poc-05
      stateName: Top-up cancel
      provider: paymentService
      consumer: orchestrator
  Prepare update point:
    comment: orchestrator received response from payment and ready to send update point request to Point service
    end: False
    nextState:
      testCaseId: t-orc-point-01
      stateName: Update point
      provider: orchestrator
      consumer: pointService
  Upate point:
    comment: do update point
    end: False
    options:
      Prepare log:
        stateName: Prepare log
        testCaseId: t-point-orc-01
        provider: pointService
        consumer: orchestrator
      Update point Fail:
        stateName: Update point Fail
        testCaseId: poc-08
        provider: pointService
        consumer: orchestrator
  Update point Fail:
    comment: revert points
    end: False
    nextState:
      testCaseId: poc-08
      stateName: Revert point
      provider: orchestrator
      consumer: pointService
  Revert point:
    comment: revert points
    end: False
    nextState:
      testCaseId: poc-09
      stateName: Prepare reverse payment
      provider: pointService
      consumer: orchestrator
  Prepare reverse payment:
    comment: orchestrator received response that point finished compensation , ready to do payment compensation
    end: False
    nextState:
      testCaseId: poc-10
      stateName: Payment reverse
      provider: orchestrator
      consumer: paymentService
  Prepare Log:
    comment: orchestrator ready to send Log request
    end: False
    nextState:
      testCaseId: t-orc-logging-01
      stateName: Log
      provider: orchestrator
      consumer: loggingService
  Log:
    comment: logging
    end: False
    nextState:
      testCaseId: t-logging-orc-01
      stateName: Approved top-update
      provider: loggingService
      consumer: orchestrator
  Approved top-up:
    comment: finishe saga process
    end: True
  Top-up cancel:
    comment: finishe compensation process and finishe saga
    end: True

```
:::
---
### v0.6
#### 變更紀錄
+ 統一用詞:
    + `publisher` $\rightarrow$ `provider`
    + `subscriber` $\rightarrow$ `consumer`
    + add *CCTS message spce v2* support
#### spec
+ `CCTSversion`: 對應CCTS版本
+ `title`: 本測試名稱
+ `startAt`: 初始狀態
+ `states`: 狀態圖 hashMap
    + `狀態名稱`:
        + `comment`: 狀態說明
        + `nextState`: (下一個狀態)
            + `stateName`: 下一個狀態的名稱，需定義在`states中`
            + `testCaseId`: 對應的Contract Term(description in pact)
            + `provider`
            + `consumer`
        + `options`: 狀態分歧
            + `狀態名`:
                + `testCaseId`: 對應的合約項目(pact message description)
                + `provider`:
                + `consumer`:
            + `狀態名`
        + `end`: 結束狀態 *True* or *False*




---
### v0.5
#### 變更紀錄
+ 修改key格式，大小寫等等以便CCTS解析
#### spec
+ `CCTSversion`: 對應CCTS版本
+ `title`: 本測試名稱
+ `startAt`: 初始狀態
+ `states`: 狀態圖 hashMap
    + `狀態名稱`:
        + `comment`: 狀態說明
        + `nextState`: (下一個狀態)
            + `State`: 下一個狀態的名稱，需定義在`States中`
            + `contractName`: 對應的Contract Test文件名稱
            + `publisher`
            + `subscriber`
        + `options`: 狀態分歧
            + `狀態名`:
                + `contractName`: 對應的Contract Test文件名稱
                + `publisher`:
                + `subscriber`:
            + `狀態名`
        + `end`: 結束狀態 *True* or *False*




:::spoiler v0.5
```yaml=
CCTSversion: "0.5"
title: CCTS saga test case1
startAt: Create top-up event
states:
  Create top-up event:
    comment: initial state
    end: False
    nextState:
      contractName: payment process contract
      state: payment process
      publisher: Orchestrator
      subscriber: Payment
  Payment process:
    comment: make payment
    end: False
    options:
      Prepare update point:
        contractName:  make payment contract
        publisher: Payment
        subscriber: Orchestrator
      Payment Fail:
        contractName:  Payment Fail response
        publisher: Payment
        subscriber: Orchestrator
  Payment Fail:
    comment: do payment fail compensation
    end: False
    nextState:
      state: Payment reversed
      contractName: Payment Fail response
      publisher: Orchestrator
      subscriber: Payment
  Payment reversed:
    comment: reverse Payment
    end: False
    nextState:
      state: Top-up cancel
      contractName: compensation done and cancel saga
      publisher: Payment
      subscriber: Orchestrator
  Prepare update point:
    comment: orchestrator received response from payment and ready to send update point request to Point service
    end: False
    nextState:
      state: Update point
      contractName: update point request contract
      publisher: Orchestrator
      subscriber: Point
  Upate point:
    comment: do update point
    end: False
    options:
      Prepare log:
        contractName: response orchestrator contract
        publisher: Point
        subscriber: Orchestrator
      Update point Fail:
        contractName: notify orchestrator revert point contract
        publisher: Point
        subscriber: Orchestrator
  Update point Fail:
    comment: revert points
    end: False
    nextState:
      state: Revert point
      contractName: orchestrator request Point service revert point
      publisher: Orchestrator
      subscriber: Point
  Revert point:
    comment: revert points
    end: False
    nextState:
      state: Prepare reverse payment
      contractName: notify orchestrator do compensation for payment contract
      publisher: Point
      subscriber: Orchestrator
  Prepare reverse payment:
    comment: orchestrator received response that point finished compensation , ready to do payment compensation
    end: False
    nextState:
      state: Payment reverse
      contractName: request payment do compensation contract
      publisher: Orchestrator
      subscriber: Payment
  Prepare Log:
    comment: orchestrator ready to send Log request
    end: False
    nextState:
      state: Log
      contractName: Log request Contract
      publisher: Orchestrator
      subscriber: Log
  Log:
    comment: logging
    end: False
    nextState:
      state: Approved top-update
      contractName: log response contract
      publisher: Log
      subscriber: Orchestrator
  Approved top-up:
    comment: finishe saga process
    end: True
  Top-up cancel:
    comment: finishe compensation process and finishe saga
    end: True
```
:::


### v0.4
#### 變更紀錄
+ 取消`booleaneuqal`
+ 以`option`替代`choice`
+ 修改key格式
#### spec
+ `CCTS`: 對應CCTS版本
+ `Title`: 本測試名稱
+ `StartAt`: 初始狀態
+ `States`: 狀態圖hashtable
    + `狀態名稱`:
        + `Comment`: 狀態說明
        + `Next`: (下一個狀態)
            + `State`: 下一個狀態的名稱，需定義在`States中`
            + `ContractName`: 對應的Contract Test文件名稱
            + `Publisher`
            + `Subscriber`
        + `Options`: 狀態分歧
            + `狀態名`:
                + `ContractName`: 對應的Contract Test文件名稱
                + `Publisher`:
                + `Subscriber`:
            + `狀態名`
        + `End`: 結束狀態 *True* or *False*

:::spoiler v0.4
```yaml=
CCTS: 0.4
Title: CCTS saga test case1
StartAt: Create top-up event
States:
  Create top-up event:
    Comment: initial state
    End: False
    Next:
      ContractName: payment process contract
      State: payment process
      Publisher: Orchestrator
      Subscriber: Payment
  Payment process:
    Comment: make payment
    End: False
    Options:
      Prepare update point:
        ContractName:  make payment contract
        Publisher: Payment
        Subscriber: Orchestrator
      Payment Fail:
        ContractName:  Payment Fail response
        Publisher: Payment
        Subscriber: Orchestrator
  Payment Fail:
    Comment: do payment fail compensation
    End: False
    Next:
      State: Payment reversed
      ContractName: Payment Fail response
      Publisher: Orchestrator
      Subscriber: Payment
  Payment reversed:
    Comment: reverse Payment
    End: False
    Next:
      State: Top-up cancel
      ContractName: compensation done and cancel saga
      Publisher: Payment
      Subscriber: Orchestrator
  Prepare update point:
    Comment: orchestrator received response from payment and ready to send update point request to Point service
    End: False
    Next:
      State: Update point
      ContractName: update point request contract
      Publisher: Orchestrator
      Subscriber: Point
  Upate point:
    Comment: do update point
    End: False
    Options:
      Prepare log:
        ContractName: response orchestrator contract
        Publisher: Point
        Subscriber: Orchestrator
      Update point Fail:
        ContractName: notify orchestrator revert point contract
      Publisher: Point
      Subscriber: Orchestrator
  Update point Fail:
    Comment: revert points
    End: False
    Next:
      State: Revert point
      ContractName: orchestrator request Point service revert point
      Publisher: Orchestrator
    Subscriber: Point
  Revert point:
    Comment: revert points
    End: False
    Next:
      State: Prepare reverse payment
      ContractName: notify orchestrator do compensation for payment contract
    Publisher: Point
    Subscriber: Orchestrator
  Prepare reverse payment:
    Comment: orchestrator received response that point finished compensation , ready to do payment compensation
    End: False
    Next:
      State: Payment reverse
      ContractName: request payment do compensation contract
    Publisher: Orchestrator
    Subscriber: Payment
  Prepare Log:
    Comment: orchestrator ready to send Log request
    End: False
    Next:
      State: Log
      ContractName: Log request Contract
    Publisher: Orchestrator
    Subscriber: Log
  Log:
    Comment: logging
    End: False
    Next:
      State: Approved top-update
      ContractName: log response contract
    Publisher: Log
    Subscriber: Orchestrator
  Approved top-up:
    Comment: finishe saga process
    End: True
  Top-up cancel:
    Comment: finishe compensation process and finishe saga
    End: True
```
:::







### v0.3
#### 變更紀錄
取消participant service欄位
choices欄位直接將候選狀態作為key
#### spec
+ `CCTS`: 對應CCTS版本
+ `Title`: 本測試名稱
+ `StartAt`: 初始狀態
+ `States`: 狀態圖hashtable
    + `狀態名稱`:
        + `Comment`: 狀態說明
        + `Next`: (下一個狀態)
            + `State`: 下一個狀態的名稱，需定義在`States中`
            + `Contract Name`: 對應的Contract Test文件名稱
            + `Publisher`
            + `Subscriber`
        + `Choices`: 狀態分歧
            + `狀態名`:
                + `BooleanEquals`: True
                + `Contract Name`: 對應的Contract Test文件名稱
                + `Publisher`:
                + `Subscriber`:
            + `狀態名`
                + `BooleanEquals`: False
                + `Contract Name`: 對應的Contract Test文件名稱
                + `Publisher`:
                + `Subscriber`:
        + `End`: 結束狀態 *True* or *False*

:::spoiler v0.3
```yaml=
CCTS: 0.3
Title: CCTS saga test case1
StartAt: Create top-up event
States:
  Create top-up event:
    Comment: initial state
    Next: 
      Contract Name: payment process contract
      State: payment process
      Publisher: Orchestrator
      Subscriber: Payment
  Payment process:
    Comment: make payment
    Choices:
      Prepare update point:
        BooleanEquals: True
        Contract Name:  make payment contract
        Publisher: Payment
        Subscriber: Orchestrator
      Payment Fail:
        BooleanEquals: False
        Contract Name:  Payment Fail response
        Publisher: Payment
        Subscriber: Orchestrator
  Payment Fail:
    Comment: do payment fail compensation
    Next:
      State: Payment reversed
      Contract Name: Payment Fail response
      Publisher: Orchestrator
      Subscriber: Payment
  Payment reversed:
    Comment: reverse Payment
    Next:
      State: Top-up cancel
      Contract Name: compensation done and cancel saga
      Publisher: Payment
      Subscriber: Orchestrator
  Prepare update point:
    Comment: orchestrator received response from payment and ready to send update point request to Point service
    Next: 
      State: Update point
      Contract Name: update point request contract
      Publisher: Orchestrator
      Subscriber: Point
  Upate point: 
    Comment: do update point
    Choices: 
      Prepare log:
        BooleanEquals: True
        Contract Name: response orchestrator contract
        Publisher: Point
        Subscriber: Orchestrator
      Update point Fail:
        BooleanEquals: False
        Contract Name: notify orchestrator revert point contract
      Publisher: Point
      Subscriber: Orchestrator
  Update point Fail:
    Comment: revert points
    Next:
      State: Revert point
      Contract Name: orchestrator request Point service revert point
      Publisher: Orchestrator
    Subscriber: Point
  Revert point: 
    Comment: revert points
    Next:
      State: Prepare reverse payment
      Contract Name: notify orchestrator do compensation for payment contract
    Publisher: Point
    Subscriber: Orchestrator
  Prepare reverse payment:
    Comment: orchestrator received response that point finished compensation , ready to do payment compensation
    Next: 
      State: Payment reverse
      Contract Name: request payment do compensation contract
    Publisher: Orchestrator
    Subscriber: Payment
  Prepare Log:
    Comment: orchestrator ready to send Log request
    Next:
      State: Log
      Contract Name: Log request Contract
    Publisher: Orchestrator
    Subscriber: Log
  Log:
    Comment: logging
    Next: 
      State: Approved top-update
      Contract Name: log response contract
    Publisher: Log
    Subscriber: Orchestrator
  Approved top-up:
    Comment: finishe saga process
    End: True
  Top-up cancel:
    Comment: finishe compensation process and finishe saga  
    End: True
```
:::

---
### v0.2
#### 變更紀錄
+ 修改`Next`格式，增加狀態轉換預期的pact資訊以及message資訊
+ `Contract Name`表示對應的contract文件
+ 增加欄位型態
#### ccts spec v0.2
+ `CCTS`: 對應CCTS版本
+ `Title`: 本測試名稱
+ `StartAt`: 初始狀態
+ `States`: 狀態圖hashtable
    + `狀態名稱`:
        + `Comment`: 狀態說明
        + `Next`: (下一個狀態)
            + `State`: 下一個狀態的名稱，需定義在`States中`
            + `Contract Name`: 對應的Contract Test文件名稱
            + `Participant Service`:
                + `Publisher`
                + `Subscriber`
        + `Choices`: 狀態分歧
            + `Success`:
                + `BooleanEquals`: True
                + `State`: 狀態名稱
                + `Contract Name`: 對應的Contract Test文件名稱
                + `Participant Service`:
                    + `Publisher`
                    + `Subscriber`
            + `Faliure`
                + `BooleanEquals`: False
                + `State`: 狀態名稱
                + `Contract Name`: 對應的Contract Test文件名稱
                + `Participant Service`:
                    + `Publisher`
                    + `Subscriber`
        + `End`: 結束狀態 *True* or *False*

::: spoiler v0.2
```yaml	
CCTS: 0.2
Title: CCTS saga test case1
StartAt: Create top-up event
States:
  Create top-up event:
    Comment: initial state
    Next: 
      Contract Name: payment process contract
      State: payment process
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Payment
  Payment process:
    Comment: make payment
    Choices:
      Success:
        BooleanEquals: True
        State: Prepare update point
        Contract Name:  make payment contract
        Participant Service:
          Publisher: Payment
          Subscriber: Orchestrator
      Failure:
        BooleanEquals: False
        State: Payment Fail
        Contract Name:  Payment Fail response
        Participant Service:
          Publisher: Payment
          Subscriber: Orchestrator
  Payment Fail:
    Comment: do payment fail compensation
    Next:
      State: Payment reversed
      Contract Name: Payment Fail response
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Payment
  Payment reversed:
    Comment: reverse Payment
    Next:
      State: Top-up cancel
      Contract Name: compensation done and cancel saga
      Participant Service:
        Publisher: Payment
        Subscriber: Orchestrator
  Prepare update point:
    Comment: orchestrator received response from payment and ready to send update point request to Point service
    Next: 
      State: Update point
      Contract Name: update point request contract
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Point
  Upate point: 
    Comment: do update point
    Choices: 
      Success:
        BooleanEquals: True
        State: Prepare log
        Contract Name: response orchestrator contract
        Participant Service:
          Publisher: Point
          Subscriber: Orchestrator
      Failure:
        BooleanEquals: False
        State: Update point Fail
        Contract Name: notify orchestrator revert point contract
        Participant Service:
          Publisher: Point
          Subscriber: Orchestrator
  Update point Fail:
    Comment: revert points
    Next:
      State: Revert point
      Contract Name: orchestrator request Point service revert point
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Point
  Revert point: 
    Comment: revert points
    Next:
      State: Prepare reverse payment
      Contract Name: notify orchestrator do compensation for payment contract
      Participant Service:
        Publisher: Point
        Subscriber: Orchestrator
  Prepare reverse payment:
    Comment: orchestrator received response that point finished compensation , ready to do payment compensation
    Next: 
      State: Payment reverse
      Contract Name: request payment do compensation contract
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Payment
  Prepare Log:
    Comment: orchestrator ready to send Log request
    Next:
      State: Log
      Contract Name: Log request Contract
      Participant Service:
        Publisher: Orchestrator
        Subscriber: Log
  Log:
    Comment: logging
    Next: 
      State: Approved top-update
      Contract Name: log response contract
      Participant Service:
        Publisher: Log
        Subscriber: Orchestrator
  Approved top-up:
    Comment: finishe saga process
    End: True
  Top-up cancel:
    Comment: finishe compensation process and finishe saga  
    End: True
```
:::




### v0.1
+ `CCTS`: 對應CCTS版本
+ `Title`: 本測試名稱
+ `StartAt`: 初始狀態
+ `States`: 狀態圖，以陣列呈現
    + `狀態名稱`:
        + `Comment`: 狀態說明
        + `Next`: 下一個狀態
        + `Choices`: 狀態分歧
            + `BooleanEquals`: bool條件，`Yes` 或 `No`
            + `Next`: 下一個狀態
        + `End`: 結束狀態

:::spoiler Click to show v0.1
```yaml=
CCTS: 0.1
Title: CCTS saga test case1
StartAt: Top-up event start
States:
  Top-up event Start:
    Comment: initial state
    Next: payment Verify
  Payment Verify:
    Comment: make payment in payment service
    Next: Update Points
    Choices:
      - BooleanEquals: yes
        Next: Update Points
      - BooleanEquals: no
        Next: rollback payment
  Update Points:
    Comment: update points in point service
    Choices:
      - BooleanEquals: yes
        Next: log
      - BooleanEquals: no
        Next: rollback points
  rollback payment:
    Comment: payment service compensation
    Next: log
  rollback points:
    Comment: point service compensation
    Next: rollback payment
  Log:
    Comment: logging
    End: True
```
:::


