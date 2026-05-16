# Changelog / 变更日志

## 1.0.0-beta.12

1. **残月血酿 (Crescent Blood Vintage)**

   新增可饮用物品「残月血酿」。女仆饮用必定触发「狐火增幅」效果，将冷却缩减提升到约 90%（即冷却时间为原本的 10%），并立即同步缩减已有冷却；其他生物饮用有 60% 概率触发「魔力紊乱」减益。同时附带新增的「狐火增幅」与「魔力紊乱」两种状态效果。

   Added drinkable item "Crescent Blood Vintage". Maids drinking it always trigger the "Foxfire Boost" effect, raising cooldown reduction to about 90% (cooldowns shrink to 10% of normal) and immediately syncing existing cooldowns; other creatures have a 60% chance to be afflicted by "Mana Disruption". Two new mob effects ("Foxfire Boost" and "Mana Disruption") are introduced alongside.

2. **FTB Teams 友伤兼容 (FTB Teams Friendly Fire Compatibility)**

   通过 Mixin 增强 `Entity.isAlliedTo()`：在原始检查返回 false 时，将双方实体沿召唤链（`IMagicSummon` / `OwnableEntity`）解析到顶层召唤者后再次检查，使 FTB Teams Friendly Fire 等基于队伍的友伤判定能够正确覆盖召唤的女仆和其他召唤物。

   Enhanced `Entity.isAlliedTo()` via mixin: when the original check returns false, both entities are resolved up the summon chain (`IMagicSummon` / `OwnableEntity`) to their root summoners and re-checked, so team-based friendly-fire mods such as FTB Teams Friendly Fire correctly recognize summoned maids and other summons as allies.

3. **修复扫帚保存重复 (Fix Broom Save Duplication)**

   修复召唤女仆扫帚在卸载/保存时被重复持久化导致重新加载后出现多个扫帚实体的问题。

   Fixed an issue where the summoned maid broom was persisted multiple times on save/unload, causing duplicate broom entities to appear after reload.

4. **修复死锁与扫帚碰撞箱 (Fix Deadlock and Broom Bounding Box)**

   引入 `LineOfSightGuard` 工具修复女仆 AI 任务（施法、辅助施法、调试饮药）在 LOS 检查时的死锁问题；同时修正召唤女仆扫帚的碰撞箱尺寸。

   Introduced a `LineOfSightGuard` utility to fix a deadlock in maid AI tasks (casting, support casting, debug potion drinking) caused during line-of-sight checks; also corrected the bounding box size of the summoned maid broom.

5. **依赖版本更新 (Dependency Version Update)**

   更新 NeoForge、Iron's Spells 'n Spellbooks、车万女仆 (1.5.0 → 1.5.2)、Ars Nouveau 及多个法术附属模组到最新兼容版本。

   Updated NeoForge, Iron's Spells 'n Spellbooks, Touhou Little Maid (1.5.0 → 1.5.2), Ars Nouveau and various spell addon mods to the latest compatible versions.

6. **内部重构 (Internal Refactor)**

   将注册表类从 `Init*` 命名重命名为 `Wsb*`（`WsbAttachments` / `WsbCommands` / `WsbEntities` / `WsbItems` / `WsbSpells`），新增 `WsbEffects`，统一项目命名风格。

   Renamed registry classes from `Init*` to `Wsb*` (`WsbAttachments` / `WsbCommands` / `WsbEntities` / `WsbItems` / `WsbSpells`), added `WsbEffects`, unifying the project's naming convention.

7. **酒狐巫法学派 (Winefox Hex Spell School)**

   新增独立的「酒狐巫法」学派，注册到 Iron's `SchoolRegistry`，配套两个百分比属性（法术强度 / 抗性）、自有伤害类型 `winefox_hex_magic` 与紫粉 #C846FF 主题色。`召唤女仆`、`魔力传输` 两个法术从 Ender 学派切换到本学派。

   Added the standalone "Winefox Hex" spell school registered into Iron's `SchoolRegistry`, with paired percent attributes (spell power / resist), its own `winefox_hex_magic` damage type, and a violet-pink #C846FF theme color. The existing `Summon Maid` and `Mana Transfer` spells were moved from the Ender school to this new one.

8. **灵狐精魂学派焦点 (Vulpine Anima Focus)**

   新增 `灵狐精魂` 物品作为酒狐巫法学派的 focus；通过 additive datagen 写入 `irons_spellbooks:school_focus` tag，使 Iron's 卷轴锻造 (Scroll Forge) 能识别本学派。

   Added the `Vulpine Anima` item as the focus for the Winefox Hex school; injected into `irons_spellbooks:school_focus` via additive datagen so Iron's Scroll Forge recognizes this school.

9. **满好感度女仆晨赠 (Max-Favorability Maid Morning Gift)**

   仿原版猫晨赠机制：玩家正常睡满一晚醒来后，若身边半径内有已驯化且达到满好感度的女仆，该女仆会赠予一件 `灵狐精魂`（每游戏日上限一次，具体掉落由数据驱动战利品表 `gameplay/maid_morning_gift` 决定，数据包可整张覆盖）。新增 3 项配置：总开关、搜索半径（默认 8 格）、是否强制睡满。

   Modeled after vanilla's cat morning gift: when a player wakes from a normal full sleep with a tamed, max-favorability maid nearby, that maid gifts one `Vulpine Anima` (once per game day, governed by the data-driven loot table `gameplay/maid_morning_gift`; datapacks may replace it wholesale). Three new config options: master toggle, search radius (default 8 blocks), and whether a full sleep is required.

10. **灵狐精魂掉落注入 (Vulpine Anima Loot Drops)**

    通过 Global Loot Modifier 向若干战利品表追加 `灵狐精魂`：女仆击杀车万女仆模组的妖精时按概率掉落（驯化女仆 25%、召唤女仆 35%，每级抢夺附魔 +5%）；同时按概率追加到原版林地府邸、要塞图书馆、下界要塞，以及 Iron's Spells 的火焰法师塔、地下墓室、城堡藏书库等宝箱。

    Added Global Loot Modifiers that inject `Vulpine Anima` into several loot tables: maids killing Touhou Little Maid fairies drop them by chance (25% for tamed maids, 35% for summoned maids, +5% per Looting level); and they're also added at various rates to vanilla Woodland Mansion, Stronghold Library, and Nether Fortress chests, plus Iron's Spells' Pyromancer Tower, Catacombs, and Citadel chests.

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