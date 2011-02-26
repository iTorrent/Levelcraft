package me.samkio.levelcraft.Functions;

import java.util.HashMap;

import me.samkio.levelcraft.Admin;
import me.samkio.levelcraft.Help;
import me.samkio.levelcraft.Levelcraft;
import me.samkio.levelcraft.Settings;
import me.samkio.levelcraft.Whitelist;
import me.samkio.levelcraft.SamToolbox.DataMySql;
import me.samkio.levelcraft.SamToolbox.DataSqlite;
import me.samkio.levelcraft.SamToolbox.Toolbox;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class PlayerFunctions {
	private final static HashMap<Player, Boolean> NotifyUsers = new HashMap<Player, Boolean>();

	public static void doThis(Player player, String[] split) {
		if ((split[1].equalsIgnoreCase("wc")
				|| split[1].equalsIgnoreCase("wood")
				|| split[1].equalsIgnoreCase("woodcut") || split[1]
				.equalsIgnoreCase("w")) && Settings.enableWCLevel == true) {
			showStat(player, "w");
		} else if ((split[1].equalsIgnoreCase("mine")
				|| split[1].equalsIgnoreCase("m") || split[1]
				.equalsIgnoreCase("mining"))
				&& Settings.enableMineLevel == true) {
			showStat(player, "m");
		} else if ((split[1].equalsIgnoreCase("slay")
				|| split[1].equalsIgnoreCase("s") || split[1]
				.equalsIgnoreCase("slayer"))
				&& Settings.enableSlayerLevel == true) {
			showStat(player, "s");
		} else if (split[1].equalsIgnoreCase("list")) {
			Help.ListLevels(player);
		} else if (split[1].equalsIgnoreCase("admin")
				&& Whitelist.isAdmin(player.getName()) == true
				&& split.length >= 3) {
			Admin.dothis(player, split);
		} else if (split[1].equalsIgnoreCase("notify")) {
			toggleNotify(player);
		} else if (split[1].equalsIgnoreCase("shout") && split.length >= 3) {
			Help.shout(player, split[2]);
		} else if ((split[1].equalsIgnoreCase("all"))) {
			int level = 0;
			int level2 = 0;
			int level3 = 0;
			double mineexp = 0;
			double slayexp = 0;
			double wcexp = 0;
			if (Settings.database.equalsIgnoreCase("flatfile")) {
				level = LevelFunctions.getLevel(player, Levelcraft.MiExpFile);
				level2 = LevelFunctions
						.getLevel(player, Levelcraft.SlayExpFile);
				level3 = LevelFunctions.getLevel(player, Levelcraft.WCExpFile);
				mineexp = LevelFunctions.getExp(player, Levelcraft.MiExpFile);
				slayexp = LevelFunctions.getExp(player, Levelcraft.SlayExpFile);
				wcexp = LevelFunctions.getExp(player, Levelcraft.WCExpFile);
			}else if (Settings.database.equalsIgnoreCase("sqlite")) {
				level3 = DataSqlite.getLevel(player, "WoodcuttingExp");
				level = DataSqlite.getLevel(player, "MiningExp");
				level2 = DataSqlite.getLevel(player, "SlayingExp");
				mineexp = DataSqlite.getExp(player, "MiningExp");
				slayexp = DataSqlite.getExp(player, "SlayingExp");
				wcexp = DataSqlite.getExp(player, "WoodcuttingExp");
			}
			player.sendMessage(ChatColor.GOLD
					+ "[LC] ---LevelCraftPlugin By Samkio--- ");
			player.sendMessage(ChatColor.GOLD + "[LC]" + ChatColor.GREEN
					+ " (M): " + level + ". Exp:" + mineexp);
			player.sendMessage(ChatColor.GOLD + "[LC]" + ChatColor.GREEN
					+ " (S): " + level2 + ", Exp:" + slayexp);
			player.sendMessage(ChatColor.GOLD + "[LC]" + ChatColor.GREEN
					+ " (W): " + level3 + ". Exp:" + wcexp);

		}

		else if (split[1].equalsIgnoreCase("unlocks")) {
			if (split.length >= 3) {
				Help.unlocks(player, split);
			} else {
				Help.IncorrectExp(player);
			}

		} else {
			player.sendMessage(ChatColor.GOLD + "[LC]" + ChatColor.YELLOW
					+ " Stat not found type '/level list' to list all stats. ");
		}

	}

	public static void checkAccount(Player player) {
		if (Settings.database.equalsIgnoreCase("flatfile")) {
			boolean HasWCAccount = LevelFunctions.containskey(player,
					Levelcraft.WCExpFile);
			boolean HasMineAcc = LevelFunctions.containskey(player,
					Levelcraft.MiExpFile);
			boolean HasSlayAcc = LevelFunctions.containskey(player,
					Levelcraft.SlayExpFile);
			if (HasWCAccount == false) {
				LevelFunctions.write(player, 0, Levelcraft.WCExpFile);
			}

			if (HasMineAcc == false) {
				LevelFunctions.write(player, 0, Levelcraft.MiExpFile);
			}

			if (HasSlayAcc == false) {
				LevelFunctions.write(player, 0, Levelcraft.SlayExpFile);
			}
		} else if (Settings.database.equalsIgnoreCase("mysql") && DataMySql.PlayerExsists(player) == false) {
			DataMySql.NewPlayer(player, 0);
		} else if (Settings.database.equalsIgnoreCase("sqlite")
				&& DataSqlite.PlayerExsists(player) == false) {
			DataSqlite.NewPlayer(player, 0);
		}
	}

	public static boolean enabled(Player player) {
		return NotifyUsers.containsKey(player);
	}

	public static void toggleNotify(Player player) {
		if (enabled(player)) {
			NotifyUsers.remove(player);
			Toolbox.sendMessage(player, "Experience notify disabled.", true);
		} else {
			NotifyUsers.put(player, null);
			player.sendMessage(ChatColor.GOLD + "[LC]" + ChatColor.GREEN
					+ " Experience notify enabled.");
		}
	}

	public static void showStat(Player player, String string) {
		int level = 0;
		double stat = 0;
		double expLeft = 0;
		String str = "NULL";
		if (Settings.database.equalsIgnoreCase("flatfile")
				&& string.equalsIgnoreCase("W")) {
			level = LevelFunctions.getLevel(player, Levelcraft.WCExpFile);
			stat = LevelFunctions.getExp(player, Levelcraft.WCExpFile);
			expLeft = LevelFunctions.getExpLeft(player, Levelcraft.WCExpFile);
			str = "Woodcut";
		} else if (Settings.database.equalsIgnoreCase("flatfile")
				&& string.equalsIgnoreCase("M")) {
			level = LevelFunctions.getLevel(player, Levelcraft.MiExpFile);
			stat = LevelFunctions.getExp(player, Levelcraft.MiExpFile);
			expLeft = LevelFunctions.getExpLeft(player, Levelcraft.MiExpFile);
			str = "Mining";
		} else if (Settings.database.equalsIgnoreCase("flatfile")
				&& string.equalsIgnoreCase("S")) {
			level = LevelFunctions.getLevel(player, Levelcraft.SlayExpFile);
			stat = LevelFunctions.getExp(player, Levelcraft.SlayExpFile);
			expLeft = LevelFunctions.getExpLeft(player, Levelcraft.SlayExpFile);
			str = "Slaying";
		} else if (Settings.database.equalsIgnoreCase("sqlite")
				&& string.equalsIgnoreCase("M")) {
			level = DataSqlite.getLevel(player, "MiningExp");
			stat = DataSqlite.getExp(player, "MiningExp");
			expLeft = DataSqlite.getExpLeft(player, "MiningExp");
			str = "Mining";
		} else if (Settings.database.equalsIgnoreCase("sqlite")
				&& string.equalsIgnoreCase("W")) {
			level = DataSqlite.getLevel(player, "WoodcuttingExp");
			stat = DataSqlite.getExp(player, "WoodcuttingExp");
			expLeft = DataSqlite.getExpLeft(player, "WoodcuttingExp");
			str = "Woodcutting";

		} else if (Settings.database.equalsIgnoreCase("sqlite")
				&& string.equalsIgnoreCase("S")) {
			level = DataSqlite.getLevel(player, "SlayingExp");
			stat = DataSqlite.getExp(player, "SlayingExp");
			expLeft = DataSqlite.getExpLeft(player, "SlayingExp");
			str = "Slaying";
		}
	 else if (Settings.database.equalsIgnoreCase("mysql")
			&& string.equalsIgnoreCase("m")) {
		level = DataMySql.getLevel(player, "MiningExp");
		stat = DataMySql.getExp(player, "MiningExp");
		expLeft = DataMySql.getExpLeft(player, "MiningExp");
		str = "Mining";
	}
	 else if (Settings.database.equalsIgnoreCase("mysql")
				&& string.equalsIgnoreCase("s")) {
			level = DataMySql.getLevel(player, "SlayingExp");
			stat = DataMySql.getExp(player, "SlayingExp");
			expLeft = DataMySql.getExpLeft(player, "SlayingExp");
			str = "Slaying";
		}
	 else if (Settings.database.equalsIgnoreCase("mysql")
				&& string.equalsIgnoreCase("w")) {
			level = DataMySql.getLevel(player, "WoodcuttingExp");
			stat = DataMySql.getExp(player, "WoodcuttingExp");
			expLeft = DataMySql.getExpLeft(player, "WoodcuttingExp");
			str = "Woodcutting";
		}
		player.sendMessage(ChatColor.GOLD
				+ "[LC] ---LevelCraftPlugin By Samkio--- ");
		player.sendMessage(ChatColor.GOLD + "[LC] " + ChatColor.GREEN + str
				+ " experience: " + stat);
		player.sendMessage(ChatColor.GOLD + "[LC] " + ChatColor.GREEN + str
				+ " level: " + level);
		player.sendMessage(ChatColor.GOLD + "[LC] " + ChatColor.GREEN
				+ "Experience left to next level: " + expLeft);
	}

}
