package de.Der_Mark_.Manhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

public class ZuweisungsBefehle implements CommandExecutor {
        static ManhuntMain plugin;
        public ZuweisungsBefehle(ManhuntMain plugin) {
            this.plugin = plugin;
        }
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(sender instanceof Player && !sender.isOp()) {
                sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Für diesen Befehl benötigst du OP-Rechte.");
                return true;
            }
            if (command.getName().equalsIgnoreCase("speedrunner_add") ||
                    command.getName().equalsIgnoreCase("hunter_add") ||
                    command.getName().equalsIgnoreCase("speedrunner_remove") ||
                    command.getName().equalsIgnoreCase("hunter_remove") ||
                    command.getName().equalsIgnoreCase("gestorbener_speedrunner_add") ||
                    command.getName().equalsIgnoreCase("gestorbener_speedrunner_remove")
            ) {
                if (args.length == 0) {
                    sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Du hast keinen Spieler angegeben.");
                    return true;
                }
                Player gefragterSpieler = plugin.getServer().getPlayer(args[0]);
                boolean gefragterSpielerOnline = gefragterSpieler != null;
                String gefragterSpielerName = gefragterSpielerName(args[0]);
                if (gefragterSpielerName == null) {
                    sender.sendMessage(String.format(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Der Spieler %s existiert nicht.", args[0]));
                    return true;
                }

                if (command.getName().equalsIgnoreCase("speedrunner_add")) {
                    if (!ManhuntMain.speedrunnerListe.contains(gefragterSpielerName)) {
                        ManhuntMain.speedrunnerListe.add(gefragterSpielerName);
                        if (!ManhuntMain.hunterListe.contains(gefragterSpielerName)) {
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " als Speedrunner hinzugefügt.");
                        } else {
                            ManhuntMain.hunterListe.remove(gefragterSpielerName);
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " zum Speedrunner gewechselt.");
                        }
                        if (gefragterSpielerOnline) {
                            //Kompass von allen Huntern soll auf neuen Speedrunner zeigen, wenn noch kein Ziel vorhanden
                            for (String hunterName : ManhuntMain.hunterListe) {
                                if (!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(hunterName)) { //Wenn der Hunter noch kein Ziel hat:
                                    ManhuntMain.wessenKompassZeigtAufWenGerade.put(hunterName, gefragterSpielerName);
                                    Player hunter = plugin.getServer().getPlayer(hunterName);
                                    boolean hunterOnline = hunter != null;
                                    if (hunterOnline) {
                                        ManhuntMain.aufWenZeigtKompassNachricht(hunterName, gefragterSpielerName);
                                    }
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpielerName + " ist bereits Speedrunner.");
                    }
                } else if (command.getName().equalsIgnoreCase("hunter_add")) {
                    if (!ManhuntMain.hunterListe.contains(gefragterSpielerName)) {
                        ManhuntMain.hunterListe.add(gefragterSpielerName);

                        if (!ManhuntMain.speedrunnerListe.contains(gefragterSpielerName)) {
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " als Hunter hinzugefügt.");
                        } else {
                            ManhuntMain.speedrunnerListe.remove(gefragterSpielerName);
                            ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpielerName);
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " zum Hunter gewechselt.");
                        }

                        if (gefragterSpielerOnline) {
                            if (ManhuntMain.speedrunnerListe.size() != 0) {
                                //Kompass von neuem Hunter soll auf ersten verfügbaren Speedrunner zeigen
                                Player speedrunner;
                                for (int i = 0; i < ManhuntMain.speedrunnerListe.size(); i++) {
                                    speedrunner = plugin.getServer().getPlayer(ManhuntMain.speedrunnerListe.get(i));
                                    if (speedrunner != null && speedrunner.getWorld() == gefragterSpieler.getWorld()) {
                                        if (!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpielerName)) {
                                            ManhuntMain.wessenKompassZeigtAufWenGerade.put(gefragterSpielerName, speedrunner.getName());
                                            gefragterSpieler.sendMessage("Dein Kompass zeigt nun auf " + speedrunner.getName());
                                        }
                                    }
                                }
                                //Wenn kein verfügbarer Speedrunner gefunden wurde, dann soll Kompass auf ersten nicht verfügbaren Speedrunner zeigen:
                                if (!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpielerName)) {
                                    for (int i = 0; i < ManhuntMain.speedrunnerListe.size(); i++) {
                                        if (!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpielerName)) {
                                            String speedrunnerName = ManhuntMain.speedrunnerListe.get(i);
                                            speedrunner = plugin.getServer().getPlayer(speedrunnerName);
                                            ManhuntMain.wessenKompassZeigtAufWenGerade.put(gefragterSpielerName, speedrunner.getName());
                                            ManhuntMain.aufWenZeigtKompassNachricht(gefragterSpielerName, speedrunnerName);
                                        }
                                    }
                                }
                            }

