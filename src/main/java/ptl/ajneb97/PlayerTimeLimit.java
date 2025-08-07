package ptl.ajneb97;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ptl.ajneb97.api.ExpansionPlayerTimeLimit;
import ptl.ajneb97.api.PlayerTimeLimitAPI;
import ptl.ajneb97.configs.ConfigsManager;
import ptl.ajneb97.listeners.PlayerListener;
import ptl.ajneb97.managers.MessageManager;
import ptl.ajneb97.managers.PlayerManager;
import ptl.ajneb97.managers.ServerManager;
import ptl.ajneb97.tasks.DataSaveTask;
import ptl.ajneb97.tasks.PlayerTimeTask;
import ptl.ajneb97.tasks.ServerTimeResetTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PlayerTimeLimit extends JavaPlugin {

    public static String PLUGIN_PREFIX = ChatColor.translateAlternateColorCodes('&', "&8[&bPlayerTime&cLimit&8] ");
    public String latestversion;
    public String rutaConfig;
    PluginDescriptionFile pdfFile = getDescription();
    public String version = pdfFile.getVersion();
    private PlayerManager playerManager;
    private ConfigsManager configsManager;
    private MessageManager messageManager;
    private ServerManager serverManager;

    private DataSaveTask dataSaveTask;

    public void onEnable() {
        this.playerManager = new PlayerManager(this);
        this.serverManager = new ServerManager(this);

        registerConfig();
        registerEvents();

        Objects.requireNonNull(getCommand("playertimelimit"), "Command unknown. Is your plugin.yml up to date?").setExecutor(new PlayerTimeCommand(this));

        this.configsManager = new ConfigsManager(this);
        this.configsManager.configurar();

        serverManager.executeDataTime();

        final var timeTask = new PlayerTimeTask(this);
        timeTask.start();
        ServerTimeResetTask serverTask = new ServerTimeResetTask(this);
        serverTask.start();

        recargarDataSaveTask();
        checkMessagesUpdate();

        final var api = new PlayerTimeLimitAPI(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ExpansionPlayerTimeLimit(this).register();
        }

        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Thanks for using my plugin!  " + ChatColor.WHITE + "~Ajneb97");
    }

    public void onDisable() {
        this.configsManager.getPlayerConfigsManager().guardarJugadores();
        serverManager.saveDataTime();
        Bukkit.getConsoleSender().sendMessage(PLUGIN_PREFIX + ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
    }

    public void registerConfig() {
        File config = new File(this.getDataFolder(), "config.yml");
        rutaConfig = config.getPath();
        if (!config.exists()) {
            this.getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    public void recargarConfigs() {
        this.configsManager.getMensajesConfigManager().reloadMessages();
        this.configsManager.getPlayerConfigsManager().guardarJugadores();
        reloadConfig();
        this.configsManager.getMainConfigManager().configurar();

        recargarDataSaveTask();
    }

    public void recargarDataSaveTask() {
        if (dataSaveTask != null) {
            dataSaveTask.end();
        }
        dataSaveTask = new DataSaveTask(this);
        dataSaveTask.start(getConfig().getInt("data_save_time"));
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public FileConfiguration getMessages() {
        return this.configsManager.getMensajesConfigManager().getMessages();
    }

    public ConfigsManager getConfigsManager() {
        return configsManager;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void checkMessagesUpdate() {
        Path archivoConfig = Paths.get(rutaConfig);
        Path archivoMessages = Paths.get(configsManager.getMensajesConfigManager().getPath());
        try {
            String textoConfig = new String(Files.readAllBytes(archivoConfig));
            String textoMessages = new String(Files.readAllBytes(archivoMessages));
            FileConfiguration messages = configsManager.getMensajesConfigManager().getMessages();

            if (!textoMessages.contains("commandResetTimeError:")) {
                messages.set("commandResetTimeError", "&cYou need to use: &7/ptl resettime <player>");
                messages.set("commandResetTimeCorrect", "&aCurrent time has been reset for player &7%player%&a!");
                messages.set("commandTakeTimeError", "&cYou need to use: &7/ptl taketime <player> <time>");
                messages.set("invalidNumber", "&cYou need to use a valid number!");
                messages.set("commandTakeTimeCorrect", "&aTaken &7%time% seconds &afrom &7%player% &atime!");
                messages.set("playerNotOnline", "&cThat player is not online.");
                messages.set("commandAddTimeError", "&cYou need to use: &7/ptl addtime <player> <time>");
                messages.set("commandAddTimeCorrect", "&aAdded &7%time% seconds &ato &7%player% &atime!");
                configsManager.getMensajesConfigManager().saveMessages();
            }
            if (!textoConfig.contains("world_whitelist_system:")) {
                getConfig().set("world_whitelist_system.enabled", false);
                List<String> lista = new ArrayList<String>();
                lista.add("world");
                lista.add("world_nether");
                lista.add("world_the_end");
                getConfig().set("world_whitelist_system.worlds", lista);
                getConfig().set("world_whitelist_system.teleport_coordinates_on_kick", "spawn;0;60;0;90;0");
                saveConfig();
            }
            if (!textoConfig.contains("update_notification:")) {
                getConfig().set("update_notification", true);
                saveConfig();
                messages.set("playerDoesNotExists", "&cThat player doesn't exists.");
                List<String> lista = new ArrayList<String>();
                lista.add("&c&m                                          ");
                lista.add("&7&l%player% Data:");
                lista.add("&7Time left: &a%time_left%");
                lista.add("&7Total played time: &a%total_time%");
                lista.add("&c&m                                          ");
                messages.set("checkCommandMessage", lista);
                lista = new ArrayList<String>();
                lista.add("&c&m                                          ");
                lista.add("&7Exact time when playtimes will be reset:");
                lista.add("&e%reset_time%");
                lista.add("");
                lista.add("&7Remaining time until reset:");
                lista.add("&e%remaining%");
                lista.add("&c&m                                          ");
                messages.set("infoCommandMessage", lista);
                configsManager.getMensajesConfigManager().saveMessages();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
