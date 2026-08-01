# 开发指南 (DEVELOPMENT)

面向要改这个仓库代码的人。玩法说明见 [README-zh_CN.md](README-zh_CN.md)。

---

## 一、环境与命令

Minecraft 1.21.1 / NeoForge 21.1 / Java 21，Parchment 映射。具体版本号全部集中在 `gradle.properties`，**不要往 `build.gradle` 里写死版本**。

```bash
./gradlew build            # 编译 + 打包，产物在 build/libs/
./gradlew runClient        # 开发客户端
./gradlew runServer        # 开发服务端
./gradlew runData          # 数据生成，输出到 src/generated/resources/
./gradlew gameTestServer   # 跑 gametest 后退出
./gradlew exportClasspath  # 把整条 compileClasspath 的 jar 导出到 agent_context/libs/（供反编译查 API）
./gradlew publishMod       # runData + 发 Modrinth + 发 CurseForge
```

> `build.gradle` 里有一段 `-PwithAgentDriver` 的可选 runtime 依赖，AgentDriver 尚未发布，忽略即可。

---

## 二、目录结构

```
src/main/java/net/magicterra/winefoxsspellbooks/
├── WinefoxsSpellbooks.java          @Mod 主类：注册所有子系统
├── LittleMaidSpellbooksCompat.java  @LittleMaidExtension：往 TLM 注册任务/Brain/饰品
├── Config.java                      SERVER 配置
├── api/            对外 API：法术分类枚举 + 四个可监听事件（外部模组在用，改动要谨慎）
├── magic/          魔力、施法、法术目录、recast、召唤物管理、自定义法术
├── entity/         适配器、AI 行为、召唤女仆、配装系统、投射物、云雾实体
├── registry/       所有 DeferredRegister
├── datagen/        数据生成器
├── mixin/          注入 TLM / Iron's 内部
├── effect/         状态效果
├── item/           物品
├── event/          NeoForge 事件处理器
├── client/         渲染、粒子、动画、音效
├── task/           TLM 女仆任务（法术攻击 / 法术支援 / 调试任务）
├── loot/           全局战利品修改器
├── compat/         Jade / JEI / ALI 兼容
├── damage/         伤害类型
└── command/        /wsb 指令

src/main/resources/       手写资源：lang、贴图、geo、animation、音效、mixin 配置、Patchouli 词条
src/generated/resources/  runData 产物，别手改
src/main/templates/       neoforge.mods.toml 模板，用 gradle.properties 的变量插值
```

---

## 三、架构

### 3.1 三个入口

| 入口 | 触发 | 职责 |
|---|---|---|
| `WinefoxsSpellbooks` | `@Mod` | 注册 DeferredRegister、挂 NeoForge 事件、注册 SERVER 配置、挂 reload listener |
| `DataGenerateMod` | `@Mod`（同 modid，只监听 `GatherDataEvent`） | 组装所有 datagen provider |
| `LittleMaidSpellbooksCompat` | `@LittleMaidExtension` | 往 TLM 注册女仆任务、额外 Brain、法术书饰品绑定、Jade 信息 |

### 3.2 法术分类体系

一个法术对女仆的意义由两组正交概念描述：

- **`MaidSpellAction`（行为分类，互斥）**——攻击 / 自我强化 / 位移 / 自我治疗 / 增益队友 / 减益敌人 / 治疗队友。**一个法术只能属于一类**，不属于任何一类的法术女仆永远不会用。
- **`SpellFlag`（标记，可叠加）**——`SUMMON`（召唤术）、`SHOULD_RECAST`（要二段咏唱）。与行为分类正交。

两者都实现 sealed 接口 `SpellCategory`，同时派生出四样东西，**改枚举等于同时改这四处**：

| 派生物 | 例（`SUPPORT_OTHER`） |
|---|---|
| 法术标签 path | `winefoxs_spellbooks:support_effect_spells` |
| 服务端配置键 | `extraSupportEffectSpells` |
| 配装数据包字段 | `support_other` |
| 配置翻译键后缀 | `extra_support_effect_spells` |

