# 开发指南 (DEVELOPMENT)

面向本仓库的开发者。玩法说明见 [README-zh_CN.md](README-zh_CN.md)。

---

## 一、环境与命令

Minecraft 1.21.1 / NeoForge 21.1 / Java 21，Parchment 映射。版本号统一定义于 `gradle.properties`，**不要在 `build.gradle` 中硬编码版本**。

```bash
./gradlew build            # 编译 + 打包，产物在 build/libs/
./gradlew runClient        # 开发客户端
./gradlew runServer        # 开发服务端
./gradlew runData          # 数据生成，输出到 src/generated/resources/
./gradlew gameTestServer   # 运行 gametest 后退出
./gradlew exportClasspath  # 导出 compileClasspath 上的全部 jar 至 agent_context/libs/（供反编译查阅 API）
./gradlew publishMod       # runData + 发布至 Modrinth 与 CurseForge
```

> `build.gradle` 中的 `-PwithAgentDriver` 为可选 runtime 依赖，AgentDriver 尚未发布，可暂不关注。

---

## 二、目录结构

```
src/main/java/net/magicterra/winefoxsspellbooks/
├── WinefoxsSpellbooks.java          @Mod 主类：注册所有子系统
├── LittleMaidSpellbooksCompat.java  @LittleMaidExtension：向 TLM 注册任务/Brain/饰品
├── Config.java                      SERVER 配置
├── api/            对外 API：法术分类枚举 + 四个可监听事件（已有外部模组依赖，改动需谨慎）
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
src/generated/resources/  runData 产物，请勿手动修改
src/main/templates/       neoforge.mods.toml 模板，以 gradle.properties 中的变量插值生成
```

---

## 三、架构

### 3.1 三个入口

| 入口 | 触发 | 职责 |
|---|---|---|
| `WinefoxsSpellbooks` | `@Mod` | 注册 DeferredRegister、挂载 NeoForge 事件、注册 SERVER 配置、注册 reload listener |
| `DataGenerateMod` | `@Mod`（同 modid，只监听 `GatherDataEvent`） | 组装所有 datagen provider |
| `LittleMaidSpellbooksCompat` | `@LittleMaidExtension` | 向 TLM 注册女仆任务、额外 Brain、法术书饰品绑定、Jade 信息 |

### 3.2 法术分类体系

一个法术对女仆的意义由两组正交概念描述：

- **`MaidSpellAction`（行为分类，互斥）**——攻击 / 自我强化 / 位移 / 自我治疗 / 增益队友 / 减益敌人 / 治疗队友。**一个法术只能属于一类**；不属于任何分类的法术，女仆不会使用。
- **`SpellFlag`（标记，可叠加）**——`SUMMON`（召唤术）、`SHOULD_RECAST`（需要二段咏唱）。与行为分类正交。

两者都实现 sealed 接口 `SpellCategory`，并派生出以下四项，**修改枚举将同时影响这四处**：

| 派生物 | 例（`SUPPORT_OTHER`） |
|---|---|
| 法术标签 path | `winefoxs_spellbooks:support_effect_spells` |
| 服务端配置键 | `extraSupportEffectSpells` |
| 配装数据包字段 | `support_other` |
| 配置翻译键后缀 | `extra_support_effect_spells` |

⚠️ `catalogName()` 是**已持久化的序列化名**，修改会破坏现存数据包与存档。以下两处命名不符直觉，但不可更改：`SUPPORT_OTHER` → `support_effect_spells`；`SpellFlag.SHOULD_RECAST` 的翻译键没有 `extra_` 前缀。

运行时由 `MaidSpellCatalog` 缓存「类别 → 法术集合」：

```
ServerStartingEvent / TagsUpdatedEvent
  → 清空并从法术标签重填
  → post RegisterSpellTypeEvent（外部模组在此增删）
  → 并入 SERVER 配置里的 extraXxxSpells
```

两点实现约束：

