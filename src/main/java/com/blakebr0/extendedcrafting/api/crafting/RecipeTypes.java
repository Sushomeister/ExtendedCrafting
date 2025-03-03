package com.blakebr0.extendedcrafting.api.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * The custom recipe types added by Extended Crafting
 */
public class RecipeTypes {
    public static final RecipeType<ICombinationRecipe> COMBINATION = new RecipeType<ICombinationRecipe>() {
        @Override
        public <C extends Container> Optional<ICombinationRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
            return recipe.matches(inv, world) ? Optional.of((ICombinationRecipe) recipe) : Optional.empty();
        }
    };
    public static final RecipeType<ITableRecipe> TABLE = new RecipeType<ITableRecipe>() {
        @Override
        public <C extends Container> Optional<ITableRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
            return recipe.matches(inv, world) ? Optional.of((ITableRecipe) recipe) : Optional.empty();
        }
    };
    public static final RecipeType<ICompressorRecipe> COMPRESSOR = new RecipeType<ICompressorRecipe>() {
        @Override
        public <C extends Container> Optional<ICompressorRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
            return recipe.matches(inv, world) ? Optional.of((ICompressorRecipe) recipe) : Optional.empty();
        }
    };
    public static final RecipeType<IEnderCrafterRecipe> ENDER_CRAFTER = new RecipeType<IEnderCrafterRecipe>() {
        @Override
        public <C extends Container> Optional<IEnderCrafterRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
            return recipe.matches(inv, world) ? Optional.of((IEnderCrafterRecipe) recipe) : Optional.empty();
        }
    };
}
