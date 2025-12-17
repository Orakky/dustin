
# 会话Session控制层
## 根据工号生成sessionId

**URL:** `/session/createSessionId`

**Type:** `POST`


**Content-Type:** `application/x-www-form-urlencoded;charset=UTF-8`

**Description:** 根据工号生成sessionId



**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|username|string|false|No comments found.|-|


**Request-example:**
```bash
curl -X POST -i '/session/createSessionId' --data '=0zm7wl'
```

**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|status|string|响应状态|-|
|msg|string|响应编码|-|
|data|string|返回数据|-|

**Response-example:**
```json
{
  "status": "",
  "msg": "",
  "data": ""
}
```

## 根据sessionId获取到session

**URL:** `/session/getSession`

**Type:** `POST`


**Content-Type:** `application/x-www-form-urlencoded;charset=UTF-8`

**Description:** 根据sessionId获取到session



**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|sessionId|string|false|No comments found.|-|


**Request-example:**
```bash
curl -X POST -i '/session/getSession' --data '=v8j2q6'
```

**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|status|string|响应状态|-|
|msg|string|响应编码|-|
|data|object|返回数据|-|
|└─bcsId|int32|No comments found.|-|
|└─sessionId|string|会话ID|-|
|└─sessionName|string|会话名称|-|
|└─username|string|会话所属用户编号|-|
|└─createdBy|string|创建人|-|
|└─createdTime|string|创建时间|-|
|└─updatedBy|string|更新人|-|
|└─updatedTime|string|更新时间|-|
|└─deletedFlg|string|删除标记;删除标记 0-未删除 1-已删除|-|
|└─deletedBy|string|删除人|-|
|└─deletedTime|string|删除时间|-|

**Response-example:**
```json
{
  "status": "",
  "msg": "",
  "data": {
    "bcsId": 0,
    "sessionId": "",
    "sessionName": "",
    "username": "",
    "createdBy": "",
    "createdTime": "yyyy-MM-dd HH:mm:ss",
    "updatedBy": "",
    "updatedTime": "yyyy-MM-dd HH:mm:ss",
    "deletedFlg": "",
    "deletedBy": "",
    "deletedTime": "yyyy-MM-dd HH:mm:ss"
  }
}
```

## 根据工号查询历史会话集合

**URL:** `/session/listHistorySession`

**Type:** `POST`


**Content-Type:** `application/x-www-form-urlencoded;charset=UTF-8`

**Description:** 根据工号查询历史会话集合



**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|username|string|false|No comments found.|-|


**Request-example:**
```bash
curl -X POST -i '/session/listHistorySession' --data '=r2q5pi'
```

**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|status|string|响应状态|-|
|msg|string|响应编码|-|
|data|array|返回数据|-|
|└─sessionId|string|No comments found.|-|
|└─sessionName|string|No comments found.|-|
|└─username|string|No comments found.|-|
|└─createTime|string|No comments found.|-|
|└─messageVoList|array|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─bcmId|int32|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─sessionId|string|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─username|string|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─messageType|string|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─message|string|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─createTime|string|No comments found.|-|
|└─chatMessageList|array|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─role|enum|角色<br/>(See: 聊天角色)|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─content|string|内容|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─metadata|map|获取元数据|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─any object|object|any object.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─thinking|boolean|是否思考中|-|

**Response-example:**
```json
{
  "status": "",
  "msg": "",
  "data": [
    {
      "sessionId": "",
      "sessionName": "",
      "username": "",
      "createTime": "yyyy-MM-dd HH:mm:ss",
      "messageVoList": [
        {
          "bcmId": 0,
          "sessionId": "",
          "username": "",
          "messageType": "",
          "message": "",
          "createTime": ""
        }
      ],
      "chatMessageList": [
        {
          "role": "SYSTEM",
          "content": "",
          "metadata": {
            "mapKey": {}
          },
          "thinking": true
        }
      ]
    }
  ]
}
```

## 根据sessionId查出历史消息

**URL:** `/session/getMessages`

**Type:** `POST`


**Content-Type:** `application/x-www-form-urlencoded;charset=UTF-8`

**Description:** 根据sessionId查出历史消息



**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|sessionId|string|false|No comments found.|-|


**Request-example:**
```bash
curl -X POST -i '/session/getMessages' --data '=v82aq2'
```

**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|status|string|响应状态|-|
|msg|string|响应编码|-|
|data|array|返回数据|-|
|└─bcmId|int32|表主键|-|
|└─sessionId|string|会话ID|-|
|└─username|string|会话所属人工号|-|
|└─messageType|string|消息类型;消息类型,0-userMessage 1-systemMessage 2-assistantMessage 3-toolMessage|-|
|└─messageJson|string|消息体|-|
|└─createdBy|string|创建人|-|
|└─createdTime|string|创建时间|-|
|└─updatedBy|string|更新人|-|
|└─updatedTime|string|更新时间|-|
|└─deletedFlg|string|删除标记;删除标记 0-未删除 1-已删除|-|
|└─deletedBy|string|删除人|-|
|└─deletedTime|string|删除时间|-|

**Response-example:**
```json
{
  "status": "",
  "msg": "",
  "data": [
    {
      "bcmId": 0,
      "sessionId": "",
      "username": "",
      "messageType": "",
      "messageJson": "",
      "createdBy": "",
      "createdTime": "yyyy-MM-dd HH:mm:ss",
      "updatedBy": "",
      "updatedTime": "yyyy-MM-dd HH:mm:ss",
      "deletedFlg": "",
      "deletedBy": "",
      "deletedTime": "yyyy-MM-dd HH:mm:ss"
    }
  ]
}
```

