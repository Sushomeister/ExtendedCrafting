package com.blakebr0.extendedcrafting.container;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import com.blakebr0.extendedcrafting.container.slot.AutoTableOutputSlot;
import com.blakebr0.extendedcrafting.container.slot.TableOutputSlot;
import com.blakebr0.extendedcrafting.init.ModContainerTypes;
import com.blakebr0.extendedcrafting.tileentity.AutoTableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class AdvancedAutoTableContainer extends AbstractContainerMenu {
	private final Function<Player, Boolean> isUsableByPlayer;
	private final ContainerData data;
	private final BlockPos pos;
	private final Level world;
	private final Container result;

	private AdvancedAutoTableContainer(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buffer) {
		this(type, id, playerInventory, p -> false, AutoTableTileEntity.Advanced.createInventoryHandler(null), new SimpleContainerData(6), buffer.readBlockPos());
	}

	private AdvancedAutoTableContainer(MenuType<?> type, int id, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
		super(type, id);
		this.isUsableByPlayer = isUsableByPlayer;
		this.data = data;
		this.pos = pos;
		this.world = playerInventory.player.level;
		this.result = new ResultContainer();

		var matrix = new ExtendedCraftingInventory(this, inventory, 5, true);

		this.addSlot(new TableOutputSlot(this, matrix, this.result, 0, 154, 37));
		
		int i, j;
		for (i = 0; i < 5; i++) {
			for (j = 0; j < 5; j++) {
				this.addSlot(new Slot(matrix, j + i * 5, 26 + j * 18, 18 + i * 18));
			}
		}

		this.addSlot(new AutoTableOutputSlot(this, matrix, inventory, 25, 154, 81));

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 13 + j * 18, 124 + i * 18));
			}
		}

		for (j = 0; j < 9; j++) {
			this.addSlot(new Slot(playerInventory, j, 13 + j * 18, 182));
		}

		this.slotsChanged(matrix);
		this.addDataSlots(data);
	}

	@Override
	public void slotsChanged(Container matrix) {
		var recipe = this.world.getRecipeManager().getRecipeFor(RecipeTypes.TABLE, matrix, this.world);

		if (recipe.isPresent()) {
			var result = recipe.get().assemble(matrix);
			this.result.setItem(0, result);
		} else {
			this.result.setItem(0, ItemStack.EMPTY);
		}

		super.slotsChanged(matrix);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.isUsableByPlayer.apply(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotNumber) {
		var itemstack = ItemStack.EMPTY;
		var slot = this.slots.get(slotNumber);

		if (slot.hasItem()) {
			var itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();

			if (slotNumber == 0 || slotNumber == 26) {
				if (!this.moveItemStackTo(itemstack1, 27, 63, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (slotNumber >= 27 && slotNumber < 63) {
				if (!this.moveItemStackTo(itemstack1, 1, 26, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 27, 63, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public static AdvancedAutoTableContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new AdvancedAutoTableContainer(ModContainerTypes.ADVANCED_AUTO_TABLE.get(), windowId, playerInventory, buffer);
	}

	public static AdvancedAutoTableContainer create(int windowId, Inventory playerInventory, Function<Player, Boolean> isUsableByPlayer, BaseItemStackHandler inventory, ContainerData data, BlockPos pos) {
		return new AdvancedAutoTableContainer(ModContainerTypes.ADVANCED_AUTO_TABLE.get(), windowId, playerInventory, isUsableByPlayer, inventory, data, pos);
	}
}