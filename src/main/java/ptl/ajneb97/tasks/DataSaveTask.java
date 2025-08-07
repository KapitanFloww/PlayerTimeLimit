package ptl.ajneb97.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import ptl.ajneb97.PlayerTimeLimit;

import java.util.Objects;

public class DataSaveTask {

    private final PlayerTimeLimit plugin;
    private boolean end;

    public DataSaveTask(PlayerTimeLimit plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.end = false;
    }

    public void end() {
        end = true;
    }

    public void start(int minutes) {
        long ticks = (long) minutes * 60 * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (end) {
                    this.cancel();
                } else {
                    execute();
                }
            }

        }.runTaskTimerAsynchronously(plugin, 0L, ticks);
    }

    public void execute() {
        plugin.getConfigsManager().getPlayerConfigsManager().guardarJugadores();
        plugin.getServerManager().saveDataTime();
    }
}
