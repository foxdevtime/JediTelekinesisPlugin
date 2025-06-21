package xyz.foxdevtime.jeditelekinesis;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TelekinesisCommand implements CommandExecutor {

    private final JediTelekinesis plugin;

    public TelekinesisCommand(JediTelekinesis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_PLAYERS_ONLY));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("jeditelekenisis.command.givecrystal")) {
            player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.NO_PERMISSION_COMMAND));
            return true;
        }

        ItemStack crystal = plugin.createForceCrystal();
        player.getInventory().addItem(crystal);
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.CRYSTAL_RECEIVED));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HEADER));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HAND));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_RMB));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_LMB));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_SHIFT_RMB));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_FREE_SHIFT_LMB));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_HEADER));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_RMB));
        //player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_LMB));
        player.sendMessage(plugin.getMessageManager().getMessage(MessageManager.COMMAND_CRYSTAL_GIVE_INSTRUCTION_HOLDING_SHIFT_RMB));

        return true;
    }
}