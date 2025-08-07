package ptl.ajneb97.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import ptl.ajneb97.PlayerTimeLimit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ServerTimeResetTask {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm");

    private final PlayerTimeLimit plugin;

    public ServerTimeResetTask(PlayerTimeLimit plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                execute();
            }

        }.runTaskTimer(plugin, 0L, 1200L); // all 60 seconds
    }

    public void execute() {
        final var mainConfig = plugin.getConfigsManager().getMainConfigManager();
        final var resetTime = mainConfig.getResetTime();
        final var now = LocalDateTime.now();
        final var currentTime = DTF.format(now);
        if (resetTime.equals(currentTime)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getPlayerManager().resetPlayers();
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
