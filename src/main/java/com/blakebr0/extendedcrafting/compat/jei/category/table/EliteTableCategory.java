package com.blakebr0.extendedcrafting.compat.jei.category.table;

import com.blakebr0.cucumber.util.Localizable;
import com.blakebr0.extendedcrafting.ExtendedCrafting;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.compat.jei.JeiCompat;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessTableRecipe;
import com.blakebr0.extendedcrafting.init.ModBlocks;
import com.blakebr0.extendedcrafting.lib.ModTooltips;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EliteTableCategory implements IRecipeCategory<ITableRecipe> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ExtendedCrafting.MOD_ID, "textures/gui/jei/elite_crafting.png");
	public static final RecipeType<ITableRecipe> RECIPE_TYPE = RecipeType.create(ExtendedCrafting.MOD_ID, "elite_crafting", ITableRecipe.class);

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable required;

	public EliteTableCategory(IGuiHelper helper) {
		this.background = helper.createDrawable(TEXTURE, 0, 0, 126, 159);
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ELITE_TABLE.get()));
		this.required = helper.createDrawable(JeiCompat.ICONS, 0, 0, 15, 15);
	}

	@Override
	public ResourceLocation getUid() {
		return RECIPE_TYPE.getUid();
	}

	@Override
	public Class<? extends ITableRecipe> getRecipeClass() {
		return ITableRecipe.class;
	}

	@Override
	public RecipeType<ITableRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@Override
	public Component getTitle() {
		return Localizable.of("jei.category.extendedcrafting.elite_crafting").build();
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
	public void draw(ITableRecipe recipe, IRecipeSlotsView slots, PoseStack stack, double mouseX, double mouseY) {
		stack.pushPose();
		stack.scale(0.5F, 0.5F, 0.5F);

		var shapeless = recipe instanceof ShapelessTableRecipe;

		if (recipe.hasRequiredTier())
			this.required.draw(stack, shapeless ? 217 : 237, 257);

		stack.popPose();
	}

	@Override
	public List<Component> getTooltipStrings(ITableRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		var shapeless = recipe instanceof ShapelessTableRecipe;
		int sX = (shapeless ? 217 : 237) / 2, sY = 257 / 2;

		if (recipe.hasRequiredTier() && mouseX > sX - 1 && mouseX < sX + 8 && mouseY > sY - 1 && mouseY < sY + 8) {
			return List.of(ModTooltips.REQUIRES_TABLE.args(recipe.getTier()).color(ChatFormatting.WHITE).build());
		}

		return List.of();
	}

	@Override
	public void setIngredients(ITableRecipe recipe, IIngredients ingredients) {
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		ingredients.setInputIngredients(recipe.getIngredients());
	}

	@Override
	public void setRecipe(IRecipeLayout layout, ITableRecipe recipe, IIngredients ingredients) {
		var stacks = layout.getItemStacks();
		var inputs = ingredients.getInputs(VanillaTypes.ITEM);
		var outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);

		stacks.init(0, false, 65, 137);
		stacks.set(0, outputs);

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				int index = 1 + j + (i * 7);
				stacks.init(index, true, j * 18, i * 18);
			}
		}

		if (recipe instanceof ShapedTableRecipe shaped) {
			int heightOffset = Math.floorDiv(7 - shaped.getHeight(), 2);
			int widthOffset = Math.floorDiv(7 - shaped.getWidth(), 2);
			int stackIndex = 0;

			for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++) {
				for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
					int index = 1 + (i * 7) + j;

					stacks.set(index, inputs.get(stackIndex));

					stackIndex++;
				}
			}
		} else if (recipe instanceof ShapelessTableRecipe) {
			for (int i = 0; i < inputs.size(); i++) {
				stacks.set(i + 1, inputs.get(i));
			}
		}

		layout.moveRecipeTransferButton(113, 146);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ITableRecipe recipe, IFocusGroup focuses) {
		var inputs = recipe.getIngredients();
		var output = recipe.getResultItem();

		if (recipe instanceof ShapedTableRecipe shaped) {
			int heightOffset = Math.floorDiv(7 - shaped.getHeight(), 2);
			int widthOffset = Math.floorDiv(7 - shaped.getWidth(), 2);
			int stackIndex = 0;

			for (int i = heightOffset; i < shaped.getHeight() + heightOffset; i++) {
				for (int j = widthOffset; j < shaped.getWidth() + widthOffset; j++) {
					builder.addSlot(RecipeIngredientRole.INPUT, j * 18, i * 18).addIngredients(inputs.get(stackIndex));

					stackIndex++;
				}
			}
		} else if (recipe instanceof ShapelessTableRecipe) {
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 7; j++) {
					int index = j + (i * 7);

					builder.addSlot(RecipeIngredientRole.INPUT, j * 18, i * 18).addIngredients(inputs.get(index));
				}
			}

			builder.setShapeless(237, 257);
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 138).addItemStack(output);

		builder.moveRecipeTransferButton(113, 146);
	}
}
