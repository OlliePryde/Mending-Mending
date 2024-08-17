package com.mendmend;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MendingMending implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("mending-mending");

	//region GameRules

	//region Trident Repair
	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_PRISMARINE_SHARD =
			GameRuleRegistry.register("tridentRepairWithPrismarineShard", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_PRISMARINE_CRYSTAL =
			GameRuleRegistry.register("tridentRepairWithPrismarineCrystal", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

	public static final GameRules.Key<GameRules.BooleanRule> TRIDENT_REPAIR_SHELL =
			GameRuleRegistry.register("tridentRepairWithShell", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	//endregion

	//region Repair Costs

	public enum RepairXPCostMethod
	{
		vanilla,
		fixed
	}

	public static final GameRules.Key<EnumRule<RepairXPCostMethod>> REPAIRING_XP_COST_METHOD =
			GameRuleRegistry.register("repairingXPCostMethod", GameRules.Category.MISC, GameRuleFactory.createEnumRule(RepairXPCostMethod.fixed));

	//endregion


	@Override
	public void onInitialize() { }
}