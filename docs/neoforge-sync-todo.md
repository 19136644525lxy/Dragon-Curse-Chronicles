# NeoForge 同步待办事项

> 本文件记录 Forge 端已完成但 NeoForge 端尚未同步的功能变更。
> 每次 Forge 端提交后更新此清单，NeoForge 开发时按此逐项同步。

---

## 流星雨事件系统完善

- [ ] **维度检测修复**：`areAllPlayersInOverworld()` 改用 `server.getPlayerList().getPlayers()` 全局玩家列表
- [ ] **新增配置项**：`triggerProbability`(0.05)、`cubeSpawnProbability`(0.10)、`askTimeoutSeconds`(60)
- [ ] **配置覆盖检测**：旧配置文件缺失项自动补全并写回磁盘，非法值回退默认值
- [ ] **触发逻辑重构**：午夜0点 → 5%概率判定 → 失败通知 / 成功询问随机玩家 → ClickEvent 确认 → 启动
- [ ] **新增指令**：`confirm`/`cancel`（玩家确认取消）、`config probability`/`cube`（概率设置）
- [ ] **宝箱安全区**：爆炸点离已有宝箱 <15 格时跳过方块破坏
- [ ] **唐扇魔方概率**：从硬编码 35% 改为配置读取，默认 10%
- [ ] **语言文件**：新增 15 个翻译键（中/英文）

### NeoForge API 映射注意

| Forge API | NeoForge API |
|-----------|-------------|
| `FMLPaths.CONFIGDIR` | `FMLPaths.CONFIGDIR`（相同） |
| `server.getPlayerList().getPlayers()` | `server.getPlayerList().getPlayers()`（相同） |
| `connection.isAcceptingMessages()` | `connection.isAcceptingMessages()`（相同） |
| `ClickEvent(Action.RUN_COMMAND, ...)` | `ClickEvent(ClickEvent.Action.RUN_COMMAND, ...)`（相同） |
| `Style.withClickEvent()` | `Style.withClickEvent()`（相同） |
| `BlockPos.distManhattan()` | `BlockPos.distManhattan()`（相同） |

> NeoForge 1.21.1 的事件系统和 Brigadier 指令系统与 Forge 1.20.1 基本一致，
> 但需注意 `LivingTickEvent` → `EntityTickEvent.Post`、包路径变更等差异。

---

## 生肖边缘光效（BEWLR 方案）

- [ ] **TalismanGlowRenderer**：自定义 BlockEntityWithoutLevelRenderer，在物品表面渲染加法混合发光层
- [ ] **TalismanClientExtensions**：注册 IClientItemExtensions，接管符咒物品渲染
- [ ] **RenderHandEvent 监听**：第一人称施法时追加脉冲光效
- [ ] **生肖颜色映射**：12 个生肖各自的颜色 RGB 值
- [ ] **发光纹理**：12 张生肖发光叠加纹理（128x128，黑底亮色图案）
- [ ] **TalismanBase 接口扩展**：`getZodiacColor()` 方法供渲染器调用

### NeoForge API 映射注意

| Forge 1.20.1 API | NeoForge 1.21.1 API |
|-------------------|---------------------|
| `BlockEntityWithoutLevelRenderer` | `BlockEntityWithoutLevelRenderer`（包路径可能变更） |
| `IClientItemExtensions` | NeoForge 使用 `IClientItemExtensions` 或等效接口 |
| `RenderType.eyes(texture)` | `RenderType.eyes(texture)`（需验证） |
| `RenderHandEvent` | `RenderHandEvent`（包路径 `net.neoforged.neoforge.client.event`） |
| `InitializeClient` 事件 | NeoForge 客户端注册事件（需验证） |
| `BakedModel.isCustomRenderer()` | 需验证 NeoForge 1.21.1 是否保留 |
| `ItemRenderer.renderStatic()` | 需验证签名是否变更 |

---

## 其他历史待同步项

- [ ] 老爹的河豚干激光粒子修复（Forge/Fabric 已修，NeoForge 待同步）
- [ ] 始源之力附魔系统（6-10级合成、盔甲6层效果、光环开关）
- [ ] 河豚干5秒使用冷却
- [ ] 鸡兔之力效果修复
