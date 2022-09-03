package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class KompassFürNeuGejointeHunter implements Listener {
    ManhuntMain plugin;
    public KompassFürNeuGejointeHunter(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onHunterJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //Code-Abbruch, wenn Spieler kein Hunter ist
        if(!ManhuntMain.hunterListe.contains(player.getName())) {
            return;
        }
        if (!player.getInventory().contains(Material.COMPASS)) {
            player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
        }
        if (ManhuntMain.speedrunnerListe.size() > 0) {
            String nunVerfolgterSpeedrunner_Name = ManhuntMain.speedrunnerListe.get(0);
            ManhuntMain.wessenKompassZeigtAufWenGerade.put(player.getName(), nunVerfolgterSpeedrunner_Name);
            //Nachricht:
            ManhuntMain.aufWenZeigtKompassNachricht(player.getName(), nunVerfolgterSpeedrunner_Name);
        }
    }
}
