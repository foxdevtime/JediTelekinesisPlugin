package xyz.foxdevtime.jeditelekinesis;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MessageManager {

    private final JediTelekinesis plugin;
    private FileConfiguration messagesConfig;
    private String lang;

    public static final String NO_PERMISSION_COMMAND = "no-permission.command";
    public static final String NO_PERMISSION_RELEASE_COMMAND = "no-permission.release-command";
    public static final String NO_PERMISSION_ABILITY_TELEKINESIS = "no-permission.ability.telekinesis";
    public static final String NO_PERMISSION_ABILITY_TELEKINESIS_PLAYER = "no-permission.ability.telekinesis-player";
    public static final String NO_PERMISSION_ABILITY_DASH = "no-permission.ability.dash";
    public static final String NO_PERMISSION_ABILITY_PUSH_TARGETED = "no-permission.ability.push-targeted";
    public static final String NO_PERMISSION_ABILITY_PUSH_AOE = "no-permission.ability.push-aoe";

    public static final String CRYSTAL_NAME = "crystal.name";
    public static final String CRYSTAL_RECEIVED = "crystal.received";
    public static final String CRYSTAL_LORE_ATMOSPHERE = "crystal.lore.atmosphere";
    public static final String CRYSTAL_LORE_HEADER_FREE = "crystal.lore.header-free";
    public static final String CRYSTAL_LORE_GRAB = "crystal.lore.grab";
    public static final String CRYSTAL_LORE_DASH = "crystal.lore.dash";
    public static final String CRYSTAL_LORE_PUSH_TARGETED = "crystal.lore.push-targeted";
    public static final String CRYSTAL_LORE_PUSH_AOE = "crystal.lore.push-aoe";
    public static final String CRYSTAL_LORE_HEADER_HOLDING = "crystal.lore.header-holding";
    public static final String CRYSTAL_LORE_RELEASE_GENTLE = "crystal.lore.release-gentle";
    public static final String CRYSTAL_LORE_THROW_HELD = "crystal.lore.throw-held";

    public static final String TELEKINESIS_GRAB_SUCCESS = "telekinesis.grab.success";
    public static final String TELEKINESIS_GRAB_INSTRUCTION = "telekinesis.grab.instruction"; // New
    public static final String TELEKINESIS_GRAB_FAIL_NO_TARGET = "telekinesis.grab.fail-no-target";
    public static final String TELEKINESIS_GRAB_FAIL_BANNED = "telekinesis.grab.fail-banned";

    public static final String TELEKINESIS_RELEASE_GENTLE = "telekinesis.release.gentle";
    public static final String TELEKINESIS_RELEASE_THROW = "telekinesis.release.throw";
    public static final String TELEKINESIS_FREED = "telekinesis.freed";

    public static final String TELEKINESIS_HELD_PLAYER_MESSAGE = "telekinesis.held-player.message";
    public static final String TELEKINESIS_HELD_PLAYER_RELEASE_COMMAND_TIP = "telekinesis.held-player.release-command-tip";
    public static final String TELEKINESIS_COLLISION = "telekinesis.collision";
    public static final String TELEKINESIS_AUTO_RELEASE_HOLDER = "telekinesis.auto-release.holder";
    public static final String TELEKINESIS_AUTO_RELEASE_HELD = "telekinesis.auto-release.held";
    public static final String TELEKINESIS_DEATH_HOLDER = "telekinesis.death.holder";
    public static final String TELEKINESIS_DEATH_HELD_PLAYER_FREED = "telekinesis.death.held-player-freed";
    public static final String TELEKINESIS_PLAYER_QUIT_RELEASED = "telekinesis.player-quit-released";

    public static final String DASH_SUCCESS = "dash.success";

    public static final String PUSH_TARGETED_SUCCESS = "push.targeted.success";
    public static final String PUSH_TARGETED_FAIL_NO_TARGET = "push.targeted.fail-no-target";
    public static final String PUSH_TARGETED_FAIL_BANNED = "push.targeted.fail-banned";

    public static final String PUSH_AOE_SUCCESS = "push.aoe.success";
    public static final String PUSH_AOE_FAIL_NO_TARGETS = "push.aoe.fail-no-targets";

    public static final String COOLDOWN_MESSAGE = "cooldown.message";
    public static final String ABILITY_NAME_DASH = "ability.name.dash";
    public static final String ABILITY_NAME_TARGETED_PUSH = "ability.name.targeted-push";
    public static final String ABILITY_NAME_AOE_PUSH = "ability.name.aoe-push";

    public static final String RELEASE_COMMAND_SUCCESS = "release-command.success";
    public static final String RELEASE_COMMAND_HOLDER_NOTIFIED = "release-command.holder-notified";
    public static final String RELEASE_COMMAND_HOLDER_OFFLINE = "release-command.holder-offline";
    public static final String RELEASE_COMMAND_NOT_HELD = "release-command.not-held";
    public static final String COMMAND_PLAYERS_ONLY = "command.players-only";

    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HEADER = "command.crystal-give-instruction-header";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HAND = "command.crystal-give-instruction-hand";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_RMB = "command.crystal-give-instruction-free-rmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_LMB = "command.crystal-give-instruction-free-lmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_SHIFT_RMB = "command.crystal-give-instruction-free-shift-rmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_SHIFT_LMB = "command.crystal-give-instruction-free-shift-lmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_HEADER = "command.crystal-give-instruction-holding-header";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_RMB = "command.crystal-give-instruction-holding-rmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_LMB = "command.crystal-give-instruction-holding-lmb";
    public static final String COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_SHIFT_RMB = "command.crystal-give-instruction-holding-shift-rmb";


    public MessageManager(JediTelekinesis plugin, String lang) {
        this.plugin = plugin;
        this.lang = lang;
        loadMessages();
    }

    public void loadMessages() {
        File langFile = new File(plugin.getDataFolder(), "lang_" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang_" + lang + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = plugin.getResource("lang_" + lang + ".yml");
        if (defaultStream != null) {
            try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                messagesConfig.setDefaults(defaultConfig);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load default messages for lang_" + lang + ".yml", e);
            }
        } else {
            plugin.getLogger().warning("Default lang_" + lang + ".yml not found in JAR resources!");
        }
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            message = messagesConfig.getDefaults() != null ? messagesConfig.getDefaults().getString(key) : null;
            if (message == null) {
                plugin.getLogger().warning("Missing message key in lang_" + lang + ".yml: " + key + " (and not in defaults)");
                return ChatColor.RED + "Error: Missing message for " + key;
            }
            plugin.getLogger().info("Message key '" + key + "' not found in user's lang_" + lang + ".yml, using default.");
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String key, String placeholder, String replacement) {
        String message = getMessage(key);
        if (replacement == null) replacement = "null";
        return message.replace(placeholder, replacement);
    }

    public String getMessage(String key, String placeholder1, String replacement1, String placeholder2, String replacement2) {
        String message = getMessage(key);
        if (replacement1 == null) replacement1 = "null";
        if (replacement2 == null) replacement2 = "null";
        return message.replace(placeholder1, replacement1).replace(placeholder2, replacement2);
    }

    public String getMessage(String key, Placeholder... placeholders) {
        String message = getMessage(key);
        for (Placeholder p : placeholders) {
            String replacement = p.replacement != null ? p.replacement : "null";
            message = message.replace(p.placeholder, replacement);
        }
        return message;
    }

    public static class Placeholder {
        String placeholder;
        String replacement;

        public Placeholder(String placeholder, String replacement) {
            this.placeholder = placeholder;
            this.replacement = replacement;
        }
    }


    public List<String> getMessageList(String key) {
        List<String> messages = messagesConfig.getStringList(key);
        if (messages == null || messages.isEmpty()) {
            if (messagesConfig.getDefaults() != null) {
                messages = messagesConfig.getDefaults().getStringList(key);
            }
            if (messages == null || messages.isEmpty()) {
                plugin.getLogger().warning("Missing message list key in lang_" + lang + ".yml: " + key + " (and not in defaults)");
                return Arrays.asList(ChatColor.RED + "Error: Missing message list for " + key);
            }
            plugin.getLogger().info("Message list key '" + key + "' not found in user's lang_" + lang + ".yml, using default list.");
        }

        List<String> coloredMessages = new ArrayList<>();
        for (String line : messages) {
            coloredMessages.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return coloredMessages;
    }

    public void reloadMessages(String newLang) {
        this.lang = newLang;
        loadMessages();
        plugin.getLogger().info("Messages reloaded for language: " + newLang);
    }
}