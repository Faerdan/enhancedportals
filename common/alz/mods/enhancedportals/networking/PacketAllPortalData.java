package alz.mods.enhancedportals.networking;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import alz.mods.enhancedportals.portals.PortalData;
import alz.mods.enhancedportals.portals.PortalTexture;
import alz.mods.enhancedportals.portals.TeleportData;
import alz.mods.enhancedportals.reference.Reference;

import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketAllPortalData extends PacketUpdate
{
	public List<PortalData> portalDataList;
	
	public PacketAllPortalData()
	{
		portalDataList = new ArrayList<PortalData>();
	}
	
	public PacketAllPortalData(int x, int y, int z, int dim, List<PortalData> portalData)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		Dimension = dim;
		portalDataList = portalData;
	}
	
	@Override
	public int getPacketID()
	{
		return Reference.Networking.DialDevice_AllPortalData;
	}

	@Override
	public Packet250CustomPayload getClientPacket()
	{
		return null; // We don't want to send this packet from the client
	}

	@Override
	public Packet250CustomPayload getServerPacket()
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		Packet250CustomPayload packet = new Packet250CustomPayload();

		try
		{
			dataStream.writeByte(getPacketID());
			addPacketData(dataStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		packet.channel = Reference.MOD_ID;
		packet.data = byteStream.toByteArray();
		packet.length = packet.data.length;
		packet.isChunkDataPacket = true;
		
		return packet;
	}

	@Override
	public void getPacketData(DataInputStream stream) throws IOException
	{
		xCoord = stream.readInt();
		yCoord = stream.readInt();
		zCoord = stream.readInt();
		Dimension = stream.readInt();
		
		int size = stream.readInt();
		
		for (int i = 0; i < size; i++)
		{
			PortalData PortalData = new PortalData();
			PortalData.DisplayName = stream.readUTF();
			PortalData.Texture = PortalTexture.getPortalTexture(stream.readInt());
			
			boolean hasTeleportData = stream.readBoolean();
			
			if (hasTeleportData)
			{
				PortalData.TeleportData = new TeleportData(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt());
			}
			else
			{
				PortalData.Frequency = stream.readInt();
			}
			
			portalDataList.add(PortalData);
		}
	}

	@Override
	public void addPacketData(DataOutputStream stream) throws IOException
	{
		stream.writeInt(xCoord);
		stream.writeInt(yCoord);
		stream.writeInt(zCoord);
		stream.writeInt(Dimension);
		stream.writeInt(portalDataList.size());
		
		for (PortalData portalData : portalDataList)
		{
			stream.writeUTF(portalData.DisplayName);
			stream.writeInt(portalData.Texture.ordinal());
			
			if (portalData.TeleportData != null)
			{
				stream.writeBoolean(true);
				stream.writeInt(portalData.TeleportData.GetX());
				stream.writeInt(portalData.TeleportData.GetY());
				stream.writeInt(portalData.TeleportData.GetZ());
				stream.writeInt(portalData.TeleportData.GetDimension());
			}
			else
			{
				stream.writeBoolean(false);
				stream.writeInt(portalData.Frequency);
			}
		}
	}
}