package com.mendmend.mixin.xp;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.mendmend.MendingMending.MENDING_REPAIR_FACTOR;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "repairPlayerGears", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getRepairWithXp(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;I)I"))
    public int repairPlayerGears$getRepairWithXp(ServerWorld world, ItemStack stack, int baseRepairWithXp) {
        return EnchantmentHelper.getRepairWithXp(world, stack, (int) (baseRepairWithXp * world.getGameRules().get(MENDING_REPAIR_FACTOR).get()));
    }
}
