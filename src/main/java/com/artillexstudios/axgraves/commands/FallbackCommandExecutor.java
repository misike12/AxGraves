package com.artillexstudios.axgraves.commands;

import com.artillexstudios.axgraves.commands.subcommands.Help;
import com.artillexstudios.axgraves.commands.subcommands.Reload;
import com.artillexstudios.axgraves.commands.subcommands.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.artillexstudios.axgraves.AxGraves.MESSAGEUTILS;

/**
 * Simple Bukkit command executor used when Lamp/Brigadier command
 * registration is unavailable (e.g., on newer server versions).
 */
public enum FallbackCommandExecutor implements CommandExecutor, TabCompleter {
    INSTANCE;

    private static final List<String> SUBCOMMANDS = List.of("help", "reload", "list", "tp");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return handleHelp(sender);
        }

        return switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "help" -> handleHelp(sender);
            case "reload" -> {
                if (hasPermission(sender, "axgraves.reload")) {
                    Reload.INSTANCE.execute(sender);
                }
                yield true;
            }
            case "list" -> {
                if (hasPermission(sender, "axgraves.list")) {
                    com.artillexstudios.axgraves.commands.subcommands.List.INSTANCE.execute(sender);
                }
                yield true;
            }
            case "tp" -> handleTeleport(sender, args);
            default -> {
                MESSAGEUTILS.sendLang(sender, "commands.invalid-command");
                yield true;
            }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sub -> sender.hasPermission("axgraves." + sub))
                    .filter(sub -> sub.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && "tp".equalsIgnoreCase(args[0])) {
            List<String> worlds = Bukkit.getWorlds().stream().map(World::getName).toList();
            return filterStartingWith(worlds, args[1]);
        }

        return Collections.emptyList();
    }

    private boolean handleHelp(CommandSender sender) {
        if (hasPermission(sender, "axgraves.help")) {
            Help.INSTANCE.execute(sender);
        }
        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MESSAGEUTILS.sendLang(sender, "commands.player-only");
            return true;
        }

        if (!hasPermission(sender, "axgraves.tp")) {
            return true;
        }

        if (args.length == 1) {
            Teleport.INSTANCE.execute(player, null, null, null, null);
            return true;
        }

        if (args.length != 5) {
            MESSAGEUTILS.sendLang(sender, "commands.invalid-command");
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            MESSAGEUTILS.sendLang(sender, "commands.invalid-value", java.util.Map.of("%value%", args[1]));
            return true;
        }

        Double x = parseDouble(sender, args[2]);
        Double y = parseDouble(sender, args[3]);
        Double z = parseDouble(sender, args[4]);

        if (x == null || y == null || z == null) {
            return true;
        }

        Teleport.INSTANCE.execute(player, world, x, y, z);
        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        MESSAGEUTILS.sendLang(sender, "commands.no-permission");
        return false;
    }

    private Double parseDouble(CommandSender sender, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            MESSAGEUTILS.sendLang(sender, "commands.invalid-value", java.util.Map.of("%value%", value));
            return null;
        }
    }

    private List<String> filterStartingWith(List<String> source, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ENGLISH);
        return source.stream()
                .filter(entry -> entry.toLowerCase(Locale.ENGLISH).startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
