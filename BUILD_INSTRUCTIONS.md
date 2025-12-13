# Building AxGraves for Minecraft 1.21.11

## Prerequisites

1. Java 21 (as specified in pom.xml)
2. Maven 3.6+
3. Access to the following repositories:
   - https://repo.artillex-studios.com/releases/ (for axapi)
   - https://hub.spigotmc.org/nexus/content/repositories/snapshots/ (for spigot-api)

## Build Instructions

### Standard Build

```bash
mvn clean package
```

This will:
1. Download dependencies (axapi 1.6.0, spigot-api 1.21.3)
2. Compile the source code
3. Run the shade plugin to relocate dependencies
4. Create the final JAR in `target/AxGraves-1.24.0.jar`

### Build Without Tests

```bash
mvn clean package -DskipTests
```

### Build Offline (if dependencies are cached)

```bash
mvn clean package -o
```

## Dependency Notes

### axapi 1.6.0
- **Purpose**: Core API library for packet entities, holograms, and NMS handling
- **Critical for**: Minecraft 1.21.11 compatibility
- **Shaded into**: `com.artillexstudios.axgraves.libs.axapi`
- **Repository**: https://repo.artillex-studios.com/releases/

If axapi 1.6.0 is not yet available, you may need to:
1. Contact Artillex Studios for the updated version
2. Or temporarily use version 1.4.813 (available at https://repo.artillex-studios.com/#/releases/com/artillexstudios/axapi/api/axapi/1.4.813)
   - Note: This will not fix MC 1.21.11 compatibility

### spigot-api 1.21.3-R0.1-SNAPSHOT
- **Purpose**: Minecraft server API
- **Scope**: Provided (not included in final JAR)
- **Repository**: https://hub.spigotmc.org/nexus/content/repositories/snapshots/

## Troubleshooting

### Issue: "Could not resolve dependencies"
**Cause**: Maven cannot access the required repositories

**Solutions**:
1. Check your internet connection
2. Verify repository URLs are accessible
3. Check Maven settings.xml for proxy configuration
4. Try clearing Maven cache: `rm -rf ~/.m2/repository/com/artillexstudios/axapi`

### Issue: "axapi version 1.6.0 not found"
**Cause**: The axapi version may not be released yet

**Solutions**:
1. Check with Artillex Studios if 1.6.0 is available
2. Try alternative versions: 1.4.813 (confirmed available), 1.5.x, 1.6.x series
3. Check the repository for available versions:
   - Browse: https://repo.artillex-studios.com/#/releases/com/artillexstudios/axapi/api/axapi/

### Issue: "spigot-api 1.21.3 not found"  
**Cause**: The specific version may not be available yet

**Solutions**:
1. Try 1.21-R0.1-SNAPSHOT
2. Try 1.21.1-R0.1-SNAPSHOT
3. Check SpigotMC for available versions

## Installation

After building successfully:

1. Stop your Minecraft server
2. Copy `target/AxGraves-1.24.0.jar` to your server's `plugins/` directory
3. Start your server
4. Check logs for successful initialization:
   ```
   [AxGraves] NMS handlers initialized successfully
   [AxGraves] AxGraves successfully enabled!
   ```

## Compatibility Testing

After installation on Minecraft 1.21.11:

1. **Basic Functionality**
   - [ ] Plugin loads without errors
   - [ ] Player death creates a grave
   - [ ] Grave displays hologram
   - [ ] Grave displays player head
   - [ ] Clicking grave opens inventory

2. **Item Management**
   - [ ] Items are stored in grave
   - [ ] Items can be retrieved from grave GUI
   - [ ] Instant pickup (shift-click) works
   - [ ] Auto-equip armor works

3. **XP Storage**
   - [ ] XP is stored in grave
   - [ ] XP is restored when opening grave

4. **Persistence**
   - [ ] Graves survive server restart (if enabled)
   - [ ] Saved graves reload correctly

5. **Cleanup**
   - [ ] Graves despawn after timeout
   - [ ] Empty graves despawn (if enabled)
   - [ ] Items drop on grave removal (if enabled)

## Reporting Issues

If you encounter issues with Minecraft 1.21.11 compatibility:

1. Check the server logs for error messages
2. Look for NMS-related errors
3. Note your exact server version (Purpur build number)
4. Report to: https://github.com/Artillex-Studios/Issues

Include in your report:
- Server version and type (Spigot/Paper/Purpur)
- AxGraves version
- Full error stack trace
- Steps to reproduce
