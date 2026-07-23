# Frontend

Vue 3 SPA，提供館藏搜尋、書籍明細、註冊登入、借書、還書與個人借閱紀錄。

## 結構

- `views`：路由頁面與頁面級資料載入。
- `components`：導覽、書封、分頁與狀態顯示。
- `services`：Axios client 與 REST API 封裝。
- `stores`：Pinia 登入狀態。
- `utils`：session、格式化與表單驗證。

JWT 儲存在 `sessionStorage`，重新整理後保留，同一分頁關閉後清除。401 回應會清除失效 session；token 不會寫入 URL 或 console。

## 視覺規則

- 冷白背景、炭黑文字、深綠單一強調色。
- 不使用漸層、陰影、發光與玻璃效果。
- 不使用 emoji 或裝飾性狀態圖示。
- 版面採不對稱縮排與不等寬配置，不使用等寬卡片陣列。
- 桌面頁面標題不超過 40px，手機不超過 31px。
- 動態僅限必要的顏色回饋，並支援 `prefers-reduced-motion`。

完整設計決策請見 [`docs/frontend-design.md`](../docs/frontend-design.md)。

## 本機開發

```bash
npm ci
npm run dev
```

Vite 預設在 `http://localhost:5173`，並將 `/api` 代理到 `http://localhost:8080`。

## 驗證

```bash
npm test
npm run build
```

正式容器由 Nginx 提供 SPA 靜態檔案、history fallback、安全回應標頭與 `/api` reverse proxy。
