package xyz.foxdevtime.jeditelekinesis;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class TelekinesisReleaseCommand implements CommandExecutor {

    private final JediTelekinesis plugin;

    public TelekinesisReleaseCommand(JediTelekinesis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_PLAYERS_ONLY));
            return true;
        }

        Player playerToRelease = (Player) sender;
        if (!playerToRelease.hasPermission("jeditelekenisis.command.release")) {
            playerToRelease.sendMessage(plugin.getMessageManager().getMessage(MessageManager.NO_PERMISSION_RELEASE_COMMAND));
            return true;
        }

        Player holdingPlayer = null;
        UUID holdingPlayerId = null;

        for (Map.Entry<UUID, Entity> entry : plugin.getHeldEntities().entrySet()) {
            if (entry.getValue().getUniqueId().equals(playerToRelease.getUniqueId())) {
                holdingPlayerId = entry.getKey();
                // holdingPlayer = plugin.getServer().getPlayer(holdingPlayerId);
                break;
            }
        }

        if (holdingPlayerId != null) {
            holdingPlayer = plugin.getServer().getPlayer(holdingPlayerId);
            if (holdingPlayer != null && holdingPlayer.isOnline()) {
                plugin.releaseEntity(holdingPlayer, false);
                playerToRelease.sendMessage(plugin.getMessageManager().getMessage(MessageManager.RELEASE_COMMAND_SUCCESS));
                holdingPlayer.sendMessage(plugin.getMessageManager().getMessage(MessageManager.RELEASE_COMMAND_HOLDER_NOTIFIED, "{playerName}", playerToRelease.getName()));
            } else {
                plugin.releaseHeldEntityByHolderId(holdingPlayerId);
                playerToRelease.sendMessage(plugin.getMessageManager().getMessage(MessageManager.RELEASE_COMMAND_HOLDER_OFFLINE));
            }
        } else {
            playerToRelease.sendMessage(plugin.getMessageManager().getMessage(MessageManager.RELEASE_COMMAND_NOT_HELD));
        }
        return true;
    }
}