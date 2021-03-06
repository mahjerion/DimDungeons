package com.catastrophe573.dimdungeons.block;

import com.catastrophe573.dimdungeons.DimDungeons;
import com.catastrophe573.dimdungeons.item.ItemPortalKey;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class TileEntityPortalKeyhole extends TileEntity
{
    public static final String REG_NAME = "tileentity_portal_keyhole";

    @ObjectHolder(DimDungeons.RESOURCE_PREFIX + REG_NAME)
    public static TileEntityType<TileEntityPortalKeyhole> TYPE;

    public TileEntityPortalKeyhole()
    {
	super(TYPE);
    }

    public TileEntityPortalKeyhole(TileEntityType<?> tileEntityTypeIn)
    {
	super(tileEntityTypeIn);
    }

    private ItemStack objectInserted = ItemStack.EMPTY;
    private static final String ITEM_PROPERTY_KEY = "objectInserted";

    @Override
    public void read(CompoundNBT compound)
    {
	super.read(compound);
	readMyNBTData(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
	writeMyNBTData(compound);
	return super.write(compound);
    }

    // synchronize on chunk loading
    @Override
    public void handleUpdateTag(CompoundNBT nbt)
    {
	read(nbt);
    }

    // synchronize on chunk loading
    @Override
    public CompoundNBT getUpdateTag()
    {
	return write(new CompoundNBT());
    }

    // synchronize on block updates
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
	read(packet.getNbtCompound());
    }

    // synchronize on block updates
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
	CompoundNBT tag = write(new CompoundNBT());
	return new SUpdateTileEntityPacket(pos, 1, tag); // Forge recommends putting -1 as the second parameter
    }

    // business logic for this object
    public void writeMyNBTData(CompoundNBT compound)
    {
	// always send this, even if it is empty or air
	CompoundNBT itemNBT = new CompoundNBT();
	compound.put(ITEM_PROPERTY_KEY, objectInserted.write(itemNBT));
    }

    // business logic for this object
    public void readMyNBTData(CompoundNBT compound)
    {
	if (compound.contains(ITEM_PROPERTY_KEY))
	{
	    this.objectInserted = (ItemStack.read(compound.getCompound(ITEM_PROPERTY_KEY)));
	}
    }

    public boolean isFilled()
    {
	return !objectInserted.isEmpty();
    }

    public boolean isActivated()
    {
	ItemStack item = getObjectInserted();

	if (item.isEmpty())
	{
	    return false;
	}
	// awakened ItemPortalKeys will open a portal to the dungeon dimension
	else if (item.getItem() instanceof ItemPortalKey)
	{
	    ItemPortalKey key = (ItemPortalKey) item.getItem();
	    return key.isActivated(item);
	}
	// three vanilla blocks will also open portals to the 3 vanilla dimensions?
	else if (Block.getBlockFromItem(item.getItem()) != null)
	{
//	    Block b = Block.getBlockFromItem(item.getItem());
//	    return b == Blocks.NETHERRACK || b == Blocks.END_STONE || b == Blocks.GRASS_BLOCK;
	}

	return false;
    }

    public ItemStack getObjectInserted()
    {
	return this.objectInserted;
    }

    //  be sure to notify the world of a block update after calling this
    public void setContents(ItemStack item)
    {
	this.objectInserted = item;
	this.objectInserted.setCount(1);
	this.markDirty();
    }

    //  be sure to notify the world of a block update after calling this
    public void removeContents()
    {
	this.objectInserted = ItemStack.EMPTY;
	this.markDirty();
    }
}