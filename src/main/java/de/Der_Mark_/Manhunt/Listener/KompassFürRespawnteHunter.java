package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class KompassFürRespawnteHunter implements Listener {
    ManhuntMain plugin;
    public KompassFürRespawnteHunter(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onHunterRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        //Code-Abbruch, wenn Spieler kein Hunter ist
        if(!ManhuntMain.hunterListe.contains(player.getName())) {
            return;
        }
        player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
    }
}
