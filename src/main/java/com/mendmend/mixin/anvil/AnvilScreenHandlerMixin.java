package com.mendmend.mixin.anvil;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static com.mendmend.MendingMending.*;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow @Final private Property levelCost;

    @Shadow
    private @Nullable String newItemName;

    @Shadow
    private int repairItemUsage;

    @Shadow public abstract int getLevelCost();

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void updateResult(CallbackInfo ci) {
        var input = this.input.getStack(0);
        if (input.isEmpty()) {
            this.levelCost.set(0);
            this.output.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            return;
        }

        var totalCost = 0;
        var item = input.copy();
        if (this.newItemName != null && !StringHelper.isBlank(this.newItemName)) {
            if (!this.newItemName.equals(input.getName().getString())) {
                totalCost++;
                item.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
            }
        } else if (input.contains(DataComponentTypes.CUSTOM_NAME)) {
            totalCost++;
            item.remove(DataComponentTypes.CUSTOM_NAME);
        }

        var modifier = this.input.getStack(1);
        if (!modifier.isEmpty()) {
            var cost = repairAndEnchantItem(item, modifier);
            totalCost = (cost == 0) ? 0 : totalCost + cost;
        }

        if (totalCost == 0) {
            item = ItemStack.EMPTY;
        }

        this.levelCost.set(totalCost);
        this.output.setStack(0, item);
        this.sendContentUpdates();

        ci.cancel();
    }

    @Unique
    private int repairAndEnchantItem(ItemStack item, ItemStack modifier) {
        if (canRepairItem(item, modifier)) {
            return repairItem(item, modifier);
        }

        if (modifier.isOf(Items.ENCHANTED_BOOK)) {
            return enchantItem(item, modifier);
        }

        if (!item.isOf(Items.ENCHANTED_BOOK)) {
            return combineItems(item, modifier);
        }

        return 0;
    }

    @Unique
    private boolean canRepairItem(ItemStack item, ItemStack modifier) {
        if (item.isOf(Items.TRIDENT)) {
            return IsValidTridentRepairIngredient(modifier);
        }

        return item.getItem().canRepair(item, modifier);
    }

    @Unique
    private boolean IsValidTridentRepairIngredient(ItemStack ingredient)
    {
        // Trident specific repair recipes
        GameRules gr = player.getWorld().getGameRules();
        if (gr.getBoolean(TRIDENT_REPAIR_PRISMARINE_CRYSTAL)
                && ingredient.itemMatches(Registries.ITEM.getEntry(Items.PRISMARINE_CRYSTALS)))
        {
            return true;
        }
        if (gr.getBoolean(TRIDENT_REPAIR_PRISMARINE_SHARD)
                && ingredient.itemMatches(Registries.ITEM.getEntry(Items.PRISMARINE_SHARD)))
        {
            return true;
        }
        if (gr.getBoolean(TRIDENT_REPAIR_SHELL)
                && ingredient.itemMatches(Registries.ITEM.getEntry(Items.NAUTILUS_SHELL)))
        {
            return true;
        }

        return false;
    }

    @Unique
    private int repairItem(ItemStack item, ItemStack modifier) {
        var maxHealth = item.getMaxDamage();
        var health = maxHealth - item.getDamage();
        var maxRepairs = modifier.getCount();

        var singleRepair = singleItemRepairPercent(item) * maxHealth;

        var repaired = Math.min(maxHealth - health, maxRepairs * singleRepair);
        var newHealth = (int) (health + repaired);
        item.setDamage(maxHealth - newHealth);
        this.repairItemUsage = (int) Math.ceil(repaired / singleRepair);

        return (int) Math.ceil(repaired / 4.0);
    }

    @Unique
    private double singleItemRepairPercent(ItemStack item) {
        var one = Set.of(
                Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.GOLDEN_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
                Items.SHIELD, Items.CROSSBOW, Items.FLINT_AND_STEEL, Items.MACE
                        );
        if (one.contains(item.getItem())) {
            return 1.0;
        }

        var two = Set.of(
                Items.WOODEN_HOE, Items.STONE_HOE, Items.GOLDEN_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE,
                Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
                Items.SHEARS, Items.FISHING_ROD, Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK
                        );
        if (two.contains(item.getItem())) {
            return 1 / 2.0;
        }

        var three = Set.of(
                Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
                Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
                Items.BOW
                          );
        if (three.contains(item.getItem())) {
            return 1 / 3.0;
        }

        return 1 / 4.0;
    }

    @Unique
    private int enchantItem(ItemStack item, ItemStack modifier) {
        var enchants = new ItemEnchantmentsComponent.Builder(item.getEnchantments());
        var isBook = item.isOf(Items.ENCHANTED_BOOK);
        var totalCost = 0;

        var modifierEnchants = EnchantmentHelper.getEnchantments(modifier);
        var outputEnchants = new ItemEnchantmentsComponent.Builder(item.getEnchantments());
        for (var enchant : modifierEnchants.getEnchantments()) {
            if (!isBook && !item.canBeEnchantedWith(enchant, EnchantingContext.ACCEPTABLE)) {
                continue;
            }

            outputEnchants.add(enchant, modifierEnchants.getLevel(enchant));
        }
        var output = outputEnchants.build();

        // Combine all the enchants and get the costs here to make it identical no matter the order
        for (var enchant : output.getEnchantments()) {
            if (!isBook && !item.canBeEnchantedWith(enchant, EnchantingContext.ACCEPTABLE)) {
                continue;
            }

            enchants.add(enchant, modifierEnchants.getLevel(enchant));
            totalCost += enchant.value().getAnvilCost();
        }

        EnchantmentHelper.set(item, enchants.build());
        return totalCost;
    }

    @Unique
    private int combineItems(ItemStack item, ItemStack modifier) {
        if (!item.isOf(modifier.getItem())) {
            return 0;
        }

        var totalCost = enchantItem(item, modifier);
        if (!item.isDamaged()) {
            return totalCost;
        }

        var maxHealth = item.getMaxDamage();
        var health = maxHealth - item.getDamage();
        var modifierHealth = maxHealth - modifier.getDamage();
        var newHealth = Math.min(maxHealth, (int) (health + modifierHealth + .12 * maxHealth));

        item.setDamage(maxHealth - newHealth);
        return totalCost + (int) Math.ceil((newHealth - health) / 4.0);
    }
}