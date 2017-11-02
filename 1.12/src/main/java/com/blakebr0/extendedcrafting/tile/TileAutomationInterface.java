package com.blakebr0.extendedcrafting.tile;

import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.text.WordUtils;

import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.lib.CustomEnergyStorage;
import com.blakebr0.extendedcrafting.config.ModConfig;
import com.blakebr0.extendedcrafting.crafting.CombinationRecipe;
import com.blakebr0.extendedcrafting.crafting.table.ITieredRecipe;
import com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager;
import com.blakebr0.extendedcrafting.lib.FakeRecipeHandler;
import com.blakebr0.extendedcrafting.lib.IExtendedTable;
import com.blakebr0.extendedcrafting.util.VanillaPacketDispatcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class TileAutomationInterface extends TileEntity implements ITickable {
	
	private final ItemStackHandler inventory = new StackHandler(2);
	private final ItemStackHandler recipe = new FakeRecipeHandler();
	private final CustomEnergyStorage energy = new CustomEnergyStorage(ModConfig.confInterfaceRFCapacity);
	private int oldEnergy;
	private ItemStack result = ItemStack.EMPTY;
	private boolean hasRecipe = false;
	private int autoInsert = -1;
	private int autoExtract = -1;
		
	public IItemHandlerModifiable getInventory() {
		return inventory;
	}
	
	public ItemStackHandler getRecipe() {
		return recipe;
	}
	
	public ItemStack getResult() {
		return result;
	}
	
	public CustomEnergyStorage getEnergy() {
		return energy;
	}
	
	@Override
	public void update() {
		boolean mark = false;
		if (!this.getWorld().isRemote) {
			ItemStack input = this.getInventory().getStackInSlot(0);
			ItemStack output = this.getInventory().getStackInSlot(1);
			if (this.hasTable()) {
				if (!input.isEmpty()) {
					if (this.hasRecipe() && this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate) {
						this.handleInput(input);
					}
				} // TODO: cleanup
				ItemStack result = this.getTable().getResult();
				IItemHandlerModifiable matrix = this.getTable().getMatrix();
				if (!result.isEmpty() && (output.isEmpty() || (output.isItemEqual(result) && output.getCount() < result.getMaxStackSize()))) {				
					if (this.getEnergy().getEnergyStored() >= ModConfig.confInterfaceRFRate) {
						ItemStack toInsert = result.copy(); toInsert.setCount(1);
						result.shrink(1);
						for (int i = 0; i < matrix.getSlots(); i++) {
							ItemStack slotStack = matrix.getStackInSlot(i);
							if (!slotStack.isEmpty()) {
								if (slotStack.getItem().hasContainerItem(slotStack) && slotStack.getCount() == 1) {
									matrix.setStackInSlot(i, slotStack.getItem().getContainerItem(slotStack));
								} else {
									matrix.setStackInSlot(i, StackHelper.decrease(slotStack.copy(), 1, false));
								}
							}
						}
						this.getInventory().insertItem(1, toInsert, false);
						this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
						this.markDirty();
					}
				}
			}
		}
		
		if (this.oldEnergy != this.energy.getEnergyStored()) {
			this.oldEnergy = this.energy.getEnergyStored();
			if (!mark) {
				mark = true;
			}
		}
		
		if (mark) {
			this.markDirty();
		}
	}
	
	private void handleInput(ItemStack input) {
		IExtendedTable table = this.getTable();
		IItemHandlerModifiable matrix = table.getMatrix();
		ItemStackHandler recipe = this.getRecipe();
		
		int slotToPut = -1;
		ItemStack stackToPut = ItemStack.EMPTY;
		for (int i = 0; i < matrix.getSlots(); i++) {
			ItemStack slot = matrix.getStackInSlot(i);
			ItemStack recipeStack = recipe.getStackInSlot(i);
			if ((slot.isEmpty() || slot.isItemEqual(input)) && input.isItemEqual(recipeStack)) {
				if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) {
					if (slot.isEmpty()) {
						slotToPut = i;
						break;
					} else if (stackToPut.isEmpty() || (!stackToPut.isEmpty() && slot.getCount() < stackToPut.getCount())) {
						slotToPut = i;
						stackToPut = slot.copy();
					}
				}
			}
		}
		
		if (slotToPut > -1) {
			ItemStack toInsert = input.copy(); toInsert.setCount(1);
			matrix.insertItem(slotToPut, toInsert, false);
			input.shrink(1);
			this.getEnergy().extractEnergy(ModConfig.confInterfaceRFRate, false);
			this.markDirty();
		}
	}
	
	public IExtendedTable getTable() {
		TileEntity tile = this.getWorld().getTileEntity(this.getPos().down());
		return tile != null && tile instanceof IExtendedTable ? (IExtendedTable) tile : null;
	}
	
	public boolean hasTable() {
		return getTable() != null;
	}
	
	public boolean hasRecipe() {
		return this.hasRecipe;
	}
	
	public void setHasRecipe(boolean hasRecipe) {
		this.hasRecipe = hasRecipe;
	}
	
	public void saveRecipe() {
		ItemStackHandler recipe = this.getRecipe();
		IItemHandlerModifiable matrix = this.getTable().getMatrix();
		recipe.setSize(matrix.getSlots());
		for (int i = 0; i < matrix.getSlots(); i++) {
			recipe.setStackInSlot(i, matrix.getStackInSlot(i).copy());
		}
		ItemStack result = this.getTable().getResult();
		if (result != null) {
			this.result = result;
		}
		this.setHasRecipe(true);
		this.markDirty();
	}
	
	public void clearRecipe() {
		ItemStackHandler recipe = this.getRecipe();
		recipe.setSize(1);
		this.result = ItemStack.EMPTY;
		this.setHasRecipe(false);
		this.markDirty();
	}
	
	public EnumFacing getInserterFace() {
		return this.autoInsert > -1 && this.autoInsert < 6 ? EnumFacing.values()[this.autoInsert] : null;
	}
	
	public EnumFacing getExtractorFace() {
		return this.autoExtract > -1 && this.autoExtract < 6 ? EnumFacing.values()[this.autoExtract] : null;
	}
	
	public String getInserterFaceName() {
		return this.getInserterFace() != null ? this.getInserterFace().getName().toUpperCase(Locale.ROOT) : "NONE"; // TODO: localize
	}
	
	public String getExtractorFaceName() {
		return this.getExtractorFace() != null ? this.getExtractorFace().getName().toUpperCase(Locale.ROOT) : "NONE"; // TODO: localize
	}
		
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tag.merge(inventory.serializeNBT());
		tag.merge(recipe.serializeNBT());
		tag.setInteger("Energy", this.energy.getEnergyStored());
		tag.setTag("Result", this.result.serializeNBT());
		tag.setBoolean("HasRecipe", this.hasRecipe);
		return tag;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		inventory.deserializeNBT(tag);
		recipe.deserializeNBT(tag);
		energy.setEnergy(tag.getInteger("Energy"));
		this.result = new ItemStack(tag.getCompoundTag("Result"));
		this.hasRecipe = tag.getBoolean("HasRecipe");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void markDirty() {
		super.markDirty();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing side) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		} else if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energy);
		}
		return super.getCapability(capability, side);
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.getWorld().getTileEntity(this.getPos()) == this && player.getDistanceSq(this.getPos().add(0.5, 0.5, 0.5)) <= 64;
	}
	
	class StackHandler extends ItemStackHandler {
				
		StackHandler(int size) {
			super(size);
		}

		@Override
		public void onContentsChanged(int slot) {
			TileAutomationInterface.this.markDirty();
		}
	}
}
