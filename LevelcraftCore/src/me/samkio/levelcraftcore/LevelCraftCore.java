package me.samkio.levelcraftcore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.samkio.levelcraftcore.Listeners.LCPlayerListener;
import me.samkio.levelcraftcore.util.FlatFile;
import me.samkio.levelcraftcore.util.Language;
import me.samkio.levelcraftcore.util.SqliteDB;
import me.samkio.levelcraftcore.util.MySqlDB;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

public class LevelCraftCore extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public final FlatFile FlatFile = new FlatFile(this);
	public final MySqlDB MySqlDB = new MySqlDB(this);
	public final LCCommands LCCommands = new LCCommands(this);
	public final Language lang = new Language(this);
	public final LCChat LCChat = new LCChat(this);
	public final SqliteDB SqliteDB = new SqliteDB(this);
	public final Tools Tools = new Tools(this);
	public final LCAdminCommands LCAdminCommands = new LCAdminCommands(this);
	public final Whitelist Permissions = new Whitelist(this);
	public final LevelFunctions LevelFunctions = new LevelFunctions(this);
	private final LCPlayerListener playerListener = new LCPlayerListener(this);
	public HashMap<Plugin, String[]> LevelReferenceKeys = new HashMap<Plugin, String[]>();
	public HashMap<Plugin, String> LevelIndexes = new HashMap<Plugin, String>();
	public HashMap<Plugin, String> LevelNames = new HashMap<Plugin, String>();
	public HashMap<Plugin, String[]> LevelUnlocks = new HashMap<Plugin, String[]>();
	public HashMap<Plugin, File> LevelFiles = new HashMap<Plugin, File>();
	public HashMap<Player, String> NotifyUsers = new HashMap<Player, String>();
	public HashMap<Plugin, String> LevelAuthors = new HashMap<Plugin, String>();
	public HashMap<Plugin, String[]> LevelExp = new HashMap<Plugin, String[]>();
	public HashMap<Plugin, int[]> LevelUnlocksLevel = new HashMap<Plugin, int[]>();
	public HashMap<Plugin, String[]> LevelHelp = new HashMap<Plugin, String[]>();
	public String database;
	public String MySqlDir;
	public String MySqlPass;
	public String MySqlUser;
	public String c1;
	public String c2;
	public String c3;
	public String c4;
	public String[] Worlds;
	public int LevelCap;
	public int Constant;
	public boolean EnableLevelCap;
	public boolean EnableSkillMastery;
	public boolean PermissionUse;
	public boolean NotifyAll;
	public PermissionHandler PermissionH;

	@Override
	public void onDisable() {
		if (database.equalsIgnoreCase("sqlite"))
			SqliteDB.closeConnection();
	}

	@Override
	public void onEnable() {
		this.getDataFolder().mkdir();
		new File(this.getDataFolder() + "/Data/").mkdirs();
		new File(this.getDataFolder() + "/Configs/").mkdirs();
		this.loadConfig();
		this.registerEvents();
		this.Permissions.LoadPerms();
		this.lang.LoadLang();
		PluginManager pm = getServer().getPluginManager();
		for (Plugin plugin : pm.getPlugins()) {
			if (plugin.getDescription().getName().startsWith("LC")) {
				if (!getServer().getPluginManager().isPluginEnabled(plugin)) {
					pm.enablePlugin(plugin);
				}
				String[] str = (String[]) plugin.getConfiguration()
						.getProperty("ReferenceKeys");
				String[] exp = (String[]) plugin.getConfiguration()
						.getProperty("LevelExpPer");
				String[] unlocks = (String[]) plugin.getConfiguration()
						.getProperty("LevelUnlocks");
				String[] help = (String[]) plugin.getConfiguration()
				.getProperty("LevelHelp");
				int[] unlockslevel = (int[]) plugin.getConfiguration()
						.getProperty("LevelUnlocksLevel");
				String index = (String) plugin.getConfiguration().getProperty(
						"ReferenceIndex");
				String name = (String) plugin.getConfiguration().getProperty(
						"LevelName");
				String author = (String) plugin.getConfiguration().getProperty(
						"Author");
				this.LevelReferenceKeys.put(plugin, str);
				this.LevelIndexes.put(plugin, index);
				this.LevelNames.put(plugin, name);
				this.LevelUnlocks.put(plugin, unlocks);
				this.LevelAuthors.put(plugin, author);
				this.LevelExp.put(plugin, exp);
				this.LevelUnlocksLevel.put(plugin, unlockslevel);
				this.LevelHelp.put(plugin, help);
			}

		}
		this.createData();
		this.logger.log(Level.INFO, "[LC] LevelCraftCore "+this.getDescription().getVersion()+" Loaded");
		this.logger
				.log(Level.INFO, "[LC] Loaded levels:" + LevelNames.values());
	}

	public void loadConfig() {
		Configuration gC = getConfiguration();
		gC.load();
		this.database = gC.getString("Database", "FlatFile");
		this.MySqlDir = gC.getString("MySqlDatabaseDirectory",
				"localhost:3306/minecraft");
		this.MySqlUser = gC.getString("MySqlDatabaseUsername", "root");
		this.MySqlPass = gC.getString("MySqlDatabasePassword", "");
		this.EnableLevelCap = gC.getBoolean("EnableLevelCap", true);
		this.LevelCap = gC.getInt("LevelCap", 100);
		this.Constant = gC.getInt("LevelConstant", 20);
		this.EnableSkillMastery = gC.getBoolean("EnableSkillMastery", true);
		this.c1 = gC.getString("ColourOne", "GOLD");
		this.c2 = gC.getString("ColourTwo", "YELLOW");
		this.c3 = gC.getString("ColourGood", "GREEN");
		this.c4 = gC.getString("ColourBad", "RED");
		this.NotifyAll = gC.getBoolean("NotifyAll", true);
		List<World> worldRun = this.getServer().getWorlds();
		String str = "";
		for(World w: worldRun){
			str = str + w.getName() + ",";
		}
		String worldsraw = gC.getString("Worlds",str);
		Worlds = worldsraw.split(",");
		gC.save();
	}

	public boolean createData() {
		if (this.database.equalsIgnoreCase("FlatFile")) {

			for (Plugin p : LevelNames.keySet()) {
				String S = LevelNames.get(p);
				File ExpFile = new File(getDataFolder() + "/Data/" + S + ".exp");
				try {
					ExpFile.createNewFile();
					this.LevelFiles.put(p, ExpFile);
				} catch (IOException e) {
					this.logger.log(Level.SEVERE, "[LC] Could not write file: "
							+ S + ".exp");
					this.logger.log(Level.SEVERE, "[LC] " + e);
					return false;
				}
			}
			this.logger.log(Level.INFO, "[LC] Using FlatFile To Store Data.");
			return true;
		} else if (this.database.equalsIgnoreCase("MySql")) {
			this.MySqlDB.prepare();
			this.logger.log(Level.INFO, "[LC] Using MySql To Store Data.");
		} else if (this.database.equalsIgnoreCase("Sqlite")) {
			this.SqliteDB.prepare();
			this.logger.log(Level.INFO, "[LC] Using Sqlite To Store Data.");
		}
		return false;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("level")
				|| commandLabel.equalsIgnoreCase("lvl")) {
			if (args.length >= 1 && sender instanceof Player) {
				this.LCCommands.determineMethod((Player) sender, args);
			} else if (sender instanceof Player) {
				this.LCCommands.about(sender);
			} else {
				this.LCCommands.credits(sender);
			}
			return true;
		} else {
			return false;
		}
	}

	public void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener,
				Event.Priority.Lowest, this);
		if(EnableSkillMastery){
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener,
				Event.Priority.High, this);
		}
	}

}
