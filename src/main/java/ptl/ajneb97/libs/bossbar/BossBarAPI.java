package ptl.ajneb97.libs.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ptl.ajneb97.managers.MessageManager;

public class BossBarAPI {

    public static BossBar create(Player jugador, String titulo, BarColor color, BarStyle style) {
        BossBar bossBar = Bukkit.getServer().createBossBar(MessageManager.getMensajeColor(titulo), color, style);
        bossBar.removeAll();
        bossBar.addPlayer(jugador);
        bossBar.setProgress(0);
        bossBar.setVisible(true);
        return bossBar;
    }
}
