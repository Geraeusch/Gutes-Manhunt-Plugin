package de.Der_Mark_.Manhunt;

import de.Der_Mark_.Manhunt.Listener.*;
import org.bukkit.*;
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
    ConfigurationManager configManager;

    public static ArrayList<String> speedrunnerListe = new ArrayList<>();
    public static ArrayList<String> hunterListe = new ArrayList<>();
    public static ArrayList<String> gestorbeneSpeedrunnerListe = new ArrayList<>();
    public static HashMap<String, String> wessenKompassZeigtAufWenGerade = new HashMap<>();
    public static HashMap<Location, Material> welcherBlockWarBevorLeitsteinHier = new HashMap<>();
    public static HashMap<String, Location> letztePostitionDesSpeedrunnersInOberwelt = new HashMap<>();
    public static HashMap<String, Location> letztePostitionDesSpeedrunnersImNether = new HashMap<>();
    public static HashMap<String, Location> letztePostitionDesSpeedrunnersImEnde = new HashMap<>();
    public static HashMap<String, Location> zugangsPostitionDesHuntersInOberwelt = new HashMap<>();
    public static HashMap<String, Location> zugangsPostitionDesHuntersInNether = new HashMap<>();
    public static HashMap<String, Location> zugangsPostitionDesHuntersInEnde = new HashMap<>();
    public static Boolean siegFürSpeedrunner;
    public static Boolean endeWurdeBetreten = false;

    public static String PLUGIN_PREFIX = ChatColor.DARK_RED + "[" + ChatColor.GREEN + "Manhunt-Plugin" + ChatColor.DARK_RED + "] ";
    public static String PRIVATE_NACHRICHT_NORMAL =  PLUGIN_PREFIX + ChatColor.GREEN;
    public static String PRIVATE_NACHRICHT_FEHLSCHLAG = PLUGIN_PREFIX + ChatColor.RED;
    public static String GLOBALE_NACHRICHT_NORMAL = PLUGIN_PREFIX + ChatColor.GOLD;
    public static String GLOBALE_NACHRICHT_FEHLSCHLAG = PLUGIN_PREFIX + ChatColor.DARK_RED;

    @Override
    public void onEnable() {
        plugin = this;
        configManager = new ConfigurationManager(getDataFolder(), "config.yml", this);
        configManager.load();

        new SpeedrunnerTodListener(this);
        new EnderdrachenTodListener(this);
        new GestorbeneSpeedrunnerNichtAlsErstesInsEnde(this);
        new KompassZielWechseln(this);
        new AntiLeitsteinZerstörung(this);
        new KompassFürRespawnteHunter(this);
        new KompassFürNeuGejointeHunter(this);
        new KompassTracktErstenGejointenSpeedrunner(this);
        new LetztesPortalEinesSpielersSpeichern(this);

        ZuweisungsBefehle zuweisungsBefehle = new ZuweisungsBefehle(this);
        this.getCommand("speedrunner_add").setExecutor(zuweisungsBefehle);
        this.getCommand("hunter_add").setExecutor(zuweisungsBefehle);
        this.getCommand("speedrunner_remove").setExecutor(zuweisungsBefehle);
        this.getCommand("hunter_remove").setExecutor(zuweisungsBefehle);
        this.getCommand("gestorbener_speedrunner_add").setExecutor(zuweisungsBefehle);
        this.getCommand("gestorbener_speedrunner_remove").setExecutor(zuweisungsBefehle);
        this.getCommand("switch_kompasszeigtzuportal").setExecutor(zuweisungsBefehle);
        this.getCommand("change_anzahltotespeedrunnerfürhuntersieg").setExecutor(zuweisungsBefehle);

        parseValues();

        ladeEndeBetreten();

        kompasseAktualisieren();
    }

    private void kompasseAktualisieren() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //Alte Leitsteine entfernen:
                AlteLeitsteineEntfernen();

                for (String hunterName : hunterListe) {
                    Player hunter = plugin.getServer().getPlayer(hunterName);
                    boolean hunterOnline = hunter != null;
                    if(hunterOnline) {
                        String anvisierterSpeedrunnerName = wessenKompassZeigtAufWenGerade.get(hunterName);
                        Player anvisierterSpeedrunner = null;
                        if(anvisierterSpeedrunnerName != null) {
                            anvisierterSpeedrunner = plugin.getServer().getPlayer(anvisierterSpeedrunnerName);
                        }
                        boolean anvisierterSpeedrunnerOnline = anvisierterSpeedrunner != null;

                        Location leitsteinLoc = null;
                        if(anvisierterSpeedrunnerOnline) {
                            World world = hunter.getWorld();
                            if (world == anvisierterSpeedrunner.getWorld() || ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL) {
                                //Neuen Leitstein setzen:
                                Location speedrunnerOderPortalLoc = speedrunnerOderPortalLoc(anvisierterSpeedrunner, hunter);
                                if (!world.getEnvironment().equals(World.Environment.THE_END)) {
                                    int y;
                                    if (world.getEnvironment().equals(World.Environment.NORMAL)) {
                                        y = -64;
                                    } else {
                                        y = 0;
                                    }
                                    leitsteinLoc = new Location(hunter.getWorld(), speedrunnerOderPortalLoc.getBlock().getX(), y, speedrunnerOderPortalLoc.getBlock().getZ());
                                    if (leitsteinLoc.getBlock().getType() == Material.BEDROCK) {
                                        welcherBlockWarBevorLeitsteinHier.put(leitsteinLoc, leitsteinLoc.getBlock().getType());
                                        leitsteinLoc.getBlock().setType(Material.LODESTONE);
                                    }
                                } else {
                                    if (speedrunnerOderPortalLoc.getY() < 128) {
                                        leitsteinLoc = new Location(hunter.getWorld(), speedrunnerOderPortalLoc.getBlock().getX(), 255, speedrunnerOderPortalLoc.getBlock().getZ());
                                        if (leitsteinLoc.getBlock().getType() == Material.AIR) {
                                            welcherBlockWarBevorLeitsteinHier.put(leitsteinLoc, leitsteinLoc.getBlock().getType());
                                            leitsteinLoc.getBlock().setType(Material.LODESTONE);
                                        }
                                    } else {
                                        leitsteinLoc = new Location(hunter.getWorld(), speedrunnerOderPortalLoc.getBlock().getX(), 0, speedrunnerOderPortalLoc.getBlock().getZ());
                                        if (leitsteinLoc.getBlock().getType() == Material.AIR ||
                                                leitsteinLoc.getBlock().getType() == Material.OBSIDIAN
                                        ) {
                                            welcherBlockWarBevorLeitsteinHier.put(leitsteinLoc, leitsteinLoc.getBlock().getType());
                                            leitsteinLoc.getBlock().setType(Material.LODESTONE);
                                        }
                                    }
                                }
                            }
                        }
                        for (int i = 0; i < 41; i++) {
                            if(hunter.getInventory().getItem(i) != null && hunter.getInventory().getItem(i).getType() == Material.COMPASS) {
                                ItemStack kompass = hunter.getInventory().getItem(i);
                                CompassMeta meta = (CompassMeta) kompass.getItemMeta();
                                meta.setLodestone(leitsteinLoc);
                                kompass.setItemMeta(meta);
                                hunter.getInventory().setItem(i, kompass);
                            }
                        }

                    }
                }
            }
        }.runTaskTimer(plugin,0,10);
    }

    private static Location speedrunnerOderPortalLoc (Player anvisierterSpeedrunner, Player hunter) {
        if (anvisierterSpeedrunner.getWorld().equals(hunter.getWorld())) {
            return anvisierterSpeedrunner.getLocation();
        }
        Location portalLoc = null;
        switch (hunter.getWorld().getEnvironment()) {
            case NORMAL:
                portalLoc =  letztePostitionDesSpeedrunnersInOberwelt.get(anvisierterSpeedrunner.getName());
                break;
            case NETHER:
                portalLoc =  letztePostitionDesSpeedrunnersImNether.get(anvisierterSpeedrunner.getName());
                break;
            case THE_END:
                portalLoc =  letztePostitionDesSpeedrunnersImEnde.get(anvisierterSpeedrunner.getName());
                break;
        }
        if (portalLoc != null) {return portalLoc; }
        switch (hunter.getWorld().getEnvironment()) {
            case NORMAL: return zugangsPostitionDesHuntersInOberwelt.get(hunter.getName());
            case NETHER: return zugangsPostitionDesHuntersInNether.get(hunter.getName());
            case THE_END: return zugangsPostitionDesHuntersInEnde.get(hunter.getName());
        }

        Bukkit.broadcastMessage(GLOBALE_NACHRICHT_FEHLSCHLAG + "ManhuntMain.speedrunnerOderPortalLoc: Dieser Fall sollte nicht eintreten. " +
                "hunter.getWorld().getEnvironment(): " + hunter.getWorld().getEnvironment()
        );
        return null;
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

    public static void aufWenZeigtKompassNachricht(String hunterName, String speedrunnerName) {
        Player nunVerfolgterSpeedrunner = plugin.getServer().getPlayer(speedrunnerName);
        Player hunter = plugin.getServer().getPlayer(hunterName);
        if (hunter == null) {return; }
        if(nunVerfolgterSpeedrunner == null || nunVerfolgterSpeedrunner.isDead()) {
            hunter.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Dein Kompass würde jetzt auf " + speedrunnerName + " zeigen, " +
                    "aber " + speedrunnerName + " ist gerade nicht auf dem Server.");
        } else {
            if(nunVerfolgterSpeedrunner.getWorld() != hunter.getWorld() && !ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL) {
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

    public static int ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG = 1;
    public static boolean KOMPASS_ZEIGT_ZU_PORTAL = true;

    public void parseValues() {
        try {
            ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG = Integer.parseInt(getConfig().getString("anzahlToteSpeedrunnerFürHunterSieg"));
            KOMPASS_ZEIGT_ZU_PORTAL = Boolean.parseBoolean(getConfig().getString("kompassZeigtZuPortal"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("MESSAGE GHGJFESFES");
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
