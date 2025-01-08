package com.f1shy312.sellWand;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Collections;

public class SellWand {
    private static NamespacedKey SELLWAND_KEY = null;
    private static NamespacedKey MULTIPLIER_KEY = null;
    private static NamespacedKey USES_KEY = null;

    private static void initKeys() {
        if (SELLWAND_KEY == null) {
            main plugin = main.getInstance();
            SELLWAND_KEY = new NamespacedKey(plugin, "sellwand");
            MULTIPLIER_KEY = new NamespacedKey(plugin, "multiplier");
            USES_KEY = new NamespacedKey(plugin, "uses");
        }
    }

    public static ItemStack createSellWand(double multiplier, int uses) {
        initKeys();
        main plugin = main.getInstance();
        FileConfiguration config = plugin.getConfig();
        
        Material material = Material.valueOf(config.getString("wand.material", "BLAZE_ROD"));
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();
        
        if (meta != null) {
            String name = ChatColor.translateAlternateColorCodes('&', config.getString("wand.name", "&6SellWand"))
                .replace("{multiplier}", String.format("%.1f", multiplier))
                .replace("{uses}", uses == -1 ? "∞" : String.valueOf(uses));
            meta.setDisplayName(name);
            
            List<String> lore = new ArrayList<>();
            for (String loreLine : config.getStringList("wand.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', loreLine)
                    .replace("{multiplier}", String.format("%.1f", multiplier))
                    .replace("{uses}", uses == -1 ? "∞" : String.valueOf(uses)));
            }
            meta.setLore(lore);
            
            int customModelData = getCustomModelData(multiplier);
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            meta.getPersistentDataContainer().set(MULTIPLIER_KEY, PersistentDataType.DOUBLE, multiplier);
            meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, uses);
            meta.getPersistentDataContainer().set(SELLWAND_KEY, PersistentDataType.BYTE, (byte)1);
            
            wand.setItemMeta(meta);
        }
        
        return wand;
    }

    private static int getCustomModelData(double multiplier) {
        main plugin = main.getInstance();
        ConfigurationSection modelDataSection = plugin.getConfig().getConfigurationSection("wand.custom_model_data");
        
        if (modelDataSection == null) {
            return 1001; // Default fallback
        }

        // Round down to nearest whole number
        int roundedMultiplier = (int) Math.floor(multiplier);
        String key = roundedMultiplier + ".0";
        
        // Get the model data for the rounded multiplier, or use the highest defined value
        return modelDataSection.getInt(key, 
               modelDataSection.getInt("5.0", // Use highest defined multiplier if beyond range
               1001)); // Default if nothing else matches
    }

    public static boolean useWand(ItemStack wand) {
        if (!isSellWand(wand)) return false;
        
        ItemMeta meta = wand.getItemMeta();
        int uses = getUses(wand);
        
        if (uses == -1) return true;
        
        if (uses <= 1) return false;
        
        uses--;
        meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, uses);
        
        List<String> lore = new ArrayList<>();
        for (String loreLine : main.getInstance().getConfig().getStringList("wand.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', loreLine)
                .replace("{multiplier}", String.format("%.1f", getMultiplier(wand)))
                .replace("{uses}", uses == -1 ? "∞" : String.valueOf(uses)));
        }
        meta.setLore(lore);
        
        wand.setItemMeta(meta);
        return true;
    }

    public static int getUses(ItemStack wand) {
        if (!isSellWand(wand)) return 0;
        
        ItemMeta meta = wand.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(USES_KEY, PersistentDataType.INTEGER, 0);
    }

    public static boolean isSellWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        initKeys();
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(SELLWAND_KEY, PersistentDataType.BYTE);
    }

    public static double getMultiplier(ItemStack wand) {
        if (!isSellWand(wand)) return 1.0;
        
        ItemMeta meta = wand.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(MULTIPLIER_KEY, PersistentDataType.DOUBLE, 1.0);
    }
} 