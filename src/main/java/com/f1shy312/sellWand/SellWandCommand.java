package com.f1shy312.sellWand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellWandCommand implements CommandExecutor {
    private final main plugin;

    public SellWandCommand(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("sellwand.command.use")) {
            player.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(main.getPrefix() + "§cUsage: /sellwand [give|reload|info]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (!player.hasPermission("sellwand.command.give")) {
                    player.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
                    return true;
                }
                double multiplier = 1.0;
                int uses = plugin.getConfig().getInt("wand.default-uses", 100);
                Player target = player;

                if (args.length >= 2) {
                    Player possibleTarget = Bukkit.getPlayer(args[1]);
                    if (possibleTarget != null) {
                        target = possibleTarget;
                        if (args.length >= 3) {
                            try {
                                multiplier = Double.parseDouble(args[2]);
                                if (multiplier <= 0) {
                                    player.sendMessage(main.getPrefix() + "§cMultiplier must be greater than 0!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage(main.getPrefix() + "§cInvalid multiplier! Must be a number.");
                                return true;
                            }
                        }
                        if (args.length >= 4) {
                            try {
                                uses = Integer.parseInt(args[3]);
                                if (uses <= 0 && uses != -1) {
                                    player.sendMessage(main.getPrefix() + "§cUses must be greater than 0 or -1 for infinite!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage(main.getPrefix() + "§cInvalid uses! Must be a number.");
                                return true;
                            }
                        }
                    } else {
                        try {
                            multiplier = Double.parseDouble(args[1]);
                            if (multiplier <= 0) {
                                player.sendMessage(main.getPrefix() + "§cMultiplier must be greater than 0!");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(main.getPrefix() + "§cPlayer not found or invalid multiplier!");
                            return true;
                        }

                        if (args.length >= 3) {
                            try {
                                uses = Integer.parseInt(args[2]);
                                if (uses <= 0 && uses != -1) {
                                    player.sendMessage(main.getPrefix() + "§cUses must be greater than 0 or -1 for infinite!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage(main.getPrefix() + "§cInvalid uses! Must be a number.");
                                return true;
                            }
                        }
                    }
                }

                target.getInventory().addItem(SellWand.createSellWand(multiplier, uses));

                if (target != player) {
                    player.sendMessage(main.getPrefix() + plugin.getMessage("messages.wand-given",
                        "{player}", target.getName(),
                        "{multiplier}", String.format("%.1f", multiplier),
                        "{uses}", String.valueOf(uses)));
                }
                target.sendMessage(main.getPrefix() + plugin.getMessage("messages.wand-received", 
                    "{multiplier}", String.format("%.1f", multiplier),
                    "{uses}", String.valueOf(uses)));
                break;
            case "reload":
                if (!player.hasPermission("sellwand.command.reload")) {
                    player.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadTrustedUUIDs();
                player.sendMessage(main.getPrefix() + plugin.getMessage("messages.config-reloaded"));
                return true;
            case "info":
                sendPluginInfo(player);
                break;
            default:
                player.sendMessage(main.getPrefix() + plugin.getMessage("messages.unknown-command"));
                break;
        }

        return true;
    }

    private void sendPluginInfo(Player player) {
        player.sendMessage("§8§l§m--------------------");
        player.sendMessage(main.getPrefix() + "§fPlugin Information:");
        player.sendMessage("§8» §7Version: §6" + plugin.getDescription().getVersion());
        player.sendMessage("§8» §7Developer: §6Das_F1sHy312");
        player.sendMessage("§8» §7GitHub: §6https://github.com/f1shyondrugs/sellwand");
        player.sendMessage("§8» §7API Version: §6" + plugin.getDescription().getAPIVersion());
        player.sendMessage("§8§l§m--------------------");
    }
} 