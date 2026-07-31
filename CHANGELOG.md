# Changelog / 变更日志

## 1.0.0-beta.12

1. **酒狐巫法学派 (Winefox Hex Spell School)**

   新增独立的「酒狐巫法」学派，配套自有伤害类型与紫粉主题色，并新增焦点物品 `灵狐精魂`，Iron's 卷轴锻造可识别本学派。`召唤女仆`、`魔力转移` 两个法术归入新学派。同步在游戏内 Patchouli 手册中补全中英文条目。

   Added the standalone "Winefox Hex" spell school with its own damage type and violet-pink theme, plus a new focus item `Vulpine Anima` that lets Iron's Scroll Forge recognize the school. The `Summon Maid` and `Mana Transfer` spells are now classified under this school. Patchouli manual entries (Chinese / English) for the school and Mana Transfer are added in-game.

2. **酒狐巫法首批战斗法术 (Winefox Hex Combat Spells)**

   新学派迎来 6 个战斗法术，围绕「烙印引爆」与「微醺控场」两条主线：
   - **残月烙印**：单体标记法术。烙印期间（12 秒 + 每级 1 秒）目标一旦受到任意酒狐巫法伤害即引爆，引爆伤害 = 基础 6 点 + 挂印时的法术强度；由自家女仆引爆时伤害 ×1.5、范围更大且可连锁引爆下一个带印目标；烙印自然到期只发生小范围弱自爆。
   - **酒杯狐影**：逐发施法，最多 3 发追踪狐影从侧翼包抄；三发等价，打出的酒狐巫法伤害命中带印目标即引爆残月烙印。女仆 AI 支持自动逐发施放。
   - **残月披风**：为自己与附近自家女仆披上伤害吸收（6 级起提升至吸收 III）与短暂真实隐身（Iron's 效果：装备一并隐去、脱离仇恨），两者时长每级 +0.5 秒，紧急脱险。
   - **醒酒之礼**：将自身或视线所指自家女仆的负面效果剩余时长减半，满级（5 级）直接尽数清除。
   - **醉狐之雾**：咏唱 1 秒后在落点铺开半径 5 格的紫粉酒雾（持续 6 秒 + 每级 0.5 秒），雾中生物持续微醺，自家女仆免疫；5 级起施加微醺 II。
   - **狐酿炸弹**：投掷酒瓶直击造成学派伤害，落点铺开酒池——敌人泡出微醺，自家女仆泡在池中持续回魔。

   配套内容：
   - 新增「微醺」（减速、踉跄、弹道打偏，不伤血）与「酒涌」（每次引爆残月烙印为指挥者叠一层，自家女仆引爆同样计入；10 秒滚动刷新，至多 3 层，每层 +10% 学派法术强度）两种状态效果；
   - 学派专属咏唱音效与施法粒子（狐火 + 金色符箓）；
   - 魔力转移的传输视觉重做：密集的彩色魔力弹幕沿光束流向目标，外圈环绕金光十字双螺旋（新增两种自定义粒子）；
   - 酒狐巫法卷轴按概率出现在要塞图书馆、下界要塞及 Iron's Spells 的火焰法师塔、地下墓室、城堡藏书库宝箱中；
   - Patchouli 手册新增全部 6 个法术的中英文条目；
   - 新增本模组创造模式物品栏标签页，收录灵狐精魂、残月血酿与酒狐巫法全部法术卷轴（逐等级）。

   The new school gains six combat spells, built around two pillars — brand detonation and tipsy crowd control:
   - **Crescent Brand**: single-target mark. While branded (12 seconds, +1s per level), any Winefox Hex damage taken by the target detonates the brand into an AoE burst dealing a base of 6 plus the spell power at branding time; detonations by your own maids deal ×1.5 damage with a larger radius and can chain to the next branded target. A brand that expires naturally only weakly self-detonates in a small radius.
   - **Foxshade Wisps**: recast spell — up to 3 homing wisps flank in from the sides; all three are identical, and their Winefox Hex damage detonates a Crescent Brand on hit. Maid AI can chain the recasts automatically.
   - **Crescent Mantle**: veils you and nearby owned maids in absorption (Absorption III from level 6) and brief True Invisibility (Iron's effect: gear vanishes too, mobs lose aggro) for emergency escapes; both durations gain +0.5s per level.
   - **Decant the Bind**: halves the remaining duration of harmful effects on you or an owned maid in your gaze; at max level (5) they are purged outright.
   - **Tipsy Mist**: after a 1-second cast, rolls out a 5-block-radius plum-pink fog (6 seconds, +0.5s per level) that keeps creatures inside tipsy; your own maids are immune. From level 5 it applies Tipsy II.
   - **Vintner's Bomb**: hurls a wine bottle dealing school damage on impact and spreading a wine pool — foes soak up Tipsy while your maids soak up mana.

   Supporting content:
   - Two new mob effects: "Tipsy" (slowness, staggering, deflected projectiles — no damage) and "Brewsurge" (each Crescent Brand detonation — yours or your maids' — stacks one level on the commander; rolling 10s refresh, up to 3 stacks, +10% Winefox Hex spell power per stack);
   - A dedicated school casting sound and cast particles (foxfire and golden runes);
   - Reworked Mana Transfer's beam visual: a dense stream of rainbow mana bolts flows to the target, wrapped in a double helix of spinning golden crosses (two new custom particles);
   - Winefox Hex scrolls now appear at various rates in Stronghold Library and Nether Fortress chests, plus Iron's Spells' Pyromancer Tower, Catacombs, and Citadel chests;
   - Patchouli manual entries (Chinese / English) for all six spells;
   - A creative-mode tab for this mod, holding Vulpine Anima, Crescent Blood Vintage, and every Winefox Hex spell scroll at each level.

3. **灵狐精魂获取方式 (Vulpine Anima Acquisition)**

   新增三种获取 `灵狐精魂` 的途径：
   - **女仆晨间礼物**：玩家睡满一晚醒来后，若身边有已驯化且满好感度的女仆，对方会赠予一件（每游戏日一次；新增配置项：总开关、搜索半径（默认 8 格）、是否强制睡满；具体掉落由数据包可覆盖的战利品表决定）。
   - **女仆击杀妖精**：女仆击杀车万女仆模组的妖精时按概率掉落（驯化 25% / 召唤 35%，每级抢夺 +5%）。
   - **结构宝箱**：以不同概率追加到原版林地府邸、要塞图书馆、下界要塞，以及 Iron's Spells 的火焰法师塔、地下墓室、城堡藏书库等宝箱。

   Added three ways to obtain `Vulpine Anima`:
   - **Maid morning gift**: when a player wakes from a full sleep with a tamed, max-favorability maid nearby, the maid gifts one (once per game day; new config options for master toggle, search radius (default 8 blocks), and full-sleep requirement; the drop itself is governed by a datapack-overridable loot table).
   - **Maid-killed fairies**: maids killing Touhou Little Maid fairies drop them by chance (25% tamed / 35% summoned, +5% per Looting level).
   - **Structure chests**: added at various rates to vanilla Woodland Mansion, Stronghold Library, and Nether Fortress chests, and Iron's Spells' Pyromancer Tower, Catacombs, and Citadel chests.

4. **残月血酿 (Crescent Blood Vintage)**

   新增可饮用物品「残月血酿」。女仆饮用必定触发「狐火增幅」效果，将冷却缩减提升到约 90%（即冷却时间为原本的 10%），并立即同步缩减已有冷却；其他生物饮用有 60% 概率触发「魔力紊乱」减益。同时附带新增的「狐火增幅」与「魔力紊乱」两种状态效果。

   **获取方式：结构宝箱。** 以不同概率追加到原版村庄民居（五个生物群系变体）与村庄神殿、掠夺者前哨站、林地府邸，以及 Iron's Spells 的火焰麦酒桶、旧酒桶、麦酒库、冰蛛巢酒桶、破冰船食物桶、城堡城墙补给、红树屋药水储藏、地下墓室棺椁等宝箱。

   Added drinkable item "Crescent Blood Vintage". Maids drinking it always trigger the "Foxfire Boost" effect, raising cooldown reduction to about 90% (cooldowns shrink to 10% of normal) and immediately syncing existing cooldowns; other creatures have a 60% chance to be afflicted by "Mana Disruption". Two new mob effects ("Foxfire Boost" and "Mana Disruption") are introduced alongside.

   **How to obtain: structure chests.** Added at various rates to vanilla village houses (all five biome variants) and village temples, Pillager Outposts, and Woodland Mansions, plus Iron's Spells' fire ale casks, old casks, ale troves, Ice Spider Den
   casks, Impaled Icebreaker food barrels, Citadel rampart supplies, Mangrove Hut potion storage, and Catacombs coffins.

5. **FTB Teams 友伤兼容 (FTB Teams Friendly Fire Compatibility)**

   将友伤判定沿召唤链解析到顶层召唤者，使 FTB Teams Friendly Fire 等基于队伍的友伤判定能正确覆盖召唤的女仆与其他召唤物。

   Friendly-fire checks now resolve both sides up the summon chain to their root summoners, so team-based friendly-fire mods such as FTB Teams Friendly Fire correctly recognize summoned maids and other summons as allies.

6. **Advanced Loot Info 兼容 (Advanced Loot Info Integration)**

   若同时安装 Advanced Loot Info：
   - 女仆晨间礼物战利品表会作为独立分类「女仆的晨间礼物」显示在 ALI / EMI 中。
   - 新增 ALI 插件，把本模组「向宝箱追加固定物品」的全局战利品修饰器注册进 ALI。此前灵狐精魂与残月血酿的宝箱来源在 JEI 里查不到——ALI 按修饰器的 Java 类查处理器，认不出的类只会退化成一段原始 JSON 提示，不会生成可索引的物品条目。现在两者的宝箱获取途径都能正常显示与反查。

   When Advanced Loot Info is installed:
   - The maid morning gift loot table appears as its own category ("Maid Morning Gift") in ALI / EMI.
   - A new ALI plugin registers this mod's "append a fixed item to a chest" global loot modifier with ALI. Previously the chest sources for Vulpine Anima and Crescent Blood Vintage were not discoverable in JEI: ALI looks up handlers by the modifier's Java
     class, and an unrecognised class only degrades to a raw-JSON note rather than an indexable item entry. Both items' chest sources now display and reverse-look-up correctly.

7. **物品说明自动显示 (Auto-rendered Item Descriptions)**

   本模组物品现在会自动把语言文件中的说明文本显示到悬浮提示和 JEI 信息页，无需为每个新物品单独处理。仅作用于本模组物品，不影响其他模组。`灵狐精魂` 是首个使用该机制的物品。

   Items from this mod now automatically render their description text (from the lang file) in their tooltip and on a JEI info page, with no per-item wiring needed. Scoped to this mod only — items from other mods are unaffected. `Vulpine Anima` is the first item using it.

8. **召唤女仆与扫帚修复 (Summoned Maid & Broom Fixes)**

   - 修复女仆 AI 在视线检测时偶发的死锁问题，影响施法、辅助施法、调试饮药等任务。
   - 修复召唤女仆扫帚在卸载/保存时被重复持久化、重载后出现多个扫帚的问题。
   - 修正召唤女仆扫帚的碰撞箱尺寸。
   - 修复 `/wsb summon` 永久召唤因 Iron's SummonManager 的 tick 溢出导致召唤物瞬间消失、或重登后约 1 秒被解除的问题。

   - Fixed an intermittent deadlock in maid AI during line-of-sight checks, affecting casting, support casting, and debug potion drinking.
   - Fixed the summoned-maid broom being persisted multiple times on save/unload, which produced duplicate brooms after reload.
   - Corrected the bounding box size of the summoned-maid broom.
   - Fixed `/wsb summon` permanent summons vanishing instantly (or being dismissed about a second after relogging) due to int tick overflow in Iron's SummonManager.

9. **女仆强度上限配置 (Maid Power Caps)**

   召唤出的女仆携带的法术现在受三个新配置项约束，默认**最高 3 级、最高史诗、不带邪术**：

   - `summonedMaidMaxSpellLevel`（默认 3）——与配装数据包里各层级的等级上限取小，只会压低不会抬高。
   - `summonedMaidMaxSpellRarity`（默认 EPIC 史诗）——在任何等级都超过该稀有度的法术（附属模组里大量把最低稀有度直接设为传说的「终极法术」）整条移出法术池；其余法术逐级下压到符合上限为止。
   - `summonedMaidAllowEldritch`（默认 false）——不给召唤女仆发「需要习得」学派的法术，当前即 Iron's 的邪术。这类法术原本要求玩家先习得才能施放，女仆随机摸到一个等于绕过了这道门槛。

   前两项的剔除都发生在抽取之前，因此女仆拿到的法术**数量不变**，只是强度降低。三项均可在运行时修改，无需重启世界。

   另新增 `lightningManaRegen`（默认 false）：被雷劈过的女仆原本魔力恢复速度 ×100，实际效果是每 0.5 秒回满整条法力条，等同于无限法力；而「被雷劈过」是永久且存档持久的状态，一次触发终身有效。现在默认关闭，可开回。

   已有的 `drinkPotionInSpellBattle`（法术战斗中无限治疗药水）**默认值由 true 改为 false**：女仆血量低于 60% 就会喝一次治疗药水且不消耗物品，冷却一到即可再喝，实际是战斗中的无限续航。**注意**：默认值只对新生成的配置文件生效，已经存在的
   `config/winefoxs_spellbooks-server.toml` 不会被改写，需要手动改或删掉该行。

   Spells carried by summoned maids are now bounded by three new config options, defaulting to **level 3, Epic rarity, no Eldritch**:

   - `summonedMaidMaxSpellLevel` (default 3) — combined with the per-tier cap from the loadout datapack by taking the lower of the two; it can only reduce the level, never raise it.
   - `summonedMaidMaxSpellRarity` (default EPIC) — spells that exceed this rarity at every level (the many "ultimate" spells in addon mods that set their minimum rarity straight to Legendary) are removed from the loadout pools entirely; the rest are
     levelled down until they fit.
   - `summonedMaidAllowEldritch` (default false) — keeps learned-only schools, today meaning Iron's Eldritch, out of summoned maids' hands. Those spells normally require a player to learn them first; a maid rolling one bypasses that gate.

   The first two remove spells before rolling, so the **number** of spells a maid receives is unchanged — only their power. All three take effect at runtime without a world restart.

   Also added `lightningManaRegen` (default false): maids that have been struck by lightning previously regenerated mana 100x faster, which in practice refilled the entire mana bar every half second — effectively unlimited mana — and "has been struck by
   lightning" is a permanent, saved state that triggers once and never wears off. Now off by default, and can be turned back on.

   The existing `drinkPotionInSpellBattle` option **now defaults to false instead of true**: a maid below 60% health drinks a healing potion without consuming an item, and can do so again as soon as the cooldown elapses — effectively unlimited sustain in
   combat. **Note**: a changed default only applies to newly generated config files; an existing `config/winefoxs_spellbooks-server.toml` is not rewritten, so edit or delete that line to pick up the new default.

10. **配置改为服务端配置 (Config Moved to Server Type)**

    > ⚠️ **破坏性变更：旧配置不会自动迁移。** 文件名由 `config/winefoxs_spellbooks-common.toml` 变为
    > 同目录下的 `config/winefoxs_spellbooks-server.toml`。升级后所有选项回到默认值，需要重新设置。
    > 旧的 common 文件不再被读取，可手动对照抄一遍后删除。

    本模组全部配置项原本注册为 COMMON 类型。逐项审计后确认它们全是逻辑服务端读取的玩法数值（女仆 AI、法术、
    战利品、召唤配装），`client/` 包内没有任何一处读取，因此按 NeoForge 的推荐改为 SERVER 类型。带来的好处：
    由服务端权威持有并自动同步给客户端 —— 多人游戏中客户端不会再读到与服务端不一致的值；
    另外还可以把 toml 拷进 `<存档>/serverconfig/` 来给单个存档单独调参（该目录是覆盖目录，
    只在已存在同名文件时生效；默认仍读全局 `config/`）。

    CLIENT / COMMON / STARTUP 三类目前没有归属项，判定理由记在 `Config` 类文档里。

    另外补齐了配置界面的本地化：每个配置项都补上了鼠标悬停说明，配置页标题也不再显示原始文件名。此前只有
    `.comment()` 里的英文注释顶着，中文玩家看到的全是英文。

    > ⚠️ **Breaking: existing settings do not migrate.** The file is renamed from
    > `config/winefoxs_spellbooks-common.toml` to `config/winefoxs_spellbooks-server.toml` in the same folder.
    > All options revert to their defaults after updating and must be set again. The old common file is no
    > longer read and can be deleted once copied over.

    Every config option in this mod was registered as COMMON. An audit confirmed they are all gameplay values
    read on the logical server (maid AI, spells, loot, summon loadouts), with no reads anywhere under `client/`,
    so they are now registered as SERVER as NeoForge recommends. Benefits: the server is authoritative and the
    values sync to clients automatically — a multiplayer client can no longer read values that disagree with
    the server. You can also drop a copy of the toml into `<world>/serverconfig/` to override the settings for
    a single world (that folder only takes effect when a file with the same name already exists there;
    otherwise the global `config/` copy is used).

    The configuration screen is also fully localized now: every option has a hover description, and the config
    page no longer shows the raw file name as its title. Previously only the English `.comment()` text stood in,
    so Chinese players saw English throughout.

    No options qualify for CLIENT / COMMON / STARTUP today; the reasoning is recorded in the `Config` class docs.

11. **补全学派姻缘戒指模型 (Winefox Hex Affinity Ring Model)**

补上缺失的 `affinity_ring_winefox_hex` 物品模型与贴图。Iron's 会为每个已注册学派登记一个 `<学派命名空间>:item/affinity_ring_<学派路径>` 独立模型，此前本模组只提供了学派卷轴的那一份，导致启动时刷缺失模型警告，且绑定酒狐巫法法术的姻缘戒指会渲染成紫黑格子。

Added the missing `affinity_ring_winefox_hex` item model and texture. Iron's registers one standalone `<school_namespace>:item/affinity_ring_<school_path>` model per registered school; this mod previously shipped only the school-scroll counterpart, which
produced a missing-model warning on startup and rendered any Affinity Ring attuned to a Winefox Hex spell as the missing-texture checkerboard.

12. **代码与命名整理 (Code & Naming Cleanup)** *(内部 / Internal)*

   - 注册表类从 `Init*` 重命名为 `Wsb*`，统一项目命名风格。
   - 中文文本中的「晨赠」统一为更直观的「晨间礼物」，相关代码同步重命名。
   - 法术数据生成器拆分为按附属模组划分的独立文件，便于多人并行更新附属支持，生成结果不变。

   - Renamed registry classes from `Init*` to `Wsb*` for a consistent naming convention.
   - Renamed the Chinese-facing term 「晨赠」 to the clearer 「晨间礼物」 and aligned the supporting code accordingly.
   - Split the previously monolithic spell-data generators into per-addon files, making it easier to update addon support in parallel; generated output is unchanged.

13. **架构分层与性能整理 (Architecture Layering & Performance Pass)** *(内部 / Internal)*

   一轮不改变游戏行为的分层与性能整理。总代码量基本持平（去注释后 10547 → 10621 行），变化在结构上：最大的单个类从 785 行拆成 9 个文件，成批的重复样板换成枚举遍历与接口 default 方法，腾出的篇幅补给了此前缺失的注释。

   - **苦力怕躲避不再全服搜实体。** 原先给每只苦力怕挂的 `AvoidEntityGoal` 每 tick 都做一次范围实体搜索，哪怕世界里根本没有召唤女仆。新增 `SummonedMaidPresence` 按维度维护 O(1) 存在计数，`AvoidSummonedMaidGoal` 先查计数再决定是否搜索；并新增服务端配置 `creeperAvoidSummonedMaid`（默认开）可整体关掉这个目标。
   - **法术类别枚举化。** 新增 `api/spell/` 下的 `SpellCategory`（密封接口）+ `MaidSpellAction` + `SpellFlag`，把每个类别的池名、目录名、配置键、翻译后缀与 TOML 注释收进枚举常量。`Config`、`MaidSpellCatalog`、`MaidSpellDataHolder` 三处原本逐类别手写的 7～9 段重复代码改为遍历枚举。数据包 JSON 字段名、配置键与语言键全部保持不变（已用 `runData` 逐字节校验）。
   - **二段咏唱拆分。** 785 行的 `MaidRecasts` 拆成 `magic/recast/` 下的分派表、上下文记录与按附属模组划分的处理器；对附属模组的反射统一走 `AddonReflection`，并在服务器启动时自检——模组装了但类 / 方法找不到会打 WARN，模组没装则静默。此前反射失败被 `catch (Exception ignored)` 吞掉，表现为附属法术打出 0 伤害的静默弹体。
   - **委托样板抽成 default 接口。** 新增 `MagicMaidDelegate`，把约 20 个转发到 `MagicMaidAdapter` 的方法收进 default 实现，`SummonedEntityMaid` 与 `MaidEntityMixin` 各减近百行。
   - **包归位。** `entity/spells/` 按语义拆为 `entity/summon/`、`entity/projectile/`、`entity/cloud/`；服务端也会读取的 `MagicCastingAnimateStateHolder` 与 `TransferStreamOption` 从 `client/`、`particle/` 移入 `magic/animation/`、`magic/particle/`；`registry/MaidSpellRegistry` 改名并移动为 `magic/catalog/MaidSpellCatalog`。**依赖本模组的附属请注意：** `MaidSpellRegistry` 与 `MaidSpellAction` 的包路径已变，`RegisterSpellTypeEvent` 构造签名同步调整（原有 getter 保留）。
   - **杂项。** 删除若干无调用者的死代码（其中 `spawnParticles` 会把粒子发给全服玩家）；引爆递归深度计数从 `ThreadLocal` 改为普通 `static int`（唯一入口已在服务端主线程）；伏击监听器把最便宜的判定前置；`MaidSummonManager` 提出循环不变量。

   An internal layering and performance pass with no gameplay changes. Total code size is essentially flat (10547 → 10621 comment-free lines); the change is structural — the largest single class became nine focused files, batches of repeated boilerplate gave way to enum iteration and interface defaults, and the room that freed up went into comments that were missing before.

   - **Creeper avoidance no longer scans entities server-wide.** The `AvoidEntityGoal` attached to every creeper used to run a ranged entity search every tick even with no summoned maid anywhere. `SummonedMaidPresence` now keeps an O(1) per-dimension count that `AvoidSummonedMaidGoal` checks first, and a new server config `creeperAvoidSummonedMaid` (default on) can drop the goal entirely.
   - **Spell categories are now enums.** New `SpellCategory` (sealed) / `MaidSpellAction` / `SpellFlag` under `api/spell/` carry each category's pool name, catalog name, config key, translation suffix, and TOML comment. The 7-to-9-way hand-written repetition in `Config`, `MaidSpellCatalog`, and `MaidSpellDataHolder` became enum iteration. Datapack JSON field names, config keys, and language keys are all unchanged (verified byte-for-byte via `runData`).
   - **Recast handling split up.** The 785-line `MaidRecasts` is now a dispatch table, a context record, and per-addon handlers under `magic/recast/`. Addon reflection goes through `AddonReflection` with a startup self-check that WARNs when a mod is present but a class/method is missing, and stays silent when the mod is absent. Reflection failures were previously swallowed by `catch (Exception ignored)`, surfacing as silent zero-damage addon projectiles.
   - **Delegation boilerplate moved to interface defaults.** The new `MagicMaidDelegate` holds ~20 default methods forwarding to `MagicMaidAdapter`, shrinking both `SummonedEntityMaid` and `MaidEntityMixin` by close to a hundred lines each.
   - **Package moves.** `entity/spells/` split into `entity/summon/`, `entity/projectile/`, and `entity/cloud/`; `MagicCastingAnimateStateHolder` and `TransferStreamOption`, both read server-side, moved out of `client/` and `particle/` into `magic/animation/` and `magic/particle/`; `registry/MaidSpellRegistry` was renamed and relocated to `magic/catalog/MaidSpellCatalog`. **For addons depending on this mod:** the packages of `MaidSpellRegistry` and `MaidSpellAction` have changed, and the `RegisterSpellTypeEvent` constructor signature changed with them (the existing getters are retained).
   - **Misc.** Removed dead code with no callers (including a `spawnParticles` that broadcast particles to every player on the server); the detonation recursion counter went from `ThreadLocal` to a plain `static int` (its only entry point is already on the server main thread); the ambush listener now front-loads its cheapest checks; `MaidSummonManager` hoists a loop invariant.

14. **依赖更新 (Dependency Updates)**

   本版本周期内更新 NeoForge、Iron's Spells 'n Spellbooks、车万女仆 (1.5.0 → 1.5.2)、Ars Nouveau、Hazen 'N Stuff、Aces Spell Utils、Ender's Spells Requiem、Lionfish 等若干依赖到最新兼容版本；并新增 `hazentouvelib` (1.0.6) 作为 Hazen 'N Stuff 的运行时依赖以修复缺类错误。

   Over this beta cycle, updated NeoForge, Iron's Spells 'n Spellbooks, Touhou Little Maid (1.5.0 → 1.5.2), Ars Nouveau, Hazen 'N Stuff, Aces Spell Utils, Ender's Spells Requiem, Lionfish, and various spell addon mods to their latest compatible versions; also added `hazentouvelib` (1.0.6) as a runtime dependency for Hazen 'N Stuff to fix missing-class errors.

## 1.0.0-beta.11

1. **依赖版本更新 (Dependency Version Update)**

   更新 Iron's Spells 'n Spellbooks 及其附属模组到最新版本。

   Updated Iron's Spells 'n Spellbooks and its addon mods to the latest versions.

---

## 1.0.0-beta.10

1. **魔力转移法术 (Mana Transfer Spell)**

   新增「魔力转移」法术，可将施法者的魔力持续转移给目标生物。使用射线检测目标，支持玩家和魔法实体。传输效率为 80%（20% 损耗），最大持续时间 5 秒。法术等级影响传输速率和最大距离。归类为支援类法术，女仆可在辅助模式下使用。

   Added "Mana Transfer" spell that continuously transfers caster's mana to target entities. Uses raycast targeting, supports players and magic entities. 80% transfer efficiency (20% loss), max duration 5 seconds. Spell level affects transfer rate and max
   distance. Classified as support spell, usable by maids in support mode.

2. **召唤女仆互相支援 (Summoned Maid Mutual Support)**

   召唤女仆现在可以互相支援和协同作战。新增协同攻击行为：查找盟友正在攻击的敌人并加入战斗。新增紧急支援行为：自动寻找生命危急的盟友并提供治疗/增益。支援搜索范围 16 格，生命值阈值 30%（主人 40%）。允许召唤女仆使用正面效果和支援类法术。

   Summoned maids can now support each other and coordinate in combat. Added cooperative attack behavior: find enemies allies are attacking and join the fight. Added emergency support behavior: automatically find critically injured allies and provide
   healing/buffs. Support search range 16 blocks, health threshold 30% (40% for owner). Summoned maids can now use positive effect and support spells.

3. **Jade 插件优化 (Jade Plugin Enhancement)**

   优化召唤女仆的 Jade 提示信息显示。按住 Shift 键显示所有法术详情，不按 Shift 时仅显示法术数量，避免信息过载。

   Optimized Jade tooltip display for summoned maids. Hold Shift to show all spell details. Without Shift, only shows spell count to avoid information overload.

---

## 1.0.0-beta.9

1. 兼容车万女仆 1.5.0

   支持 TLM 1.5.0 的 Curios 饰品栏，如果在 TLM 配置中关闭饰品栏兼容，将回退到默认女仆饰品栏。

   Support TLM 1.5.0's Curios accessory slots, if Curios compatibility is disabled in the TLM configuration, it will revert to the default maid accessory slots.

2. **召唤女仆法术**

   新增「召唤女仆」法术，可召唤临时的女仆战斗单位。包括完整的召唤女仆实体系统、女仆扫帚飞行系统、装备随机化与职业配装系统（战士/法师/辅助）、召唤女仆 AI 大脑与行为树、女仆法力与重复施法系统、法术数据驱动配置系统、Jade 兼容提示信息、施法动画系统，以及大量配置项与注册表。

   Added "Summon Maid" spell, allowing players to summon temporary maid combat units. Includes the full summoned maid entity system, maid broom flight system, equipment randomization and class loadout system (warrior/mage/support), summoned maid AI brain
   and behavior tree, maid mana and recast system, data-driven spell configuration system, Jade compatibility tooltip, casting animation system, and extensive config options and registries.

3. **正面效果法术选择错误**

   修复了女仆在选择正面效果法术时的逻辑错误。

   Fixed a logic error when maids choose positive effect spells.

4. **游戏内手册更新**

   更新了游戏内帕秋莉手册，添加英语翻译。

   Update ingame Patchouli manual, add English translation.

5. **支持更多法术**

   添加了更多的附属法术兼容，可通过数据包和配置文件配置。

   Add more addon spell support, can be configured by datapack or config file.

---

## 1.0.0-beta.8

1. Improved spell casting AI
2. Added support for more addon spells
3. Implemented dedicated configuration options
4. Fixed sitting maid's strafing issue – now unable to strafe while sitting
5. Added task icon, thanks to 星海境

---

## 1.0.0-beta.7

1. TLM 1.4.5 support
2. Now requires Iron's Spell'n Spellbooks 3.14.0
3. Animation fix
4. Remove YSM support
5. Rewrite magic maid AI system
6. Add some configuration to adjust magic attack AI
7. Add some addon mod support (not complete yet)
8. Show spell name in chat bubble
9. Melee attack in magic attack mode

---

## 1.0.0-beta.6

[WIP] Add YSM 2.5.1 support

---

## 1.0.0-beta.5

[WIP] Add YSM 2.5.0 support

---

## 1.0.0-beta.4

Add the Magic Support task. The little maid will support the owner with magic.

---

## 1.0.0-beta.3

1. Optimized the management of the maid's summoned entities — now, when a maid is recalled using a Soul Spell, her summoned entities will also be recalled at the same time.
2. Creatures summoned by the maid will no longer attack their owner.
3. Option to disable YSM support (need edit json and restart).

---

## 1.0.0-beta.2

1. TLM 1.4.5 support
2. Now requires Iron's Spell'n Spellbooks 3.14.0
3. Animation fix
4. Remove YSM support
5. Rewrite magic maid AI system
6. Add some configuration to adjust magic attack AI
7. Add some addon mod support (not complete yet)
8. Show spell name in chat bubble
9. Melee attack in magic attack mode

---

## 1.0.0-beta.1

### Usage Instructions

1. Install the required mods (Touhou Little Maid + Iron's Spells 'n Spellbooks).
2. Install this mod.
3. Place a spellbook in the maid's accessory slot or enchant gear with spells.
4. Switch the maid to "Magic Attack Mode."
5. Strike the maid with lightning to enable infinite mana.
