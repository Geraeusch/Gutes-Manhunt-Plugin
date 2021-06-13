package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpeedrunnerTodListener implements Listener {
    ManhuntMain plugin;
    public SpeedrunnerTodListener(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpeedrunnerDeath(PlayerDeathEvent event) {
        //Code-Abbruch, wenn Spiel schon entschieden ist
        if(ManhuntMain.siegFürSpeedrunner != null) {
            return;
        }
        final Player player = event.getEntity();
        //Code-Abbruch, wenn gestorbener Spieler kein Speedrunner ist
        if(!ManhuntMain.speedrunnerListe.contains(player.getName())) {
            return;
        }
        //Code-Abbruch, wenn Spieler schon einmal gestorben ist
        if(ManhuntMain.gestorbeneSpeedrunnerListe.contains(player.getName())) {
            return;
        }

        ManhuntMain.gestorbeneSpeedrunnerListe.add(player.getName());
        final int anzahlVerbleibendeSpeedrunner = ManhuntMain.speedrunnerListe.size() - ManhuntMain.gestorbeneSpeedrunnerListe.size();
        new BukkitRunnable() {
            double gerundeteSiegerVerbliebeneKP;
            @Override
            public void run() {
                if(anzahlVerbleibendeSpeedrunner != 0) {
                    if (!ManhuntMain.EndeWurdeBetreten()) {
                        Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + player.getName() + " ist nun gestorben und kann somit nicht mehr als erstes das Ende betreten. " +
                                anzahlVerbleibendeSpeedrunner + " Speedrunner verbleiben.");
                    } else {
                        Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + player.getName() + " ist nun gestorben. " +
                                anzahlVerbleibendeSpeedrunner + " Speedrunner verbleiben.");
                    }
                } else {
                    ManhuntMain.siegFürSpeedrunner = false;
                    Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + player.getName() + " ist als letzter verbleibender Speedrunner gestorben, somit haben die Hunter gewonnen!!!");
                }
                this.cancel();
            }
        }.runTaskTimer(plugin,0,1);
    }
}
