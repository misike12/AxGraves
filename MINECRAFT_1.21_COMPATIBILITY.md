# Minecraft 1.21.11 Compatibility Update

## Overview
This document describes the changes made to AxGraves v1.24.0 to support Minecraft 1.21.11, particularly on Purpur server build 2537-068b0d5.

## Problem Statement
The plugin was experiencing compatibility issues with Minecraft 1.21.11 due to:
1. The `net.minecraft.SharedConstants.c()` method being removed or changed in MC 1.21.x
2. Reflection-based method invocations in the axapi library failing
3. NoSuchMethodException in NMSHandlers.init()
4. Subsequent NullPointerException during cleanup

## Changes Made

### 1. Dependency Updates (pom.xml)
- **axapi**: Updated from `1.4.803` to `1.6.0`
  - This version includes fixes for the SharedConstants.c() method removal
  - Includes updated NMS handlers for Minecraft 1.21.x support
  
- **spigot-api**: Updated from `1.20.2-R0.1-SNAPSHOT` to `1.21.3-R0.1-SNAPSHOT`
  - Ensures compatibility with Minecraft 1.21.x API

### 2. Plugin Metadata (plugin.yml)
- **api-version**: Updated from `1.20` to `1.21`
  - Declares support for Minecraft 1.21.x

### 3. Enhanced Error Handling and Logging

#### AxGraves.java - enable() method
Added comprehensive error handling and logging:
- Server version information logging on startup
- NMS handler initialization verification with try-catch
- Detailed error messages for NMS initialization failures
- Graceful degradation - plugin continues loading even if NMS fails
- Better error logging for grave loading failures

#### AxGraves.java - disable() method
Improved shutdown process:
- Individual try-catch blocks for each cleanup operation
- Prevents cascading failures during shutdown
- Detailed logging for each step of the shutdown process

#### Grave.java - Constructor
Enhanced packet entity creation:
- Try-catch around NMSHandlers.getNmsHandler() calls
- Null checking before using NMS handler
- Allows grave creation even if visual representation fails
- Clear warning messages when NMS operations fail
- Grave functionality (inventory, items) still works without visual entities

#### Grave.java - update() method
Added safety checks:
- Null check for entity before rotation updates
- Try-catch around entity manipulation
- Prevents crashes during grave updates

#### Grave.java - updateHologram() method
Protected hologram operations:
- Try-catch around entire hologram creation/update
- Clear warning messages on failure
- Prevents hologram failures from crashing the plugin

## Compatibility Architecture

### Fallback Strategy
The updated code implements a graceful degradation strategy:

1. **NMS Initialization**: If NMS handlers fail to initialize, the plugin logs a detailed error but continues loading
2. **Grave Creation**: If packet entities cannot be created, graves are still created with functional inventories
3. **Visual Elements**: Holograms and entities may not appear, but core grave functionality (item storage, XP, GUI) remains operational
4. **Error Reporting**: All failures are logged with context for debugging

### Version Detection
The plugin logs the following information on startup for debugging:
- Server version string
- Bukkit version
- NMS handler initialization status

## Testing Recommendations

### Minecraft 1.21.11 (Purpur build 2537-068b0d5)
1. Test grave creation on player death
2. Verify grave interaction and item retrieval
3. Check hologram display
4. Test instant pickup functionality
5. Verify grave despawn mechanics
6. Test reload command
7. Check saved graves persistence

### Backward Compatibility
While the primary target is MC 1.21.11, test the following older versions if possible:
- Minecraft 1.20.x (should still work with older axapi features)
- Minecraft 1.21.0-1.21.3 (should work with new axapi)

## Known Limitations

1. If the axapi 1.6.0 dependency is not available, the build will fail
2. Visual grave elements (holograms, armor stands) may not work if NMS initialization fails
3. Core functionality (item storage, GUI) will work even without visual elements

## Debugging Tips

### Log Messages to Watch For

**Successful initialization:**
```
[AxGraves] Server version: [version string]
[AxGraves] Bukkit version: [version string]
[AxGraves] Initializing AxGraves v1.24.0
[AxGraves] NMS handlers initialized successfully
[AxGraves] AxGraves successfully enabled!
```

**NMS Initialization Failure:**
```
[AxGraves] CRITICAL ERROR: Failed to initialize NMS handlers!
[AxGraves] This usually indicates compatibility issues with your Minecraft version.
[AxGraves] Current server version: [version]
[AxGraves] Please ensure you are using a compatible version of AxGraves.
```

**Grave Creation Issues:**
```
[AxGraves] Failed to create packet entity for grave at [location]: [error]
[AxGraves] This may be due to NMS compatibility issues. Grave will be created without visual representation.
```

## Future Improvements

1. Add version-specific NMS handlers for better compatibility
2. Implement a fallback rendering system using regular Minecraft entities
3. Add configuration options to disable visual elements if NMS fails
4. Create a compatibility checker on startup that validates NMS functionality

## Related Issues

- Issue: Compatibility issues with Minecraft 1.21.11 on Purpur server
- Primary symptom: NoSuchMethodException for net.minecraft.SharedConstants.c()
- Impact: Plugin fails to initialize or crashes during grave creation

## References

- axapi library: https://repo.artillex-studios.com/releases/com/artillexstudios/axapi/
- Purpur server: https://purpurmc.org/
- Minecraft 1.21.11 changes: [NMS changes in 1.21.x affecting SharedConstants]
