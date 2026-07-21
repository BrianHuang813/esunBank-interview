# Backend

Spring Boot REST API。程式依題目要求切成四個主要責任層：

- `presentation`：HTTP controller、request/response DTO 與輸入驗證
- `business`：使用案例、借還書規則與交易協調
- `data`：Stored Procedure 呼叫與資料列映射
- `common`：統一回應、例外與跨層共用元件

`security` 獨立負責 JWT 與 Spring Security，不將驗證邏輯混入 controller。

層級依賴只能由外向內：`presentation -> business -> data`。`presentation` 不得直接呼叫 repository，`data` 不得依賴 controller DTO。

啟動：`mvn spring-boot:run`。測試：`mvn test`。
