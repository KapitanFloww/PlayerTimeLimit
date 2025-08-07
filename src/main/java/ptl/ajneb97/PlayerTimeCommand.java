package ptl.ajneb97;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ptl.ajneb97.managers.MessageManager;
import ptl.ajneb97.managers.PlayerManager;
import ptl.ajneb97.model.TimeLimitPlayer;
import ptl.ajneb97.utils.UtilsTime;

import java.util.List;
import java.util.Objects;

public class PlayerTimeCommand implements CommandExecutor {

    private final PlayerTimeLimit plugin;

    public PlayerTimeCommand(PlayerTimeLimit plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        FileConfiguration messages = plugin.getMessages();
        MessageManager msgManager = plugin.getMessageManager();
        if (!(sender instanceof Player player)) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    reload(args, sender, messages, msgManager);
                } else if (args[0].equalsIgnoreCase("resettime")) {
                    resettime(args, sender, messages, msgManager);
                } else if (args[0].equalsIgnoreCase("taketime")) {
                    taketime(args, sender, messages, msgManager);
                } else if (args[0].equalsIgnoreCase("addtime")) {
                    addtime(args, sender, messages, msgManager);
                }
            }

            return false;
        }
		boolean hasPermissions = player.isOp() || player.hasPermission("playertimelimit.admin");
        if (args.length >= 1) {
            if (player.hasPermission("playertimelimit.command." + args[0].toLowerCase())) {
                hasPermissions = true;
            }
            if (!hasPermissions) {
                msgManager.enviarMensaje(sender, messages.getString("noPermissions"), true);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                reload(args, sender, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("message")) {
                message(args, player, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("info")) {
                info(args, player, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("check")) {
                check(args, player, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("resettime")) {
                resettime(args, player, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("taketime")) {
                taketime(args, player, messages, msgManager);
            } else if (args[0].equalsIgnoreCase("addtime")) {
                addtime(args, player, messages, msgManager);
            } else {
                help(sender);
            }
        } else {
            if (hasPermissions) {
                help(sender);
            } else {
                msgManager.enviarMensaje(sender, messages.getString("noPermissions"), true);
            }
        }

        return true;

    }

    public void help(CommandSender sender) {
        sender.sendMessage(MessageManager.getMensajeColor("&c&m                                                                    "));
        sender.sendMessage(MessageManager.getMensajeColor("      &b&lPlayerTime&c&lLimit &eCommands"));
        sender.sendMessage(MessageManager.getMensajeColor(" "));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl message &7Enables or disables the time limit information message for yourself."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl info &7Checks the remaining time for playtimes reset."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl check <player> &7Checks player time left and total time."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl resettime <player> &7Resets playtime for a player."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl addtime <player> <time> &7Adds playtime to a player."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl taketime <player> <time> &7Takes playtime from a player."));
        sender.sendMessage(MessageManager.getMensajeColor("&8- &c/ptl reload &7Reloads the config."));
        sender.sendMessage(MessageManager.getMensajeColor(" "));
        sender.sendMessage(MessageManager.getMensajeColor("&c&m                                                                    "));
    }

    public void reload(String[] args, CommandSender sender, FileConfiguration messages, MessageManager msgManager) {
        plugin.recargarConfigs();
        msgManager.enviarMensaje(sender, messages.getString("commandReload"), true);
    }

    public void message(String[] args, Player player, FileConfiguration messages, MessageManager msgManager) {
        // /ptl message
        TimeLimitPlayer p = plugin.getPlayerManager().getPlayerByUUID(player.getUniqueId().toString());
        boolean messagesEnabled = p.isMessageEnabled();
        if (messagesEnabled) {
            msgManager.enviarMensaje(player, messages.getString("messageDisabled"), true);
        } else {
            msgManager.enviarMensaje(player, messages.getString("messageEnabled"), true);
        }
        p.setMessageEnabled(!messagesEnabled);
    }

    public void info(String[] args, Player player, FileConfiguration messages, MessageManager msgManager) {
        // /ptl info
        String timeReset = plugin.getConfigsManager().getMainConfigManager().getResetTime();
        String remaining = plugin.getServerManager().getRemainingTimeForTimeReset();

        List<String> msg = messages.getStringList("infoCommandMessage");
        for (String m : msg) {
            player.sendMessage(MessageManager.getMensajeColor(m.replace("%reset_time%", timeReset)
                    .replace("%remaining%", remaining)));
        }
    }

    public void check(String[] args, Player player, FileConfiguration messages, MessageManager msgManager) {
        // /ptl check <player>
        TimeLimitPlayer p = null;
        if (args.length == 1) {
            p = plugin.getPlayerManager().getPlayerByUUID(player.getUniqueId().toString());
        } else {
            if (player.isOp() || player.hasPermission("playertimelimit.admin")
                    || player.hasPermission("playertimelimit.command.check.others")) {
                p = plugin.getPlayerManager().getPlayerByName(args[1]);
            } else {
                msgManager.enviarMensaje(player, messages.getString("noPermissions"), true);
                return;
            }
        }

        if (p == null) {
            msgManager.enviarMensaje(player, messages.getString("playerDoesNotExists"), true);
            return;
        }

        Player checkPlayer = Bukkit.getPlayer(p.getName());
        if (checkPlayer == null) {
            msgManager.enviarMensaje(player, messages.getString("playerNotOnline"), true);
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        int timeLimit = playerManager.getTimeLimitPlayer(checkPlayer);
        String timeLeft = playerManager.getTimeLeft(p, timeLimit);
        String totalTime = UtilsTime.getTime(p.getTotalTime(), msgManager);
        List<String> msg = messages.getStringList("checkCommandMessage");
        for (String m : msg) {
            player.sendMessage(MessageManager.getMensajeColor(m.replace("%player%", p.getName())
                    .replace("%time_left%", timeLeft).replace("%total_time%", totalTime)));
        }
    }

    public void resettime(String[] args, CommandSender sender, FileConfiguration messages, MessageManager msgManager) {
        // /ptl resettime <player>
        if (args.length == 1) {
            msgManager.enviarMensaje(sender, messages.getString("commandResetTimeError"), true);
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
        if (p == null) {
            msgManager.enviarMensaje(sender, messages.getString("playerDoesNotExists"), true);
            return;
        }

        p.resetTime();

        msgManager.enviarMensaje(sender, messages.getString("commandResetTimeCorrect")
                .replace("%player%", args[1]), true);
    }

    //Se puede usar solo cuando el jugador esta conectado
    public void taketime(String[] args, CommandSender sender, FileConfiguration messages, MessageManager msgManager) {
        // /ptl taketime <player> <time>
        if (args.length <= 2) {
            msgManager.enviarMensaje(sender, messages.getString("commandTakeTimeError"), true);
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
        if (p == null) {
            msgManager.enviarMensaje(sender, messages.getString("playerDoesNotExists"), true);
            return;
        }

        if (Bukkit.getPlayer(p.getName()) == null) {
            msgManager.enviarMensaje(sender, messages.getString("playerNotOnline"), true);
            return;
        }

        int time = 0;
        try {
            time = Integer.valueOf(args[2]);
            if (time <= 0) {
                msgManager.enviarMensaje(sender, messages.getString("invalidNumber"), true);
                return;
            }
        } catch (NumberFormatException e) {
            msgManager.enviarMensaje(sender, messages.getString("invalidNumber"), true);
            return;
        }

        playerManager.takeTime(p, time);

        msgManager.enviarMensaje(sender, messages.getString("commandTakeTimeCorrect")
                .replace("%player%", args[1]).replace("%time%", time + ""), true);
    }

    //Se puede usar solo cuando el jugador esta conectado
    public void addtime(String[] args, CommandSender sender, FileConfiguration messages, MessageManager msgManager) {
        // /ptl addtime <player> <time>
        if (args.length <= 2) {
            msgManager.enviarMensaje(sender, messages.getString("commandAddTimeError"), true);
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        TimeLimitPlayer p = playerManager.getPlayerByName(args[1]);
        if (p == null) {
            msgManager.enviarMensaje(sender, messages.getString("playerDoesNotExists"), true);
            return;
        }

        if (Bukkit.getPlayer(p.getName()) == null) {
            msgManager.enviarMensaje(sender, messages.getString("playerNotOnline"), true);
            return;
        }

        int time = 0;
        try {
            time = Integer.valueOf(args[2]);
            if (time <= 0) {
                msgManager.enviarMensaje(sender, messages.getString("invalidNumber"), true);
                return;
            }
        } catch (NumberFormatException e) {
            msgManager.enviarMensaje(sender, messages.getString("invalidNumber"), true);
            return;
        }

        playerManager.addTime(p, time);

        msgManager.enviarMensaje(sender, messages.getString("commandAddTimeCorrect")
                .replace("%player%", args[1]).replace("%time%", time + ""), true);
    }
}
