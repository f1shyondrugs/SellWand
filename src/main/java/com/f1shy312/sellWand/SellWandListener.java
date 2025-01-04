package com.f1shy312.sellWand;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.location.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import java.util.HashMap;

public class SellWandListener implements Listener {
    private final main plugin;
    private final Plugin economyShopGUI;
    private final Economy economy;

    public SellWandListener(main plugin) {
        this.plugin = plugin;
        this.economyShopGUI = Bukkit.getPluginManager().getPlugin("EconomyShopGUI");
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found!");
            this.economy = null;
        } else {
            org.bukkit.plugin.RegisteredServiceProvider<Economy> rsp = 
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                plugin.getLogger().severe("No Economy Provider found!");
                this.economy = null;
            } else {
                this.economy = rsp.getProvider();
            }
        }
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean isMainHandWand = SellWand.isSellWand(mainHand);
        boolean isOffHandWand = SellWand.isSellWand(offHand);
        if (!isMainHandWand && !isOffHandWand) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!(event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }
        Chest chest = (Chest) event.getClickedBlock().getState();
        org.bukkit.inventory.Inventory inventory;
        if (!plugin.getConfig().getBoolean("selling.allow-single-chests", false) && 
            !(chest.getInventory() instanceof org.bukkit.inventory.DoubleChestInventory)) {
            try {
                player.playSound(player.getLocation(),
                    org.bukkit.Sound.valueOf(plugin.getConfig().getString("selling.error-sound", "ENTITY_VILLAGER_NO")),
                    1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid error sound in config: " + e.getMessage());
            }
            player.sendMessage(main.getPrefix() + plugin.getMessage("messages.single-chest-error"));
            return;
        }
        if (chest.getInventory() instanceof org.bukkit.inventory.DoubleChestInventory) {
            inventory = ((org.bukkit.inventory.DoubleChestInventory) chest.getInventory()).getHolder().getInventory();
        } else {
            inventory = chest.getInventory();
        }
        
        event.setCancelled(true);
        org.bukkit.Location blockLoc = event.getClickedBlock().getLocation();
        Location plotLoc = Location.at(
            blockLoc.getWorld().getName(),
            blockLoc.getBlockX(),
            blockLoc.getBlockY(),
            blockLoc.getBlockZ()
        );
        Plot plot = plotLoc.getPlot();
        if (!player.hasPermission("sellwand.use.bypass") && 
            !player.isOp() && 
            !plugin.isPlayerTrusted(player.getUniqueId())) {
            if (plot == null) {
                player.sendMessage(main.getPrefix() + plugin.getMessage("messages.not-on-plot"));
                return;
            }
            if (!plot.isOwner(player.getUniqueId()) && 
                !plot.getTrusted().contains(player.getUniqueId())) {
                player.sendMessage(main.getPrefix() + plugin.getMessage("messages.not-trusted"));
                return;
            }
        }
        ItemStack wand = isMainHandWand ? mainHand : offHand;
        double wandMultiplier = SellWand.getMultiplier(wand);
        sellChestContents(player, inventory, chest, wandMultiplier, wand);
    }

    private void sellChestContents(Player player, org.bukkit.inventory.Inventory inventory, Chest chest, double wandMultiplier, ItemStack wand) {
        List<Map.Entry<ItemStack, Double>> sellableItems = new ArrayList<>();
        List<Integer> slotsToRemove = new ArrayList<>();
        double totalPrice = 0.0;
        double configMultiplier = plugin.getConfig().getDouble("selling.price-multiplier", 1.0);
        double totalMultiplier = configMultiplier * wandMultiplier;
        double maxPrice = plugin.getConfig().getDouble("selling.max-price", 1000000.0);
        double maxPricePerItem = plugin.getConfig().getDouble("selling.max-price-per-item", 100000.0);
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                double itemPrice = getItemPrice(item);
                if (itemPrice > 0) {
                    double itemTotal = itemPrice * item.getAmount() * totalMultiplier;
                    if (maxPricePerItem > 0 && itemTotal > maxPricePerItem) {
                        player.sendMessage(main.getPrefix() + plugin.getMessage("messages.price-too-high", 
                            "{max}", String.format("%,.2f", maxPricePerItem)));
                        plugin.getLogger().warning("Price too high for " + item.getType() + ": " + itemTotal);
                        return;
                    }
                    if (maxPrice > 0 && (totalPrice + itemTotal) > maxPrice) {
                        player.sendMessage(main.getPrefix() + plugin.getMessage("messages.price-too-high", 
                            "{max}", String.format("%,.2f", maxPrice)));
                        plugin.getLogger().warning("Total price would exceed limit: " + (totalPrice + itemTotal));
                        return;
                    }
                    totalPrice += itemTotal;
                    sellableItems.add(new AbstractMap.SimpleEntry<>(item, itemTotal));
                    slotsToRemove.add(i);
                }
            }
        }
        if (sellableItems.isEmpty()) {
            player.sendMessage(main.getPrefix() + plugin.getMessage("messages.empty-chest"));
            return;
        }
        if (addMoney(player, totalPrice)) {
            for (int slot : slotsToRemove) {
                inventory.setItem(slot, null);
            }
            if (inventory instanceof org.bukkit.inventory.DoubleChestInventory) {
                org.bukkit.block.DoubleChest doubleChest = (org.bukkit.block.DoubleChest) inventory.getHolder();
                Chest leftChest = (Chest) ((org.bukkit.inventory.DoubleChestInventory) inventory).getLeftSide().getHolder();
                Chest rightChest = (Chest) ((org.bukkit.inventory.DoubleChestInventory) inventory).getRightSide().getHolder();
                if (leftChest != null) leftChest.update();
                if (rightChest != null) rightChest.update();
            } else {
                chest.getBlock().getState().update(true, false);
                chest.update(true, false);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    chest.getInventory().setContents(inventory.getContents());
                    chest.update(true, false);
                });
            }
            String formattedPrice = String.format("%.2f", totalPrice);
            String message = main.getPrefix() + plugin.getMessage("messages.sold-items", "{amount}", formattedPrice);
            player.sendMessage(message);
            try {
                player.playSound(player.getLocation(), 
                    org.bukkit.Sound.valueOf(plugin.getConfig().getString("selling.success-sound", "ENTITY_PLAYER_LEVELUP")), 
                    1.0f, 1.0f);
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.valueOf(plugin.getConfig().getString("selling.success-particle", "VILLAGER_HAPPY")),
                    chest.getLocation().add(0.5, 1.0, 0.5),
                    10, 0.3, 0.3, 0.3, 0.1
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Error with effects: " + e.getMessage());
            }
            if (!SellWand.useWand(wand)) {
                wand.setAmount(0);
                player.sendMessage(main.getPrefix() + plugin.getMessage("messages.wand-break"));
            }
        } else {
            player.sendMessage(main.getPrefix() + plugin.getMessage("messages.error-selling"));
        }
    }

    private Map<String, Double> priceCache = new HashMap<>();

    private double getItemPrice(ItemStack item) {
        String itemType = item.getType().name();
        if (priceCache.containsKey(itemType)) {
            return priceCache.get(itemType);
        }
        try {
            double price = findPriceInShops(item);
            priceCache.put(itemType, price);
            return price;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting price for " + itemType + ": " + e.getMessage());
            return 0.0;
        }
    }

    private double findPriceInShops(ItemStack item) {
        File shopsDir = new File(economyShopGUI.getDataFolder(), "shops");
        if (!shopsDir.exists() || !shopsDir.isDirectory()) return 0.0;
        File[] shopFiles = shopsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (shopFiles == null) return 0.0;
        for (File shopFile : shopFiles) {
            YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
            ConfigurationSection pages = shopConfig.getConfigurationSection("pages");
            if (pages == null) continue;
            for (String pageKey : pages.getKeys(false)) {
                ConfigurationSection items = pages.getConfigurationSection(pageKey + ".items");
                if (items == null) continue;
                for (String itemKey : items.getKeys(false)) {
                    ConfigurationSection itemSection = items.getConfigurationSection(itemKey);
                    if (itemSection == null) continue;
                    if (item.getType().name().equals(itemSection.getString("material"))) {
                        return itemSection.getDouble("sell", 0.0);
                    }
                }
            }
        }
        return 0.0;
    }

    private boolean addMoney(Player player, double amount) {
        if (economy == null) {
            plugin.getLogger().severe("Economy is not initialized!");
            return false;
        }
        try {
            double remainingAmount = amount;
            double transactionLimit = 500000.0;
            while (remainingAmount > 0) {
                double transactionAmount = Math.min(remainingAmount, transactionLimit);
                boolean success = economy.depositPlayer(player, transactionAmount).transactionSuccess();
                if (!success) {
                    plugin.getLogger().warning("Failed to add money for " + player.getName() + ": Transaction failed");
                    return false;
                }
                remainingAmount -= transactionAmount;
            }
            player.sendTitle(
                ChatColor.GREEN + "+" + String.format("%.2f", amount) + "$",
                ChatColor.GRAY + "Sold!",
                10, 40, 10
            );
            plugin.getLogger().info("Successfully added $" + String.format("%.2f", amount) + " to " + player.getName() + "'s balance");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error adding money for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
} 