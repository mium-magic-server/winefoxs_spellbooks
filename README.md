# Winefox's Spellbooks

[中文说明](README-zh_CN.md) · Minecraft 1.21.1 · NeoForge · MIT

**Bridges *Touhou Little Maid* and *Iron's Spells 'n Spellbooks*: maids read spellbooks, manage mana and pick their own spells, and players can summon maids onto the battlefield in return.**

---

## Contents

- [Mod Overview](#-mod-overview)
- [Quick Start](#-quick-start)
- [Maid Spellcasting](#-maid-spellcasting)
- [Winefox Hex School](#-winefox-hex-school)
- [Addon Support](#-addon-support)
- [Configuration](#️-configuration)
- [Datapack Customisation](#-datapack-customisation)
- [Commands](#-commands)
- [Compatibility & Integration](#-compatibility--integration)
- [FAQ](#-faq)
- [Requirements & Versions](#️-requirements--versions)
- [Download & Installation](#-download--installation)
- [Development & Contributing](#-development--contributing)
- [License](#-license)

---

## 🌟 Mod Overview

**Winefox's Spellbooks** turns the maids of *Touhou Little Maid* into real spellcasters: put a spellbook in her accessory slot and she picks spells to suit the situation, budgets her mana, respects cooldowns, strafes and repositions — and plays a casting animation if she uses a GeckoLib model.

Beyond that main line, the mod also provides:

- **Winefox Hex** — a standalone school with its own damage type and attributes, built around **player–maid teamwork**: the player opens, the maid follows up, and fighting together pays far better than fighting apart;
- **Addon compatibility** — behaviour classifications for several hundred spells across 15 Iron's Spells addons. Installed addons take effect automatically; missing ones cause no errors.

About the name: casting animations only play on GeckoLib maid models, and "Winefox" is one such model.

---

## ⚡ Quick Start

1. Install the dependencies: **Touhou Little Maid** and **Iron's Spells 'n Spellbooks**, plus their own dependencies (Curios, GeckoLib).
2. Install this mod.
3. Give the maid a **spellbook** with spells inscribed and put it in her **accessory slot**, or give her an **imbued weapon / armour** piece.
4. Open the maid GUI and switch her task to **Magic Attack** or **Magic Support**.
5. With *Show spell names in spell battle* enabled (the default), the spell she is casting appears above her head.

---

## 🧙 Maid Spellcasting

### Casting Media

Her **main hand, armour slots, maid accessory slots and Curios slots** are all scanned; spells in any "spell container" (spellbook, imbued weapon, imbued armour) enter her spell list.

- Only **one spellbook takes effect** at a time, same as for players; imbued gear is not subject to that limit and stacks.
- Spell level = the spell's own level + the Affinity bonus from gear.
- **A spell that belongs to no behaviour category is ignored** — the most common reason for "she just won't cast that spell". See [Seven Behaviour Categories](#seven-behaviour-categories).
- Any change of equipment recalculates the spell list immediately; after `/reload` updates the spell tags, loaded maids rescan as well.

### The Two Tasks

| Task | Categories used | Target |
|---|---|---|
| **Magic Attack** | Attack / Defense / Movement / Self-heal / Negative Effect | Her own hostile targets |
| **Magic Support** | Defense / Self-heal / Ally Buff / Negative Effect / Ally Heal | Whatever her owner and allies are fighting |

**Magic Attack** is the regular combat role: seek targets, reposition, cast, and optionally finish with a melee weapon.

**Magic Support** never starts a fight. It heals, buffs and cleanses the owner and same-team creatures (including other maids of the same owner), and debuffs enemies along the way.

> Both tasks share the same unlock condition: an imbued weapon in the main hand or armour slots, or a spellbook with inscribed spells in an accessory slot.

### Seven Behaviour Categories

A maid's spell list is split into seven behaviour categories; the in-game config screen uses these same names. Each category also has an English field name in the spell tags and the loadout datapack:

| Category | Spell tag | Config option (key) | Loadout `spells` field |
|---|---|---|---|
| Attack | `attack_spells` | Extra attack spell ids (`extraAttackSpells`) | `attack` |
| Defense | `defense_spells` | Extra defense spell ids (`extraDefenseSpells`) | `defense` |
| Movement | `movement_spells` | Extra movement spell ids (`extraMovementSpells`) | `movement` |
| Self-heal | `support_spells` | Extra self-heal spell ids (`extraSupportSpells`) | `support` |
| Ally Buff | `positive_effect_spells` | Extra ally buff spell ids (`extraPositiveEffectSpells`) | `positive_effect` |
| Negative Effect | `negative_effect_spells` | Extra negative effect spell ids (`extraNegativeEffectSpells`) | `negative_effect` |
| Ally Heal | `support_effect_spells` | Extra ally heal spell ids (`extraSupportEffectSpells`) | `support_other` |

Two further **marker tags** take no part in classification: `summon_spells` (summons) and `maid_should_recast_spells` (needs several casts). They only mark a spell; they do not decide whether a maid may cast it.

### Mana

- A maid has her own mana bar, **independent of the player's**; point at her with Jade to see it.
- The cap uses the same formula as for players: "Max Mana" affixes on staves and mage gear apply, and the *Maximum mana multiplier* option scales the whole thing.
- Natural regeneration defaults to roughly **2% per second**, modified by mana regen affixes.
- **Having been struck by lightning** is a permanent flag saved on the maid — one strike and it never wears off. With *Lightning-struck maids regenerate mana 100x faster* enabled, such maids refill the entire bar every 0.5 seconds, which is effectively unlimited mana. That option is **off by default**.

### How a Maid Picks a Spell

She makes a decision every short interval (upper bound set by *Max combo delay tick*, 10 ticks by default):

1. **Drop what cannot be cast right now**: anything short on mana or still on cooldown. If a multi-cast spell is unfinished, she finishes it first.
2. **Weight every behaviour category**, pick one category by weighted random, then pick a spell within it with equal probability.

Rules of thumb for the weights:

- The lower an ally's health, the higher the Ally Heal weight; **a dying ally outranks everything else**;
- With allies nearby, support categories dominate the round and combat categories sit it out;
- The lower her own health, the higher the Defense and Movement weights;
- With no line of sight to the target she rarely picks attack spells, and repositions instead;
- If an ally lacks a buff, or carries a debuff that can be cleansed, the matching spell's weight rises sharply;
- If Defense / Negative Effect was picked last round, both weights drop, so she does not spam the same category.

The search radius comes from *Maximum spell attack search range* (64 blocks by default), and **enemies carrying a [Crescent Brand](#-winefox-hex-school) are preferred**. She has to close to *Start spell casting range* (15 blocks by default), or the spell's own casting distance, before she starts.

Other combat behaviour:

- **Positioning**: backs off inside half her range, circles in place while in range and in sight, moves slower while casting, and stops repositioning when told to sit.
- **Melee finisher**: controlled by *Allow maids to use melee weapons during spell attacks*, on by default.

### Casting Animations

The mod wires Iron's Spells' casting animations into Touhou Little Maid's animation system; maids on GeckoLib models play the matching animation when they cast.

Model pack authors can define animation clips prefixed with `iss:` in their own animation files; for the key names see [Touhou Little Maid's iss.animation.json](https://github.com/TartaricAcid/TouhouLittleMaid/blob/1.21/src/main/resources/assets/touhou_little_maid/animation/iss.animation.json).

> The animation assets are converted from Iron's Spells' originals. A few poses may have bone misalignment; these will keep being fixed in later versions.

---

## 🍶 Winefox Hex School

**Winefox Hex** is the standalone school added by this mod, with its own damage type and two attributes (Winefox Hex Spell Power / Winefox Hex Magic Resist), stackable with mage gear affixes from Iron's Spells itself.

It is designed **for "player + maid"**: its spells pass value back and forth between the two — the player brands, the maid follows up and detonates, and the mantle and cleanse the player casts also cover nearby maids. Solo play works, but a squad of maids is its complete form.

The school is still growing; later versions will keep adding new directions.

### School Items

| Item | Notes | Obtained from |
|---|---|---|
| **Vulpine Anima** | The school's Focus: the crafting ingredient for its scrolls at the Scroll Forge, and a casting implement for higher-tier spells | Morning gift from a max-favorability maid (95%); dropped by fairies killed by maids (tamed maid 25% / summoned maid 35%, +5% per level of Looting); chests: woodland mansion 25%, nether fortress 20%, stronghold library 15%, plus Iron's Spells' Pyromancer Tower, Catacombs and Citadel library |
| **Crescent Blood Vintage** | Drinkable. **A maid always gains Foxfire Boost** for 10 seconds: new cooldowns count as 10% of normal, and existing cooldowns are cut by 90% the moment she drinks. Anyone else has a 60% chance of Nausea, Poison and Mana Disruption respectively | Village houses / temples, pillager outposts, woodland mansions, plus Iron's Spells' various casks, troves and food barrels |

The school's scrolls also appear by chance in stronghold libraries, nether fortresses and Iron's Spells' Pyromancer Tower, Catacombs and Citadel library chests. The mod ships a creative tab holding both items and every level of every school scroll.

### Spell List

| Spell | Level | Rarity | Cooldown | Maid category | Notes |
|---|---|---|---|---|---|
| **Crescent Brand** | 1–8 | Common | 10s | Negative Effect | Brands a single target with a 16-block ray; 12s base, +1s per level. The brand deals no damage itself — it is a fuse waiting to be lit |
| **Foxshade Wisps** | 3–8 | Uncommon | 15s | Attack | Multi-cast: each cast releases one homing foxshade, up to 3 within a 4-second window, flanking from both sides; damage per wisp = spell power × 2 |
| **Foxshade Ambush** | 1–4 | Common | 8s | Defense | Marks yourself for 10 seconds; the next time you hit a hostile creature, a foxshade lands and bites (damage = spell power). Best cast before a fight |
| **Crescent Mantle** | 3–8 | Uncommon | 30s | Defense | Grants yourself and your own maids within 6 blocks both damage absorption (Absorption II, 4s base, upgraded to Absorption III from level 6) and **true invisibility** (3s base, gear hidden as well, drops aggro); both durations +0.5s per level |
| **Decant the Bind** | 2–5 | Common | 20s | Self-heal | Halves the duration of debuffs on yourself, or on one of your maids within 16 blocks in your line of sight; at max level (5) removes them outright |
| **Tipsy Mist** | 1–8 | Common | 12s | Negative Effect | After a 1-second cast, spreads a 5-block-radius mist at the impact point (up to 24 blocks away) for 6s base, +0.5s per level; from level 5 it applies Tipsy II. Your own maids are immune |
| **Vintner's Bomb** | 1–8 | Common | 8s | Attack | Throws a bottle dealing school damage on a direct hit; the shards leave a wine pool that keeps enemies Tipsy while your own maids regain mana in it |
| **Mana Transfer** | 1–10 | Common | 15s | Ally Buff | Channels for up to 5 seconds, transferring your mana to the player or maid under your crosshair; it costs about 20% more mana than it delivers. On the Magic Support task, maids cast it on allies who are low on mana |
| **Summon Maid** | 1–6 | Uncommon | 150s | —— | Summons a squad of temporary maids for 10 minutes. Maids never cast this spell themselves |

### Brand Detonation and Brewsurge

Currently the clearest expression of "player + maid" teamwork in the school:

1. Brand an enemy with **Crescent Brand** (12s base, +1s per level).
2. Deal any **Winefox Hex damage** to it — Foxshade Wisps, the Foxshade Ambush bite and a direct Vintner's Bomb hit all qualify.
3. The brand detonates at once: damage = 10 base + the spell power at the time it was applied.
   - **Detonated by the player**: 3-block radius, no chaining;
   - **Detonated by your own maid**: ×1.5 damage, 4-block radius (5 blocks at full player Brewsurge), and it can **chain-detonate** the next branded target.
4. Every detonation gives the player one stack of **Brewsurge**: rolling 10-second refresh, up to 3 stacks, each granting **+10% school spell power**. Maid detonations count for the player as well.

A brand that simply expires does a small self-detonation within 2 blocks (3 points of vanilla magic damage, no Brewsurge stack, no chaining).

In short: the player brands, the maid detonates — keep the detonations flowing and Brewsurge never lapses.

### Tipsy

**Tipsy** is a neutral effect. It deals no damage, but a creature under it:

- moves 30% slower (scaled by effect level);
- staggers while walking and has its facing jitter randomly (players are exempt from the jitter, since their view is client-side);
- fires arrows and other projectiles far off target.

Your own maids are immune to Tipsy Mist and wine pools.

### Summon Maid

One of the school's spells: summons a squad of temporary maids to help finish the current fight.

- The count grows with level (2 at level 1, 7 at level 6, and higher still with spell power gear) and they last **10 minutes**. Casting the spell again during that time does not add another squad — it dismisses the existing one early.
- Each maid rolls a model at random and rolls gear and spells by summon level; some ride a **broom** into the air and cast while circling above the summoner.
- They **cannot be interacted with, take no orders and never sit**; they simply fight alongside the summoner. They **drop no gear on death** and vanish when their time runs out. By default creepers also actively avoid them.

Three built-in loadouts (replaceable or extendable by datapack):

| Loadout | Weight | Broom | Role |
|---|---|---|---|
| **warrior** | 40 | Never | Mostly attack spells, heaviest armour |
| **mage** | 35 | Allowed | Attack + negative effect + some defense / movement |
| **support** | 25 | Allowed | Ally heal + ally buff + some attack |

Each loadout has three tiers by summon level — 1–2, 3–4 and 5–6. Higher tiers mean more complete armour, more spells and a higher spell level cap.

> ⚠️ **Summoned maids are currently on the strong side, and later versions will keep adding balance limits** (count, duration, permitted spells may all change).
> To rein them in now, adjust the caps under *Summoned Maid* in the config — the defaults already limit them to level 3, Epic rarity and no learned-only school (i.e. Eldritch) spells. You can also swap the loadouts out with a datapack.

---

## 🧩 Addon Support

The mod ships behaviour classifications for the Iron's Spells addons below. **Installed addons take effect automatically; missing ones cause no errors** (the first row is Iron's Spells itself):

| Mod | Spells classified |
|---|---|
| Monsters & Spellbooks | 107 |
| Iron's Spells 'n Spellbooks (base) | 101 |
| Somake Spells | 66 |
| Apprentice's Codex | 53 |
| Ender's Spells and Stuff: Requiem | 46 |
| Hazen 'N Stuff | 38 |
| Cataclysm: Spellbooks | 34 |
| Magic From The East | 22 |
| Discerning The Eldritch | 21 |
| GTBC's Spellbooks | 16 |
| Peyro's Scythes & Spells | 16 |
| GTBC's Geomancy Plus | 12 |
| Fire's Ender Expansion | 11 |
| SnackPirate's Aeromancy Additions | 10 |
| Dreamless Spells and Spellbooks | 4 |
| Snow Waifu Spell | 1 |

Spells that need several casts (Cataclysm, Requiem, Fire's Ender Expansion and others) have dedicated handling, so maids finish the remaining casts just like players do.

Spells that are not covered cause no errors; maids simply never pick them. Add a classification under *Spell Compatibles* in the config, or classify them yourself with datapack tags.

---

## ⚙️ Configuration

Everything is **server config** (in multiplayer the server's settings win). Edit it from the config screen in the mod list, or in `<world>/serverconfig/winefoxs_spellbooks-server.toml`. The second column below is the key in that file.

### Combat & AI

| Screen name | Key | Default | Notes |
|---|---|---|---|
| Maximum spell attack search range | `maxSpellRange` | 64 | Target search radius (8–192). Larger values cost more scanning |
| Start spell casting range | `startSpellRange` | 15 | Distance at which casting starts (2–64); a spell's own range overrides it |
| Maximum mana multiplier | `maxManaMultiplier` | 1.0 | Overall multiplier on the maid's mana cap |
| Lightning-struck maids regenerate mana 100x faster | `lightningManaRegen` | off | Such maids refill their whole bar every 0.5 seconds |
| Walk speed in spell battle | `spellBattleWalkSpeed` | 1.0 | Speed multiplier while repositioning in combat |
| Max combo delay tick | `maxComboDelayTick` | 10 | Longest wait between two casts; lower chains spells more tightly |
| Show spell names in spell battle | `showChatBubblesInSpellBattle` | on | Shows the current spell in a chat bubble |
| Allow maids to use melee weapons during spell attacks | `meleeAttackInMagicTask` | on | When off, maids only cast and never melee |

### Flying Maids

| Screen name | Key | Default |
|---|---|---|
| Base chance for air force maid | `airForceBaseChance` | 0.1 |
| Air force chance per level | `airForceChancePerLevel` | 0.1 |
| Air force follow height | `airForceFollowHeight` | 4.0 |
| Air force follow radius | `airForceFollowRadius` | 6.0 |
| Air force fly speed | `airForceFlySpeed` | 0.4 |
| Minimum air casting distance | `airForceMinAttackRange` | 5.0 |

### Summoned Maid

| Screen name | Key | Default | Notes |
|---|---|---|---|
| Highest spell level a summoned maid may carry | `summonedMaidMaxSpellLevel` | 3 | Takes the lower of this and the loadout's tier cap; only reduces, never raises. 10 = unlimited |
| Highest spell rarity a summoned maid may carry | `summonedMaidMaxSpellRarity` | EPIC | Spells above this rarity leave the pools entirely. LEGENDARY = unlimited |
| Allow summoned maids to carry learned-only school spells, i.e. Eldritch | `summonedMaidAllowEldritch` | off | These normally require a player to learn them first |
| Creepers flee from summoned maids | `creeperAvoidSummonedMaid` | on | |

### Vulpine Anima Morning Gift

| Screen name | Key | Default | Notes |
|---|---|---|---|
| Enable max-favorability maid morning gift | `vulpineGiftEnabled` | on | Master switch; other sources are unaffected |
| Morning gift search radius | `vulpineGiftRadius` | 8.0 | Searched around the bed for an eligible maid |
| Require a normal full sleep cycle | `vulpineGiftRequireFullSleep` | on | When off, waking early to monsters or thunder also counts |

### Spell Compatibles

Used to add **spells from addons or datapacks** to the behaviour categories. Each of the seven categories has an "Extra … spell ids" list; for the screen names and keys see [Seven Behaviour Categories](#seven-behaviour-categories). Four more entries live here:

| Screen name | Key | Notes |
|---|---|---|
| Extra summon spell ids | `extraSummonSpells` | Marks a spell as a summon; does not affect availability |
| Maid should recast spell ids | `maidShouldRecastSpells` | Maids finish the remaining casts of the spells listed here |
| Maid start casting range every spell id | `extra_spell_casting_range` | Overrides the casting start distance per spell id |
| Extra spell cause effect registry | `extra_spell_caused_effects` | Declares which mob effect a spell applies |

> ⚠️ This section can only **add**, never remove, and changes need a game restart. To stop a maid casting a spell, see the next section.

---

## 📦 Datapack Customisation

### Excluding Spells

Which spells a maid may cast is decided by **spell tags**. To ban one (a friendly-fire heavy AoE, say), remove it from its tag with a datapack.

Create `<world>/datapacks/<your-pack>/pack.mcmeta`:

```json
{ "pack": { "pack_format": 48, "description": "Maid spell blacklist" } }
```

Then `<your-pack>/data/winefoxs_spellbooks/tags/irons_spellbooks/spells/attack_spells.json`:

```json
{
  "values": [],
  "remove": [
    { "id": "irons_spellbooks:fireball" }
  ]
}
```

Run `/reload` and it takes effect: the spell catalog rebuilds at once and loaded maids rescan their spell lists.

**Behaviour tags** (a spell belongs to exactly one; removing it from that one is enough — see [Seven Behaviour Categories](#seven-behaviour-categories) for the mapping):

`attack_spells` · `defense_spells` · `movement_spells` · `support_spells` · `positive_effect_spells` · `negative_effect_spells` · `support_effect_spells`

To find out which tag holds a given spell, open this mod's jar with any archiver and look in `data/winefoxs_spellbooks/tags/irons_spellbooks/spells/` — the seven behaviour tags and the two marker tags are all there as json, holding the mod's default classification. Copy one as the starting point for your own pack.

**Marker tags** — `summon_spells` and `maid_should_recast_spells` do not control availability. Removing an entry there only drops the summon / multi-cast marker; the maid still casts the spell.

> ⚠️ Config extras are merged **after** tags, so do not list the same spell id in `extraAttackSpells` and friends — it would be added straight back.

### Casting Range and Applied Effects

Under `data/<namespace>/magic_maid_spell_data/`:

- `default_casting_range.json` — the distance at which a maid starts casting each spell;
- `default_caused_effect.json` — declares which mob effect a spell applies, so a maid can tell whether to re-apply a buff or cleanse a debuff on a target.

Both take effect on `/reload`; matching entries in the config file take priority over the datapack.

### Summoned Maid Loadouts

Under `data/<namespace>/summoned_maid_loadouts/<name>.json`; minimal skeleton:

```json
{
  "weight": 40,
  "broom_mode": "default",
  "tiers": [
    {
      "conditions": [
        { "condition": "winefoxs_spellbooks:summon_level", "min": 1, "max": 2 }
      ],
      "spell_level_cap": 3,
      "weapon": {
        "rolls": 1,
        "entries": [ { "type": "tag", "expand": true, "name": "modid:weapon_tag" } ]
      },
      "armor": {
        "helmet": {
          "chance": 0.3,
          "pools": [ { "rolls": 1, "entries": [ { "type": "tag", "expand": true, "name": "modid:helmet_tag" } ] } ]
        }
      },
      "spells": {
        "attack": {
          "rolls": { "min": 1, "max": 2 },
          "entries": [ { "type": "tag", "expand": true, "name": "modid:attack_spells" } ]
        }
      }
    }
  ]
}
```

Fields:

- `weight` — selection weight; `0` disables the loadout;
- `broom_mode` — `always` / `never` / `default` (roll the chance);
- `model_filter` (optional) — `{ "mode": "all" | "allowlist" | "denylist", "models": [...] }`;
- `tiers[].conditions` — currently supports the `min` / `max` range of `winefoxs_spellbooks:summon_level`;
- `tiers[].spell_level_cap` — the tier's spell level cap, combined with the config option by taking the lower;
- `tiers[].weapon` / `armor` / `spells` — item and spell pools; `rolls` accepts an integer or `{ "min": n, "max": m }`, and entries reference item tags / spell tags;
- the keys under `spells` are the seven behaviour categories: `attack`, `defense`, `movement`, `support`, `positive_effect`, `negative_effect`, `support_other` (see [Seven Behaviour Categories](#seven-behaviour-categories)).

The built-in `warrior.json`, `mage.json` and `support.json` work as templates.

### Morning Gift Drops

The morning gift's contents come from the loot table `winefoxs_spellbooks:gameplay/maid_morning_gift` (95% for one Vulpine Anima by default); modpacks can override it to hand out something else.

---

## 💻 Commands

Both require permission level 2:

```
/wsb cast <targets> <spell> [level]
/wsb summon [target] [count] [summonLevel] [loadout] [duration] [airForce]
```

- `/wsb cast` — makes the given entities cast a spell, level 1–10. It only skips the pre-cast availability checks and **still consumes mana**, so raise the target's mana cap before testing.
- `/wsb summon` — with no arguments, summons for yourself. `count` 1–10, `summonLevel` 1–10, `loadout` takes a loadout id (random if left out), `duration` is in seconds (`-1` for permanent), and `airForce` forces broom riding on or off.

---

## 🔌 Compatibility & Integration

- **Curios** — spellbooks occupy the spellbook slot defined by Iron's Spells; a maid wears at most one. Accessories is supported as an alternative.
- **Jade** — point at a maid to see her mana; point at a summoned maid to also see her spell count, and hold Shift for the full list.
- **JEI** — every item from this mod has an info page (the "i" in the top right).
- **Advanced Loot Info** — the loot this mod injects into chests, and the maid morning gift, both show their odds in JEI / EMI / REI.
- **Patchouli** — 12 guide entries under the "Winefox's Spellbooks" category of Touhou Little Maid's *Memorizable Gensokyo*, covering mana, casting media, tasks, animations, mod integration, troubleshooting, the school with its items and spells (summoned maids included), datapacks, commands and config.

---

## ❓ FAQ

**She does not cast at all.** Check in order:

1. Is her task set to Magic Attack / Magic Support?
2. Is the spellbook in an **accessory slot**, and are its spell slots inscribed?
3. Does the spell belong to a behaviour tag? **Spells outside every behaviour category are ignored.**
4. Is there enough mana, and is the cooldown over? A "Spell reloading" bubble means one of the two.
5. Is the enemy beyond *Maximum spell attack search range*, or has she not yet closed to *Start spell casting range*?

**Her AoE spells keep hitting allies.** Remove that spell from its behaviour tag with a [datapack](#excluding-spells) and run `/reload`.

**My config change did nothing.** Changes under *Spell Compatibles* need a game restart; datapack-side changes only need `/reload`.

**Why won't maids cast Summon Maid?** By design — that spell is reserved for players (and `/wsb summon`); a maid will not use it to summon another squad of maids.

**My support maid never heals anyone.** Magic Support only acts on same-team targets, and she also needs spells in the matching categories (Ally Heal / Ally Buff) before she switches to support behaviour.

---

## 🛠️ Requirements & Versions

| Item | Version |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge |
| Java | 21+ |
| [Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid) | 1.5.0+ |
| [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) | 3.14.0+ |
| GeckoLib / Curios | Installed with the dependencies |

Casting animations only work on maids using a **GeckoLib model**; other models cast normally but play no animation.

---

## 📦 Download & Installation

- [Modrinth](https://modrinth.com/mod/winefoxs_spellbooks)
- [CurseForge](https://www.curseforge.com/projects/1379815)

Drop the jar into `mods`; no further setup is needed.

---

## 🧑‍💻 Development & Contributing

Issues and pull requests are welcome. Before changing code in this repository, read [DEVELOPMENT.md](DEVELOPMENT.md) — it covers the architecture, the spell classification system, a full breakdown of the maid combat AI, and the steps for "add a new spell / support a new addon".

The mod also exposes an API for other developers: `RegisterSpellTypeEvent` (add or remove spell categories), `MaidSpellActionChooseEvent` (adjust category weights), `MaidSpellChooseEvent` (pick the spell directly) and `MaidSpellBookEvent` (observe spellbook equipment changes).

This mod only extends the two mods above and will not support the casting frameworks of other spell / combat mods — but contributions that classify spells for more Iron's Spells **addons** are welcome.

---

## 📝 License

Released under the [MIT License](LICENSE). You are free to use, modify and distribute it, provided you retain the original copyright notice.
