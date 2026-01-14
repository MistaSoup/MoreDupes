# MoreDupes

A Minecraft plugin that adds multiple item duplication methods to your server. Compatible with Folia and Paper 1.21+.

## Features

### 🔧 Piston Dupe
Push shulker boxes with pistons into solid blocks to duplicate them. Configurable player distance requirements and cooldowns.

### 🌵 Cactus Dupe
Drop shulker boxes onto cacti to duplicate them instead of destroying them.

### 🖼️ Frame Dupe
Remove items from item frames (normal or glow) and they'll duplicate as they pop out.

### 🌀 Portal Dupe
Throw any item through a nether portal and it has a chance to duplicate. Each item can only dupe once.

### 🛤️ Minecart Dupe
Break chest or hopper minecarts to duplicate all items inside.

### 📦 Dropper/Dispenser Dupe
When droppers or dispensers activate, the dispensed item has a chance to duplicate. Items spawn at the front of the block.

## Installation

1. Download the latest `MoreDupes-1.0.0.jar` from the releases page
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure settings in `plugins/MoreDupes/config.yml`

## Building from Source

Requires Java 21 and Maven:

```bash
git clone https://github.com/yourusername/MoreDupes.git
cd MoreDupes
mvn clean package
```

The compiled jar will be in `target/MoreDupes-1.0.0.jar`

## Configuration

Each dupe method can be individually enabled/disabled and configured. See `config.yml` for all options:

- **Enable/disable** each dupe method
- **Probability settings** - control how often dupes occur (0-100%)
- **Player distance requirements** - require players to be nearby for some methods
- **Cooldowns** - prevent spam duping
- **Verbose logging** - detailed console output for debugging

### Example Config Snippet

```yaml
piston-dupe:
  enabled: true
  adjacent-block-protection: false
  require-player-nearby: false
  player-distance: 4.0
  dupe-probability: 100.0
  verbose-logging: true

minecart-dupe:
  enabled: true
  dupe-probability: 100.0
  verbose-logging: true
```

## Commands

- `/moredupesreload` (aliases: `/mdr`, `/mdreload`) - Reload the plugin configuration
  - Permission: `moredupes.reload` (default: op)

## Compatibility

- **Server Software**: Folia, Paper, Purpur (1.21+)
- **Java Version**: 21
- **API Version**: 1.21

## Author

Created by **MistaSoup**

## Support

Found a bug or have a suggestion? Open an issue on the [GitHub Issues](https://github.com/MistaSoup/MoreDupes/issues) page.

## License

This project is provided as-is for educational and server customization purposes.
