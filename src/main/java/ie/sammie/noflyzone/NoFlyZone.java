package ie.sammie.noflyzone;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class NoFlyZone implements ModInitializer {
    public static final String MOD_ID = "noflyzone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("The server has become a NoFlyZone for players during initial login.");

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            CompletableFuture.supplyAsync(() -> {
                return Permissions.check(player, "noflyzone.bypass"); // Perform permission check asynchronously
            }).thenAccept(hasBypassPermission -> {
                server.submitAndJoin(() -> {
                    // Check if player does NOT have the bypass permission
                    if (hasBypassPermission == null || !hasBypassPermission) {
                        if (player.getAbilities().allowFlying) {
                            player.getAbilities().allowFlying = false; // Disable flight
                            player.getAbilities().flying = false; // Ensure they stop flying
                            player.sendAbilitiesUpdate(); // Sync changes with client

                            player.sendMessage(Text.literal("§cYour flight has been disabled."), false);
                        }
                    }
                });
            }).exceptionally(ex -> {
                LOGGER.error("Error checking permissions for player: " + player.getName().getString());
                return null;
            });
        });
    }
}