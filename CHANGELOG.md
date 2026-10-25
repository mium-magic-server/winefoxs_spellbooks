# Changelog / 变更日志

## Unreleased / 未发布

1. **潜行右键喂女仆饮用 (Sneak-Right-Click to Feed Your Maid)** — 手持品酒排或铁魔法的药剂潜行右键自家女仆，即可喂其饮下（品酒排为最右一支）；指向女仆时显示车万女仆的喂药提示。  
   Sneak-right-click your own maid while holding a Vixen's Flight or one of Iron's elixirs to have her drink it (the rightmost vial of a Vixen's Flight); pointing at her shows Touhou Little Maid's feeding tip.

2. **品酒排可装入铁魔法药剂 (Vixen's Flight Holds Iron's Elixirs)** — 酒签现在也可装入铁魔法（及附属）的药剂。女仆生命低于 60% 时优先饮用治疗类，不论其位于哪一支。  
   Its vials now also take Iron's (and its addons') elixirs. Below 60% health a maid drinks healing-type vials first, wherever they sit on the rack.

3. **新增 24 个铁魔法附属的法术支持 (Spell Support for 24 More Iron's Addons)** — 女仆可识别并施放以下附属的法术：Chronomancy、Farmer's Spell、Spellbooks of Twilight、Deeper & Darker Spellbooks、Echoing Magic、Legendary Mage、Apex Arcana、Wind's Spellbooks、Paladin Spells、Aether Spellbooks、Unstable Magic、Asterism Arcanum、Ozymandias' Sundries、Alshanex's Familiars、Eldritch Expansions、[ES] Esoteric Spells、Cursed School、CS Curse Spells、ISS: Elden Ring、Leylines、Frieren Spells、Frozen Dominion Spells、Hyper 'N Hiei's Shenanigans、Wizards Help!；万法皆通自带的法术亦已归类。  
   Maids now recognise and cast the spells of the addons listed above; Touhou Little Maid: Spell's own spells are classified as well.

4. **已支持附属的法术归类更新 (Existing Addon Spells Reclassified)** — 按各附属的新版本补充新增法术；Monsters & Spellbooks 新版不再注册的风系法术随之移除。仅限玩家施放的法术（如 Apprentice's Codex 的 Bound Bow、Bound Sword，Somake 的 Crimson Reflection）以及对女仆无效或有害的法术，女仆不再选用。  
   Spells added in the addons' new versions are classified, and the Aero spells Monsters & Spellbooks no longer registers are dropped. Maids no longer pick player-only spells (such as Apprentice's Codex's Bound Bow and Bound Sword, or Somake's Crimson Reflection), nor spells that are useless or harmful in their hands.

5. **同一主人名下互为友军 (Allies Under the Same Owner)** — 主人、女仆、宠物与召唤物之间，凡最终归属同一主人者互为友军，且双向成立：主人的横扫攻击、各模组按友军判定的范围法术，不再伤及己方。万法皆通的「破法回响」不再清除主人一方的召唤物。  
   An owner, their maids, pets and summons are now allies of one another in both directions, so the owner's sweeping attacks and other mods' ally-aware area spells no longer hit their own side. Touhou Little Maid: Spell's Spellbreaking Echo no longer clears the owner's side's summons.

6. **传奇法师死亡爆炸不再误伤己方 (Legendary Mage Death Explosions Spare Your Side)** — 带「火焰异常」或「烈焰」效果的生物死亡时的爆炸，不再伤及击杀者、标记施法者及其主人、女仆与宠物；敌方照常受伤。  
   The explosion when a creature with Fire Mark or Pyro Flame dies no longer hurts the killer, the mark's caster, or their owner, maids and pets; enemies are still hit.

7. **起手失败的法术暂缓选用 (Failing Spells Are Set Aside)** — 女仆选中前置条件不满足的法术而起手失败后，暂停选用该法术 3 秒，连续失败时逐次加倍至 60 秒，成功施放后复原，不再反复选中它而挤占其余法术。  
   When a maid's chosen spell fails its pre-cast check, she sets it aside for 3 seconds, doubling on each further failure up to 60 seconds and resetting once it casts, so it no longer crowds out her other spells.

