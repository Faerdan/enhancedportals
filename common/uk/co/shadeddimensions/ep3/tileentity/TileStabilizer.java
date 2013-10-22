package uk.co.shadeddimensions.ep3.tileentity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;
import uk.co.shadeddimensions.ep3.network.CommonProxy;
import uk.co.shadeddimensions.ep3.util.ChunkCoordinateUtils;
import uk.co.shadeddimensions.ep3.util.WorldCoordinates;

public class TileStabilizer extends TileEnhancedPortals
{
    public boolean hasConfigured;
    ArrayList<ChunkCoordinates> blockList;
    ChunkCoordinates mainBlock;
    
    public TileStabilizer()
    {
        mainBlock = null;
        hasConfigured = false;
        blockList = new ArrayList<ChunkCoordinates>();
    }
    
    public TileStabilizer getMainBlock()
    {
        if (mainBlock != null)
        {
            TileEntity tile = worldObj.getBlockTileEntity(mainBlock.posX, mainBlock.posY, mainBlock.posZ);
        
            if (tile != null && tile instanceof TileStabilizer)
            {
                return (TileStabilizer) tile;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean activate(EntityPlayer player)
    {
        if (CommonProxy.isClient() || hasConfigured || player.inventory.getCurrentItem() == null || player.inventory.getCurrentItem().getItem().itemID != CommonProxy.itemWrench.itemID)
        {
            return false;
        }
        
        WorldCoordinates topLeft = getWorldCoordinates();
        
        while (topLeft.offset(ForgeDirection.WEST).getBlockId() == CommonProxy.blockStabilizer.blockID)  // Get the westernmost block
        {
            topLeft = topLeft.offset(ForgeDirection.WEST);
        }
        
        while (topLeft.offset(ForgeDirection.NORTH).getBlockId() == CommonProxy.blockStabilizer.blockID) // Get the northenmost block
        {
            topLeft = topLeft.offset(ForgeDirection.NORTH);
        }
        
        while (topLeft.offset(ForgeDirection.UP).getBlockId() == CommonProxy.blockStabilizer.blockID) // Get the highest block
        {
            topLeft = topLeft.offset(ForgeDirection.UP);
        }
        
        ArrayList<ChunkCoordinates> blocks = checkShape(topLeft, true); // Try the X axis
        
        if (blocks.isEmpty())
        {
            blocks = checkShape(topLeft, false); // Try the Z axis before failing
        }
        
        if (!blocks.isEmpty())
        {
            for (ChunkCoordinates c : blocks)
            {
                TileStabilizer t = (TileStabilizer) worldObj.getBlockTileEntity(c.posX, c.posY, c.posZ);
                t.hasConfigured = true;
                t.mainBlock = topLeft;
                CommonProxy.sendUpdatePacketToAllAround(t);
            }
            
            getMainBlock().blockList = blocks;
        }
        
        return true;
    }
    
    ArrayList<ChunkCoordinates> checkShape(WorldCoordinates topLeft, boolean isX)
    {
        ArrayList<ChunkCoordinates> blocks = new ArrayList<ChunkCoordinates>();
        
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                for (int k = 0; k < 2; k++)
                {
                    if (worldObj.getBlockId(topLeft.posX + (isX ? i : j), topLeft.posY - k, topLeft.posZ + (!isX ? i : j)) != CommonProxy.blockStabilizer.blockID)
                    {
                        return new ArrayList<ChunkCoordinates>();
                    }
                    
                    blocks.add(new ChunkCoordinates(topLeft.posX + (isX ? i : j), topLeft.posY - k, topLeft.posZ + (!isX ? i : j)));
                }
            }
        }
        
        return blocks;
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        
        tag.setBoolean("hasConfigured", hasConfigured);
        ChunkCoordinateUtils.saveChunkCoord(tag, mainBlock, "mainBlock");
        
        if (!blockList.isEmpty())
        {
            ChunkCoordinateUtils.saveChunkCoordList(tag, blockList, "blockList");
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        
        hasConfigured = tag.getBoolean("hasConfigured");
        mainBlock = ChunkCoordinateUtils.loadChunkCoord(tag, "mainBlock");
        
        if (tag.hasKey("blockList"))
        {
            blockList = ChunkCoordinateUtils.loadChunkCoordList(tag, "blockList");
        }
    }
    
    @Override
    public void fillPacket(DataOutputStream stream) throws IOException
    {
        super.fillPacket(stream);
        
        stream.writeBoolean(hasConfigured);
        
        if (mainBlock != null)
        {
            stream.writeInt(mainBlock.posX);
            stream.writeInt(mainBlock.posY);
            stream.writeInt(mainBlock.posZ);
        }
        else
        {
            stream.writeInt(0);
            stream.writeInt(-1);
            stream.writeInt(0);
        }
    }
    
    public void deconstruct()
    {        
        for (ChunkCoordinates c : blockList)
        {
            TileStabilizer t = (TileStabilizer) worldObj.getBlockTileEntity(c.posX, c.posY, c.posZ);
            t.hasConfigured = false;
            t.mainBlock = null;
            CommonProxy.sendUpdatePacketToAllAround(t);
        }
    }
    
    @Override
    public void breakBlock(int oldBlockID, int oldMetadata)
    {
        if (hasConfigured)
        {
            TileStabilizer main = getMainBlock();
            
            if (main == null)
            {
                return;
            }
            
            main.deconstruct();
        }
    }
    
    @Override
    public void usePacket(DataInputStream stream) throws IOException
    {
        super.usePacket(stream);
        
        hasConfigured = stream.readBoolean();        
        ChunkCoordinates c = new ChunkCoordinates(stream.readInt(), stream.readInt(), stream.readInt());
        
        if (c.posX == -1)
        {
            c = null;
        }
        
        mainBlock = c;
        
        worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
    }
}