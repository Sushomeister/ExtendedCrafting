package com.blakebr0.extendedcrafting.compat.jei.category;

import com.blakebr0.cucumber.util.Localizable;
import com.blakebr0.extendedcrafting.ExtendedCrafting;
import com.blakebr0.extendedcrafting.api.crafting.IEnderCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedEnderCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessEnderCrafterRecipe;
import com.blakebr0.extendedcrafting.init.ModBlocks;
import com.blakebr0.extendedcrafting.lib.ModTooltips;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class EnderCrafterCategory implements IRecipeCategory<IEnderCrafterRecipe> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ExtendedCrafting.MOD_ID, "textures/gui/jei/ender_crafting.png");
	public static final ResourceLocation UID = new ResourceLocation(ExtendedCrafting.MOD_ID, "ender_crafting");

	private final IDrawable background;
	private final IDrawableAnimated arrow;
	private final IDrawable icon;

	public EnderCrafterCategory(IGuiHelper helper) {
		this.background = helper.createDrawable(TEXTURE, 0, 0, 116, 54);

		var arrowDrawable = helper.createDrawable(TEXTURE, 195, 0, 24, 17);

		this.arrow = helper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);
		this.icon = helper.createDrawableIngredient(new ItemStack(ModBlocks.ENDER_CRAFTER.get()));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends IEnderCrafterRecipe> getRecipeClass() {
		return IEnderCrafterRecipe.class;
	}

	@Override
	public Component getTitle() {
		return Localizable.of("jei.category.extendedcrafting.ender_crafting").build();
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void draw(IEnderCrafterRecipe recipe, PoseStack stack, double mouseX, double mouseY) {
		this.arrow.draw(stack, 61, 19);
	}

	@Override
	public List<Component> getTooltipStrings(IEnderCrafterRecipe recipe, double mouseX, double mouseY) {
		if (mouseX > 60 && mouseX < 83 && mouseY > 19 && mouseY < 34) {
			return Collections.singletonList(ModTooltips.SECONDS.args(recipe.getCraftingTime()).color(ChatFormatting.WHITE).build());
		}

		return Collections.emptyList();
	}

	@Override
	public void setIngredients(IEnderCrafterRecipe recipe, IIngredients ingredients) {
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		ingredients.setInputIngredients(recipe.getIngredients());
	}

	@Override
	public void setRecipe(IRecipeLayout layout, IEnderCrafterRecipe recipe, IIngredients ingredients) {
		var stacks = layout.getItemStacks();
		var inputs = ingredients.getInputs(VanillaTypes.ITEM);
		var outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);

		stacks.init(0, false, 94, 18);
		stacks.set(0, outputs);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int index = 1 + j + (i * 3);
				stacks.init(index, true, j * 18, i * 18);
			}
		}

		if (recipe instanceof ShapedEnderCrafterRecipe shaped) {
			int stackIndex = 0;

			for (int i = 0; i < shaped.getHeight(); i++) {
				for (int j = 0; j < shaped.getWidth(); j++) {
					int index = 1 + (i * 3) + j;

					stacks.set(index, inputs.get(stackIndex));

					stackIndex++;
				}
			}
		} else if (recipe instanceof ShapelessEnderCrafterRecipe) {
			for (int i = 0; i < inputs.size(); i++) {
				stacks.set(i + 1, inputs.get(i));
			}

			layout.setShapeless();
		}

		layout.moveRecipeTransferButton(122, 41);
	}
}
