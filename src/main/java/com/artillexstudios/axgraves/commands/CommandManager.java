package com.artillexstudios.axgraves.commands;

import com.artillexstudios.axgraves.AxGraves;
import com.artillexstudios.axgraves.utils.CommandMessages;
import org.bukkit.command.PluginCommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.orphan.Orphans;

import java.util.List;
import java.util.Locale;

import static com.artillexstudios.axgraves.AxGraves.CONFIG;

public class CommandManager {
    private static BukkitCommandHandler handler = null;
    private static boolean fallbackMode = false;

    public static void load() {
        fallbackMode = false;

        try {
            handler = BukkitCommandHandler.create(AxGraves.getInstance());
        } catch (Throwable throwable) {
            AxGraves.getInstance().getLogger().warning("Failed to initialize Lamp command handler; falling back to basic Bukkit commands. Reason: " + throwable.getMessage());
            registerFallbackExecutor();
            fallbackMode = true;
            return;
        }

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(Locale.of("en", "US"));

        reload();
    }

    public static void reload() {
        if (fallbackMode) {
            registerFallbackExecutor();
            return;
        }

        handler.unregisterAllCommands();

        List<String> aliases = CONFIG.getStringList("command-aliases");
        if (!aliases.isEmpty()) {
            handler.register(Orphans.path(aliases.toArray(String[]::new)).handler(new Commands()));
        }

        try {
            handler.registerBrigadier();
        } catch (Throwable throwable) {
            AxGraves.getInstance().getLogger().warning("Brigadier registration is unavailable on this server version; using Bukkit command handling only. Reason: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    private static void registerFallbackExecutor() {
        PluginCommand command = AxGraves.getInstance().getCommand("axgraves");
        if (command == null) {
            AxGraves.getInstance().getLogger().severe("Unable to register fallback commands because base command 'axgraves' is missing from plugin.yml.");
            return;
        }

        command.setExecutor(FallbackCommandExecutor.INSTANCE);
        command.setTabCompleter(FallbackCommandExecutor.INSTANCE);
    }
}
