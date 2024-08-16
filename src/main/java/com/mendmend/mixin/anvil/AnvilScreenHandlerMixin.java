package com.mendmend.mixin.anvil;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.mendmend.MendingMending.*;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;canRepair(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean updateResult$canRepair(Item instance, @NotNull ItemStack stack, ItemStack ingredient)
    {
        if (stack.itemMatches(Registries.ITEM.getEntry(Items.TRIDENT)))
        {
            return IsValidTridentRepairIngredient(ingredient);
        }
        return instance.canRepair(stack, ingredient);
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
}