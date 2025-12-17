# basicMcp 生产者

## 根据工作室名称获取键盘数据

**1 service-name:** `getKeyboardByName`

**2 description:** 根据工作室名称获取键盘数据

**3 query-parameters:**

| parameter  | type   | required | description | since |
|------------|--------|----------|-------------|-------|
| studioName | string | true     | 工作室名称       | -     |


**4 response-fields:**

| field    | type   | description | since |
|----------|--------|-------------|-------|
| keyboard | string | 键盘合集        | -     |

**5 response-example（返回示例，仅作结构参考和约束）:**
```json
{
  "keyboards": "one,two,three"
}
```


## 修改键盘参数

**1 service-name:** `modifyKeyboard`

**2 description:** 修改键盘参数

**3 query-parameters:**

| parameter    | type   | required | description | since |
|--------------|--------|----------|-------------|-------|
| studioName   | string | true     | 工作室名称       | -     |
| keyboardName | string | true     | 键盘名称        | -     |
| degree       | string | false    | 坡度          | -     |
| frontHeight  | string | false    | 前高          | -     |

**4 response-field:**

| field        | type   | description | since |
|--------------|--------|-------------|-------|
| studioName   | string | 工作室名称       | -     |
| keyboardName | string | 键盘名称        | -     |
| degree       | string | 坡度          | -     |
| frontHeight  | string | 前高          | -     |

**5 response-example（返回示例，仅作结构参考和约束）:**
```json
{
  "studioName": "studio1",
  "keyboardName": "keyboard1",
  "degree": "slope1",
  "frontHeight": "height1"
}
```

## 查询键盘工作室

**1 service-name:** `queryKeyboardStudio`

**2 description:** 查询键盘工作室

**3 query-parameters:**

| parameter | type   | required | description | since |
|-----------|--------|----------|-------------|-------|
| 工作室名称     | string | false    | 查询键盘工作室     | -     |


**4 response-fields:**

| field        | type  | description | since |
|--------------|-------|-------------|-------|
| studioList   | array | 工作室合集       | -     |
| keyboardList | array | 键盘合集        |       |

**5 response-example（返回示例，仅作结构参考和约束）:**
```json
{
  "studioList": ["sd1","sd2","sd3"],
  "keyboardList": ["one","two","three"]
}
```
