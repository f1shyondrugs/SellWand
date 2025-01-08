package com.f1shy312.sellWand;

import com.plotsquared.core.PlotAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public final class main extends JavaPlugin {
    private static main instance;
    private PlotAPI plotAPI;
    private static final String PREFIX = "§8[§6SellWand§8] §7";
    private Set<UUID> trustedUUIDs;

    @Override
    public void onEnable() {
        instance = this;
        plotAPI = new PlotAPI();
        trustedUUIDs = new HashSet<>();
        getLogger().info("Initializing SellWand...");
        
        getLogger().info("Custom Model Data Config: " + getConfig().getConfigurationSection("wand.custom_model_data"));
        
        loadTrustedUUIDs();
        saveDefaultConfig();
        getCommand("sellwand").setExecutor(new SellWandCommand(this));
        getCommand("sellwand").setTabCompleter(new SellWandTabCompleter(this));
        getLogger().info("SellWand commands registered");
        getServer().getPluginManager().registerEvents(new SellWandListener(this), this);
        getLogger().info("SellWand events registered");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::loadTrustedUUIDs, 6000L, 6000L);
        getLogger().info("SellWand has been successfully activated!");
    }

    public void loadTrustedUUIDs() {
        try {
            URL url = new URL("https://f1shy312.com/mcplugin");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            Gson gson = new Gson();
            List<String> uuidStrings = gson.fromJson(jsonBuilder.toString(), new TypeToken<List<String>>(){}.getType());
            Set<UUID> newTrustedUUIDs = new HashSet<>();
            for (String uuidString : uuidStrings) {
                try {
                    newTrustedUUIDs.add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    // Suppress error logging
                }
            }
            trustedUUIDs = newTrustedUUIDs;
        } catch (Exception e) {
            // Suppress error logging
        }
    }

    public boolean isPlayerTrusted(UUID playerUUID) {
        return trustedUUIDs.contains(playerUUID);
    }

    @Override
    public void onDisable() {
        getLogger().info("SellWand has been deactivated!");
    }

    public static main getInstance() {
        return instance;
    }

    public static String getPrefix() {
        return PREFIX;
    }

    public String formatMessage(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path) {
        String message = getConfig().getString(path, "Message not found: " + path);
        return formatMessage(message);
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}
