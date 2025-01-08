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
        if (!(sender instanceof Player) && (!args[0].equalsIgnoreCase("give") || args.length < 2)) {
            sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.console-usage"));
            return true;
        }

        if (sender instanceof Player && !sender.hasPermission("sellwand.command.use")) {
            sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(main.getPrefix() + "§cUsage: /sellwand [give|reload|info]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (sender instanceof Player && !sender.hasPermission("sellwand.command.give")) {
                    sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
                    return true;
                }
                double multiplier = 1.0;
                int uses = plugin.getConfig().getInt("wand.default-uses", 100);
                Player target = (sender instanceof Player) ? (Player) sender : null;

                if (args.length >= 2) {
                    Player possibleTarget = Bukkit.getPlayer(args[1]);
                    if (possibleTarget != null) {
                        target = possibleTarget;
                        if (args.length >= 3) {
                            try {
                                multiplier = Double.parseDouble(args[2]);
                                if (multiplier <= 0) {
                                    sender.sendMessage(main.getPrefix() + "§cMultiplier must be greater than 0!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage(main.getPrefix() + "§cInvalid multiplier! Must be a number.");
                                return true;
                            }
                        }
                        if (args.length >= 4) {
                            try {
                                uses = Integer.parseInt(args[3]);
                                if (uses <= 0 && uses != -1) {
                                    sender.sendMessage(main.getPrefix() + "§cUses must be greater than 0 or -1 for infinite!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage(main.getPrefix() + "§cInvalid uses! Must be a number.");
                                return true;
                            }
                        }
                    } else {
                        if (sender instanceof Player) {
                            try {
                                multiplier = Double.parseDouble(args[1]);
                                if (multiplier <= 0) {
                                    sender.sendMessage(main.getPrefix() + "§cMultiplier must be greater than 0!");
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage(main.getPrefix() + "§cPlayer not found or invalid multiplier!");
                                return true;
                            }

                            if (args.length >= 3) {
                                try {
                                    uses = Integer.parseInt(args[2]);
                                    if (uses <= 0 && uses != -1) {
                                        sender.sendMessage(main.getPrefix() + "§cUses must be greater than 0 or -1 for infinite!");
                                        return true;
                                    }
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(main.getPrefix() + "§cInvalid uses! Must be a number.");
                                    return true;
                                }
                            }
                            target = (Player) sender;
                        } else {
                            sender.sendMessage(main.getPrefix() + "§cPlayer not found!");
                            return true;
                        }
                    }
                }

                if (target == null) {
                    sender.sendMessage(main.getPrefix() + "§cMust specify a target player when using command from console!");
                    return true;
                }

                target.getInventory().addItem(SellWand.createSellWand(multiplier, uses));

                if (target != sender) {
                    sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.wand-given",
                        "{player}", target.getName(),
                        "{multiplier}", String.format("%.1f", multiplier),
                        "{uses}", String.valueOf(uses)));
                }
                target.sendMessage(main.getPrefix() + plugin.getMessage("messages.wand-received", 
                    "{multiplier}", String.format("%.1f", multiplier),
                    "{uses}", String.valueOf(uses)));
                break;
            case "reload":
                if (!sender.hasPermission("sellwand.command.reload")) {
                    sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.loadTrustedUUIDs();
                sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.config-reloaded"));
                return true;
            case "info":
                sendPluginInfo(sender);
                break;
            default:
                sender.sendMessage(main.getPrefix() + plugin.getMessage("messages.unknown-command"));
                break;
        }

        return true;
    }

    private void sendPluginInfo(CommandSender sender) {
        sender.sendMessage("§8§l§m--------------------");
        sender.sendMessage(main.getPrefix() + "§fPlugin Information:");
        sender.sendMessage("§8» §7Version: §6" + plugin.getDescription().getVersion());
        sender.sendMessage("§8» §7Developer: §6Das_F1sHy312");
        sender.sendMessage("§8» §7GitHub: §6https://github.com/f1shyondrugs/sellwand");
        sender.sendMessage("§8» §7API Version: §6" + plugin.getDescription().getAPIVersion());
        sender.sendMessage("§8§l§m--------------------");
    }
} 