# basicMcp 生产者

## 根据工作室名称获取键盘数据

**service-name:** `getKeyboardByName`

**description:** 根据工作室名称获取键盘数据

**query-parameters:**

| parameter | type   | required | description | since |
|-----|--------|------|------|-------|
| studioName| string | true | 工作室名称| -     |


**response-fields:**

| field    | type   | description | since |
|----------|--------|-------------|-------|
| keyboard | string | 键盘合集        | -     |

**response-example:**
```json
{
  "keyboards": "one,two,three"
}
```
