package com.artillexstudios.axgraves;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axgraves.commands.CommandManager;
import com.artillexstudios.axgraves.grave.Grave;
import com.artillexstudios.axgraves.grave.GravePlaceholders;
import com.artillexstudios.axgraves.grave.SpawnedGraves;
import com.artillexstudios.axgraves.listeners.DeathListener;
import com.artillexstudios.axgraves.listeners.PlayerInteractListener;
import com.artillexstudios.axgraves.schedulers.SaveGraves;
import com.artillexstudios.axgraves.schedulers.TickGraves;
import com.artillexstudios.axgraves.utils.UpdateNotifier;
import org.bstats.bukkit.Metrics;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AxGraves extends AxPlugin {
    private static AxPlugin instance;
    public static Config CONFIG;
    public static Config LANG;
    public static MessageUtils MESSAGEUTILS;
    public static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static AxMetrics metrics;

    public static AxPlugin getInstance() {
        return instance;
    }

    public void enable() {
        instance = this;

        // Initialize bStats metrics
        new Metrics(this, 20332);

        // Load configuration files
        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "messages.yml"), getResource("messages.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        // Log server version information for debugging
        getLogger().info("Server version: " + getServer().getVersion());
        getLogger().info("Bukkit version: " + getServer().getBukkitVersion());
        getLogger().info("Initializing AxGraves v" + getDescription().getVersion());

        // Verify NMS handlers are properly initialized
        try {
            NMSHandlers.getNmsHandler();
            getLogger().info("NMS handlers initialized successfully");
        } catch (Throwable e) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("═══════════════════════════════════════════════════════════════").append(System.lineSeparator());
            errorMsg.append("CRITICAL ERROR: Failed to initialize NMS handlers!").append(System.lineSeparator());
            errorMsg.append("This usually indicates compatibility issues with your Minecraft version.").append(System.lineSeparator());
            errorMsg.append("Current server version: ").append(getServer().getVersion()).append(System.lineSeparator());
            errorMsg.append("Please ensure you are using a compatible version of AxGraves.").append(System.lineSeparator());
            errorMsg.append("Error details: ").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append(System.lineSeparator());
            errorMsg.append("═══════════════════════════════════════════════════════════════");
            getLogger().severe(errorMsg.toString());
            e.printStackTrace();
            getLogger().severe("AxGraves will continue to load, but grave functionality may be limited.");
            getLogger().severe("NOTE: Graves will still function (inventory, XP) but visual elements (holograms, armor stands) may not work.");
        }

        // Register event listeners
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

        // Load commands
        CommandManager.load();

        // Register placeholders
        GravePlaceholders.register();

        // Load saved graves if enabled
        if (CONFIG.getBoolean("save-graves.enabled", true)) {
            try {
                SpawnedGraves.loadFromFile();
            } catch (Exception e) {
                getLogger().warning("Failed to load saved graves: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Start schedulers
        TickGraves.start();
        SaveGraves.start();

        // Start metrics
        metrics = new AxMetrics(this, 20);
        metrics.start();

        // Check for updates
        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5076);

        getLogger().info("AxGraves successfully enabled!");
    }

    public void disable() {
        getLogger().info("Disabling AxGraves...");

        // Stop metrics
        if (metrics != null) {
            try {
                metrics.cancel();
            } catch (Exception e) {
                getLogger().warning("Error stopping metrics: " + e.getMessage());
            }
        }

        // Stop schedulers
        try {
            TickGraves.stop();
            SaveGraves.stop();
        } catch (Exception e) {
            getLogger().warning("Error stopping schedulers: " + e.getMessage());
            e.printStackTrace();
        }

        // Clean up graves
        try {
            for (Grave grave : SpawnedGraves.getGraves()) {
                if (!CONFIG.getBoolean("save-graves.enabled", true)) grave.remove();
                
                // Safely remove entities and holograms
                try {
                    if (grave.getEntity() != null) grave.getEntity().remove();
                } catch (Exception e) {
                    getLogger().warning("Error removing grave entity: " + e.getMessage());
                }
                
                try {
                    if (grave.getHologram() != null) grave.getHologram().remove();
                } catch (Exception e) {
                    getLogger().warning("Error removing grave hologram: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            getLogger().severe("Error cleaning up graves: " + e.getMessage());
            e.printStackTrace();
        }

        // Save graves if enabled
        if (CONFIG.getBoolean("save-graves.enabled", true)) {
            try {
                SpawnedGraves.saveToFile();
                getLogger().info("Saved graves to file");
            } catch (Exception e) {
                getLogger().warning("Failed to save graves: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Shutdown executor
        try {
            EXECUTOR.shutdownNow();
        } catch (Exception e) {
            getLogger().warning("Error shutting down executor: " + e.getMessage());
        }

        getLogger().info("AxGraves disabled successfully");
    }

    public void updateFlags() {
        FeatureFlags.USE_LEGACY_HEX_FORMATTER.set(true);
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        FeatureFlags.HOLOGRAM_UPDATE_TICKS.set(5L);
        FeatureFlags.ENABLE_PACKET_LISTENERS.set(true);
    }
}
