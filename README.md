# Winefox's Spellbooks

[中文说明](README-zh_CN.md)

**A Minecraft mod that deeply integrates *Touhou Little Maid* with *Iron's Spells 'n Spellbooks*, granting maids powerful magical combat abilities.**

---
## 🌟 Mod Overview
**Winefox's Spellbooks** is a mod designed to integrate *Touhou Little Maid* and *Iron's Spells 'n Spellbooks*, allowing maids to wield powerful spells in combat. Maids can equip spellbooks as accessories and use a wide variety of spells, including attack, defense, summoning, movement, and support types.

This mod introduces a new "Magic Attack Mode" and supports the GeckoLib animation system, allowing maids to display casting animations (based on animations from *Iron's Spells 'n Spellbooks*). The mod is named after its GeckoLib-based model, "Winefox."

---
## ✨ Key Features
- **Spellbooks as Accessories**: Spellbooks can be placed in the maid's accessory slot (they won't render visually), serving as a medium for spellcasting.
- **Magic Attack Mode**: When in this mode, maids can use spells from a spellbook or enchanted gear in their main hand or accessory slot.
- **Magic Support Mode**: When in this mode, maids will use magic to support allied mobs (include owner and maids).
- **Mana System Support**:
    - Casting spells consumes mana.
    - Max Mana is affected by "Max Mana" attribute.
    - After a maid is struck by lightning, they gain infinite mana (100% per 0.5 seconds), but still affected by max mana.
- **Diverse Spell Types**:
    - Attack
    - Defense
    - Summoning
    - Movement
    - Support
    - Positive buffs
    - Negative buffs
    - ⚠️ Note: Some spells may accidentally damage friendly entities.
- **Regeneration Potion Use**: If no support spells are available in Magic Attack Mode, maids will use regeneration potions to heal.
- **GeckoLib Animation Support**:
    - Maids using GeckoLib models display casting animations.
    - Animations are adapted from *Iron's Spells 'n Spellbooks*. Some animation issues (e.g., bone misalignment) may exist and will be fixed in future updates.

---
## 🛠️ Required Mods
- **Dependencies**:
    - [Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)
    - [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks)
- **Compatibility**:
    - Only supports **GeckoLib**-based maids. Other models do not support animations.
    - Compatible with Minecraft 1.21.1, built on **NeoForge**.

---
## 🧪 Usage Instructions
1. Install the required mods (Touhou Little Maid + Iron's Spells 'n Spellbooks).
2. Install this mod.
3. Place a spellbook in the maid's accessory slot or enchant gear with spells.
4. Switch the maid to "Magic Attack Mode."
5. Strike the maid with lightning to enable infinite mana.

---
## 🚫 Excluding Spells

Which spells a maid may cast is decided by spell tags. The `[spell_compat]` options in `winefoxs_spellbooks-server.toml` can only **add** spells, so to keep maids away from a spell (a friendly-fire heavy AoE, say) remove it from its tag with a data pack.

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

Run `/reload` — the maid spell catalog rebuilds right away, and any spell that belongs to none of the behaviour tags is skipped when the maid reads her spellbook.

**Behaviour tags** (a spell belongs to exactly one; removing it from that one is enough):

`attack_spells` · `defense_spells` · `movement_spells` · `support_spells` · `positive_effect_spells` · `negative_effect_spells` · `support_effect_spells`

To find out which tag holds a given spell, open this mod's jar with any archiver and look in `data/winefoxs_spellbooks/tags/irons_spellbooks/spells/` — the seven behaviour tags and the two marker tags are all there as json, holding the mod's default classification. Copy one as the starting point for your own pack.

**Marker tags** — `summon_spells` and `maid_should_recast_spells` do not control availability. Removing an entry there only drops the summon / recast marker; the maid still casts the spell.

> ⚠️ Config extras are merged **after** tags, so do not list the same spell id in `extraAttackSpells` and friends — it would be added straight back.

---
## 🛠️ Development & Contributions
This mod is open to developers and mod enthusiasts. If you find bugs, have optimization suggestions, or want to add new features, please submit an issue or pull request.

This mod only adds features to the above two mods and does **not** support additional spell or combat mods.

---
## 📦 Download & Installation
Download the latest version of the mod from the [Modrinth page](https://modrinth.com/mod/winefoxs_spellbooks), and follow standard installation procedures.

---
## 📝 License
This mod is released under the [MIT License](LICENSE). You are free to use, modify, and distribute this mod, provided you retain the original copyright notice.
