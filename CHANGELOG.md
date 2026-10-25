# Changelog / 变更日志

## 1.0.2

1. **车万女仆：万法皆通联动 (Touhou Little Maid: Spell Integration)** — 可选联动。安装后女仆在万法皆通的「法术战斗」「远程法术」任务中按本模组的逻辑施放铁魔法法术、治疗增益队友，并按任务保持站位；法术书识别、冷却与饰品以万法皆通为准，法力照常消耗（`tlmSpellMana` 可关闭）。万法皆通自带的法术亦已归类。  
   Optional integration. Maids on its Spell Combat and Ranged Spell tasks cast Iron's spells by this mod's logic, heal and buff allies, and keep each task's spacing. Spellbook detection, cooldowns and baubles follow Touhou Little Maid: Spell; mana is still spent (switch off with `tlmSpellMana`). Its own spells are classified as well.

2. **品酒排 (Vixen's Flight)** — 新物品，四支酒签各装一瓶药水或铁魔法药剂，与药水、玻璃瓶互相右键即可装入、倒出，四个玻璃瓶加任意木台阶合成。女仆战斗中只饮用品酒排里的药水，不再凭空饮用治疗药水；生命低于 60% 时优先治疗类。手持品酒排或药剂潜行右键自家女仆可直接喂其饮用。⚠️ `drinkPotionInSpellBattle` 默认值改为开，已有配置文件不受影响。  
   A new item holding four vials of potions or Iron's elixirs; right-click it with a potion or glass bottle to fill or pour. Crafted from four glass bottles and any wooden slab. In combat maids now drink only from it instead of conjuring free healing potions, healing first below 60% health. Sneak-right-click your maid with it or an elixir to feed her. ⚠️ `drinkPotionInSpellBattle` now defaults to on; existing config files keep their value.

3. **攻击法术击杀规划 (Kill-Time Attack Spell Planning)** — 女仆按最快击倒目标选择攻击法术，伤害由实战记录修正，必要时积攒法力以施放更强的法术（`spellPlanningEnabled` 可关闭）。起手失败的法术会暂缓选用，不再反复挤占其他法术。  
   Maids pick attack spells by time-to-kill, learning damage from their own casts and saving mana for stronger spells when worthwhile (switch off with `spellPlanningEnabled`). Spells that fail their pre-cast check are set aside for a while.

4. **战斗与支援 AI 调整 (Combat & Support AI)** — 跟随模式下接战与走位不再超出跟随范围、绕着主人转圈；战斗中保持待机姿势、不再讨食，持续施法结束后不再卡在施法姿势。支援法术仅在队友确实需要时优先，支援时不丢失攻击目标；「法术支援」任务会攻击主人的目标。女仆不再把自己的坐骑当作目标。  
   In follow mode maids fight within the follow range instead of circling their owner, hold a combat idle pose, no longer beg, and no longer stay stuck in the casting pose after a continuous spell. Support spells take priority only when an ally needs them, without dropping the attack target; the Magic Support task attacks the owner's target. Maids no longer target their own mount.

5. **同一主人名下互为友军 (Allies Under the Same Owner)** — 主人、女仆、宠物与召唤物互为友军，主人的横扫攻击与其他模组的范围法术不再伤及己方；万法皆通的「破法回响」、传奇法师的死亡爆炸与感电连锁闪电同样不再波及己方。  
   An owner, their maids, pets and summons are allies of one another, so sweeping attacks and other mods' area spells spare their own side, as do Touhou Little Maid: Spell's Spellbreaking Echo and Legendary Mage's death explosions and electrocuted chain lightning.

6. **法术分类编辑器 (Spell Category Editor)** — 配置界面的「法术兼容」小节改为可视化编辑器，可逐个法术调整行为类别、标记、施法距离等，支持拼音搜索（需 Just Enough Characters）；每个类别新增「移除的法术 ID」配置。  
   The Spell Compatibles config section is now an editor for each spell's categories, markers, casting range and more, with pinyin search via Just Enough Characters. Each category also gains a removed-spell-ID list.

7. **女仆浮动法力条 (Floating Maid Mana Bar)** — 施法任务中的自家女仆身旁显示法力条，回满后淡出（客户端配置 `showMaidManaBar`）。  
   Your spellcasting maids show a mana bar that fades once full (client config `showMaidManaBar`).

8. **学派更名为「灵狐」 (School Renamed to Vulpine)** — 「酒狐巫法」更名为「灵狐」（内部 ID 不变），法术图标重绘，灵狐精魂换新贴图与掉落光效。  
   The Winefox Hex school is renamed Vulpine (internal ID unchanged), with redrawn spell icons and a new Vulpine Anima texture and drop glow.

9. **学派法术调整 (School Spell Changes)** — 狐影伏击每级多咬一口、停留更久，等级改为卓越至传说；酒杯狐影伤害系数 ×2 → ×3；醒酒之礼的减益缩短比例随等级提升，5 级直接清除。  
   Foxshade Ambush bites once more per level, lingers longer and now runs Uncommon to Legendary; Foxshade Wisps' damage multiplier rises from ×2 to ×3; Decant the Bind cuts debuffs more with each level and purges them at level 5.

10. **饰品栏调整 (Bauble Slots)** — 铁魔法饰品在女仆的任意 Curios 栏位都生效；全部饰品栏合计只能装一本法术书。  
    Iron's curios work in any of a maid's Curios slots; only one spellbook can be equipped across all bauble slots.

11. **附属法术支持 (Addon Spell Support)** — 新增 24 个附属：Chronomancy、Farmer's Spell、Spellbooks of Twilight、Deeper & Darker Spellbooks、Echoing Magic、Legendary Mage、Apex Arcana、Wind's Spellbooks、Paladin Spells、Aether Spellbooks、Unstable Magic、Asterism Arcanum、Ozymandias' Sundries、Alshanex's Familiars、Eldritch Expansions、[ES] Esoteric Spells、Cursed School、CS Curse Spells、ISS: Elden Ring、Leylines、Frieren Spells、Frozen Dominion Spells、Hyper 'N Hiei's Shenanigans、Wizards Help!。已支持附属按新版本更新归类，仅限玩家或对女仆有害的法术不再选用。  
    Added the 24 addons listed above. Existing addons are reclassified for their new versions, and player-only or self-harming spells are no longer used.

12. **兼容性 (Compatibility)** — 修复与 Iron's Spells JS 同装时女仆无法施法。战利品查询插件改为对接 Advanced Loot Info 2.5。⚠️ 安装 ALI 时须为 2.5.0 及以上（前置 Advanced Core Info 1.5.0 及以上），旧版将在启动时被拒绝。  
    Fixed maids being unable to cast alongside Iron's Spells JS. The loot lookup plugin now targets Advanced Loot Info 2.5. ⚠️ ALI, if installed, must be 2.5.0 or newer (with Advanced Core Info 1.5.0 or newer); older versions are rejected at startup.

13. **依赖更新 (Dependency Updates)** — NeoForge、Iron's Spells (3.16.3)、Iron's Lib (2.2.0)、GeckoLib、JEI 等更新到最新兼容版本。  
    Updated NeoForge, Iron's Spells (3.16.3), Iron's Lib (2.2.0), GeckoLib, JEI and others.

---

## 1.0.1

1. **目标选取放宽至视线所指 (Target Selection Widened to the Line of Sight)** — 残月烙印、酒杯狐影与醉狐之雾不再限定敌对生物：视线所指的中立生物（猪、铁傀儡等）同样可被选中，其身上的残月烙印亦可照常引爆。  
   Crescent Brand, Foxshade Wisps and Tipsy Mist are no longer restricted to hostile mobs: a neutral creature in your line of sight — a pig, an iron golem — can now be selected, and a Crescent Brand placed on it detonates as usual.

2. **女仆施法仅指向交战目标 (Maid Casts Confined to Her Combat Target)** — 女仆施法时仅选取其 AI 已指定的交战目标，途经视线的村民与牲畜不再被误加残月烙印；在「女仆攻击列表」中设为「敌对」的生物，以及主人已先行攻击的中立生物，仍可正常作为目标。  
   A maid's cast now takes only the target her AI has engaged, so villagers and livestock crossing her line of sight are no longer branded by mistake; creatures set to Hostile in the Maid Attack List, and neutral ones the owner has already attacked, remain valid targets.

3. **狐影伏击的触发条件放宽 (Foxshade Ambush Trigger Relaxed)** — 蓄势状态改由施法者造成的任意伤害触发，近战、法术及其名下酒雾、酒池的持续伤害均计入；触发目标亦不再限定为敌对生物。  
   The coiled state is now released by any damage credited to the caster — melee, spells, and the lingering damage of their own tipsy mists and wine pools — and the entity that triggers it no longer has to be a hostile mob.

---

## 1.0.0

1. **酒狐巫法学派 (Winefox Hex Spell School)** — 新增独立学派，自带伤害类型与焦点物品「灵狐精魂」，`召唤女仆`、`魔力转移` 归入其中。  
   Added a standalone school with its own damage type and the `Vulpine Anima` focus item; `Summon Maid` and `Mana Transfer` now belong to it.

2. **7 个学派战斗法术 (Seven Combat Spells)** — 残月烙印、酒杯狐影、狐影伏击、残月披风、醒酒之礼、醉狐之雾、狐酿炸弹，配套「微醺」「酒涌」两种效果。  
   Crescent Brand, Foxshade Wisps, Foxshade Ambush, Crescent Mantle, Decant the Bind, Tipsy Mist and Vintner's Bomb, with the Tipsy and Brewsurge effects.

3. **两件新物品 (Two New Items)** — 「灵狐精魂」与「残月血酿」（女仆饮用大幅缩短冷却），来源为女仆晨间礼物、击杀妖精与各类结构宝箱。  
   `Vulpine Anima` and `Crescent Blood Vintage` (a big cooldown cut for maids who drink it), obtained from maid morning gifts, fairies killed by maids, and structure chests.

4. **女仆强度上限配置 (Maid Power Caps)** — 召唤女仆的法术默认限到 3 级 / 史诗 / 无邪术，`drinkPotionInSpellBattle` 默认改为关，另新增 `lightningManaRegen`（默认关）。  
   Summoned maid spells default to level 3 / Epic / no Eldritch, `drinkPotionInSpellBattle` now defaults to off, and `lightningManaRegen` was added (off by default).

5. **配置改为服务端配置 (Config Moved to Server Type)** — ⚠️ 配置文件更名为 `winefoxs_spellbooks-server.toml`，**旧配置不会自动迁移**；配置界面同时补齐了本地化。  
   ⚠️ The config file is renamed to `winefoxs_spellbooks-server.toml` and **old settings do not migrate**; the config screen is also fully localized.

6. **附属模组支持扩充 (Expanded Addon Support)** — 新增五个附属的法术分类（覆盖 10 → 15 个附属），并为三个附属补上二段咏唱支持。  
   Behaviour classifications for five more addons (coverage grows from 10 to 15) and recast support for three more.

7. **法术行为类别调整 (Spell Category Rework)** — 每个法术只归属一个类别，三类按实际作用改名；配置键与数据包字段名不变。  
   Each spell now belongs to exactly one category and three categories were renamed to match what they do; config keys and datapack fields are unchanged.

8. **模组联动 (Mod Integration)** — FTB Teams 友伤沿召唤链解析，Advanced Loot Info 可查本模组的宝箱来源，物品说明自动显示到悬浮提示与 JEI。  
   Friendly fire resolves up the summon chain for FTB Teams, Advanced Loot Info can look up this mod's chest sources, and item descriptions render automatically in tooltips and JEI.

9. **女仆施法 AI 修复 (Maid Casting AI Fixes)** — 自愈不再抢占战斗法术、法术等级计入 Curios 饰品栏、不再对召唤类法术连续逐发等。  
   Self-heal no longer pre-empts combat spells, spell levels count Curios slots, and maids stop chain-recasting summon spells.

10. **魔力系统修复 (Mana Fixes)** — `maxManaMultiplier` 不再影响玩家，魔力读写统一走女仆适配器并同步客户端。  
    `maxManaMultiplier` no longer affects players, and mana reads/writes go through the maid adapter and sync to the client.

11. **召唤女仆修复 (Summoned Maid Fixes)** — 修复 AI 死锁、扫帚重复、永久召唤消失与召唤物存档丢失，并补上缺失的姻缘戒指模型。  
    Fixed an AI deadlock, duplicate brooms, vanishing permanent summons and summons lost across save/reload; added the missing Affinity Ring model.

12. **说明文档与游戏内手册 (Manuals & In-game Book)** — README 重写为完整手册，Patchouli 手册改为按主题分条目并补齐缺失内容。  
    The READMEs were rewritten as full manuals, and the Patchouli book was regrouped into topic entries with its gaps filled.

13. **代码与架构整理 (Code & Architecture Cleanup)** *(内部 / Internal)* — 结构与性能整理，玩法不变；**依赖本模组的附属注意** `MaidSpellRegistry`、`MaidSpellAction` 包路径与 `RegisterSpellTypeEvent` 签名已变。  
    A structural and performance pass with no gameplay change; **addons depending on this mod** should note the moved packages and the changed `RegisterSpellTypeEvent` signature.

14. **依赖更新 (Dependency Updates)** — NeoForge、Iron's Spells (3.16.2)、车万女仆 (1.5.2)、GeckoLib、JEI 等更新到最新兼容版本。  
    Updated NeoForge, Iron's Spells (3.16.2), Touhou Little Maid (1.5.2), GeckoLib, JEI and others to their latest compatible versions.

---

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
