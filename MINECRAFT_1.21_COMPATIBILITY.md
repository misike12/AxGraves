# Minecraft 1.21.11 Compatibility Update

## Overview
This document describes the changes made to AxGraves v1.24.0 to support Minecraft 1.21.11, particularly on Purpur server build 2537-068b0d5.

## Problem Statement
The plugin was experiencing compatibility issues with Minecraft 1.21.11 due to:
1. The axapi library version 1.4.813 tries to access obfuscated field 'c' in `net.minecraft.world.entity.Entity`
2. This field does not exist in Minecraft 1.21.11, causing a `NoSuchFieldException`
3. The exception occurs during static initialization of `ServerWrapper` class in axapi
4. This results in an `ExceptionInInitializerError` which was not being caught by `catch (Exception e)`
5. The uncaught error caused the plugin to crash and graves to not be created

## Root Cause Analysis
The error trace shows:
```
[ERROR]: Failed to find field c of class net.minecraft.world.entity.Entity!
java.lang.NoSuchFieldException: c
	at AxGraves-1.24.0.jar//com.artillexstudios.axgraves.libs.axapi.nms.v1_21_R7.wrapper.ServerWrapper.<clinit>(ServerWrapper.java:22)
Caused by: java.lang.ExceptionInInitializerError
Caused by: java.lang.RuntimeException: java.lang.NoSuchFieldException: c
```

The `<clinit>` indicates this is a static initializer block failure. The problem is:
- `ExceptionInInitializerError` extends `Error`, not `Exception`
- Our existing `catch (Exception e)` blocks did not catch `Error` subclasses
- This caused the error to propagate and crash the plugin

## Changes Made

### 1. Exception Handling Improvements

#### AxGraves.java - enable() method
Changed exception handling from `Exception` to `Throwable`:
- Now catches both `Exception` and `Error` (including `ExceptionInInitializerError`)
- Added error class name to log messages for better debugging
- Added explicit message about fallback functionality
- Server version information logging on startup
- NMS handler initialization verification with try-catch
- Detailed error messages for NMS initialization failures
- Graceful degradation - plugin continues loading even if NMS fails
- Better error logging for grave loading failures

#### Grave.java - Constructor
Enhanced packet entity creation:
- Changed `catch (Exception e)` to `catch (Throwable e)`
- Now catches `ExceptionInInitializerError` from NMS handler initialization
- Added error class name to log messages
- Removed stack trace print to reduce log spam
- Added explicit message about core functionality still working
- Allows grave creation even if visual representation fails

#### Grave.java - updateHologram() method
Protected hologram operations:
- Changed `catch (Exception e)` to `catch (Throwable e)`
- Added error class name to log messages
- Prevents hologram failures from crashing the plugin

#### DeathListener.java - onDeath() method
Protected death event handling:
- Changed `catch (Exception e)` to `catch (Throwable e)`
- Added error class name to log messages
- Prevents grave creation failures from affecting death mechanics
- Falls back to vanilla death handling on errors

### 2. Dependency Status (pom.xml)
Currently using:
- **axapi**: 1.4.813 (latest available version)
  - Note: This version does not fully support Minecraft 1.21.11
  - The obfuscated field names (like 'c') have changed in MC 1.21.11
  - Version 1.6.0 with full MC 1.21.11 compatibility is not yet published
  
- **spigot-api**: 1.21.3-R0.1-SNAPSHOT
  - Ensures compatibility with Minecraft 1.21.x API

### 3. Plugin Metadata (plugin.yml)
- **api-version**: 1.21
  - Declares support for Minecraft 1.21.x

## Compatibility Architecture

### Why Catch Throwable Instead of Exception?

In Java, the exception hierarchy is:
```
Throwable
├── Exception (checked and unchecked exceptions)
│   ├── RuntimeException
│   └── ...
└── Error (serious problems that should not be caught normally)
    ├── ExceptionInInitializerError (thrown when static initialization fails)
    ├── NoClassDefFoundError
    └── ...
```

The issue we're addressing:
1. When `NMSHandlers.getNmsHandler()` is called, it triggers class loading
2. The `ServerWrapper` class has a static initializer that tries to access field 'c'
3. This field doesn't exist in MC 1.21.11, causing `NoSuchFieldException`
4. The exception is wrapped in `ExceptionInInitializerError` (extends `Error`)
5. `catch (Exception e)` does NOT catch `Error` subclasses
6. The error propagates and crashes the plugin

By changing to `catch (Throwable e)`, we catch both `Exception` and `Error`, allowing graceful degradation.

