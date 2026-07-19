# Changelog / 变更日志

## 1.0.0-beta.12

1. **酒狐巫法学派 (Winefox Hex Spell School)**

   新增独立的「酒狐巫法」学派，配套自有伤害类型与紫粉主题色，并新增焦点物品 `灵狐精魂`，Iron's 卷轴锻造可识别本学派。`召唤女仆`、`魔力转移` 两个法术归入新学派。同步在游戏内 Patchouli 手册中补全中英文条目。

   Added the standalone "Winefox Hex" spell school with its own damage type and violet-pink theme, plus a new focus item `Vulpine Anima` that lets Iron's Scroll Forge recognize the school. The `Summon Maid` and `Mana Transfer` spells are now classified under this school. Patchouli manual entries (Chinese / English) for the school and Mana Transfer are added in-game.

2. **酒狐巫法首批战斗法术 (Winefox Hex Combat Spells)**

   新学派迎来 6 个战斗法术，围绕「烙印引爆」与「微醺控场」两条主线：
   - **残月烙印**：单体标记法术。12 秒内目标一旦受到任意酒狐巫法伤害即引爆，造成范围爆发；由自家女仆引爆时伤害 ×1.5、范围更大且可连锁引爆下一个带印目标；烙印自然到期只发生小范围弱自爆。
   - **酒杯狐影**：逐发施法，最多 6 发追踪狐影从侧翼包抄；第 6 发为终结弹，命中时额外触发一次残月爆发。女仆 AI 支持自动逐发施放。
   - **残月披风**：为自己与附近自家女仆披上伤害吸收（高等级提升至吸收 III）与短暂隐身，紧急脱险。
   - **醒酒之礼**：将自身或视线所指自家女仆的负面效果剩余时长减半，5 级起直接尽数清除。
   - **醉狐之雾**：铺开半径 5 格的紫粉酒雾，雾中生物持续微醺，自家女仆免疫；5 级起施加微醺 II。
   - **狐酿炸弹**：投掷酒瓶直击造成学派伤害，落点铺开酒池——敌人泡出微醺，自家女仆泡在池中持续回魔。

   配套内容：
   - 新增「微醺」（减速、踉跄、弹道打偏，不伤血）与「酒涌」（玩家每施放学派法术叠一层，每层 +10% 学派法术强度，至多 3 层）两种状态效果；
   - 学派专属咏唱音效与施法粒子（紫粉酒滴 + 狐火 + 金色符箓）；
   - 酒狐巫法卷轴按概率出现在要塞图书馆、下界要塞及 Iron's Spells 的火焰法师塔、地下墓室、城堡藏书库宝箱中；
   - Patchouli 手册新增全部 6 个法术的中英文条目。

   The new school gains six combat spells, built around two pillars — brand detonation and tipsy crowd control:
   - **Crescent Brand**: single-target mark. For 12 seconds, any Winefox Hex damage taken by the target detonates the brand into an AoE burst; detonations by your own maids deal ×1.5 damage with a larger radius and can chain to the next branded target. A brand that expires naturally only weakly self-detonates in a small radius.
   - **Foxshade Wisps**: recast spell — up to 6 homing wisps flank in from the sides; the 6th is a finisher that additionally triggers a Crescent burst on hit. Maid AI can chain the recasts automatically.
   - **Crescent Mantle**: veils you and nearby owned maids in absorption (up to Absorption III at high level) and brief invisibility for emergency escapes.
   - **Decant the Bind**: halves the remaining duration of harmful effects on you or an owned maid in your gaze; from level 5 they are purged outright.
   - **Tipsy Mist**: rolls out a 5-block-radius plum-pink fog that keeps creatures inside tipsy; your own maids are immune. From level 5 it applies Tipsy II.
   - **Vintner's Bomb**: hurls a wine bottle dealing school damage on impact and spreading a wine pool — foes soak up Tipsy while your maids soak up mana.

   Supporting content:
   - Two new mob effects: "Tipsy" (slowness, staggering, deflected projectiles — no damage) and "Brewsurge" (each school spell a player casts stacks one level, +10% Winefox Hex spell power per stack, up to 3);
   - A dedicated school casting sound and cast particles (wine droplets, foxfire, golden runes);
   - Winefox Hex scrolls now appear at various rates in Stronghold Library and Nether Fortress chests, plus Iron's Spells' Pyromancer Tower, Catacombs, and Citadel chests;
   - Patchouli manual entries (Chinese / English) for all six spells.

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

   Added drinkable item "Crescent Blood Vintage". Maids drinking it always trigger the "Foxfire Boost" effect, raising cooldown reduction to about 90% (cooldowns shrink to 10% of normal) and immediately syncing existing cooldowns; other creatures have a 60% chance to be afflicted by "Mana Disruption". Two new mob effects ("Foxfire Boost" and "Mana Disruption") are introduced alongside.

5. **FTB Teams 友伤兼容 (FTB Teams Friendly Fire Compatibility)**

   将友伤判定沿召唤链解析到顶层召唤者，使 FTB Teams Friendly Fire 等基于队伍的友伤判定能正确覆盖召唤的女仆与其他召唤物。

   Friendly-fire checks now resolve both sides up the summon chain to their root summoners, so team-based friendly-fire mods such as FTB Teams Friendly Fire correctly recognize summoned maids and other summons as allies.

6. **Advanced Loot Info 兼容 (Advanced Loot Info Integration)**

   若同时安装 Advanced Loot Info，女仆晨间礼物战利品表会作为独立分类「女仆的晨间礼物」显示在 ALI / EMI 中。

   When Advanced Loot Info is installed, the maid morning gift loot table appears as its own category ("Maid Morning Gift") in ALI / EMI.

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

9. **代码与命名整理 (Code & Naming Cleanup)** *(内部 / Internal)*

   - 注册表类从 `Init*` 重命名为 `Wsb*`，统一项目命名风格。
   - 中文文本中的「晨赠」统一为更直观的「晨间礼物」，相关代码同步重命名。
   - 法术数据生成器拆分为按附属模组划分的独立文件，便于多人并行更新附属支持，生成结果不变。

   - Renamed registry classes from `Init*` to `Wsb*` for a consistent naming convention.
   - Renamed the Chinese-facing term 「晨赠」 to the clearer 「晨间礼物」 and aligned the supporting code accordingly.
   - Split the previously monolithic spell-data generators into per-addon files, making it easier to update addon support in parallel; generated output is unchanged.

10. **依赖更新 (Dependency Updates)**

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