⚠️ `catalogName()` 是**已落盘的序列化名**，改了会破坏现存数据包与存档。已知两处不合直觉但不能改：`SUPPORT_OTHER` → `support_effect_spells`；`SpellFlag.SHOULD_RECAST` 的翻译键没有 `extra_` 前缀。

运行时由 `MaidSpellCatalog` 缓存「类别 → 法术集合」：

```
ServerStartingEvent / TagsUpdatedEvent
  → 清空并从法术标签重填
  → post RegisterSpellTypeEvent（外部模组在这里增删）
  → 并入 SERVER 配置里的 extraXxxSpells
```

两个细节：

1. 每次重建是 **clear + refill 同一个 Set 实例**，不换实例——`RegisterSpellTypeEvent` 把这些 Set 直接交给外部模组，换实例会让持旧引用的监听方写进孤儿集合。
2. 首次进世界时 `TagsUpdatedEvent` 早于 SERVER 配置加载，此时跳过配置合并，靠随后的 `ServerStartingEvent` 整体重刷。

禁用一个法术有三条路，能力并不对等：

| 层 | 能加 | 能减 | 备注 |
|---|---|---|---|
| 数据包标签 | ✅ | ✅ | 整合包/服主的正道 |
| `RegisterSpellTypeEvent` | ✅ | ✅ | 代码侧唯一入口，但**会被随后的配置合并覆盖** |
| SERVER 配置 | ✅ | ❌ | 只有 `extraXxxSpells` 加法，没有排除列表 |

