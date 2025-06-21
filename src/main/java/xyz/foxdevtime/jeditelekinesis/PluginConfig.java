package xyz.foxdevtime.jeditelekinesis;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PluginConfig {

    private final JediTelekinesis plugin;
    private FileConfiguration config;

    public PluginConfig(JediTelekinesis plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        plugin.getLogger().info("Plugin configuration reloaded.");
    }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public double getMinDistanceToPlayerEyes() {
        return config.getDouble("general.min-distance-to-player-eyes", 1.2);
    }

    public Material getForceCrystalMaterial() {
        String materialName = config.getString("force-crystal.material", "NETHER_STAR").toUpperCase();
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' for force-crystal.material in config.yml! Using NETHER_STAR.");
            return Material.NETHER_STAR;
        }
    }

    public double getPullDistance() {
        return config.getDouble("telekinesis.pull-distance", 3.5);
    }

    public double getMaxGrabRange() {
        return config.getDouble("telekinesis.max-grab-range", 20.0);
    }

    public double getThrowStrength() {
        return config.getDouble("telekinesis.throw-strength", 1.7);
    }

    public int getMaxPlayerHoldDurationSeconds() {
        return config.getInt("telekinesis.max-player-hold-duration-seconds", 10);
    }

    public Set<EntityType> getBannedEntityTypes() {
        List<String> bannedTypesStr = config.getStringList("telekinesis.banned-entity-types");
        Set<EntityType> bannedEntityTypesSet = EnumSet.noneOf(EntityType.class);
        for (String typeStr : bannedTypesStr) {
            try {
                bannedEntityTypesSet.add(EntityType.valueOf(typeStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in telekinesis.banned-entity-types: '" + typeStr + "'");
            }
        }
        return bannedEntityTypesSet;
    }

    public boolean isCollisionCheckEnabled() {
        return config.getBoolean("telekinesis.collision-check.enabled", true);
    }

    public int getCollisionChecksAmount() {
        int checks = config.getInt("telekinesis.collision-check.checks-amount", 5);
        return Math.max(1, checks);
    }

    public double getForceDashStrengthHorizontal() {
        return config.getDouble("force-dash.strength-horizontal", 1.9);
    }

    public double getForceDashStrengthVertical() {
        return config.getDouble("force-dash.strength-vertical", 0.6);
    }

    public int getForceDashCooldownSeconds() {
        return config.getInt("force-dash.cooldown-seconds", 4);
    }

    public double getTargetedPushStrength() {
        return config.getDouble("targeted-force-push.strength", 2.2);
    }

    public double getTargetedPushRangeDivider() {
        double divider = config.getDouble("targeted-force-push.range-divider", 2.0);
        return (divider <= 0) ? 2.0 : divider;
    }

    public double getTargetedPushVerticalBoost() {
        return config.getDouble("targeted-force-push.vertical-boost", 0.35);
    }

    public int getTargetedPushCooldownSeconds() {
        return config.getInt("targeted-force-push.cooldown-seconds", 3);
    }

    public double getAoePushStrength() {
        return config.getDouble("aoe-force-push.strength", 1.7);
    }

    public double getAoePushRadius() {
        return config.getDouble("aoe-force-push.radius", 6.0);
    }

    public double getAoePushVerticalBoost() {
        return config.getDouble("aoe-force-push.vertical-boost", 0.35);
    }

    public int getAoePushCooldownSeconds() {
        return config.getInt("aoe-force-push.cooldown-seconds", 7);
    }

    private <T extends Enum<T>> T getSafeEnum(String path, String defaultValue, Class<T> enumType, String readableTypeName) {
        String configValue = config.getString(path, defaultValue).trim().toUpperCase();
        try {
            return Enum.valueOf(enumType, configValue);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid " + readableTypeName + " in config.yml at '" + path + "': '" + configValue + "'. Using default: " + defaultValue);
            return Enum.valueOf(enumType, defaultValue.trim().toUpperCase());
        }
    }

    // main effects
    public Particle getHoldAuraParticle1() {
        return getSafeEnum("telekinesis.effects.hold-aura-particle1", "SPELL_WITCH", Particle.class, "Particle");
    }
    public int getHoldAuraParticle1Count() {
        return Math.max(0, config.getInt("telekinesis.effects.hold-aura-particle1-count", 2));
    }
    public Particle getHoldAuraParticle2() {
        return getSafeEnum("telekinesis.effects.hold-aura-particle2", "ENCHANTMENT_TABLE", Particle.class, "Particle");
    }
    public int getHoldAuraParticle2Count() {
        return Math.max(0, config.getInt("telekinesis.effects.hold-aura-particle2-count", 1));
    }
    public Sound getGrabSoundCaster() {
        return getSafeEnum("telekinesis.effects.grab-sound-caster", "BLOCK_BEACON_POWER_SELECT", Sound.class, "Sound");
    }
    public Sound getGrabSoundTarget() {
        return getSafeEnum("telekinesis.effects.grab-sound-target", "ENTITY_ILLUSIONER_MIRROR_MOVE", Sound.class, "Sound");
    }
    public Particle getGrabParticle() {
        return getSafeEnum("telekinesis.effects.grab-particle", "REVERSE_PORTAL", Particle.class, "Particle");
    }
    public Particle getReleaseParticle() {
        return getSafeEnum("telekinesis.effects.release-particle", "SMOKE_NORMAL", Particle.class, "Particle");
    }
    public Sound getThrowSound() {
        return getSafeEnum("telekinesis.effects.throw-sound", "ENTITY_PLAYER_ATTACK_STRONG", Sound.class, "Sound");
    }
    public Particle getThrowParticle() {
        return getSafeEnum("telekinesis.effects.throw-particle", "CRIT", Particle.class, "Particle");
    }
    public Sound getReleaseSound() {
        return getSafeEnum("telekinesis.effects.release-sound", "BLOCK_BEACON_DEACTIVATE", Sound.class, "Sound");
    }
    public Sound getCollisionSound() {
        return getSafeEnum("telekinesis.effects.collision-sound", "BLOCK_STONE_HIT", Sound.class, "Sound");
    }
    public Particle getCollisionParticle() {
        return getSafeEnum("telekinesis.effects.collision-particle", "BLOCK_CRACK", Particle.class, "Particle");
    }

    // force dash
    public Sound getDashSound() { return getSafeEnum("force-dash.effects.sound", "ENTITY_PHANTOM_FLAP", Sound.class, "Sound"); }
    public Particle getDashTrailParticleMain() { return getSafeEnum("force-dash.effects.trail-particle-main", "CLOUD", Particle.class, "Particle"); }
    public Particle getDashTrailParticleSecondary() { return getSafeEnum("force-dash.effects.trail-particle-secondary", "SNOWFLAKE", Particle.class, "Particle"); }
    public Particle getDashInitialBurstParticle() { return getSafeEnum("force-dash.effects.initial-burst-particle", "CAMPFIRE_COSY_SMOKE", Particle.class, "Particle"); }
    public int getDashTrailDurationTicks() { return Math.max(0, config.getInt("force-dash.effects.trail-duration-ticks", 10)); }
    public int getDashTrailParticlesPerTick() { return Math.max(0, config.getInt("force-dash.effects.trail-particles-per-tick", 3)); }
    public double getDashTrailOffsetBehind() { return config.getDouble("force-dash.effects.trail-offset-behind", 0.5); }

    // targ push
    public Sound getTargetedPushSound() { return getSafeEnum("targeted-force-push.effects.sound", "ENTITY_WARDEN_SONIC_BOOM", Sound.class, "Sound"); }
    public Particle getTargetedPushWaveParticleMain() { return getSafeEnum("targeted-force-push.effects.wave-particle-main", "SONIC_BOOM", Particle.class, "Particle"); }
    public Particle getTargetedPushWaveParticleSecondary() { return getSafeEnum("targeted-force-push.effects.wave-particle-secondary", "CRIT_MAGIC", Particle.class, "Particle"); }
    public Particle getTargetedPushImpactParticle() { return getSafeEnum("targeted-force-push.effects.impact-particle", "EXPLOSION_NORMAL", Particle.class, "Particle"); }
    public Sound getTargetedPushFailSound() { return getSafeEnum("targeted-force-push.effects.fail-sound", "BLOCK_STONE_BUTTON_CLICK_OFF", Sound.class, "Sound"); }

    // aoe push
    public Sound getAoePushSound() { return getSafeEnum("aoe-force-push.effects.sound", "ENTITY_WARDEN_ROAR", Sound.class, "Sound"); }
    public Particle getAoePushCenterParticle() { return getSafeEnum("aoe-force-push.effects.center-particle", "EXPLOSION_LARGE", Particle.class, "Particle"); }
    public Particle getAoePushWaveParticle() { return getSafeEnum("aoe-force-push.effects.wave-particle", "SWEEP_ATTACK", Particle.class, "Particle"); }
    public Sound getAoePushFailSound() { return getSafeEnum("aoe-force-push.effects.fail-sound", "BLOCK_STONE_BUTTON_CLICK_OFF", Sound.class, "Sound"); }
}