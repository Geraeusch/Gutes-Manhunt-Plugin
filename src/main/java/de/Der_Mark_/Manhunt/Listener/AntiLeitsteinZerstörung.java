package de.Der_Mark_.Manhunt.Listener;

import de.Der_Mark_.Manhunt.ManhuntMain;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class AntiLeitsteinZerstörung implements Listener {
    ManhuntMain plugin;
    public AntiLeitsteinZerstörung(ManhuntMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeitsteinAbbau(BlockBreakEvent event) {
        Block block = event.getBlock();
        //Code-Abbruch, wenn Block kein Leistein ist
        if(block.getType() != Material.LODESTONE) {
            return;
        }
         Player player = event.getPlayer();
        //Code-Abbruch, wenn Spieler im Kreativ-Modus ist
        if(player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Location loc = block.getLocation();
        //Code-Abbruch, wenn der Leitstein nicht zu einem Speedrunner gehört
        if (!ManhuntMain.welcherBlockWarBevorLeitsteinHier.containsKey(loc)) {
            return;
        }

        //Alternative Bestimmung:
        /*
        //Code-Abbruch, wenn der Leitstein nicht auf der richtigen Höhe ist
        if (loc.getY() != 0 && (loc.getWorld().getEnvironment() != World.Environment.THE_END || loc.getY() != 255)) {
            return;
        }

        boolean aktiverLeistein;
        Player speedrunner;
        for (String speedrunnerName : ManhuntMain.speedrunnerListe) {
            speedrunner = player.getServer().getPlayer(speedrunnerName);
            if (speedrunner != null) {
                if (speedrunner.getLocation().getBlock().getX() == loc.getX() &&
                        speedrunner.getLocation().getBlock().getZ() == loc.getZ() &&
                        speedrunner.getWorld() == block.getWorld()
                ) {
                    aktiverLeistein = true;
                }
            }
        }
        if(!aktiverLeistein) {
            return;
        }
        */


        //Abbau-Event-Abbruch:
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeitsteinSprengung1(BlockExplodeEvent event) {
        Location loc;
        for(int i = 0; i < event.blockList().size(); i++) {
            Block gesprengterBlock = event.blockList().get(i);
            loc = gesprengterBlock.getLocation();
            if (ManhuntMain.welcherBlockWarBevorLeitsteinHier.containsKey(loc)) {
                event.blockList().remove(gesprengterBlock);
            }
        }
    }

    @EventHandler
    public void onLeitsteinSprengung2(EntityExplodeEvent event) {
        Location loc;
        for(int i = 0; i < event.blockList().size(); i++) {
            Block gesprengterBlock = event.blockList().get(i);
            loc = gesprengterBlock.getLocation();
            if (ManhuntMain.welcherBlockWarBevorLeitsteinHier.containsKey(loc)) {
                event.blockList().remove(gesprengterBlock);
            }
        }
    }
}
