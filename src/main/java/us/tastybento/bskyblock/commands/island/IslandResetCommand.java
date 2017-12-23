package us.tastybento.bskyblock.commands.island;

import java.io.IOException;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import us.tastybento.bskyblock.api.commands.CompositeCommand;
import us.tastybento.bskyblock.api.commands.User;
import us.tastybento.bskyblock.api.events.island.IslandEvent.Reason;
import us.tastybento.bskyblock.config.Settings;
import us.tastybento.bskyblock.database.managers.island.NewIsland;
import us.tastybento.bskyblock.database.objects.Island;

public class IslandResetCommand extends CompositeCommand {

    private static final boolean DEBUG = false;

    public IslandResetCommand(CompositeCommand command) {
        super(command, "reset", "restart");
        this.setPermission(Settings.PERMPREFIX + "island.create");
        this.setOnlyPlayer(true);
        this.setUsage("island.reset.usage");
    }

    @Override
    public boolean execute(User user, String[] args) {
        if (!getIslands().hasIsland(user.getUniqueId())) {
            user.sendMessage("general.errors.no-island");
            return true;
        }
        if (!getIslands().isOwner(user.getUniqueId())) {
            return false; 
        }
        if (plugin.getPlayers().inTeam(user.getUniqueId())) {
            user.sendMessage("island.reset.MustRemovePlayers");
            return true;
        }
        Player player = user.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
        // Get the player's old island
        Island oldIsland = getIslands().getIsland(player.getUniqueId());
        if (DEBUG)
            plugin.getLogger().info("DEBUG: old island is at " + oldIsland.getCenter().getBlockX() + "," + oldIsland.getCenter().getBlockZ());
        // Remove them from this island (it still exists and will be deleted later)
        getIslands().removePlayer(player.getUniqueId());
        if (DEBUG)
            plugin.getLogger().info("DEBUG: old island's owner is " + oldIsland.getOwner());
        // Create new island and then delete the old one
        if (DEBUG)
            plugin.getLogger().info("DEBUG: making new island ");
        try {
            NewIsland.builder()
            .player(player)
            .reason(Reason.RESET)
            .oldIsland(oldIsland)
            .build();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create island for player.");
            user.sendMessage("general.errors.general");
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub
        
    }
}