                            boolean hunterHatNochKeinenKompass = true;
                            for (int i = 0; i < 41; i++) {
                                if (gefragterSpieler.getInventory().getItem(i) != null && gefragterSpieler.getInventory().getItem(i).getType() == Material.COMPASS) {
                                    hunterHatNochKeinenKompass = false;
                                }
                            }
                            if (hunterHatNochKeinenKompass) {
                                gefragterSpieler.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                            }
                        }
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpielerName + " ist bereits Hunter.");
                    }
                } else if (command.getName().equalsIgnoreCase("speedrunner_remove")) {
                    if (ManhuntMain.speedrunnerListe.contains(gefragterSpielerName)) {
                        ManhuntMain.speedrunnerListe.remove(gefragterSpielerName);
                        ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpielerName);
                        Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " ist nun kein Speedrunner mehr.");
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist garkein Speedrunner. Du kannst nur Speedrunner aus dem Team der Speedrunner entfernen.");
                    }
                } else if (command.getName().equalsIgnoreCase("hunter_remove")) {
                    if (ManhuntMain.hunterListe.contains(gefragterSpielerName)) {
                        ManhuntMain.hunterListe.remove(gefragterSpielerName);
                        Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " ist nun kein Hunter mehr.");
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist garkein Hunter. Du kannst nur Hunter aus dem Team der Hunter entfernen.");
                    }
                } else if (command.getName().equalsIgnoreCase("gestorbener_speedrunner_add")) {
                    if (ManhuntMain.speedrunnerListe.contains(gefragterSpielerName)) {
                        if (!ManhuntMain.gestorbeneSpeedrunnerListe.contains(gefragterSpielerName)) {
                            ManhuntMain.gestorbeneSpeedrunnerListe.add(gefragterSpielerName);
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName +
                                    " ist nun als gestorbener Speedrunner markiert und kann somit nicht als erster das Ende betreten.");
                        } else {
                            sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpielerName + " ist bereits als gestorbener Speedrunner markiert.");
                        }
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist kein Speedrunner. " +
                                "Also kann er auch nicht als gestorben markiert werden.");
                    }
                } else if (command.getName().equalsIgnoreCase("gestorbener_speedrunner_remove")) {
                    if (ManhuntMain.speedrunnerListe.contains(gefragterSpielerName)) {
                        if (ManhuntMain.gestorbeneSpeedrunnerListe.contains(gefragterSpielerName)) {
                            ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpielerName);
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpielerName + " gilt nun wieder als noch nicht gestorbener Speedrunner.");
                        } else {
                            sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist noch nicht gestorben. " +
                                    "Also braucht auch nicht sein Tod ungeschehen gemacht werden.");
                        }
                    } else {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist kein Speedrunner. " +
                                "Also braucht auch nicht sein Tod ungeschehen gemacht werden.");
                    }
                }
                return true;
            } else {
                if (command.getName().equalsIgnoreCase("switch_kompasszeigtzuportal")) {
                    if (ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL) {
                        plugin.getConfig().set("kompassZeigtZuPortal", false);
                        ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL = false;
                    } else {
                        plugin.getConfig().set("kompassZeigtZuPortal", true);
                        ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL = true;
                    }
                    sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_NORMAL + "kompassZeigtZuPortal: " + ManhuntMain.KOMPASS_ZEIGT_ZU_PORTAL + " gespeichert");
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    return true;
                } else if (command.getName().equalsIgnoreCase("change_anzahltotespeedrunnerfürhuntersieg")) {
                    if (args.length == 0) {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Es fehlt die Angabe einer Zahl. anzahlToteSpeedrunnerFürHunterSieg ist aktuell " +
                                ManhuntMain.ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG);
                        return true;
                    }
                    Integer anzahlSpeedrunner = null;
                    try {
                        anzahlSpeedrunner = Integer.parseInt(args[0]);
                    } catch (NumberFormatException throwables) { }
                    if (anzahlSpeedrunner == null) {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist keine gültige Zahl.");
                        return true;
                    }
                    if (anzahlSpeedrunner <= 0) {
                        sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Zahl kann nicht negativ oder 0 sein.");
                        return true;
                    }
                    if (anzahlSpeedrunner != ManhuntMain.ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG) {
                        plugin.getConfig().set("anzahlToteSpeedrunnerFürHunterSieg", anzahlSpeedrunner);
                        ManhuntMain.ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG = anzahlSpeedrunner;
                        plugin.saveConfig();
                        plugin.reloadConfig();
                    }
                    sender.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_NORMAL + "anzahlToteSpeedrunnerFürHunterSieg: " + ManhuntMain.ANZAHL_TOTE_SPEEDRUNNER_FÜR_HUNTER_SIEG + " gespeichert");
                    return true;
                }
            }
            return true;
        }

    static String gefragterSpielerName(String arg0) {
        UUID gemeinterSpielerUUID = gefragteSpielerUUID(arg0);
        String gemeinterSpielerName = null;
        if (plugin.getServer().getPlayer(gemeinterSpielerUUID) != null) {
            gemeinterSpielerName = plugin.getServer().getPlayer(gemeinterSpielerUUID).getName();
        } else {
            if (gemeinterSpielerUUID != null) {
                gemeinterSpielerName = plugin.getServer().getOfflinePlayer(gemeinterSpielerUUID).getName();
            }
        }
        return gemeinterSpielerName;
    }

    static UUID gefragteSpielerUUID(String arg0) {
        Player gefragterSpieler;
        OfflinePlayer gefragterOfflineSpieler;
        UUID uuid = null;
        if(arg0 != null) {
            gefragterSpieler = plugin.getServer().getPlayerExact(arg0);
            if (gefragterSpieler == null) {
                for(OfflinePlayer tmp : plugin.getServer().getOfflinePlayers()) {
                    if(tmp.getName().equals(arg0)) {
                        gefragterOfflineSpieler = tmp;
                        uuid = gefragterOfflineSpieler.getUniqueId();
                    }
                }
            } else {
                uuid = gefragterSpieler.getUniqueId();
            }
        }
        return uuid;
    }
}
