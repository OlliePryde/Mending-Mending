package com.mendmend;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MendingMending implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("mending-mending");

	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_PRISMARINE_SHARD =
			GameRuleRegistry.register("tridentRepairWithPrismarineShard", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_PRISMARINE_CRYSTAL =
			GameRuleRegistry.register("tridentRepairWithPrismarineCrystal", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_SHELL =
			GameRuleRegistry.register("tridentRepairWithShell", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	@Override
	public void onInitialize() { }
}