1. 每次重建为 **clear + refill 同一个 Set 实例**，不更换实例——`RegisterSpellTypeEvent` 会将这些 Set 直接暴露给外部模组，更换实例将导致持有旧引用的监听方写入孤儿集合。
2. 首次进入世界时 `TagsUpdatedEvent` 早于 SERVER 配置加载，此时跳过配置合并，由随后的 `ServerStartingEvent` 整体重刷。

禁用一个法术有三条途径，能力并不对等：

| 层 | 能加 | 能减 | 备注 |
|---|---|---|---|
| 数据包标签 | ✅ | ✅ | 整合包与服主的推荐方式 |
| `RegisterSpellTypeEvent` | ✅ | ✅ | 代码侧唯一入口，但**会被随后的配置合并覆盖** |
| SERVER 配置 | ✅ | ❌ | 只有 `extraXxxSpells` 加法，没有排除列表 |

女仆可用的法术是一份存入附件、随实体持久化的快照。目录变更后由 `MaidEventHandler` 重新扫描已有女仆（`TagsUpdatedEvent` / `ServerStartingEvent` 上的全量扫描，以及 `EntityJoinLevelEvent` 覆盖晚加载的区块），详见 [4.2](#42-女仆可用法术的计算)。

### 3.3 魔力

女仆的魔力**不存放**在 Iron's 的玩家附件中。

```java
// 正确
MaidMagicManager.getMana(entity);
MaidMagicManager.setMana(entity, value);

// 错误：对女仆返回无人读写的孤儿附件，读取结果恒为 0
MagicData.getPlayerMagicData(maid).getMana();
```

`MaidMagicManager` 内部按实体类型路由：女仆 → `MaidMagicEntity#winefoxsSpellbooks$getMana/setMana`（由 `MaidEntityMixin` 实现，除写入 `MagicData` 外还同步 `MAID_MANA` 附件）；其它 `IMagicEntity` → `getMagicData()`；玩家 → 玩家附件，并额外发送一个 `SyncManaPacket`（Iron's 仅在自然回魔时同步，无法覆盖外部写入）。

`getMaxMana` 中的 `maxManaMultiplier` **仅对女仆生效**；作用于玩家会连带改写 Iron's 的真实上限。

### 3.4 数据驱动的两个 reload listener

两者均挂载于 `AddReloadListenerEvent`，`/reload` 时重新加载：

| 管理器 | 数据包路径 | 内容 |
|---|---|---|
| `SpellDataManager` | `data/<ns>/magic_maid_spell_data/` | `default_casting_range.json`（施法距离）、`default_caused_effect.json`（法术附带的状态效果，AI 据此判断是否需要重复施加 buff） |
| `MaidLoadoutManager` | `data/<ns>/summoned_maid_loadouts/` | 召唤女仆配装：分层 + 条件 + 盔甲/武器/法术池 + 等级上限 |

配置项 `extraSpellCastingRange` / `extraSpellCausedEffects` 叠加在数据包之上；初次进入世界时配置晚于数据包加载，由 `ServerAboutToStartEvent` 中的 `applyConfigOverrides()` 补充叠加。

### 3.5 二段咏唱（recast）

Iron's 的 recast 收尾逻辑硬编码在玩家路径上（依赖 `ServerPlayer`），女仆无法复用。本模组的处理方式：

- `MaidRecasts extends PlayerRecasts`——仅重写生命周期：不同步客户端（女仆没有 UI）、自行推进 `remainingTicks`、超时清理；记账（入库、计次、序列化）仍由父类处理，随 Iron's 一同演进。
- `RecastHandlers`——「法术 id → 收尾动作」的全局静态表。本体注册在 `IronsCoreRecasts`，附属按 mod 分文件置于 `magic/recast/addon/`。
- 反射引用集中于 `AddonReflection`，`ServerStartingEvent` 调用 `RecastHandlers.selfCheck()` 校验引用是否仍然有效——附属升级导致引用失效会在启动阶段暴露，而非等到玩家施法时才抛出异常。

⚠️ 召唤类法术即使标记了 `SHOULD_RECAST` 也不允许女仆连续施放：recast 次数耗尽会进入 `USED_ALL_RECASTS` 分支，回收刚召唤出的实体。选术端（`MaidSpellCatalog.maidMayRepeatCast`）与施法端的判定必须一致，否则女仆会反复选中必然被拒绝的法术，陷入空转。

### 3.6 召唤女仆

```
SummonMaidSpell / /wsb summon
  → MaidSummonManager 建档（存 level attachment，关服时持久化）
  → SummonedEntityMaid 生成
  → MaidLoadoutManager 按 summonLevel 选 tier → MaidEquipmentRandomizer 随机配装
  → SummonedMaidBrain 驱动（精简 Brain：仅保留战斗与跟随，无拾取/乞讨/回家）
  → 可选骑扫帚（SummonedMaidBroom + 一组 SummonedMaidFly* 行为）
```

`SummonedEntityMaid` 与普通 `EntityMaid` 在多数判定中需一视同仁，不要单独使用 `instanceof EntityMaid`，统一使用 `WinefoxHexHelper`：

- `isOwnedMaid(entity)`——判断是否为「有主人的女仆」，两类均计入（雇佣女仆取 `getOwner()`，召唤女仆取 `getSummoner()`），**不区分主人是谁**。
- `isMaidOwnedBy(target, owner)`——判断是否为「owner 阵营的己方女仆」，内部再经过一层 `MaidAllyHelper.isAllied`。增益类效果（披风、酒池回魔、微醺豁免）必须使用此方法，否则会将其他玩家的女仆误判为友方。

### 3.7 Mixin

`winefoxs_spellbooks.mixins.json` 挂载了 `WsbMixinPlugin`，约定：

> 软依赖某个 mod 的 mixin，放入 `mixin.<modid>.*` 子包，照常登记进 json。插件会自动从类名解析 modid，查询 `LoadingModList` 决定是否应用。

例：`mixin.ali.ConfigUtilsMixin` 仅在安装了 `ali` 时应用。直接位于 `mixin` 包根下的（如 `MaidEntityMixin`）视为硬依赖，一律应用。`defaultRequire: 1`，注入失败将直接崩溃——升级 TLM / Iron's 后编译通过不代表 mixin 仍能注入，务必实际启动验证。

### 3.8 对外事件 API

`api/event/` 下共四个事件：

| 事件 | 时机 | 可用操作 |
|---|---|---|
| `RegisterSpellTypeEvent` | 法术目录重建，标签之后、配置之前 | 直接增删各类别的法术集合 |
| `MaidSpellActionChooseEvent` | `SpellChooseTask#checkExtraStartConditions` 末尾 | 调整行为分类权重，或直接指定分类 |
| `MaidSpellChooseEvent` | `SpellChooseTask#start` | 直接指定要施放的法术 |
| `MaidSpellBookEvent` | 女仆装备/卸下法术书 | 感知装备变化 |

`RegisterSpellTypeEvent` **每次标签重载都会重新发送，且发送前集合已被清空重填**——监听方必须每次重新施加改动，不能仅在首次启动时施加一次。

---

## 四、女仆战斗 AI

本章是本模组最复杂的部分，修改前请先通读。

### 4.1 行为的挂载位置

TLM 的女仆 Brain 采用「按当前任务（Task）动态替换一批 Behavior」的结构。本模组：

- **`MaidMagicBrain`（`IExtraMaidBrain`）**仅承担两项职责：注册四个额外记忆模块（`CURRENT_SPELL` / `CURRENT_SPELL_ACTION` / `SUPPORT_TARGET` / `GIFT_DELIVERY`），以及向 idle/work/rest 三档挂载晨间送礼行为。**战斗行为不在此处**。
- **战斗行为由任务提供**：`MaidCastingTask#createBrainTasks`（站立于地面）与 `createRideBrainTasks`（骑乘时）返回行为列表，玩家在女仆 GUI 中切换到该任务后才生效。所有行为统一优先级 5。

两个任务：

| 任务 | 类 | 允许的行为分类 | 索敌对象 |
|---|---|---|---|
| 法术攻击 | `MaidCastingTask` | ATTACK / DEFENSE / MOVEMENT / SUPPORT / NEGATIVE | 自己的仇敌 |
| 法术支援 | `MaidMagicSupportTask`（继承前者） | DEFENSE / SUPPORT / POSITIVE / NEGATIVE / SUPPORT_OTHER | 主人和友军正在攻击的目标 |

行为清单（地面版）：

```
StartAttacking              选敌，仅在没有支援目标时启动
StopAttackingIfTargetInvalid  目标失效/超出 maxSpellRange 时放弃
SpellChooseTask             选行为分类 + 选具体法术       ← 核心
SpellAttackWalkToTarget     按法术射程与 home 范围决定是否移动过去
DrinkPotionsTask            低血量时饮用药水（攻击任务专属）
SpellStrafingTask           拉开距离 / 贴近 / 绕圈走位
SpellCastingTask            起手施法                      ← 核心
MagicMeleeAttack            近战补充攻击（配置 meleeAttackInMagicTask 开启才挂载）
```

骑乘版移除走位与寻路相关的两项行为（女仆无法自主控制坐骑）。支援任务不挂载饮药与近战。

### 4.2 女仆可用法术的计算

`MaidEventHandler.onMaidSpellDataChanged(maid)` 负责重算，结果写入 `MaidSpellDataHolder`——一个**持久化**附件（`MAID_SPELL_DATA`），随女仆一同保存。重算触发点分三类：

| 触发 | 覆盖场景 |
|---|---|
| 装备/卸下法术书、`MaidEquipEvent`、`CurioChangeEvent` | 玩家变更了女仆的装备 |
| `MaidSpellCatalog` 重建完目录后回调 `refreshLoadedMaidSpellData()` → 全量重扫已加载女仆 | 数据包或配置变更了法术分类 |
| `EntityJoinLevelEvent` | 目录建立之后才加载进来的女仆（`/reload` 后才载入的区块） |

后两类是必需的：法术清单是快照，仅重建 `MaidSpellCatalog` 不会同步更新已有女仆。全量重扫**仅扫描 `EntityMaid`**——召唤女仆的法术来自配装随机而非法术书，重扫会清空其法术表。

全量重扫由目录在重建末尾统一通知，而非让 `MaidEventHandler` 再订阅一次 `TagsUpdatedEvent` / `ServerStartingEvent` 并依靠 `EventPriority` 排在目录之后——此类顺序耦合在编译期不可见，日后目录新增重建触发点时，下游会静默遗漏。

扫描规则：

- 扫描范围：**主手 + 盔甲栏 + TLM 饰品栏 + Curios 栏**，任何 `ISpellContainer` 均计入（法术书、注魔武器/盔甲）。
- **多本法术书只有一本生效**，与玩家一致；注魔装备不受此限。
- 法术等级 = 卷轴等级 + 装备上的亲和（`AffinityData`）加成，再经过一次 Iron's 的 `ModifySpellLevelEvent`。
- **不属于任何 `MaidSpellAction` 的法术直接丢弃**——这是「女仆不会使用某法术」最常见的原因。
- 按行为分类分七个桶存放；`hasAnyOf(allowedActions())` 决定任务图标上的条件提示是否点亮，类别名单由各任务的 `MaidCastingTask.allowedActions()` 给出，与传入 `SpellChooseTask` 的是同一份。

### 4.3 索敌

- 攻击任务：优先反击最近攻击自己的生物，否则在近邻表中选取「**带残月烙印的优先**，其次最近」（`WinefoxHexHelper.findNearbyTargetPreferBranded`）。
- 支援任务：优先取主人的仇敌，否则遍历附近友军的 `ATTACK_TARGET`，取最近的一个。
- 搜索半径 = `maxSpellRange` 配置（默认 64）。**必须使用近邻表而非可见实体表**，后者会将索敌距离压回原版的 16 格，无法覆盖远程法术射程。
- 视线判定使用 `LineOfSightGuard.test(...)`；`TargetingConditions` 为复用的单例、每次只修改射程——索敌时每个候选都会经过一次判定，不应逐个新建。

### 4.4 选术：`SpellChooseTask`

整个 AI 的决策中心，在一次 `checkExtraStartConditions` 内完成筛选与投票：

1. **节流**：`nextSpellTickCount` 递减至 0 才继续，随后重置为 `random(maxComboDelayTick)`。正在施法或饮药时直接跳过。
2. **筛选可用法术**：按任务允许的分类逐桶送入 `MaidMagicManager.isSpellUsable`——排除「有未完成 recast 且不允许女仆逐发」「魔力不足」「冷却中」三种情况。
3. **收集队友**（仅当有 POSITIVE / SUPPORT_OTHER 可用时）：按 TLM 的 `searchDimension()` 范围取友军，按距离排序。
4. **计算权重**（见下表）→ 发送 `MaidSpellActionChooseEvent` → **两阶段加权随机**选出行为分类。
5. `start()` 中从该分类的桶内**等概率随机**挑选一个法术（可被 `MaidSpellChooseEvent` 覆盖），写入 `CURRENT_SPELL` 记忆，弹出聊天气泡。

权重公式（`mob` = 女仆自身，`hp%` = 当前血量比例）：

| 分类 | 权重 |
|---|---|
| ATTACK | 无视线则 0；否则 `80 + (1-目标hp%)*60 + 距离项` |
| DEFENSE | `20 + 50*(1-hp%³) - (1-目标hp%)*35 + Σ每个可用防御法术(未施加此 buff +20 / 效果未知 +10)` |
| MOVEMENT | `距离占比*50 + (无视线 ? 80 : 0) + 400*(1-hp%)²*(1-距离占比)²` |
| SUPPORT（自我治疗） | `-10 + 200*(1-hp%) - (1-距离占比)*75`（敌人贴近时抑制自愈） |
| NEGATIVE | 以 ATTACK 权重为基数，每个可用法术再 +10（敌人尚无此 debuff）/ +5（效果未知） |
| POSITIVE（增益队友） | 队友缺少此 buff → 500；净化术且队友有可净化负面 → 1000；魔力转移且目标确实缺魔 → 1000，否则 0；效果未知 → 10 |
| SUPPORT_OTHER（治疗队友） | `floor((1-队友hp%) * 2500)`——队友血量越低，该项权重越容易压过其它分类 |

另有**连招抑制**：上一次选择了 DEFENSE / NEGATIVE 则本次该项 −100，MOVEMENT 则 −50，避免女仆连续选择同一分类。

**两阶段抽取的语义容易误解**：

- 先将 SUPPORT 放入池中，再判断「是否有附近队友或已有支援目标」——**若有**，则将 POSITIVE / SUPPORT_OTHER 一并放入，只要池非空即立刻抽取并返回，战斗类分类**没有参与机会**（低血量队友必定被优先处理）。
- **若无**队友则不提前返回，SUPPORT 将与 ATTACK / DEFENSE / MOVEMENT / NEGATIVE 同池竞争。

选中 POSITIVE / SUPPORT_OTHER 后，`start()` 会遍历队友挑选第一个权重 > 0 的写入 `SUPPORT_TARGET`，同时**清除 `ATTACK_TARGET` 并停止导航与走位**——这正是女仆在战斗中突然转向治疗队友的原因。

### 4.5 施法：`SpellCastingTask`

- 起手条件：`CURRENT_SPELL` 记忆存在、未在施法/饮药、目标存活、（可见 或 失去视线不超过 50 tick）、**距离在该法术的 `casting_range` 之内**。
- 骑乘时若距离不足，直接清空目标——女仆无法自主控制扫帚，不清除将永久阻塞。
- `doSpellAction` 中再经过一道 recast 检查：已有未完成 recast 且该法术不允许女仆逐发时，本次不施放。
- 施法前调用 `lookAt(EYES, target)` 使身体与头部同时转向目标，随后 `initiateCastSpell`。
- 持续期间 `canStillUse` 监视 `magicData.isCasting()`、目标是否死亡、视线计数，以及 Iron's 自身的 `shouldAIStopCasting`。
- `stop()` 会调用 `cancelCast()` 并清除 `CURRENT_SPELL` 与 `LOOK_TARGET`，将视线交还给其它行为。

真正的施法执行在 `MagicMaidAdapter`：以 `CastSource.MOB` 走 Iron's 的 `MagicData.initiateCast`，咏唱推进依赖宿主实体每 tick 回调适配器的 `tickMagicLogic` / `aiStepMagicLogic` / `completeUsingItem`（由 `MaidEntityMixin` 接入）。适配器上的 `bypassCastCheck` 仅跳过起手检查，**不豁免魔力消耗**。

### 4.6 走位与辅助行为

- **`SpellAttackWalkToTarget`**：按当前法术的射程（而非固定值）与女仆的 home 范围决定是否移动过去。
- **`SpellStrafingTask`**：距离小于射程一半时尝试后撤（`fleeCooldown` 100 tick 限流），在射程内且有视线时原地绕圈，连续绕行满 50 tick 后每 tick 有 10% 概率反向；施法期间移速降为 0.75 倍。女仆被命令坐下时不走位。
- **`DrinkPotionsTask`**：血量低于 60% 且 100 tick 冷却已过时饮药，受 `drinkPotionInBattle` 配置控制，仅攻击任务挂载（避免非战斗状态持续饮用药水）。
- **`MagicMeleeAttack`**：可选的近战补充攻击，`meleeAttackInMagicTask` 默认开启。

### 4.7 记忆模块

| 记忆 | 写入方 | 清除方 |
|---|---|---|
| `ATTACK_TARGET`（原版） | `StartAttacking` | `StopAttackingIfTargetInvalid`；选中支援分类时也会被清除 |
| `CURRENT_SPELL_ACTION` | `SpellChooseTask#checkExtraStartConditions` | `SpellChooseTask#stop` |
| `CURRENT_SPELL` | `SpellChooseTask#start` | `SpellChooseTask#stop` / `SpellCastingTask#stop` |
| `SUPPORT_TARGET` | `SpellChooseTask#start` | `SpellChooseTask#stop` |
| `GIFT_DELIVERY` | 晨赠事件 | `MaidDeliverGiftBehavior` |

`enableLookAndRandomWalk` 仅在两个目标记忆都为空时放行，否则女仆会在战斗中频繁转头张望。

### 4.8 召唤女仆的差异

`SummonedMaidBrain` 是独立的精简 Brain，仅保留战斗与跟随召唤者，另加飞行分支（`SummonedMaidFlyStrafingTask` / `FlyToSummoner` / `RandomFlight` / `PilotBroomTask` / `DismountTask`）与 `MobClimbTask`。选术与施法复用 `SpellChooseTask` / `SpellCastingTask`（两者的形参是 `Mob` 而非 `EntityMaid`，正是为此），法术来源改为由配装系统随机生成而非法术书。

### 4.9 扩展点

优先使用事件，其次继承：

- 调整分类倾向 → `MaidSpellActionChooseEvent` 修改权重或直接指定分类。
- 指定具体法术 → `MaidSpellChooseEvent#setSelectedSpell`。
- 调整法术的分类归属 → 数据包标签，或 `RegisterSpellTypeEvent`。
- 需要完全另一套决策 → 继承 `SpellChooseTask`。`usableSpells` 各桶实例构造后不再更换（`SpellTestChooseTask` 即直接持有引用填充），重写时请保持这一约定。

---

## 五、常见开发任务

### 5.1 新增本模组的法术

1. `magic/spell/XxxSpell.java` 继承 Iron's 的 `AbstractSpell`（参考 `TipsyMistSpell` / `VintnersBombSpell`）。
2. 在 `registry/WsbSpells` 中 `registerSpell(new XxxSpell())`——注册名取自 `spell.getSpellName()`。
3. 在 `datagen/spelltag/WinefoxsSpellbooksTags.java` 中归类（必须归入某个 `MaidSpellAction`，否则女仆不会使用），按需附加 `summon` / `maidShouldRecast` 标记。
4. 在 `datagen/spelldata/WinefoxsSpellbooksData.java` 中补充施法距离与附带效果。
5. 补齐两份 lang：`en_us.json` + `zh_cn.json`。
6. 需要产出卷轴时，确认学派、`WsbItemTags`、战利品修改器（`datagen/WsbGlobalLootModifierProvider`）。
7. `./gradlew runData && ./gradlew build`。
8. 需要 Patchouli 词条时，只在 `assets/touhou_little_maid/patchouli_books/memorizable_gensokyo/en_us/entries/winefoxs_spellbooks/` 下加一份词条 json——书本只有 en_us 一棵树，正文一律走 `patchouli.…` 翻译键，中英文各自写在两份 lang 里。注意单页容量有限，超出只会静默缩小字号。

### 5.2 支持一个新的 Iron's 附属模组

1. `gradle.properties` 新增版本号变量，`build.gradle` 新增依赖（CurseMaven 使用 `curse.maven:<slug>-<projectId>:<fileId>`）。
2. `./gradlew exportClasspath` 导出 jar，反编译查看其法术注册类。
3. **新建**独立文件，不要追加到现有文件：
   - `datagen/spelltag/<Mod>Tags.java`（必需）——实现 `contribute(SpellTagContext ctx)`，逐个 `ctx.attack().addOptional(...)`。**一律使用 `addOptional`**，该模组未安装时不会报错。
   - `datagen/spelldata/<Mod>Data.java`（仅在需要覆写施法距离或附带效果时创建；例如 Snow Waifu Spell 只有 Tags 文件）。
4. 在 `MaidSpellTagsProvider#addTags`（及 `SpellDataProvider#run`）中注册新的 contributor。
5. 需要 recast 收尾逻辑的，新增 `magic/recast/addon/<Mod>Recasts.java` 并注册进 `RecastHandlers`。
6. 更新两份 README 的附属表格与「N 个附属」计数——分类数取自 `src/generated/resources/data/winefoxs_spellbooks/tags/irons_spellbooks/spells/` 下各标签里该命名空间的去重条目数。
7. `./gradlew runData && ./gradlew build`。

> 每个附属拆分为独立文件是刻意设计，便于并行修改不同附属而不产生冲突。

### 5.3 新增状态效果 / 物品 / 实体 / 粒子

统一使用 `registry/` 下对应的 `Wsb*` 类，都是 `DeferredRegister` + 静态字段。附带工作：

- **物品说明**：只需在 lang 中添加 `item.winefoxs_spellbooks.<path>.desc`，`WsbItemDescTooltipHandler` 会自动拼接进 tooltip，无需 override `appendHoverText`；JEI 物品信息页复用同一个 key，无需额外编写插件。`\n` 会被拆分为多行。
- **物品模型**：在 `datagen/ItemModelGenerator` 中添加一行，不要手写 json。
- **GeckoLib 实体**：`DefaultedEntityGeoModel` 将资源解析到 `geo/entity/`、`animations/entity/`、`textures/entity/` 三个**带 `entity/` 子目录**的路径。放错位置在编译期无法发现，渲染时才会崩溃。
- **状态效果**：增删属性使用内建的 `addAttributeModifier` builder，不要在 `applyEffectTick` 中手动增删——效果刷新时会丢失。
- **AI 相关的效果**：新效果若会影响女仆选术（例如提供某个 buff），需在对应的 `spelldata` 中登记 `caused_effect`，否则 AI 只会按「效果未知」计 10 分。

### 5.4 新增配置项

全部位于 `Config.java`，且**全部是 SERVER 配置**。新增一项需同时补充 `defineXxx` + `.translation(translateKey("..."))` + 两份 lang 的 `config.winefoxs_spellbooks.<key>`。

配置曾由 COMMON 迁移为 SERVER，文件名随之变更；主类中的 `warnAboutOrphanedCommonConfig()` 会在检测到旧文件时给出提示，请勿删除。

---

## 六、数据生成

`./gradlew runData` **全量重写** `src/generated/resources/`，手动修改会被覆盖。当前 provider：

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

datagen 会先调用 `ConfigTracker.INSTANCE.loadDefaultServerConfigs()` 模拟默认配置，因此在 provider 中读取配置是安全的。

以下资源为手写，不受 `runData` 影响：lang、贴图、geo、animation、音效、`sounds.json`、Patchouli 词条、mixin 配置。

---

## 七、测试与调试

- **调试任务**：`MaidDrinkPotionTask`、`MaidSpellTestTask` 仅在 `WinefoxsSpellbooks.DEBUG`（非 production）下注册，正式构建中不存在。法术测试任务用 `SpellTestChooseTask` 替换选术逻辑——取 `MaidSpellCatalog` 中注册的全部法术（按最高等级），**轮流**遍历行为分类与法术而非加权随机，用于将整套法术逐个验证。
- **指令**（OP 2 级）：

  ```
  /wsb cast <targets> <spell> [level]
  /wsb summon [target] [count] [summonLevel] [loadout] [duration] [airForce]
  ```

  `/wsb cast` 会临时开启适配器的 `bypassCastCheck`，**仅跳过起手时的可用性检查，施法完成阶段仍会扣除魔力**。测试前请调高 `irons_spellbooks:max_mana` 并等待魔力恢复。
- **gametest**：`neoforge.enabledGameTestNamespaces` 已设为本模组 id，`./gradlew gameTestServer` 或游戏内 `/test` 均可运行（当前仓库尚无用例）。
- **调试 AI 时**：开启 `showChatBubbles` 配置，女仆会将每次选中的法术显示在气泡中；选不出支援法术时同样通过气泡提示，较查阅日志更为直观。

---

## 八、代码风格

`.editorconfig` 是权威（IDEA 方案导出），要点：

- 4 空格缩进，LF，UTF-8，行宽按 ~120 折行
- K&R 大括号，成员字段之间空一行
- 显式 import，顺序 `java.*` → `javax.*` → 第三方 → `net.magicterra.*`
- 类名 PascalCase，方法 camelCase，常量 UPPER_SNAKE_CASE
- 公开 API 编写 JavaDoc，带 `@author` 与 `@since`
- 内部逻辑注释使用中文；优先使用 `instanceof` 模式匹配；能用 record / sealed / switch 表达式的场合优先使用
- 日志统一使用 `WinefoxsSpellbooks.LOGGER`

注释规范：**简洁自洽**。注释解释「为什么这样写」，尤其是那些看似可以简化但实际不能的地方——本仓库这类情形较多，注释是唯一的文档。不要在注释中引用外部文档编号或任务代号。

---

## 九、提交与发布

提交信息使用 Conventional Commits，祈使句、小写、不带句号：

```
feat: classify the spells added by the updated addon mods
fix: keep the foxshade biter out of saves and reset its leap on restart
refactor: drop private field reflection from maid recast bookkeeping
chore: bump neoforge, iron's spells and the addon dependencies
```

发版流程：

1. 修改 `gradle.properties` 的 `mod_version`（必要时调整 `version_type`）。
2. 在 `CHANGELOG.md` 顶部新增一段，格式必须是：

   ```markdown
   ## <版本号>

   ...中英双语条目...

   ---
   ```

   `build.gradle` 的 `getChangelogForVersion()` 以 `## <version>` 为起点、`---` 为终点截取，CI 中另有一段 `awk` 执行相同逻辑——遗漏分隔线会将后续全部内容计入本版更新日志。
3. 打标签并推送：`git tag v1.0.0-beta.13 && git push --tags`。
4. `.github/workflows/publish.yml` 接管：JDK 21 → `./gradlew publishMod -Pmod_version=<tag 去掉 v>` → 发布至 Modrinth + CurseForge（含源码 jar）→ 建立 GitHub prerelease。

所需 secrets：`MODRINTH_TOKEN`、`CURSEFORGE_TOKEN`；本地手动发版读取同名环境变量。
