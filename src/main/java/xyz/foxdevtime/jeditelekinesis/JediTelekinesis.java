package xyz.foxdevtime.jeditelekinesis;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class JediTelekinesis extends JavaPlugin implements Listener {

    private final Map<UUID, Entity> heldEntities = new HashMap<>();
    private BukkitTask movementTask;

    private PluginConfig pluginConfig;
    private MessageManager messageManager;

    private final Map<UUID, Long> dashCooldowns = new HashMap<>();
    private final Map<UUID, Long> targetedPushCooldowns = new HashMap<>();
    private final Map<UUID, Long> aoePushCooldowns = new HashMap<>();
    private final Map<UUID, Long> playerHoldStartTime = new HashMap<>();

    @Override
    public void onEnable() {
        this.pluginConfig = new PluginConfig(this);
        this.messageManager = new MessageManager(this, pluginConfig.getLanguage());

        this.getCommand("telekinesis").setExecutor(new TelekinesisCommand(this));
        this.getCommand("tkrelease").setExecutor(new TelekinesisReleaseCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        movementTask = new BukkitRunnable() {
            @Override
            public void run() {
                new HashSet<>(heldEntities.keySet()).forEach(holderId -> {
                    Player holder = Bukkit.getPlayer(holderId);
                    Entity heldEntity = heldEntities.get(holderId);

                    if (holder == null || !holder.isOnline() || heldEntity == null || heldEntity.isDead() || (heldEntity instanceof Player && !((Player) heldEntity).isOnline())) {
                        releaseEntity(holder, false);
                        return;
                    }

                    Location playerEyeLoc = holder.getEyeLocation();
                    Vector direction = playerEyeLoc.getDirection().clone();
                    Location lineOfSightTarget = playerEyeLoc.clone().add(direction.multiply(pluginConfig.getPullDistance()));
                    double entityHeight = heldEntity.getHeight();
                    Location intendedFinalTargetPos = lineOfSightTarget.clone().subtract(0, entityHeight / 2, 0);
                    double minDistanceToPlayerEyes = pluginConfig.getMinDistanceToPlayerEyes();

                    if (intendedFinalTargetPos.distanceSquared(playerEyeLoc) < Math.pow(minDistanceToPlayerEyes, 2)) {
                        Location correctedBaseLineOfSightTarget = playerEyeLoc.clone().add(playerEyeLoc.getDirection().clone().normalize().multiply(minDistanceToPlayerEyes));
                        intendedFinalTargetPos = correctedBaseLineOfSightTarget;
                    }

                    Location currentEntityCenter = heldEntity.getLocation().add(0, entityHeight / 2, 0);
                    Location targetEntityCenter = intendedFinalTargetPos.clone().add(0, entityHeight / 2, 0);
                    Vector pathVector = targetEntityCenter.clone().subtract(currentEntityCenter).toVector();

                    if (pluginConfig.isCollisionCheckEnabled() && pathVector.lengthSquared() > 0.01) {
                        Block hitBlock = null;
                        int checks = pluginConfig.getCollisionChecksAmount();
                        boolean collision = false;
                        for (int i = 1; i <= checks; i++) {
                            Location checkLoc = currentEntityCenter.clone().add(pathVector.clone().multiply((double)i / checks));
                            Block block = checkLoc.getBlock();
                            if (block.getType().isSolid() && block.getType() != Material.AIR && !block.isPassable()) {
                                collision = true;
                                hitBlock = block;
                                break;
                            }
                        }
                        if (collision && hitBlock != null) {
                            holder.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_COLLISION, "{entityName}", heldEntity.getName()));
                            holder.getWorld().playSound(heldEntity.getLocation(), pluginConfig.getCollisionSound(), 1.0f, 0.8f);
                            if (hitBlock.getWorld() != null && hitBlock.getType() != Material.AIR) {
                                holder.getWorld().spawnParticle(pluginConfig.getCollisionParticle(), hitBlock.getLocation().add(0.5,0.5,0.5), 20, 0.2,0.2,0.2, Bukkit.createBlockData(hitBlock.getType()));
                            }
                            releaseEntity(holder, false);
                            return;
                        }
                    }

                    heldEntity.teleport(intendedFinalTargetPos);
                    heldEntity.setFallDistance(0);

                    if (heldEntity instanceof Player) {
                        Player heldPlayer = (Player) heldEntity;
                        if (playerHoldStartTime.containsKey(heldPlayer.getUniqueId())) {
                            long holdDurationMillis = System.currentTimeMillis() - playerHoldStartTime.get(heldPlayer.getUniqueId());
                            if (holdDurationMillis / 1000 >= pluginConfig.getMaxPlayerHoldDurationSeconds()) {
                                holder.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_AUTO_RELEASE_HOLDER, "{playerName}", heldPlayer.getName()));
                                heldPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_AUTO_RELEASE_HELD));
                                releaseEntity(holder, false);
                                return;
                            }
                        } else {
                            playerHoldStartTime.put(heldPlayer.getUniqueId(), System.currentTimeMillis());
                        }
                    }

                    Location particleLocation = heldEntity.getLocation().add(0, entityHeight / 2, 0);
                    if (holder.getWorld().equals(particleLocation.getWorld())) {
                        holder.getWorld().spawnParticle(pluginConfig.getHoldAuraParticle1(), particleLocation, pluginConfig.getHoldAuraParticle1Count(), 0.4, 0.5, 0.4, 0.01);
                        holder.getWorld().spawnParticle(pluginConfig.getHoldAuraParticle2(), particleLocation, pluginConfig.getHoldAuraParticle2Count(), 0.3, 0.3, 0.3, 0.02);
                    }
                });
            }
        }.runTaskTimer(this, 0L, 1L);
        getLogger().info("JediTelekinesisPlugin enabled! Language: " + pluginConfig.getLanguage());
    }

    @Override
    public void onDisable() {
        new HashSet<>(heldEntities.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            releaseEntity(p, false);
        });
        heldEntities.clear();
        dashCooldowns.clear();
        targetedPushCooldowns.clear();
        aoePushCooldowns.clear();
        playerHoldStartTime.clear();
        if (movementTask != null) {
            movementTask.cancel();
        }
        getLogger().info("JediTelekinesisPlugin disabled!");
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public Map<UUID, Entity> getHeldEntities() {
        return Collections.unmodifiableMap(heldEntities);
    }

    public ItemStack createForceCrystal() {
        ItemStack crystal = new ItemStack(pluginConfig.getForceCrystalMaterial());
        ItemMeta meta = crystal.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(messageManager.getMessage("crystal.name"));
            List<String> lore = new ArrayList<>();
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_ATMOSPHERE));
            lore.add("");
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_HEADER_FREE));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_GRAB));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_DASH));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_PUSH_TARGETED));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_PUSH_AOE));
            lore.add("");
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_HEADER_HOLDING));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_RELEASE_GENTLE));
            lore.add(messageManager.getMessage(MessageManager.CRYSTAL_LORE_THROW_HELD));
            meta.setLore(lore);
            crystal.setItemMeta(meta);
        }
        return crystal;
    }

    public boolean isForceCrystal(ItemStack item) {
        if (item == null || item.getType() != pluginConfig.getForceCrystalMaterial()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(messageManager.getMessage("crystal.name"));
    }

    public void tryGrabEntityWithCrystal(Player player) {
        if (heldEntities.containsKey(player.getUniqueId())) return;
        Entity targetEntity = getTargetEntity(player, pluginConfig.getMaxGrabRange());

        if (targetEntity != null) {
            if (targetEntity instanceof Player targetPlayer && !targetPlayer.equals(player)) {
                if (!player.hasPermission("jeditelekenisis.telekinesis.player")) {
                    player.sendMessage(messageManager.getMessage(MessageManager.NO_PERMISSION_ABILITY_TELEKINESIS_PLAYER));
                    return;
                }
            } else if (!(targetEntity instanceof LivingEntity) || targetEntity.equals(player)) {
                player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_GRAB_FAIL_NO_TARGET));
                player.getWorld().playSound(player.getLocation(), pluginConfig.getTargetedPushFailSound(), 1.0f, 0.8f);
                return;
            }
            if (pluginConfig.getBannedEntityTypes().contains(targetEntity.getType())) {
                player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_GRAB_FAIL_BANNED));
                return;
            }
            heldEntities.put(player.getUniqueId(), targetEntity);
            if (targetEntity instanceof Player targetPlayer) {
                targetPlayer.setAllowFlight(true);
                targetPlayer.setFlying(true);
                targetPlayer.setGravity(false);
                targetPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_HELD_PLAYER_MESSAGE, "{holderName}", player.getName()));
                targetPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_HELD_PLAYER_RELEASE_COMMAND_TIP));
                playerHoldStartTime.put(targetPlayer.getUniqueId(), System.currentTimeMillis());
            } else if (targetEntity instanceof LivingEntity livingTarget) {
                livingTarget.setAI(false);
                targetEntity.setGravity(false);
            }
            player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_GRAB_SUCCESS, "{entityName}", targetEntity.getName()));
            player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_GRAB_INSTRUCTION));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getGrabSoundCaster(), 1.0f, 1.2f);
            player.getWorld().playSound(targetEntity.getLocation(), pluginConfig.getGrabSoundTarget(), 0.8f, 1.0f);
            player.getWorld().spawnParticle(pluginConfig.getGrabParticle(), targetEntity.getLocation().add(0, targetEntity.getHeight() / 2, 0), 30, 0.5, 0.5, 0.5, 0.1);
        } else {
            player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_GRAB_FAIL_NO_TARGET));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getTargetedPushFailSound(), 1.0f, 0.8f);
        }
    }

    private void resetPlayerState(Player player) {
        if (player == null) return;
        Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
        if (onlinePlayer == null || !onlinePlayer.isOnline()) return;

        onlinePlayer.setGravity(true);
        onlinePlayer.setFlying(false);
        if (onlinePlayer.getGameMode() != GameMode.CREATIVE && onlinePlayer.getGameMode() != GameMode.SPECTATOR) {
            onlinePlayer.setAllowFlight(false);
        }
        onlinePlayer.setFallDistance(0f);
    }


    public void releaseEntity(Player holdingPlayer, boolean throwEntity) {
        UUID holderId = (holdingPlayer != null) ? holdingPlayer.getUniqueId() : null;
        Entity entity = null;
        if (holderId != null) {
            entity = heldEntities.remove(holderId);
        } else {
        }


        if (entity != null) {
            if (entity instanceof Player targetPlayer) {
                resetPlayerState(targetPlayer);
                if (targetPlayer.isOnline()) {
                    targetPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_FREED));
                }
                playerHoldStartTime.remove(targetPlayer.getUniqueId());
            } else if (entity instanceof LivingEntity livingTarget) {
                livingTarget.setAI(true);
                entity.setGravity(true);
            }

            if (holdingPlayer != null && holdingPlayer.isOnline()) {
                holdingPlayer.getWorld().spawnParticle(pluginConfig.getReleaseParticle(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 20, 0.4, 0.4, 0.4, 0.02);
                if (throwEntity && entity.isValid()) {
                    entity.setVelocity(holdingPlayer.getEyeLocation().getDirection().multiply(pluginConfig.getThrowStrength()));
                    holdingPlayer.getWorld().playSound(holdingPlayer.getLocation(), pluginConfig.getThrowSound(), 1.0f, 0.9f);
                    holdingPlayer.getWorld().spawnParticle(pluginConfig.getThrowParticle(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 15, 0.3, 0.3, 0.3, 0.1);
                } else {
                    holdingPlayer.getWorld().playSound(holdingPlayer.getLocation(), pluginConfig.getReleaseSound(), 0.8f, 1.4f);
                }
            }
        }
    }

    public void releaseHeldEntityByHolderId(UUID holderId) {
        Entity entity = heldEntities.remove(holderId);
        if (entity != null) {
            if (entity instanceof Player targetPlayer) {
                resetPlayerState(targetPlayer);
                playerHoldStartTime.remove(targetPlayer.getUniqueId());
            } else if (entity instanceof LivingEntity livingTarget) {
                livingTarget.setAI(true);
                entity.setGravity(true);
            }
        }
    }

    private Entity getTargetEntity(Player player, double range) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), range, 0.5,
                (entity) -> !entity.equals(player) && (entity instanceof LivingEntity) && player.hasLineOfSight(entity) && !pluginConfig.getBannedEntityTypes().contains(entity.getType())
        );
        return (result != null && result.getHitEntity() != null) ? result.getHitEntity() : null;
    }

    public void performForceDash(Player player) {
        Vector initialDirection = player.getLocation().getDirection().clone();
        Vector velocity = initialDirection.clone().multiply(pluginConfig.getForceDashStrengthHorizontal());
        double yVelocity = pluginConfig.getForceDashStrengthVertical();
        if (player.getVelocity().getY() > 0) yVelocity += player.getVelocity().getY() * 0.5;
        velocity.setY(yVelocity);
        player.setVelocity(velocity);
        player.sendMessage(messageManager.getMessage(MessageManager.DASH_SUCCESS));
        player.getWorld().playSound(player.getLocation(), pluginConfig.getDashSound(), 1.0F, 1.3F);

        final int trailDurationTicks = pluginConfig.getDashTrailDurationTicks();
        final int particlesPerTick = pluginConfig.getDashTrailParticlesPerTick();
        final double trailOffsetBehind = pluginConfig.getDashTrailOffsetBehind();

        new BukkitRunnable() {
            int ticksRun = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticksRun >= trailDurationTicks) {
                    this.cancel(); return;
                }
                Vector currentVelocityDirection = player.getVelocity().clone();
                Vector trailDirection = (currentVelocityDirection.lengthSquared() > 0.01) ?
                        currentVelocityDirection.normalize().multiply(-1) :
                        initialDirection.clone().multiply(-1);

                Location particleLoc = player.getLocation().add(trailDirection.multiply(trailOffsetBehind));
                player.getWorld().spawnParticle(pluginConfig.getDashTrailParticleMain(), particleLoc.add(0, 0.7, 0), particlesPerTick, 0.25, 0.25, 0.25, 0.005);
                if (ticksRun % 2 == 0) player.getWorld().spawnParticle(pluginConfig.getDashTrailParticleSecondary(), particleLoc.add(0, -0.2, 0), 1, 0.15, 0.15, 0.15, 0.001);
                ticksRun++;
            }
        }.runTaskTimer(this, 1L, 1L);
        player.getWorld().spawnParticle(pluginConfig.getDashInitialBurstParticle(), player.getLocation().add(0,0.2,0), 15, 0.3,0.3,0.3,0.02);
    }

    public void performTargetedForcePush(Player player) {
        Entity target = getTargetEntity(player, pluginConfig.getMaxGrabRange() / pluginConfig.getTargetedPushRangeDivider());
        if (target != null && target instanceof LivingEntity && !target.equals(player)) {
            if (pluginConfig.getBannedEntityTypes().contains(target.getType())) {
                player.sendMessage(messageManager.getMessage(MessageManager.PUSH_TARGETED_FAIL_BANNED)); return;
            }
            Vector directionToTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            Vector pushDirection = directionToTarget.clone();
            pushDirection.setY(pushDirection.getY() + pluginConfig.getTargetedPushVerticalBoost());
            pushDirection.normalize();
            Vector velocity = pushDirection.multiply(pluginConfig.getTargetedPushStrength());
            target.setVelocity(velocity);
            player.sendMessage(messageManager.getMessage(MessageManager.PUSH_TARGETED_SUCCESS, "{targetName}", target.getName()));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getTargetedPushSound(), 0.7F, 1.0F);

            Location start = player.getEyeLocation();
            Vector step = directionToTarget.multiply(0.5);
            double distance = player.getLocation().distance(target.getLocation());
            double effectivePushRange = pluginConfig.getMaxGrabRange() / pluginConfig.getTargetedPushRangeDivider();
            if (distance > effectivePushRange) distance = effectivePushRange;

            for (int i = 0; i < distance / 0.5; i++) {
                start.add(step);
                player.getWorld().spawnParticle(pluginConfig.getTargetedPushWaveParticleMain(), start, 1, 0,0,0,0);
                if (i % 2 == 0) player.getWorld().spawnParticle(pluginConfig.getTargetedPushWaveParticleSecondary(), start, 1, 0.1,0.1,0.1,0);
            }
            player.getWorld().spawnParticle(pluginConfig.getTargetedPushImpactParticle(), target.getLocation().add(0,target.getHeight()/2,0), 10, 0.5,0.5,0.5, 0.1);
        } else {
            player.sendMessage(messageManager.getMessage(MessageManager.PUSH_TARGETED_FAIL_NO_TARGET));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getTargetedPushFailSound(), 1.0f, 1.0f);
        }
    }

    public void performAoeForcePush(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(pluginConfig.getAoePushRadius(), pluginConfig.getAoePushRadius(), pluginConfig.getAoePushRadius());
        boolean pushedAnyone = false;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !entity.equals(player) && !pluginConfig.getBannedEntityTypes().contains(entity.getType())) {
                Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                if (direction.lengthSquared() == 0) continue;
                direction.normalize();
                direction.setY(direction.getY() + pluginConfig.getAoePushVerticalBoost());
                direction.normalize();
                Vector velocity = direction.multiply(pluginConfig.getAoePushStrength());
                entity.setVelocity(velocity);
                pushedAnyone = true;
            }
        }
        if (pushedAnyone) {
            player.sendMessage(messageManager.getMessage(MessageManager.PUSH_AOE_SUCCESS));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getAoePushSound(), 0.8F, 1.0F);
            player.getWorld().spawnParticle(pluginConfig.getAoePushCenterParticle(), player.getLocation(), 1);
            for (double r = 1.0; r <= pluginConfig.getAoePushRadius(); r += 1.0) {
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    Location particleLoc = player.getLocation().clone().add(r * Math.cos(angle), 0.5, r * Math.sin(angle));
                    player.getWorld().spawnParticle(pluginConfig.getAoePushWaveParticle(), particleLoc, 1, 0, 0, 0, 0);
                }
            }
        } else {
            player.sendMessage(messageManager.getMessage(MessageManager.PUSH_AOE_FAIL_NO_TARGETS));
            player.getWorld().playSound(player.getLocation(), pluginConfig.getAoePushFailSound(), 1.0f, 1.0f);
        }
    }

    private boolean checkCooldown(Player player, Map<UUID, Long> cooldownMap, int cooldownSeconds, String abilityNameKey) {
        String localizedAbilityName = messageManager.getMessage(abilityNameKey);
        if (cooldownMap.containsKey(player.getUniqueId())) {
            long secondsSinceLastUse = (System.currentTimeMillis() - cooldownMap.get(player.getUniqueId())) / 1000;
            if (secondsSinceLastUse < cooldownSeconds) {
                player.sendMessage(messageManager.getMessage(MessageManager.COOLDOWN_MESSAGE,
                        "{abilityName}", localizedAbilityName,
                        "{timeLeft}", String.valueOf(cooldownSeconds - secondsSinceLastUse)));
                return false;
            }
        }
        cooldownMap.remove(player.getUniqueId());
        return true;
    }

    private boolean isCooldownActive(Player player, Map<UUID, Long> cooldownMap, int cooldownSeconds) {
        if (cooldownMap.containsKey(player.getUniqueId())) {
            long secondsSinceLastUse = (System.currentTimeMillis() - cooldownMap.get(player.getUniqueId())) / 1000;
            if (secondsSinceLastUse < cooldownSeconds) return true;
            else cooldownMap.remove(player.getUniqueId());
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!isForceCrystal(itemInHand)) return;

        event.setCancelled(true);

        if (heldEntities.containsKey(player.getUniqueId())) {
            if (player.isSneaking()) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    releaseEntity(player, true);
                    player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_RELEASE_THROW));
                }
            } else {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    releaseEntity(player, false);
                    player.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_RELEASE_GENTLE));
                }
            }
        } else {
            if (player.isSneaking()) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (!player.hasPermission("jeditelekenisis.forcepush.targeted")) {
                        player.sendMessage(messageManager.getMessage(MessageManager.NO_PERMISSION_ABILITY_PUSH_TARGETED)); return;
                    }
                    if (checkCooldown(player, targetedPushCooldowns, pluginConfig.getTargetedPushCooldownSeconds(), MessageManager.ABILITY_NAME_TARGETED_PUSH)) {
                        performTargetedForcePush(player);
                        targetedPushCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    }
                } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (!player.hasPermission("jeditelekenisis.forcepush.aoe")) {
                        player.sendMessage(messageManager.getMessage(MessageManager.NO_PERMISSION_ABILITY_PUSH_AOE)); return;
                    }
                    if (checkCooldown(player, aoePushCooldowns, pluginConfig.getAoePushCooldownSeconds(), MessageManager.ABILITY_NAME_AOE_PUSH)) {
                        performAoeForcePush(player);
                        aoePushCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    }
                }
            } else {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (!player.hasPermission("jeditelekenisis.telekinesis")) {
                        player.sendMessage(messageManager.getMessage(MessageManager.NO_PERMISSION_ABILITY_TELEKINESIS)); return;
                    }
                    tryGrabEntityWithCrystal(player);
                } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (!player.hasPermission("jeditelekenisis.forcedash")) {
                        player.sendMessage(messageManager.getMessage(MessageManager.NO_PERMISSION_ABILITY_DASH)); return;
                    }
                    if (checkCooldown(player, dashCooldowns, pluginConfig.getForceDashCooldownSeconds(), MessageManager.ABILITY_NAME_DASH)) {
                        performForceDash(player);
                        dashCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (isForceCrystal(itemInHand) && player.isSneaking() &&
                player.hasPermission("jeditelekenisis.forcepush.aoe") &&
                !isCooldownActive(player, aoePushCooldowns, pluginConfig.getAoePushCooldownSeconds()) &&
                !event.getEntity().equals(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitingPlayer = event.getPlayer();
        Entity heldByQuitingPlayer = heldEntities.get(quitingPlayer.getUniqueId());

        if (heldByQuitingPlayer != null) {
            releaseEntity(quitingPlayer, false);
        }

        UUID holderIdOfQuitingPlayer = null;
        for(Map.Entry<UUID, Entity> entry : heldEntities.entrySet()){
            if(entry.getValue().equals(quitingPlayer)){
                holderIdOfQuitingPlayer = entry.getKey();
                break;
            }
        }
        if(holderIdOfQuitingPlayer != null){
            Player holder = Bukkit.getPlayer(holderIdOfQuitingPlayer);
            if(holder != null && holder.isOnline()){
                releaseEntity(holder, false);
                holder.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_PLAYER_QUIT_RELEASED, "{playerName}", quitingPlayer.getName()));
            } else {
                releaseHeldEntityByHolderId(holderIdOfQuitingPlayer);
            }
        }

        playerHoldStartTime.remove(quitingPlayer.getUniqueId());
        dashCooldowns.remove(quitingPlayer.getUniqueId());
        targetedPushCooldowns.remove(quitingPlayer.getUniqueId());
        aoePushCooldowns.remove(quitingPlayer.getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity deceasedEntity = event.getEntity();
        UUID deadEntityUUID = deceasedEntity.getUniqueId();
        UUID holdingPlayerId = null;

        for (Map.Entry<UUID, Entity> entry : heldEntities.entrySet()) {
            if (entry.getValue().getUniqueId().equals(deadEntityUUID)) {
                holdingPlayerId = entry.getKey();
                break;
            }
        }

        if (holdingPlayerId != null) {
            Player holdingPlayer = Bukkit.getPlayer(holdingPlayerId);
            if (deceasedEntity instanceof Player) {
                Player deadHeldPlayer = (Player) deceasedEntity;
                resetPlayerState(deadHeldPlayer);
                playerHoldStartTime.remove(deadHeldPlayer.getUniqueId());
                if (deadHeldPlayer.isOnline()) {
                    deadHeldPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_DEATH_HELD_PLAYER_FREED));
                }
            }
            if (holdingPlayer != null && holdingPlayer.isOnline()) {
                releaseEntity(holdingPlayer, false);
                holdingPlayer.sendMessage(messageManager.getMessage(MessageManager.TELEKINESIS_DEATH_HOLDER, "{entityName}", deceasedEntity.getName()));
            } else {
                releaseHeldEntityByHolderId(holdingPlayerId);
            }
        } else {
            if (deceasedEntity instanceof Player) {
                Player deadPlayer = (Player) deceasedEntity;
                playerHoldStartTime.remove(deadPlayer.getUniqueId());
                resetPlayerState(deadPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        resetPlayerState(player);
        playerHoldStartTime.remove(player.getUniqueId());
    }
}