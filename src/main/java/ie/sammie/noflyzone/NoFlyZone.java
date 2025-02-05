package ie.sammie.noflyzone;

import net.fabricmc.api.ModInitializer;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoFlyZone implements ModInitializer {
    public static final String MOD_ID = "NoFlyZone";
    public static NoFlyZone INSTANCE;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
        LOGGER.info("The server has become a NoFlyZone for players during initial login.");
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            LuckPerms luckPerms = LuckPermsProvider.get();

            User user = luckPerms.getUserManager().getUser(player.getUuid());

            if (user != null) {
                boolean hasBypassPermission = user.getCachedData()
                        .getPermissionData()
                        .checkPermission("noflyzone.bypass")
                        .asBoolean();

                // If the player doesn't have the bypass permission, disable flight if enabled
                if (!hasBypassPermission) {
                    if (player.getAbilities().allowFlying) {
                        player.getAbilities().allowFlying = false; // Disable flight
                        player.getAbilities().flying = false; // Ensure they stop flying
                        player.sendAbilitiesUpdate(); // Sync changes with client
                        player.sendMessage(Text.literal("§cYour flight has been disabled."), false);
                    }
                }
            } else {
                System.err.println("Could not find LuckPerms user data for player: " + player.getName());
            }
        });
    }
}
