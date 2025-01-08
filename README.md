# SellWand Plugin

A Minecraft plugin that adds magical wands capable of selling chest contents instantly with customizable multipliers.

## Dependencies
- PlotSquared
- EconomyShopGUI
- Vault

## Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/sellwand help` | Shows help menu | `sellwand.command.use` |
| `/sellwand info` | Shows plugin information | `sellwand.command.use` |
| `/sellwand give <player> <multiplier> [uses]` | Gives a SellWand to a player | `sellwand.command.give` |
| `/sellwand reload` | Reloads plugin configuration | `sellwand.command.reload` |

## Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `sellwand.*` | Grants all SellWand permissions | op |
| `sellwand.admin` | Grants access to admin commands | op |
| `sellwand.command.use` | Allows using basic commands | true |
| `sellwand.command.give` | Allows giving SellWands to players | op |
| `sellwand.command.reload` | Allows reloading the plugin | op |
| `sellwand.use.wand` | Allows using SellWands | true |
| `sellwand.use.bypass` | Bypasses plot restrictions | op |

## Configuration

### config.yml
```yaml
prefix:
  enabled: true
  format: "&8[&6SellWand&8] &7"

messages:
  no-permission: "&cYou don't have permission for this!"
  not-on-plot: "&cThis chest is not on a plot!"
  not-trusted: "&cYou don't have permission to sell items here!"
  wand-received: "&aYou received a SellWand!"
  config-reloaded: "&aConfiguration reloaded!"
  sold-items: "&aSold for {amount}$!"
  empty-chest: "&cNo sellable items in this chest!"
  wand-break: "&cYour SellWand has broken!"
  wand-given: "&aGave a SellWand to {player} with {multiplier}x multiplier and {uses} uses!"

wand:
  material: BLAZE_ROD
  name: "&6SellWand &8(&e{multiplier}x&8) &7[{uses} uses]"
  custom_model_data:
    "1.0": 1001  # Model data for 1x multiplier
    "2.0": 1002  # Model data for 2x multiplier
    "3.0": 1003  # Model data for 3x multiplier
    "4.0": 1004  # Model data for 4x multiplier
    "5.0": 1005  # Model data for 5x multiplier
  lore:
    - "&7Right-click on a chest to"
    - "&7sell its contents!"
    - "&8» &eSell Multiplier: &6{multiplier}x"
    - "&8» &7Uses Remaining: &e{uses}"
  default-uses: 100

selling:
  price-multiplier: 1.0
  success-sound: ENTITY_PLAYER_LEVELUP
  success-particle: VILLAGER_HAPPY
  max-price: 1000000.0
  max-price-per-item: 100000.0
  allow-single-chests: false
  error-sound: ENTITY_VILLAGER_NO
```

## Features
- Sell chest contents instantly with right-click
- Customizable multipliers per wand
- Limited or infinite uses
- Plot protection integration
- Price limits for safety
- Sound and particle effects
- Double chest support
- Custom model data support for different wand tiers
- Automatic model data assignment based on multiplier

## Custom Model Data
The plugin supports different wand appearances based on the multiplier using Minecraft's custom model data system. The wand's appearance changes automatically when the multiplier reaches certain thresholds:
- 1.0x multiplier → model data 1001
- 2.0x multiplier → model data 1002
- 3.0x multiplier → model data 1003
- 4.0x multiplier → model data 1004
- 5.0x multiplier → model data 1005

**Note:** No example resource pack is provided yet. You'll need to create your own resource pack with the corresponding model data values to see different wand appearances.

## Placeholders
- `{player}` - Player name
- `{amount}` - Sale amount
- `{multiplier}` - Wand's multiplier
- `{uses}` - Remaining uses
- `{max}` - Maximum price limit

## Notes
- Prices are pulled from EconomyShopGUI configuration
- Requires plot ownership or trust to use (unless bypassed)
- Double chests are required by default (configurable)
- All messages and sounds are customizable
- Color codes use `&` for formatting
- Custom model data values can be configured in config.yml
- Multipliers are rounded down to the nearest whole number for model data selection

## Support
For support, please visit:
- GitHub: https://github.com/f1shyondrugs/sellwand
- Discord: f1shyondrugs312
- mail: info@f1shy312.com