**Important**: We only catch `Throwable` in specific, controlled locations where we know:
- The error is related to NMS compatibility
- We can provide fallback functionality
- The error won't mask serious JVM issues

### Fallback Strategy
The updated code implements a graceful degradation strategy:

1. **NMS Initialization**: If NMS handlers fail to initialize, the plugin logs a detailed error but continues loading
2. **Grave Creation**: If packet entities cannot be created, graves are still created with functional inventories
3. **Visual Elements**: Holograms and armor stand entities may not appear, but core grave functionality (item storage, XP, GUI) remains operational
4. **Error Reporting**: All failures are logged with context (including error class name) for debugging

### What Works Without NMS
Even if NMS initialization fails:
- ✅ Graves are created on player death
- ✅ Items are stored in grave inventory
- ✅ Experience points are stored
- ✅ GUI opens when interacting with grave location
- ✅ Instant pickup (shift-click) works
- ✅ Auto-equip armor works
- ✅ Grave persistence and saving works
- ❌ Visual armor stand with player head (not visible)
- ❌ Holograms (not visible)
- ❌ Packet entity interactions (must use block interaction)
## Known Limitations

1. **Visual Elements May Not Work**: If axapi 1.4.813 NMS initialization fails on MC 1.21.11:
   - Armor stand entities with player heads won't be visible
   - Holograms won't be displayed
   - Players will need to interact with the grave location directly
   
2. **Dependency Availability**: The ideal fix would be to update to axapi 1.6.0+, but:
   - Version 1.6.0 with full MC 1.21.11 compatibility is not yet published
   - The plugin uses version 1.4.813 which is the latest available
   - Future updates should use newer axapi versions when available

3. **Workaround Nature**: This fix is a workaround that allows the plugin to function with degraded visual features rather than crashing completely

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

**NMS Initialization Failure (Now Handled Gracefully):**
```
[AxGraves] ═══════════════════════════════════════════════════════════════
[AxGraves] CRITICAL ERROR: Failed to initialize NMS handlers!
[AxGraves] This usually indicates compatibility issues with your Minecraft version.
[AxGraves] Current server version: [version]
[AxGraves] Please ensure you are using a compatible version of AxGraves.
[AxGraves] Error details: ExceptionInInitializerError: [message]
[AxGraves] ═══════════════════════════════════════════════════════════════
[AxGraves] AxGraves will continue to load, but grave functionality may be limited.
[AxGraves] NOTE: Graves will still function (inventory, XP) but visual elements (holograms, armor stands) may not work.
```

**Grave Creation Issues (Now Handled Gracefully):**
```
[AxGraves] Failed to create packet entity for grave at [location]
[AxGraves] Error: ExceptionInInitializerError: [message]
[AxGraves] This may be due to NMS compatibility issues. Grave will be created without visual representation.
[AxGraves] Core functionality (inventory, XP, GUI) will still work.
```

**Hologram Issues (Now Handled Gracefully):**
```
[AxGraves] Failed to update hologram for grave at [location]: ExceptionInInitializerError: [message]
[AxGraves] This may be due to NMS compatibility issues.
```

### How to Verify the Fix Works

1. **Install the plugin** on a Minecraft 1.21.11 server
2. **Check startup logs** - Plugin should load without crashing, even if NMS fails
3. **Trigger a player death** - Should create a grave without crashing
4. **Check grave functionality**:
   - Grave inventory should be accessible (right-click grave location)
   - Items should be retrievable
   - XP should be restored
   - Instant pickup should work (shift + right-click)
5. **Visual elements may not work**:
   - Armor stand with player head may not appear
   - Hologram may not appear
   - This is expected with axapi 1.4.813 on MC 1.21.11

## Future Improvements

1. **Update to axapi 1.6.0+**: When a newer version of axapi with MC 1.21.11 support is published, update the dependency
2. **Alternative Rendering**: Consider implementing a fallback rendering system using regular Bukkit entities instead of packet entities
3. **Configuration Option**: Add a config option to disable NMS features if they're not working
4. **Version Detection**: Add startup checks to warn about known incompatible Minecraft versions

## Related Issues

- Issue: Compatibility issues with Minecraft 1.21.11 on Purpur server
- Primary symptom: NoSuchMethodException for net.minecraft.SharedConstants.c()
- Impact: Plugin fails to initialize or crashes during grave creation

## References

- axapi library: https://repo.artillex-studios.com/releases/com/artillexstudios/axapi/
- Purpur server: https://purpurmc.org/
- Minecraft 1.21.11 changes: [NMS changes in 1.21.x affecting SharedConstants]
