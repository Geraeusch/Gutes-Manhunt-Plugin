package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GestorbeneSpeedrunnerNichtAlsErstesInsEnde implements Listener {
    ManhuntMain plugin;
    public GestorbeneSpeedrunnerNichtAlsErstesInsEnde(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpielerWillEndeBetreten(PlayerTeleportEvent event) {
        //Code-Abbruch, wenn Teleport kein Teleport ins Ende wäre
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }
        Player player = event.getPlayer();
        //Ende-Wurde-Betreten-Zustand wird gespeichert:
        if(ManhuntMain.speedrunnerListe.contains(player.getName())) {
            if(!ManhuntMain.gestorbeneSpeedrunnerListe.contains(player.getName())) {
                ManhuntMain.endeWurdeBetreten = true;
            }
        }
        if(ManhuntMain.hunterListe.contains(player.getName())) {
            ManhuntMain.endeWurdeBetreten = true;
        }
        //Code-Abbruch, wenn Spieler kein gestorbener Speedrunner ist
        if(!ManhuntMain.gestorbeneSpeedrunnerListe.contains(player.getName())) {
            return;
        }
        //Code-Abbruch, wenn Ende schon betreten wurde
        if(ManhuntMain.endeWurdeBetreten) {
            return;
        }
        //Code-Abbruch, wenn Spiel schon entschieden ist
        if(ManhuntMain.siegFürSpeedrunner != null) {
            return;
        }
        //Event-Abbruch
        event.setCancelled(true);
        player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Du kannst das Ende nicht als Erster betreten, da du Speedrunner bist und bereits gestorben bist. " +
                "Du dienst nur noch dazu, die verbleibenden Speedrunner zu unterstützen.");
    }
}
