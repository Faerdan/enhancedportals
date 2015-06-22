package enhanced.portals.client.gui;

import java.awt.Color;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import enhanced.base.client.gui.BaseGui;
import enhanced.base.utilities.Localization;
import enhanced.portals.EnhancedPortals;
import enhanced.portals.block.BlockFrame;
import enhanced.portals.block.BlockPortal;
import enhanced.portals.client.gui.elements.ElementGlyphDisplay;
import enhanced.portals.inventory.BaseContainer;
import enhanced.portals.inventory.ContainerDialingAdd;
import enhanced.portals.network.ProxyClient;
import enhanced.portals.network.GuiHandler;
import enhanced.portals.network.packet.PacketGuiData;
import enhanced.portals.network.packet.PacketRequestGui;
import enhanced.portals.portal.PortalTextureManager;
import enhanced.portals.tile.TileDialingDevice;

public class GuiDialingAdd extends BaseGui {
    public static final int CONTAINER_SIZE = 131;
    protected TileDialingDevice dial;
    protected GuiTextField text;
    protected boolean isEditing = false;
    protected int particleFrameType = -1, particleFrame, particleFrameCycle;
    protected int[] particleFrames = new int[] { 0 };
    protected ElementGlyphDisplay display;

    public GuiDialingAdd(TileDialingDevice d, EntityPlayer p) {
        super(new ContainerDialingAdd(d, p.inventory), CONTAINER_SIZE);
        dial = d;
        name = "gui.dialDevice";
        setHidePlayerInventory();
        allowUserInput = true;
        Keyboard.enableRepeatEvents(true);

        if (ProxyClient.saveTexture == null)
            ProxyClient.saveTexture = new PortalTextureManager();
    }

    protected GuiDialingAdd(BaseContainer container, int cSize) {
        super(container, cSize);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (!text.textboxKeyTyped(par1, par2))
            super.keyTyped(par1, par2);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        text.updateCursorCounter();

        if (particleFrameCycle >= 20) {
            particleFrame++;
            particleFrameCycle = 0;

            if (particleFrame >= particleFrames.length)
                particleFrame = 0;
        }

        particleFrameCycle++;
    }

    @Override
    public void initGui() {
        super.initGui();

        text = new GuiTextField(getFontRenderer(), guiLeft + 7, guiTop + 18, 162, 20);
        text.setText(ProxyClient.saveName);
        text.setCursorPosition(0);

        display = new ElementGlyphDisplay(this, 7, 52, ProxyClient.saveGlyph);
        addElement(display);

        buttonList.add(new GuiButton(0, guiLeft + 7, guiTop + ySize - 27, 80, 20, Localization.get(EnhancedPortals.MOD_ID, "gui.cancel")));
        buttonList.add(new GuiButton(1, guiLeft + xSize - 87, guiTop + ySize - 27, 80, 20, Localization.get(EnhancedPortals.MOD_ID, "gui.save")));

        buttonList.add(new GuiButton(100, guiLeft + 57, guiTop + 83, 20, 20, ""));
        buttonList.add(new GuiButton(101, guiLeft + (xSize / 2 - 10), guiTop + 83, 20, 20, ""));
        buttonList.add(new GuiButton(102, guiLeft + 99, guiTop + 83, 20, 20, ""));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        text.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        super.drawGuiContainerBackgroundLayer(f, i, j);
        text.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
        getFontRenderer().drawString(Localization.get(EnhancedPortals.MOD_ID, "gui.uniqueIdentifier"), 7, 43, 0x404040);
        getFontRenderer().drawString(Localization.get(EnhancedPortals.MOD_ID, "gui.textures"), 7, 73, 0x404040);

        GL11.glColor3f(1f, 1f, 1f);

        getItemRenderer().renderWithColor = false;
        ItemStack frame = new ItemStack(BlockFrame.instance, 0, 0), portal = new ItemStack(BlockPortal.instance, 0, 0);
        Color frameColour = new Color(0xFFFFFF), portalColour = new Color(0xFFFFFF), particleColour = new Color(0x0077D8);
        int particleType = 0;

        if (ProxyClient.saveTexture != null) {
            frameColour = new Color(ProxyClient.saveTexture.getFrameColour());
            portalColour = new Color(ProxyClient.saveTexture.getPortalColour());
            particleColour = new Color(ProxyClient.saveTexture.getParticleColour());
            particleType = ProxyClient.saveTexture.getParticleType();

            if (ProxyClient.saveTexture.getFrameItem() != null)
                frame = ProxyClient.saveTexture.getFrameItem();

            if (ProxyClient.saveTexture.getPortalItem() != null)
                portal = ProxyClient.saveTexture.getPortalItem();

            if (particleFrameType != particleType) {
                particleFrameType = particleType;
                particleFrame = 0;
                particleFrameCycle = 0;
                particleFrames = ProxyClient.particleSets.get(ProxyClient.saveTexture.getParticleType()).frames;
            }
        }

        GL11.glColor3f(frameColour.getRed() / 255F, frameColour.getGreen() / 255F, frameColour.getBlue() / 255F);

        if (ProxyClient.saveTexture.hasCustomFrameTexture())
            drawIconNoReset(ProxyClient.customFrameTextures.get(ProxyClient.saveTexture.getCustomFrameTexture()), 59, 85, 0);
        else
            drawItemStack(frame, 59, 85);

        GL11.glColor3f(portalColour.getRed() / 255F, portalColour.getGreen() / 255F, portalColour.getBlue() / 255F);

        if (ProxyClient.saveTexture.hasCustomPortalTexture())
            drawIconNoReset(ProxyClient.customPortalTextures.get(ProxyClient.saveTexture.getCustomPortalTexture()), 80, 85, 0);
        else
            drawItemStack(portal, 80, 85);

        GL11.glColor3f(particleColour.getRed() / 255F, particleColour.getGreen() / 255F, particleColour.getBlue() / 255F);
        getTextureManager().bindTexture(new ResourceLocation("textures/particle/particles.png"));
        drawTexturedModalRect(101, 85, particleFrames[particleFrame] % 16 * 16, particleFrames[particleFrame] / 16 * 16, 16, 16);
        GL11.glColor3f(1f, 1f, 1f);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (!isEditing) {
            ProxyClient.saveGlyph = null;
            ProxyClient.saveName = null;
            ProxyClient.saveTexture = null;
        } else
            ProxyClient.saveName = text.getText();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0)
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, GuiHandler.DIALING_DEVICE_B));
        else if (button.id == 1) // save
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", text.getText());
            tag.setString("uid", ProxyClient.saveGlyph.getGlyphString());
            ProxyClient.saveTexture.writeToNBT(tag, "texture");
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketGuiData(tag));
        } else if (button.id == 100) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, GuiHandler.TEXTURE_DIALING_SAVE_A));
        } else if (button.id == 101) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, GuiHandler.TEXTURE_DIALING_SAVE_B));
        } else if (button.id == 102) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, GuiHandler.TEXTURE_DIALING_SAVE_C));
        }
    }
}