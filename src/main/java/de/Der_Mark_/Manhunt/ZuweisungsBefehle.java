package de.Der_Mark_.Manhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ZuweisungsBefehle implements CommandExecutor {
        static ManhuntMain plugin;
        public ZuweisungsBefehle(ManhuntMain plugin) {
            this.plugin = plugin;
        }
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("ERROR: Mit der Konsole versucht auf einen Befehl nur für Spieler zuzugreifen.");
                return false;
            }
            Player player = (Player) sender;
            if(!player.isOp()) {
                player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Für diesen Befehl benötigst du OP-Rechte.");
                return true;
            }
            Player gefragterSpieler;
            if(args.length > 0) {
                gefragterSpieler = player.getServer().getPlayer(args[0]);
            } else {
                player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + "Du hast keinen Spieler angegeben.");
                return true;
            }
            boolean gefragterSpielerOnline = gefragterSpieler != null;
            if (command.getName().equalsIgnoreCase("speedrunner_add")) {
                if(gefragterSpielerOnline) {
                    if(!ManhuntMain.speedrunnerListe.contains(gefragterSpieler.getName())) {
                        ManhuntMain.speedrunnerListe.add(gefragterSpieler.getName());
                        if (!ManhuntMain.hunterListe.contains(gefragterSpieler.getName())) {
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " als Speedrunner hinzugefügt.");
                        } else {
                            ManhuntMain.hunterListe.remove(gefragterSpieler.getName());
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " zum Speedrunner gewechselt.");
                        }
                        //Kompass von allen Huntern soll auf neuen Speedrunner zeigen, wenn noch kein Ziel vorhanden
                        for(String hunterName : ManhuntMain.hunterListe) {
                            if (!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(hunterName)) { //Wenn der Hunter noch kein Ziel hat:
                                ManhuntMain.wessenKompassZeigtAufWenGerade.put(hunterName, gefragterSpieler.getName());
                                Player hunter = gefragterSpieler.getServer().getPlayer(hunterName);
                                boolean hunterOnline = hunter != null;
                                if(hunterOnline) {
                                    ManhuntMain.aufWenZeigtKompassNachricht(hunter, gefragterSpieler.getName());
                                }
                            }
                        }
                    } else {
                        player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist bereits Speedrunner.");
                    }
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist nicht online.");
                }
            } else if (command.getName().equalsIgnoreCase("hunter_add")) {
                if(gefragterSpielerOnline) {
                    if(!ManhuntMain.hunterListe.contains(gefragterSpieler.getName())) {


                        if (!ManhuntMain.speedrunnerListe.contains(gefragterSpieler.getName())) {
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " als Hunter hinzugefügt.");
                        } else {
                            ManhuntMain.speedrunnerListe.remove(gefragterSpieler.getName());
                            ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpieler.getName());
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " zum Hunter gewechselt.");
                        }

                        if(ManhuntMain.speedrunnerListe.size() != 0) {
                            //Kompass von neuem Hunter soll auf ersten verfügbaren Speedrunner zeigen
                            Player speedrunner;
                            for (int i = 0; i < ManhuntMain.speedrunnerListe.size(); i++) {
                                speedrunner = gefragterSpieler.getServer().getPlayer(ManhuntMain.speedrunnerListe.get(i));
                                if(speedrunner != null && speedrunner.getWorld() == gefragterSpieler.getWorld()) {
                                    if(!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpieler.getName())) {
                                        ManhuntMain.wessenKompassZeigtAufWenGerade.put(gefragterSpieler.getName(), speedrunner.getName());
                                        gefragterSpieler.sendMessage("Dein Kompass zeigt nun auf " + speedrunner.getName());
                                    }
                                }
                            }
                            //Wenn kein verfügbarer Speedrunner gefunden wurde, dann soll Kompass auf ersten nicht verfügbaren Speedrunner zeigen:
                            if(!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpieler.getName())) {
                                for (int i = 0; i < ManhuntMain.speedrunnerListe.size(); i++) {
                                    if(!ManhuntMain.wessenKompassZeigtAufWenGerade.containsKey(gefragterSpieler.getName())) {
                                        String speedrunnerName = ManhuntMain.speedrunnerListe.get(i);
                                        speedrunner = gefragterSpieler.getServer().getPlayer(speedrunnerName);
                                        ManhuntMain.wessenKompassZeigtAufWenGerade.put(gefragterSpieler.getName(), speedrunner.getName());
                                        ManhuntMain.aufWenZeigtKompassNachricht(gefragterSpieler, speedrunnerName);
                                    }
                                }
                            }
                        }


                        ManhuntMain.hunterListe.add(gefragterSpieler.getName());

                        boolean hunterHatNochKeinenKompass = true;
                        for (int i = 0; i < 41; i++) {
                            if (gefragterSpieler.getInventory().getItem(i) != null && gefragterSpieler.getInventory().getItem(i).getType() == Material.COMPASS) {
                                hunterHatNochKeinenKompass = false;
                            }
                        }
                        if(hunterHatNochKeinenKompass) {
                            gefragterSpieler.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                        }
                    } else {
                        player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist bereits Hunter.");
                    }
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist nicht online.");
                }
            } else if (command.getName().equalsIgnoreCase("speedrunner_remove")) {
                if (ManhuntMain.speedrunnerListe.contains(gefragterSpieler.getName())) {
                    ManhuntMain.speedrunnerListe.remove(gefragterSpieler.getName());
                    ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpieler.getName());
                    Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " ist nun kein Speedrunner mehr.");
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist garkein Speedrunner. Du kannst nur Speedrunner aus dem Team der Speedrunner entfernen.");
                }
            } else if (command.getName().equalsIgnoreCase("hunter_remove")) {
                if (ManhuntMain.hunterListe.contains(gefragterSpieler.getName())) {
                    ManhuntMain.hunterListe.remove(gefragterSpieler.getName());
                    Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " ist nun kein Hunter mehr.");
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist garkein Hunter. Du kannst nur Hunter aus dem Team der Hunter entfernen.");
                }
            } else if (command.getName().equalsIgnoreCase("gestorbener_speedrunner_add")) {
                if(gefragterSpielerOnline) {
                    if (ManhuntMain.speedrunnerListe.contains(gefragterSpieler.getName())) {
                        if (!ManhuntMain.gestorbeneSpeedrunnerListe.contains(gefragterSpieler.getName())) {
                            ManhuntMain.gestorbeneSpeedrunnerListe.add(gefragterSpieler.getName());
                            Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() +
                                    " ist nun als gestorbener Speedrunner markiert und kann somit nicht als erster das Ende betreten.");
                        } else {
                            player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist bereits als gestorbener Speedrunner markiert.");
                        }
                    } else {
                        player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist kein Speedrunner. " +
                                "Also kann er auch nicht als gestorben markiert werden.");
                    }
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + gefragterSpieler.getName() + " ist nicht online.");
                }

            } else if (command.getName().equalsIgnoreCase("gestorbener_speedrunner_remove")) {
                if (ManhuntMain.speedrunnerListe.contains(gefragterSpieler.getName())) {
                    if (ManhuntMain.gestorbeneSpeedrunnerListe.contains(gefragterSpieler.getName())) {
                        ManhuntMain.gestorbeneSpeedrunnerListe.remove(gefragterSpieler.getName());
                        Bukkit.broadcastMessage(ManhuntMain.GLOBALE_NACHRICHT_NORMAL + gefragterSpieler.getName() + " gilt nun wieder als noch nicht gestorbener Speedrunner.");
                    } else {
                        player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist noch nicht gestorben. " +
                                "Also braucht auch nicht sein Tod ungeschehen gemacht werden.");
                    }
                } else {
                    player.sendMessage(ManhuntMain.PRIVATE_NACHRICHT_FEHLSCHLAG + args[0] + " ist kein Speedrunner. " +
                            "Also braucht auch nicht sein Tod ungeschehen gemacht werden.");
                }
            }
            return true;
        }
}
