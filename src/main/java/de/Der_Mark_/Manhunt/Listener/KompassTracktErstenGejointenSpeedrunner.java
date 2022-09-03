package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class KompassTracktErstenGejointenSpeedrunner implements Listener {
    ManhuntMain plugin;
    public KompassTracktErstenGejointenSpeedrunner(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpeedrunnerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //Code-Abbruch, wenn Spieler kein Speedrunner ist
        if(!ManhuntMain.speedrunnerListe.contains(player.getName())) {
            return;
        }
        if (schonAndereSpeedrunnerOnline(player)) {return; }
        for (String hunterName : ManhuntMain.hunterListe) {
            Player hunter = plugin.getServer().getPlayer(hunterName);
            if (hunter != null) {
                ManhuntMain.wessenKompassZeigtAufWenGerade.put(hunter.getName(), player.getName());
                //Nachricht:
                ManhuntMain.aufWenZeigtKompassNachricht(hunter.getName(), player.getName());
            }
        }
    }

    private boolean schonAndereSpeedrunnerOnline(Player speedrunner) {
        for (String srName : ManhuntMain.speedrunnerListe) {
            if (!srName.equals(speedrunner.getName())) {
                return true;
            }
        }
        return false;
    }
}