女仆手上的可用法术是一份存进附件、跟着实体落盘的快照。目录变了之后由 `MaidEventHandler` 负责把存量女仆重扫一遍（`TagsUpdatedEvent` / `ServerStartingEvent` 上的全量扫描，加上 `EntityJoinLevelEvent` 兜住晚加载的区块），详见 [4.2](#42-女仆手上有哪些法术)。

### 3.3 魔力

女仆的魔力**不在** Iron's 的玩家附件里。

```java
// 对的
MaidMagicManager.getMana(entity);
MaidMagicManager.setMana(entity, value);

// 错的：对女仆返回无人读写的孤儿附件，读恒为 0
MagicData.getPlayerMagicData(maid).getMana();
```

`MaidMagicManager` 内部按实体类型路由：女仆 → `MaidMagicEntity#winefoxsSpellbooks$getMana/setMana`（由 `MaidEntityMixin` 实现，除了写 `MagicData` 还同步 `MAID_MANA` 附件）；其它 `IMagicEntity` → `getMagicData()`；玩家 → 玩家附件 + 手动补一个 `SyncManaPacket`（Iron's 只在自然回魔时同步，兜不住外部写入）。

`getMaxMana` 里的 `maxManaMultiplier` **只对女仆生效**，套到玩家身上会连锁改写 Iron's 的真实上限。

### 3.4 数据驱动的两个 reload listener

都挂在 `AddReloadListenerEvent`，`/reload` 会重新加载：

| 管理器 | 数据包路径 | 内容 |
|---|---|---|
| `SpellDataManager` | `data/<ns>/magic_maid_spell_data/` | `default_casting_range.json`（施法距离）、`default_caused_effect.json`（法术附带的状态效果，AI 靠它判断该不该重复上 buff） |
| `MaidLoadoutManager` | `data/<ns>/summoned_maid_loadouts/` | 召唤女仆配装：分层 + 条件 + 盔甲/武器/法术池 + 等级上限 |

配置项 `extraSpellCastingRange` / `extraSpellCausedEffects` 叠在数据包之上；初次进世界配置晚于数据包加载，靠 `ServerAboutToStartEvent` 里的 `applyConfigOverrides()` 补叠。

### 3.5 二段咏唱（recast）

Iron's 的 recast 收尾逻辑写死在玩家路径上（依赖 `ServerPlayer`），女仆走不到。做法：

- `MaidRecasts extends PlayerRecasts`——只重写生命周期：不同步客户端（女仆没 UI）、自己推 `remainingTicks`、超时清理；记账（入库、计次、序列化）继续走父类，跟着 Iron's 一起变。
- `RecastHandlers`——「法术 id → 收尾动作」的全局静态表。本体在 `IronsCoreRecasts`，附属按 mod 分文件放 `magic/recast/addon/`。
- 反射引用集中在 `AddonReflection`，`ServerStartingEvent` 调 `RecastHandlers.selfCheck()` 校验引用是否还在——附属升级后引用失效会在启动时报出来，而不是等玩家施法才炸。

⚠️ 召唤类法术即使标了 `SHOULD_RECAST` 也不允许女仆连续施放：把 recast 次数用光会走 `USED_ALL_RECASTS` 分支收回刚召出来的东西。选术端（`MaidSpellCatalog.maidMayRepeatCast`）与施法端必须同口径，否则女仆会反复选中一个必被拒绝的法术空转。

### 3.6 召唤女仆

```
SummonMaidSpell / /wsb summon
  → MaidSummonManager 建档（存 level attachment，关服时落盘）
  → SummonedEntityMaid 生成
  → MaidLoadoutManager 按 summonLevel 选 tier → MaidEquipmentRandomizer 随机配装
  → SummonedMaidBrain 驱动（精简版 Brain：只有战斗与跟随，没有拾取/乞讨/回家）
  → 可选骑扫帚（SummonedMaidBroom + 一组 SummonedMaidFly* 行为）
```

`SummonedEntityMaid` 与普通 `EntityMaid` 在很多判定里要一视同仁，别单独 `instanceof EntityMaid`，统一走 `WinefoxHexHelper`：

- `isOwnedMaid(entity)`——是不是「有主人的女仆」，两类都认（雇佣女仆取 `getOwner()`，召唤女仆取 `getSummoner()`），**不区分主人是谁**。
- `isMaidOwnedBy(target, owner)`——是不是「owner 阵营的自家女仆」，内部再过一层 `MaidAllyHelper.isAllied`。增益类效果（披风、酒池回魔、微醺豁免）必须用这个，否则会把别的玩家的女仆当自家人。

### 3.7 Mixin

`winefoxs_spellbooks.mixins.json` 挂了 `WsbMixinPlugin`，约定：

> 软依赖某个 mod 的 mixin，放进 `mixin.<modid>.*` 子包，照常登记进 json。插件自动从类名解析 modid，查 `LoadingModList` 决定是否应用。

例：`mixin.ali.ConfigUtilsMixin` → 只有装了 `ali` 才应用。直接放在 `mixin` 包根下的（如 `MaidEntityMixin`）视为硬依赖，一律应用。`defaultRequire: 1`，注入失败直接崩——升级 TLM / Iron's 后编译通过不代表 mixin 还能注入，务必实际启动一次。

### 3.8 对外事件 API

`api/event/` 下四个：

| 事件 | 时机 | 能做什么 |
|---|---|---|
| `RegisterSpellTypeEvent` | 法术目录重建，标签之后、配置之前 | 直接增删各类别的法术集合 |
| `MaidSpellActionChooseEvent` | `SpellChooseTask#checkExtraStartConditions` 末尾 | 改行为分类权重，或直接指定分类 |
| `MaidSpellChooseEvent` | `SpellChooseTask#start` | 直接指定要施放的法术 |
| `MaidSpellBookEvent` | 女仆装备/卸下法术书 | 感知装备变化 |

`RegisterSpellTypeEvent` **每次标签重载都会重发，且发之前集合已被清空重填**——监听方必须每次都重新施加改动，不能只在首次启动加一遍。

---

## 四、女仆战斗 AI

这是本模组最复杂的部分，改之前先读完本章。

### 4.1 行为从哪里挂上去

TLM 的女仆 Brain 是「按当前任务（Task）动态换一批 Behavior」的结构。本模组：

- **`MaidMagicBrain`（`IExtraMaidBrain`）**只做两件事：注册四个额外记忆模块（`CURRENT_SPELL` / `CURRENT_SPELL_ACTION` / `SUPPORT_TARGET` / `GIFT_DELIVERY`），以及往 idle/work/rest 三档挂晨间送礼行为。**战斗行为不在这里**。
- **战斗行为由任务提供**：`MaidCastingTask#createBrainTasks`（站地上）与 `createRideBrainTasks`（骑乘时）返回行为列表，玩家在女仆 GUI 里切到该任务时才生效。所有行为统一优先级 5。

两个任务：

| 任务 | 类 | 允许的行为分类 | 索敌对象 |
|---|---|---|---|
| 法术攻击 | `MaidCastingTask` | ATTACK / DEFENSE / MOVEMENT / SUPPORT / NEGATIVE | 自己的仇敌 |
| 法术支援 | `MaidMagicSupportTask`（继承前者） | DEFENSE / SUPPORT / POSITIVE / NEGATIVE / SUPPORT_OTHER | 主人和友军正在打的目标 |

行为清单（地面版）：

```
StartAttacking              选敌，仅在没有支援目标时启动
StopAttackingIfTargetInvalid  目标失效/超出 maxSpellRange 时放弃
SpellChooseTask             选行为分类 + 选具体法术       ← 核心
SpellAttackWalkToTarget     按法术射程与 home 范围决定要不要走过去
DrinkPotionsTask            残血喝药（攻击任务专属）
SpellStrafingTask           拉距离 / 贴近 / 绕圈走位
SpellCastingTask            真正起手施法                  ← 核心
MagicMeleeAttack            近战补刀（配置 meleeAttackInMagicTask 开启才挂）
```

骑乘版去掉走位与寻路相关的两条（女仆没法自主控制坐骑）。支援任务不挂喝药与近战。

### 4.2 女仆手上有哪些法术

`MaidEventHandler.onMaidSpellDataChanged(maid)` 重算，结果写进 `MaidSpellDataHolder`——一个**持久化**附件（`MAID_SPELL_DATA`），跟着女仆落盘。重算触发点分三类：

| 触发 | 覆盖 |
|---|---|
| 装备/卸下法术书、`MaidEquipEvent`、`CurioChangeEvent` | 玩家改了女仆的装备 |
| `MaidSpellCatalog` 重建完目录后回调 `refreshLoadedMaidSpellData()` → 全量重扫已加载女仆 | 数据包或配置改了法术分类 |
| `EntityJoinLevelEvent` | 目录建好之后才加载进来的女仆（`/reload` 后才载入的区块） |

后两类是必需的：法术清单是快照，光重建 `MaidSpellCatalog` 不会让存量女仆跟着变。全量重扫**只扫 `EntityMaid`**——召唤女仆的法术来自配装随机而非法术书，重扫会把她的法术表清空。

全量重扫由目录自己在重建末尾收口通知，而不是让 `MaidEventHandler` 再订阅一遍 `TagsUpdatedEvent` / `ServerStartingEvent` 并靠 `EventPriority` 排在目录之后——那种顺序耦合编译期看不见，将来目录多一个重建触发点，下游就会悄悄漏掉。

扫描规则：

- 扫描范围：**主手 + 盔甲栏 + TLM 饰品栏 + Curios 栏**，任何 `ISpellContainer` 都算（法术书、注魔武器/盔甲）。
- **多本法术书只有一本生效**，与玩家一致；注魔装备不受此限。
- 法术等级 = 卷轴等级 + 装备上的亲和（`AffinityData`）加成，再过一次 Iron's 的 `ModifySpellLevelEvent`。
- **不属于任何 `MaidSpellAction` 的法术直接丢弃**——这是「女仆不会用某法术」最常见的原因。
- 按行为分类分七个桶存放；`hasAnyOf(allowedActions())` 决定任务图标上的条件提示亮不亮，类别名单由各任务的 `MaidCastingTask.allowedActions()` 给出，与喂给 `SpellChooseTask` 的是同一份。

### 4.3 索敌

- 攻击任务：先反击最近打自己的怪，否则在近邻表里挑「**带残月烙印的优先**，其次最近」（`WinefoxHexHelper.findNearbyTargetPreferBranded`）。
- 支援任务：先看主人的仇敌，否则遍历附近友军的 `ATTACK_TARGET`，取最近的一个。
- 搜索半径 = `maxSpellRange` 配置（默认 64）。**必须用近邻表而不是可见实体表**，后者会把索敌距离压回原版 16 格，够不着远程法术射程。
- 视线判定走 `LineOfSightGuard.test(...)`；`TargetingConditions` 是复用的单例、每次只改射程——索敌时每个候选都会走一遍，不能逐个新建。

### 4.4 选术：`SpellChooseTask`

整个 AI 的决策中心，一次 `checkExtraStartConditions` 完成筛选与投票：

1. **节流**：`nextSpellTickCount` 倒数，归零才继续，然后重置为 `random(maxComboDelayTick)`。正在施法或喝药时直接跳过。
2. **筛可用法术**：按任务允许的分类逐桶过 `MaidMagicManager.isSpellUsable`——挡掉「有未完成 recast 且不允许女仆逐发」「魔力不够」「冷却中」三种。
3. **收集队友**（仅当有 POSITIVE / SUPPORT_OTHER 可用时）：按 TLM 的 `searchDimension()` 范围取友军，按距离排序。
4. **算权重**（见下表）→ 发 `MaidSpellActionChooseEvent` → **两阶段加权随机**选出行为分类。
5. `start()` 里从该分类的桶中**等概率随机**挑一个法术（可被 `MaidSpellChooseEvent` 覆盖），写进 `CURRENT_SPELL` 记忆，弹聊天气泡。

权重公式（`mob` = 女仆自己，`hp%` = 当前血量比例）：

| 分类 | 权重 |
|---|---|
| ATTACK | 无视线则 0；否则 `80 + (1-目标hp%)*60 + 距离项` |
| DEFENSE | `20 + 50*(1-hp%³) - (1-目标hp%)*35 + Σ每个可用防御法术(未上此 buff +20 / 效果未知 +10)` |
| MOVEMENT | `距离占比*50 + (无视线 ? 80 : 0) + 400*(1-hp%)²*(1-距离占比)²` |
| SUPPORT（自我治疗） | `-10 + 200*(1-hp%) - (1-距离占比)*75`（敌人贴脸时不停下来喝药） |
| NEGATIVE | 以 ATTACK 权重为基数，每个可用法术再 +10（敌人没这 debuff）/ +5（效果未知） |
| POSITIVE（增益队友） | 队友缺此 buff → 500；净化术且队友有可净化负面 → 1000；魔力转移且目标确实缺蓝 → 1000，否则 0；效果未知 → 10 |
| SUPPORT_OTHER（治疗队友） | `floor((1-队友hp%) * 2500)`——队友越残压过一切 |

另有**连招抑制**：上一次选了 DEFENSE / NEGATIVE 则本次该项 −100，MOVEMENT 则 −50，避免女仆连续刷同一类。

**两阶段抽取有个容易踩的语义**：

- 先把 SUPPORT 放进池子，再看「有没有附近队友或已有支援目标」——**有**的话就把 POSITIVE / SUPPORT_OTHER 也放进去，只要池子非空立刻抽签返回，战斗类**完全没有机会参与**（残血队友必定被优先照顾）。
- **没有**队友时不提前返回，SUPPORT 会与 ATTACK / DEFENSE / MOVEMENT / NEGATIVE 同池竞争。

选中 POSITIVE / SUPPORT_OTHER 后，`start()` 会遍历队友挑第一个权重 > 0 的写进 `SUPPORT_TARGET`，同时**清掉 `ATTACK_TARGET` 并停下导航与走位**——这是女仆战斗中突然转头去奶人的原因。

### 4.5 施法：`SpellCastingTask`

- 起手条件：`CURRENT_SPELL` 记忆存在、没在施法/喝药、目标存活、（看得见 或 失去视线不超过 50 tick）、**距离在该法术的 `casting_range` 之内**。
- 骑乘时若够不着，直接清空目标——女仆无法自主控制扫帚，不清会永远卡住。
- `doSpellAction` 里再过一道 recast 门禁：已有未完成 recast 且该法术不允许女仆逐发时，本次不放。
- 施法前 `lookAt(EYES, target)` 把身体和头都转过去，随后 `initiateCastSpell`。
- 持续期间 `canStillUse` 盯着 `magicData.isCasting()`、目标是否死亡、视线计数、以及 Iron's 自己的 `shouldAIStopCasting`。
- `stop()` 会 `cancelCast()` 并清掉 `CURRENT_SPELL` 与 `LOOK_TARGET`，把视线交还给其它行为。

真正的施法执行在 `MagicMaidAdapter`：以 `CastSource.MOB` 走 Iron's 的 `MagicData.initiateCast`，咏唱推进靠宿主实体每 tick 回调适配器的 `tickMagicLogic` / `aiStepMagicLogic` / `completeUsingItem`（`MaidEntityMixin` 负责接线）。适配器上的 `bypassCastCheck` 只跳过起手检查，**不豁免魔力消耗**。

### 4.6 走位与辅助行为

- **`SpellAttackWalkToTarget`**：按当前法术的射程（不是固定值）和女仆的 home 范围决定要不要走过去。
- **`SpellStrafingTask`**：距离小于射程的一半时尝试后撤（`fleeCooldown` 100 tick 限流），在射程内且有视线时停下原地绕圈，连续绕满 50 tick 后每 tick 有 10% 概率反向；施法期间移速降为 0.75 倍。女仆被命令坐下时不走位。
- **`DrinkPotionsTask`**：血量低于 60% 且 100 tick 冷却已过时喝药，受 `drinkPotionInBattle` 配置控制，仅攻击任务挂载（避免非战斗状态狂灌药水）。
- **`MagicMeleeAttack`**：可选的近战补刀，`meleeAttackInMagicTask` 默认关闭。

### 4.7 记忆模块

| 记忆 | 写入方 | 清除方 |
|---|---|---|
| `ATTACK_TARGET`（原版） | `StartAttacking` | `StopAttackingIfTargetInvalid`；选中支援分类时也会被清 |
| `CURRENT_SPELL_ACTION` | `SpellChooseTask#checkExtraStartConditions` | `SpellChooseTask#stop` |
| `CURRENT_SPELL` | `SpellChooseTask#start` | `SpellChooseTask#stop` / `SpellCastingTask#stop` |
| `SUPPORT_TARGET` | `SpellChooseTask#start` | `SpellChooseTask#stop` |
| `GIFT_DELIVERY` | 晨赠事件 | `MaidDeliverGiftBehavior` |

`enableLookAndRandomWalk` 在两个目标记忆都为空时才放行，否则战斗中女仆会一边打一边东张西望。

### 4.8 召唤女仆的差异

`SummonedMaidBrain` 是独立的精简 Brain，只保留战斗与跟随召唤者，另加飞行分支（`SummonedMaidFlyStrafingTask` / `FlyToSummoner` / `RandomFlight` / `PilotBroomTask` / `DismountTask`）与 `MobClimbTask`。选术与施法复用 `SpellChooseTask` / `SpellCastingTask`（它们的形参是 `Mob` 而非 `EntityMaid`，就是为此），法术来源改由配装系统随机生成而不是法术书。

### 4.9 扩展点

优先用事件，其次继承：

- 改分类倾向 → `MaidSpellActionChooseEvent` 调权重或直接钦定分类。
- 改具体法术 → `MaidSpellChooseEvent#setSelectedSpell`。
- 改哪些法术算哪一类 → 数据包标签，或 `RegisterSpellTypeEvent`。
- 需要完全另一套决策 → 继承 `SpellChooseTask`。`usableSpells` 各桶实例构造后不再更换（`SpellTestChooseTask` 就直接持引用填充），重写时保持这个约定。

---

## 五、常见任务怎么做

### 5.1 加一个本模组的新法术

1. `magic/spell/XxxSpell.java` 继承 Iron's 的 `AbstractSpell`（参考 `TipsyMistSpell` / `VintnersBombSpell`）。
2. 在 `registry/WsbSpells` 里 `registerSpell(new XxxSpell())`——注册名取自 `spell.getSpellName()`。
3. `datagen/spelltag/WinefoxsSpellbooksTags.java` 里归类（必须归进某个 `MaidSpellAction`，否则女仆不会用），需要的话打 `summon` / `maidShouldRecast` 标记。
4. `datagen/spelldata/WinefoxsSpellbooksData.java` 里补施法距离与附带效果。
5. lang 两份：`en_us.json` + `zh_cn.json`。
6. 要出卷轴的话确认学派、`WsbItemTags`、战利品修改器（`datagen/WsbGlobalLootModifierProvider`）。
7. `./gradlew runData && ./gradlew build`。
8. 需要 Patchouli 词条的话，在 `assets/touhou_little_maid/patchouli_books/memorizable_gensokyo/{en_us,zh_cn}/entries/winefoxs_spellbooks/` 下加一份。

### 5.2 支持一个新的 Iron's 附属模组

1. `gradle.properties` 加版本号变量，`build.gradle` 加依赖（CurseMaven 用 `curse.maven:<slug>-<projectId>:<fileId>`）。
2. `./gradlew exportClasspath` 导出 jar，反编译看它的法术注册类。
3. **新建**独立文件，不要往现有文件里追加：
   - `datagen/spelltag/<Mod>Tags.java`（必需）——`contribute(SpellTagContext ctx)`，逐个 `ctx.attack().addOptional(...)`。**一律用 `addOptional`**，模组缺席时不会炸。
   - `datagen/spelldata/<Mod>Data.java`（有需要覆写的施法距离或附带效果时才建；例如 Snow Waifu Spell 就只有 Tags 文件）。
4. 在 `MaidSpellTagsProvider#addTags`（及 `SpellDataProvider#run`）里挂上新 contributor。
5. 需要 recast 收尾的，加 `magic/recast/addon/<Mod>Recasts.java` 并注册进 `RecastHandlers`。
6. `./gradlew runData && ./gradlew build`。

> 每个附属拆独立文件是刻意的，便于并行改不同附属而不冲突。

### 5.3 加状态效果 / 物品 / 实体 / 粒子

统一走 `registry/` 下对应的 `Wsb*` 类，都是 `DeferredRegister` + 静态字段。附带工作：

- **物品说明**：只要在 lang 里加 `item.winefoxs_spellbooks.<path>.desc`，`WsbItemDescTooltipHandler` 会自动拼进 tooltip，不用 override `appendHoverText`；JEI 物品信息页也复用这个 key，不需要额外写插件。`\n` 会被拆成多行。
- **物品模型**：在 `datagen/ItemModelGenerator` 里加一行，别手写 json。
- **GeckoLib 实体**：`DefaultedEntityGeoModel` 把资源解析到 `geo/entity/`、`animations/entity/`、`textures/entity/` 三个**带 `entity/` 子目录**的路径。放错位置编译期发现不了，渲染那一刻才崩。
- **状态效果**：加减属性用内建的 `addAttributeModifier` builder，别在 `applyEffectTick` 里手动加删——刷新时会丢。
- **AI 相关的效果**：新效果若会影响女仆选术（例如提供某个 buff），记得在对应的 `spelldata` 里登记 `caused_effect`，否则 AI 只按「效果未知」给 10 分。

### 5.4 加配置项

全在 `Config.java`，且**全部是 SERVER 配置**。加一项要同时写 `defineXxx` + `.translation(translateKey("..."))` + 两份 lang 的 `config.winefoxs_spellbooks.<key>`。

配置曾从 COMMON 改成 SERVER，文件名跟着变；主类里的 `warnAboutOrphanedCommonConfig()` 会在检测到旧文件时提示，不要删。

---

## 六、数据生成

`./gradlew runData` **全量重写** `src/generated/resources/`，手改必丢。当前 provider：

| Provider | 产物 |
|---|---|
| `MaidSpellTagsProvider` | `tags/irons_spellbooks/spells/*.json`（九个法术标签） |
| `SpellDataProvider` | `magic_maid_spell_data/default_*.json` |
| `MaidLoadoutDataProvider` | `summoned_maid_loadouts/{mage,warrior,support}.json` |
| `MaidLoadoutTagsProvider` | `tags/item/maid_loadout/**` |
| `MaidEntityTagsProvider` | 女仆实体标签 |
| `WsbDamageTypeTagsProvider` | 学派伤害 → `neoforge:is_magic` |
| `WsbGiftLootSubProvider` / `WsbGlobalLootModifierProvider` | 赠礼战利品表、宝箱注入修改器 |
| `ItemModelGenerator` | `assets/.../models/item/*.json` |

datagen 会先跑 `ConfigTracker.INSTANCE.loadDefaultServerConfigs()` 模拟默认配置，所以 provider 里读配置是安全的。

手写、不受 `runData` 影响的：lang、贴图、geo、animation、音效、`sounds.json`、Patchouli 词条、mixin 配置。

---

## 七、测试与调试

- **调试任务**：`MaidDrinkPotionTask`、`MaidSpellTestTask` 只在 `WinefoxsSpellbooks.DEBUG`（非 production）下注册，正式包没有。法术测试任务用 `SpellTestChooseTask` 替换选术逻辑——取 `MaidSpellCatalog` 里注册的全部法术（按最高等级），**轮流**遍历行为分类与法术而不是加权随机，用来把一整套法术挨个跑一遍。
- **指令**（OP 2 级）：

  ```
  /wsb cast <targets> <spell> [level]
  /wsb summon [target] [count] [summonLevel] [loadout] [duration] [airForce]
  ```

  `/wsb cast` 会临时打开适配器的 `bypassCastCheck`，**只跳过起手时的可用性检查，施法完成阶段照样扣魔力**。测试前把 `irons_spellbooks:max_mana` 拉高并等回蓝。
- **gametest**：`neoforge.enabledGameTestNamespaces` 已设为本模组 id，`./gradlew gameTestServer` 或游戏内 `/test` 可跑（目前仓库还没有用例）。
- **调 AI 时**：打开 `showChatBubbles` 配置，女仆会把每次选中的法术显示在气泡里；选不出支援法术时也会气泡提示，比翻日志快。

---

## 八、代码风格

`.editorconfig` 是权威（IDEA 方案导出），要点：

- 4 空格缩进，LF，UTF-8，行宽按 ~120 折行
- K&R 大括号，成员字段之间空一行
- 显式 import，顺序 `java.*` → `javax.*` → 第三方 → `net.magicterra.*`
- 类名 PascalCase，方法 camelCase，常量 UPPER_SNAKE_CASE
- 公开 API 写 JavaDoc，带 `@author` 与 `@since`
- 内部逻辑注释用中文；`instanceof` 模式匹配优先；能用 record / sealed / switch 表达式就用
- 日志一律走 `WinefoxsSpellbooks.LOGGER`

注释规范：**简洁自洽**。注释解释「为什么这么写」，尤其是那些看起来可以简化但实际不能的地方——本仓库这类坑很多，注释就是唯一的文档。不要在注释里引用外部文档编号或任务代号。

---

## 九、提交与发布

提交信息用 Conventional Commits，祈使句、小写、不带句号：

```
feat: classify the spells added by the updated addon mods
fix: keep the foxshade biter out of saves and reset its leap on restart
refactor: drop private field reflection from maid recast bookkeeping
chore: bump neoforge, iron's spells and the addon dependencies
```

发版流程：

1. 改 `gradle.properties` 的 `mod_version`（必要时改 `version_type`）。
2. `CHANGELOG.md` 顶部加一段，格式必须是：

   ```markdown
   ## <版本号>

   ...中英双语条目...

   ---
   ```

   `build.gradle` 的 `getChangelogForVersion()` 按 `## <version>` 起、`---` 止截取，CI 里另有一段 `awk` 做同样的事——分隔线漏了会把后面所有内容都当成本版更新日志。
3. 打 tag 推送：`git tag v1.0.0-beta.13 && git push --tags`。
4. `.github/workflows/publish.yml` 接管：JDK 21 → `./gradlew publishMod -Pmod_version=<tag 去掉 v>` → 发 Modrinth + CurseForge（含源码 jar）→ 建 GitHub prerelease。

需要的 secrets：`MODRINTH_TOKEN`、`CURSEFORGE_TOKEN`；本地手动发版认同名环境变量。
