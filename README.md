# 苍穹外卖 (sky-take-out)

前后端分离的外卖系统：Spring Boot 3 后端 + 微信小程序 C 端 + Vue 管理端。
覆盖微信登录、菜品/套餐浏览、购物车、地址簿、下单支付、管理端订单处理、WebSocket 来单提醒、工作台与运营报表导出。

> 完整的模块拆解、接口明细与遗留问题见 **[项目拆解.md](项目拆解.md)**。

## 技术栈

`Spring Boot 3.4` · `Java 17` · `MyBatis` · `MySQL` · `Redis` · `JWT (jjwt 0.12)` · `WebSocket` · `POI` · `knife4j`
前端：微信小程序（原生） / Vue 2 + Element UI + Vite

## 模块结构

| 模块 | 职责 |
|---|---|
| `sky-common` | 常量、业务异常、工具类、统一返回 `Result` |
| `sky-pojo` | Entity / DTO / VO |
| `sky-server` | Controller / Service / Mapper / AOP 自动填充 / 定时任务 / WebSocket |
| `admin-web` | 管理端演示页（来单提醒 + 语音播报） |

## 快速开始

1. **数据库**：MySQL 8 建库 `sky_take_out`，导入建表脚本（脚本未随仓库提供）
2. **Redis**：本机 6379 启动
3. **配置**：复制 `sky-server/src/main/resources/application-dev.example.yml` 为 `application-dev.yml`，填写数据库、Redis、微信参数（该文件已被 `.gitignore` 忽略）
4. **启动**：`mvn -pl sky-server spring-boot:run`，或直接运行 `SkyApplication`
5. **接口文档**：<http://localhost:8080/doc.html>

管理端演示页：`cd admin-web && npm install && npm run dev`

## 已知限制

- 微信支付依赖商户号、证书与 APIv3 密钥，未配置时支付与回调不可用
- `admin-web` 只包含来单提醒页，管理端业务操作暂通过接口文档完成
- 微信小程序代码不在本仓库
