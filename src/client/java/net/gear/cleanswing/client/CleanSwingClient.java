package net.gear.cleanswing.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CleanSwingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			// Only act when the clicked block has no collision shape (grass, flowers, etc.)
			if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
				return InteractionResult.PASS;
			}

			// Find entities around the clicked position in the player's look direction
			List<Entity> entities = world.getEntities(player, new AABB(pos).expandTowards(player.getLookAngle()));

			if (entities.isEmpty()) {
				return InteractionResult.PASS;
			}

			boolean foundEntity = false;
			// Swords can sweep — attack all nearby entities; other weapons stop at the first
			boolean canSweep = player.getItemInHand(hand).is(ItemTags.SWORDS);
			Minecraft mc = Minecraft.getInstance();

			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.isAttackable()) {
					mc.gameMode.attack(mc.player, entity);
					foundEntity = true;
					if (!canSweep) {
						break;
					}
				}
			}

			if (foundEntity) {
				player.swing(InteractionHand.MAIN_HAND);
				return InteractionResult.SUCCESS; // cancels the block interaction
			}

			return InteractionResult.PASS;
		});
	}
}