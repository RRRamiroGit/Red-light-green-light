package net.randompvp.rlgl;

import java.time.Instant;
import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Rlgl extends JavaPlugin implements Listener {

	long timeGameStarted;
	boolean isStarted = false;
	boolean greenLight = true;
	int lightId, stopId, textCount;
	Random random = new Random();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.BLUE + "Usage: /" + label + " [start/stop]");
			return false;
		}
		if (args[0].equalsIgnoreCase("start")) {
			if (isStarted) {
				sender.sendMessage(ChatColor.RED + "The game has already started!");
				return false;
			}
			isStarted = true;
			for (int time = 0; time < 3; time++) {
				final int countdown = 3 - time;
				getServer().getScheduler().scheduleSyncDelayedTask(this, () -> getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "The game will start in " + countdown + "!"), time * 20);
			}
			getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
				timeGameStarted = Instant.EPOCH.getEpochSecond();
				getServer().broadcastMessage(ChatColor.GREEN + "The game has started!");
				greenLight();
			}, 60);
			return false;
		}
		if (args[0].equalsIgnoreCase("stop")) {
			if (!isStarted) {
				sender.sendMessage(ChatColor.RED + "There is no game happening currently!");
				return false;
			}
			getServer().getScheduler().cancelTask(lightId);
			getServer().getScheduler().cancelTask(stopId);
			getServer().broadcastMessage(ChatColor.AQUA + "Current game has been halted!");
			isStarted = false;
			return false;
		}
		sender.sendMessage(ChatColor.BLUE + "Usage: /" + label + " [start/stop]");
		return super.onCommand(sender, command, label, args);
	}

	void greenLight() {
		greenLight = true;
		for (Player players : getServer().getOnlinePlayers()) {
			for (int i = 0; i < 10; i++)
				players.playSound(players.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
		}
		lightId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player players : getServer().getOnlinePlayers())
				players.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "■■■■■■■■■■■Green Light■■■■■■■■■■■"));
		}, 0, 8);
		stopId = getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			getServer().getScheduler().cancelTask(lightId);
			redLight();
		}, levelTime(820 - ((Instant.EPOCH.getEpochSecond() - timeGameStarted) / 7), 70) + random.nextInt(220));
	}

	long levelTime(long l, int min) {
		if (min > l)
			return min;
		return l;
	}

	void redLight() {
		for (Player players : getServer().getOnlinePlayers()) {
			for (int i = 0; i < 10; i++) {
				players.playSound(players.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.2F);
				players.setVelocity(players.getVelocity().multiply(0.1));
			}
		}
		textCount = 0;
		String[] text = { "▪▪▪▪▪▪▪▪▪▪▪Red Light▪▪▪▪▪▪▪▪▪▪▪", "▪▪▪▪▪▪▪▪▪▪■Red Light■▪▪▪▪▪▪▪▪▪▪", "▪▪▪▪▪▪▪▪▪■■Red Light■■▪▪▪▪▪▪▪▪▪", "▪▪▪▪▪▪▪▪■■■Red Light■■■▪▪▪▪▪▪▪▪", "▪▪▪▪▪▪▪■■■■Red Light■■■■▪▪▪▪▪▪▪", "▪▪▪▪▪▪■■■■■Red Light■■■■■▪▪▪▪▪▪", "▪▪▪▪▪■■■■■■Red Light■■■■■■▪▪▪▪▪", "▪▪▪▪■■■■■■■Red Light■■■■■■■▪▪▪▪", "▪▪▪■■■■■■■■Red Light■■■■■■■■▪▪▪", "▪▪■■■■■■■■■Red Light■■■■■■■■■▪▪", "▪■■■■■■■■■■Red Light■■■■■■■■■■▪", "■■■■■■■■■■■Red Light■■■■■■■■■■■" };
		int task = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player players : getServer().getOnlinePlayers())
				players.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + text[textCount]));
			textCount++;
		}, 0, 3);
		getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			getServer().getScheduler().cancelTask(task);
			greenLight = false;
		}, 33);
		lightId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player players : getServer().getOnlinePlayers())
				players.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "■■■■■■■■■■■Red Light■■■■■■■■■■■"));
		}, 34, 8);
		stopId = getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			getServer().getScheduler().cancelTask(lightId);
			greenLight();
		}, levelTime(200 - ((Instant.EPOCH.getEpochSecond() - timeGameStarted) / 7), 30) + random.nextInt(160));
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!isStarted || greenLight || (roundMathToEqual(event.getFrom().getX()) == roundMathToEqual(event.getTo().getX()) && roundMathToEqual(event.getFrom().getY()) == roundMathToEqual(event.getTo().getY()) && roundMathToEqual(event.getFrom().getZ()) == roundMathToEqual(event.getTo().getZ()))) // minecraft for some reason changes the position of the player when they stand
																																																																											// still for a little while, all of this is to try and cut down on that (it
																																																																											// still falsely kills them sometimes)
			return;
		getServer().broadcastMessage(ChatColor.RED + event.getPlayer().getName() + " moved and died!");
		event.getPlayer().setHealth(0);
	}

	long roundMathToEqual(double d) {
		return Math.round(d * 10);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!isStarted || greenLight || event.getAction().equals(Action.LEFT_CLICK_AIR))
			return;
		getServer().broadcastMessage(ChatColor.RED + event.getPlayer().getName() + " moved and died!");
		event.getPlayer().setHealth(0);
	}

}
