package de.Der_Mark_.Manhunt;

import de.Der_Mark_.Manhunt.Listener.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ManhuntMain extends JavaPlugin {
    public static ManhuntMain plugin;

    public static ArrayList<String> speedrunnerListe = new ArrayList<>();
    public static ArrayList<String> hunterListe = new ArrayList<>();
    public static ArrayList<String> gestorbeneSpeedrunnerListe = new ArrayList<>();
    public static HashMap<String, String> wessenKompassZeigtAufWenGerade = new HashMap<>();
    public static HashMap<Location, Material> welcherBlockWarBevorLeitsteinHier = new HashMap<>();
    public static Boolean siegFürSpeedrunner;
    public static Boolean endeWurdeBetreten = false;

    public static String PLUGIN_PREFIX = ChatColor.DARK_RED + "[" + ChatColor.GREEN + "Manhunt-Plugin" + ChatColor.DARK_RED + "] ";
    public static String PRIVATE_NACHRICHT_NORMAL =  PLUGIN_PREFIX + ChatColor.GREEN;
    public static String PRIVATE_NACHRICHT_FEHLSCHLAG = PLUGIN_PREFIX + ChatColor.RED;
    public static String GLOBALE_NACHRICHT_NORMAL = PLUGIN_PREFIX + ChatColor.GOLD;

    @Override
    public void onEnable() {
        plugin = this;

        new SpeedrunnerTodListener(this);
        new EnderdrachenTodListener(this);
        new GestorbeneSpeedrunnerNichtAlsErstesInsEnde(this);
        new KompassZielWechseln(this);
        new AntiLeitsteinZerstörung(this);
        new KompassFürRespawnteHunter(this);

        ZuweisungsBefehle zuweisungsBefehle = new ZuweisungsBefehle(this);
        this.getCommand("speedrunner_add").setExecutor(zuweisungsBefehle);
        this.getCommand("hunter_add").setExecutor(zuweisungsBefehle);
        this.getCommand("speedrunner_remove").setExecutor(zuweisungsBefehle);
        this.getCommand("hunter_remove").setExecutor(zuweisungsBefehle);
        this.getCommand("gestorbener_speedrunner_add").setExecutor(zuweisungsBefehle);
        this.getCommand("gestorbener_speedrunner_remove").setExecutor(zuweisungsBefehle);

        ladeEndeBetreten();

        new BukkitRunnable() {
            @Override
            public void run() {
                //Alte Leitsteine entfernen:
                AlteLeitsteineEntfernen();

                for (String hunterName : hunterListe) {
                    Player hunter = plugin.getServer().getPlayer(hunterName);
                    boolean hunterOnline = hunter != null;
                    if(hunterOnline) {
                        for (int i = 0; i < 41; i++) {
                            if(hunter.getInventory().getItem(i) != null && hunter.getInventory().getItem(i).getType() == Material.COMPASS) {
                                ItemStack kompass = hunter.getInventory().getItem(i);
                                CompassMeta meta = (CompassMeta) kompass.getItemMeta();
                                String anvisierterSpeedrunnerName = wessenKompassZeigtAufWenGerade.get(hunterName);
                                Player anvisierterSpeedrunner = null;
                                if(anvisierterSpeedrunnerName != null) {
                                    anvisierterSpeedrunner = plugin.getServer().getPlayer(anvisierterSpeedrunnerName);
                                }
                                boolean anvisierterSpeedrunnerOnline = anvisierterSpeedrunner != null;

                                if(anvisierterSpeedrunnerOnline) {
                                    World world = anvisierterSpeedrunner.getWorld();
                                    if (world == hunter.getWorld()) {
                                        //Neuen Leitstein setzen:
                                        Location loc;
                                        if (world.getEnvironment() != World.Environment.THE_END) {
                                            loc = new Location(hunter.getWorld(), anvisierterSpeedrunner.getLocation().getBlock().getX(), 0, anvisierterSpeedrunner.getLocation().getBlock().getZ());
                                            if (loc.getBlock().getType() == Material.BEDROCK) {
                                                welcherBlockWarBevorLeitsteinHier.put(loc, loc.getBlock().getType());
                                                loc.getBlock().setType(Material.LODESTONE);
                                            }
                                        } else {
                                            if (anvisierterSpeedrunner.getLocation().getY() < 128) {
                                                loc = new Location(hunter.getWorld(), anvisierterSpeedrunner.getLocation().getBlock().getX(), 255, anvisierterSpeedrunner.getLocation().getBlock().getZ());
                                                if (loc.getBlock().getType() == Material.AIR) {
                                                    welcherBlockWarBevorLeitsteinHier.put(loc, loc.getBlock().getType());
                                                    loc.getBlock().setType(Material.LODESTONE);
                                                }
                                            } else {
                                                loc = new Location(hunter.getWorld(), anvisierterSpeedrunner.getLocation().getBlock().getX(), 0, anvisierterSpeedrunner.getLocation().getBlock().getZ());
                                                if (loc.getBlock().getType() == Material.AIR ||
                                                        loc.getBlock().getType() == Material.OBSIDIAN
                                                ) {
                                                    welcherBlockWarBevorLeitsteinHier.put(loc, loc.getBlock().getType());
                                                    loc.getBlock().setType(Material.LODESTONE);
                                                }
                                            }
                                        }
                                        meta.setLodestone(loc);
                                    } else {
                                        meta.setLodestone(null);
                                    }
                                } else {
                                    meta.setLodestone(null);
                                }
                                kompass.setItemMeta(meta);
                                hunter.getInventory().setItem(i, kompass);
                            }
                        }

                    }
                }
            }
        }.runTaskTimer(plugin,0,10);
    }

    public void AlteLeitsteineEntfernen() {
        Iterator it = welcherBlockWarBevorLeitsteinHier.entrySet().iterator();
        List<Location> löscheHashMapEinträge = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            löscheHashMapEinträge.add((Location) pair.getKey());
        }
        for (Location tmp: löscheHashMapEinträge) {
            tmp.getBlock().setType(welcherBlockWarBevorLeitsteinHier.get(tmp));
            welcherBlockWarBevorLeitsteinHier.remove(tmp);
        }
    }

    public static boolean EndeWurdeBetreten() {
        Boolean endeWurdeBereitsBetreten = false;
        for(World world : plugin.getServer().getWorlds()) {
            if(world.getEnvironment() == World.Environment.THE_END) {
                endeWurdeBereitsBetreten = true;
            }
        }
        return endeWurdeBereitsBetreten;
    }

    public static void aufWenZeigtKompassNachricht(Player hunter, String speedrunnerName) {
        Player nunVerfolgterSpeedrunner = hunter.getServer().getPlayer(speedrunnerName);
        if(nunVerfolgterSpeedrunner == null || nunVerfolgterSpeedrunner.isDead()) {
            hunter.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Dein Kompass würde jetzt auf " + speedrunnerName + " zeigen, " +
                    "aber " + speedrunnerName + " ist gerade nicht auf dem Server.");
        } else {
            if(nunVerfolgterSpeedrunner.getWorld() != hunter.getWorld()) {
                hunter.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Dein Kompass würde jetzt auf " + speedrunnerName + " zeigen, " +
                        "aber " + speedrunnerName + " ist in einer anderen Dimension.");
            } else {
                hunter.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_NORMAL + "Dein Kompass zeigt nun auf " + speedrunnerName + ".");
            }
        }
    }

    public void speichereEndeBetreten() {
        File file = new File(getDataFolder(), "endeWurdeBetreten.yml");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource("endeWurdeBetreten.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("endeWurdeBetreten", endeWurdeBetreten.toString());
        try {
            yaml.save(file);
        } catch (IOException e) {

        }
    }

    public void ladeEndeBetreten() {
        File file = new File(getDataFolder(), "endeWurdeBetreten.yml");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource("endeWurdeBetreten.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        endeWurdeBetreten = Boolean.parseBoolean(yaml.getString("endeWurdeBetreten"));
        try {
            yaml.save(file);
        } catch (IOException e) {

        }
    }

    @Override
    public void onDisable() {
        //Alte Leitsteine entfernen:
        AlteLeitsteineEntfernen();
        speichereEndeBetreten();
    }
}

//Wenn Spieler in unterschiedlicher Dimension und zuerst hunter zugewiesen wird, klappt nichts
