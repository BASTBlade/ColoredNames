package blade.colorednames.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.permission.Permission;



public class main extends JavaPlugin  implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	FileConfiguration config;
	Permission permission;
	Vault v;
	ScoreboardManager scoreboard;
	List<String> configRanks;
	
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been disabled!");
		removeTeams();
	}
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been enabled!");

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		scoreboard = getServer().getScoreboardManager();
		handleFirstLoading();
		checkDependencies();
		LoadFeatures();
	}
	private void handleFirstLoading() {
		CheckConfig();
		File file = new File(getDataFolder().getAbsolutePath() + "/");
		try{
			if(file.mkdir());
		}
		catch(Exception e){
			
		}
	}
		
	
	private void CheckConfig() {
		config = this.getConfig();
		
		config.addDefault("Ranks", new Object());
		config.addDefault("Ranks.default", "&6");
		config.addDefault("Ranks.admins", "&5");
		
		List<String> Ranks = new ArrayList<String>();
		Ranks.add("admins");
		Ranks.add("default");
		config.addDefault("Tab Order", Ranks);
		
		config.options().copyDefaults(true);
		saveConfig();
		
	}
	private void LoadFeatures() {
		configRanks = getConfig().getStringList("Tab Order");
		createTeams();
		for(Player player : getServer().getOnlinePlayers()) {
			setColoredName(player, getColorCode(getPermissionGroup(player)));
			scoreboard.getMainScoreboard().getTeam(getPermissionGroup(player)).addEntry(player.getName());
			
		}
		
	}
	private void createTeams() {
		for(String s : configRanks) {
			scoreboard.getMainScoreboard().registerNewTeam(s);
		}
	}
	private void removeTeams() {
		for(String s : configRanks) {
			Team t = scoreboard.getMainScoreboard().getTeam(s);
			t.unregister();
		}
	}
	
	
	String color = null;
	private String getColorCode(String playerPermission) {
		getConfig().getConfigurationSection("Ranks").getKeys(false).forEach(key -> {
			if(playerPermission.equals(key)) {
				color = getConfig().getString("Ranks." + key);
			}
		});
		return color;
	}
	
	private void checkDependencies() {
		Plugin pluginv = getServer().getPluginManager().getPlugin("Vault"); 
		if(pluginv instanceof Vault){
        	v = (Vault) pluginv;

        	RegisteredServiceProvider<Permission>permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        	permission = permissionProvider.getProvider();
        }
        else{
        	this.logger.info("Vault not detected. Plugin won't work as intended without Vault.");
        }
	}
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent e) {
		setColoredName(e.getPlayer(), getColorCode(getPermissionGroup(e.getPlayer())));
		scoreboard.getMainScoreboard().getTeam(getPermissionGroup(e.getPlayer())).addEntry(e.getPlayer().getName());
	}
	
	
	private String getPermissionGroup(Player player) {
		if(player instanceof Player) {
			return permission.getPrimaryGroup(player);
		}
		return null;
	}
	
	
	private void setColoredName(Player player, String color) {
		if(player instanceof Player) {
			player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', color)+ player.getName());
		}
	}
	
}
