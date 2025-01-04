package com.f1shy312.sellWand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SellWandTabCompleter implements TabCompleter {
    private final main plugin;

    public SellWandTabCompleter(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        // Check base command permission
        if (!player.hasPermission("sellwand.command.use")) {
            return completions;
        }

        if (args.length == 1) {
            if (player.hasPermission("sellwand.command.give")) completions.add("give");
            if (player.hasPermission("sellwand.command.reload")) completions.add("reload");
            completions.add("info");
            return filterCompletions(completions, args[0]);
        }

        // Only continue with give completions if they have give permission
        if (!player.hasPermission("sellwand.command.give")) {
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));
            completions.add("1.0");
            completions.add("1.5");
            completions.add("2.0");
            completions.add("2.5");
            completions.add("3.0");
            return filterCompletions(completions, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            try {
                Double.parseDouble(args[1]);
                completions.add("-1");
                completions.add("100");
                completions.add("250");
                completions.add("500");
                completions.add("1000");
            } catch (NumberFormatException e) {
                completions.add("1.0");
                completions.add("1.5");
                completions.add("2.0");
                completions.add("2.5");
                completions.add("3.0");
            }
            return filterCompletions(completions, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            try {
                Double.parseDouble(args[1]);
                return completions;
            } catch (NumberFormatException e) {
                completions.add("-1");
                completions.add("100");
                completions.add("250");
                completions.add("500");
                completions.add("1000");
                return filterCompletions(completions, args[3]);
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }

        return completions.stream()
            .filter(completion -> completion.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }
}