8. **Advanced Loot Info 2.5 支持 (Advanced Loot Info 2.5 Support)** — 战利品查询插件改为对接 ALI 2.5.0 的新接口。⚠️ 安装 ALI 时须为 2.5.0 及以上（及其前置 Advanced Core Info 1.5.0 及以上），更早的版本将在启动时被拒绝加载；未安装 ALI 不受影响。  
   The loot lookup plugin now targets ALI 2.5.0's new API. ⚠️ If ALI is installed it must be 2.5.0 or newer (with Advanced Core Info 1.5.0 or newer); older versions are rejected at startup. Without ALI nothing changes.

---

## 1.0.2-alpha.3

1. **车万女仆：万法皆通联动 (Touhou Little Maid: Spell Integration)** — 可选联动。安装后改用万法皆通的「法术战斗」「远程法术」任务，女仆在其中按本模组的逻辑选择并施放铁魔法法术、为主人和队友治疗增益，并按任务保持站位；法术书识别、冷却与饰品以万法皆通为准，法力照常消耗（`tlmSpellMana` 可关闭）；末影腰包的女仆列表显示法力条。  
   Optional integration. With it installed, maids use its Spell Combat and Ranged Spell tasks, in which they choose and cast Iron's spells by this mod's logic, heal and buff their owner and allies, and hold the spacing each task calls for. Spellbook detection, cooldowns and baubles follow Touhou Little Maid: Spell; mana is still spent (switch off with `tlmSpellMana`), and the Ender Pocket maid list shows a mana bar.

2. **品酒排 (Vixen's Flight)** — 新物品，四支酒签各装一瓶药水：在物品栏中与药水或玻璃瓶互相右键装入、倒出，也可从炼药锅取水、从铁魔法炼金锅取药；玩家可直接饮用。女仆把它带在背包里，会在战斗中挑此刻用得上的增益饮用，低血时饮用治疗类。四个玻璃瓶加任意木台阶无序合成。  
   A new item holding four vials of one potion each: right-click it with a potion or glass bottle in your inventory to fill or pour, or draw water from a cauldron and potions from an Alchemist Cauldron; players can drink from it directly. A maid carrying it drinks the buffs she needs in combat, and healing when low on health. Crafted from four glass bottles and any wooden slab.

3. **战斗饮药改为消耗品酒排 (Battle Drinking Now Uses Vixen's Flight)** — 女仆不再凭空饮用治疗药水，只饮用品酒排中装填的药水；`drinkPotionInSpellBattle` 的默认值随之改为开。⚠️ 已有配置文件中的值不会被改动。  
   Maids no longer conjure free healing potions; they drink only what is loaded into a Vixen's Flight, and `drinkPotionInSpellBattle` now defaults to on. ⚠️ Values already present in existing config files are left unchanged.

4. **狐影伏击与酒杯狐影加强 (Foxshade Ambush and Foxshade Wisps Buffed)** — 狐影伏击每级多咬一口（1～4 口），狐影停留更久，四个等级改为卓越至传说；酒杯狐影每发伤害系数由 ×2 提高到 ×3。两者的法术说明新增伤害与扑咬次数 / 弹射物数量。  
   Foxshade Ambush bites once more per level (1–4 bites), its fox lingers longer, and its four levels now run Uncommon to Legendary; Foxshade Wisps' per-projectile damage multiplier rises from ×2 to ×3. Both spell tooltips now show damage and bite or projectile count.

5. **醒酒之礼随等级成长 (Decant the Bind Scales with Level)** — 负面效果时长不再固定减半：2～4 级依次缩短 50% / 67% / 83%，5 级尽数清除。  
   Debuffs are no longer simply halved: levels 2–4 cut their duration by 50% / 67% / 83%, and level 5 purges them outright.

6. **法术图标重绘 (Spell Icons Redrawn)** — 学派法术图标统一重绘为 16×16 精绘风格。  
   The school's spell icons are redrawn at 16×16 in one unified style.

---

## 1.0.2-alpha.1

1. **Advanced Loot Info 2.2 支持 (Advanced Loot Info 2.2 Support)** — 战利品查询插件改为对接 ALI 2.2.0 的新接口。宝箱条目的精细展示需要 ALI 2.2.0 及以上（ALI 自 2.1 起另需前置 Advanced Core Info），更早的版本会退回 ALI 自带的自动识别条目；「女仆的晨间礼物」分类页不受影响。  
   The loot lookup plugin now targets ALI 2.2.0's new API. The tailored chest entries need ALI 2.2.0 or newer (ALI itself has required Advanced Core Info since 2.1); on older ALI they fall back to its own auto-detected entry. The Maid Morning Gift category page is unaffected.

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
