package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class KompassZielWechseln implements Listener {
    ManhuntMain plugin;
    public KompassZielWechseln(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRechtsklick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //Code-Abbruch, wenn Hand Offhand ist
        if(event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        //Code-Abbruch, wenn Spieler kein Hunter ist
        if (!ManhuntMain.hunterListe.contains(player.getName())) {
            return;
        }
        //Code-Abbruch, wenn Spieler keinen Kompass in der Hand hat
        if(!(
                player.getInventory().getItemInMainHand().getType() == Material.COMPASS ||
                (player.getInventory().getItemInOffHand().getType() == Material.COMPASS && player.getInventory().getItemInMainHand().getType() == Material.AIR)
        )) {
            return;
        }
        //Code-Abbruch, wenn kein Rechtsklick
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        //Code-Abbruch mit Fehlermeldung, wenn keine Speedrunner vorhanden
        if (ManhuntMain.speedrunnerListe.size() == 0) {
            player.sendMessage( ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Du kannst kein Ziel auswählen, da es aktuell noch keine Speedrunner gibt.");
            return;
        }
        String zuvorVerfolgterSpeedrunner_Name = ManhuntMain.wessenKompassZeigtAufWenGerade.get(player.getName());
        int zuvorVerfolgterSpeedrunner_Index = -1;
        String speedrunnerName;
        for (int i = 0; i < ManhuntMain.speedrunnerListe.size(); i++) {
            speedrunnerName = ManhuntMain.speedrunnerListe.get(i);
            if(speedrunnerName.equals(zuvorVerfolgterSpeedrunner_Name)) {
                zuvorVerfolgterSpeedrunner_Index = i;
            }
        }
        int neuerSpeedrunnerIndex;
        if(zuvorVerfolgterSpeedrunner_Index == ManhuntMain.speedrunnerListe.size() - 1) {
            neuerSpeedrunnerIndex = 0;
        } else {
            neuerSpeedrunnerIndex = zuvorVerfolgterSpeedrunner_Index + 1;
        }
        String nunVerfolgterSpeedrunner_Name = ManhuntMain.speedrunnerListe.get(neuerSpeedrunnerIndex);
        ManhuntMain.wessenKompassZeigtAufWenGerade.put(player.getName(), nunVerfolgterSpeedrunner_Name);
        //Nachricht:
        ManhuntMain.aufWenZeigtKompassNachricht(player.getName(), nunVerfolgterSpeedrunner_Name);
    }
}
