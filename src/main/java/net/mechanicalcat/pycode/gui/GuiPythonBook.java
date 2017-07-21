/*
 * Copyright (c) 2017 Richard Jones <richard@mechanicalcat.net>
 * All Rights Reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.mechanicalcat.pycode.gui;

import io.netty.buffer.Unpooled;
import net.mechanicalcat.pycode.script.PythonCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GuiPythonBook extends GuiScreen
{
    private static final ResourceLocation texture = new ResourceLocation("pycode:textures/gui/code_book.png");

    // texture dimensions
    private static final int TEX_WIDTH = 334;
    private static final int TEX_HEIGHT = 238;

    // TEXTURE LOCATIONS
    private static final int BOOK_PX_WIDTH = 334;   // pixel width of the entire book
    private static final int BOOK_PX_HEIGHT = 213;  // pixel height of the entire book

    // TEXT AREA IN TEXTURE
    private static final int EDITOR_PX_WIDTH = 224;
    private static final int EDITOR_PX_HEIGHT = 190;
    private static final int EDITOR_PX_TOP = 11;
    private static final int EDITOR_PX_LEFT = 44;

    // BUTTONS LOCATION
    private static final int BUTTONS_PX_LEFT = 296;
    private static final int BUTTONS_PX_TOP = 90;

    // PAGE LOCATION
    private static final int LOC_PX_LEFT = 282;
    private static final int LOC_PX_TOP = 15;
    private static final int LOC_PX_WIDTH = 44;

    private static final int TITLE_PX_LEFT = 13;
    private static final int TITLE_PX_BOTTOM = 192; // bottom because we render upwards

    private int xPosition;
    private int yPosition;

//    private final EntityPlayer editingPlayer;
    private final ItemStack bookObj;
    private NBTTagList bookPages;
    private String bookTitle;
    private int bookTotalPages = 1;
    private boolean bookIsModified;
    private int currPage = 0;

    private static final int BUTTON_DONE_ID = 0;
    private static final int BUTTON_CANCEL_ID = 1;
    private static final int BUTTON_NEXT_ID = 2;
    private static final int BUTTON_PREV_ID = 3;
    private static final int PAGE_EDIT_ID = 4;
    private static final int TITLE_EDIT_ID = 5;
    private GuiButton buttonDone;
    private GuiButton buttonCancel;
    private GuiButton buttonNextPage;
    private GuiButton buttonPreviousPage;
    private GuiTextArea pageEdit;
    private GuiVertTextField titleEdit;

    private static String TITLE_PLACEHOLDER = I18n.format("gui.python_book.title");

    private PythonCode code;
    private ScriptException codeException;
    private int timeToCheck;
    private String oldContent;
    private boolean codeChecked;

    public GuiPythonBook(ItemStack book)
    {
        this.bookObj = book;
        this.bookIsModified = false;

        if (book.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = book.getTagCompound();
            this.bookPages = nbttagcompound.getTagList("pages", 8);
            this.bookTitle = nbttagcompound.getString("title");

            this.bookPages = this.bookPages.copy();
            this.bookTotalPages = this.bookPages.tagCount();

            if (this.bookTotalPages < 1) this.bookTotalPages = 1;
        }
        else
        {
            this.bookPages = new NBTTagList();
            this.bookPages.appendTag(new NBTTagString("\n"));
            this.bookTitle = "";
            this.bookTotalPages = 1;
        }

        if (this.bookTitle.isEmpty())
        {
            this.bookTitle = TITLE_PLACEHOLDER;
        }

        this.code = new PythonCode();
        this.codeException = null;
        this.oldContent = "";
        this.codeChecked = false;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        xPosition = (this.width - BOOK_PX_WIDTH) / 2;
        yPosition = 2;

        this.buttonDone = this.addButton(new GuiButton(BUTTON_DONE_ID, xPosition + BUTTONS_PX_LEFT, yPosition + BUTTONS_PX_TOP, 70, 20, I18n.format("gui.done", new Object[0])));
        this.buttonCancel = this.addButton(new GuiButton(BUTTON_CANCEL_ID, xPosition + BUTTONS_PX_LEFT, yPosition + BUTTONS_PX_TOP + 22, 70, 20, I18n.format("gui.cancel", new Object[0])));

        this.buttonNextPage = this.addButton(new GuiPythonBook.NextPageButton(BUTTON_NEXT_ID, xPosition + LOC_PX_LEFT + 22, yPosition + LOC_PX_TOP + 25, true));
        this.buttonPreviousPage = this.addButton(new GuiPythonBook.NextPageButton(BUTTON_PREV_ID, xPosition + LOC_PX_LEFT + 2, yPosition + LOC_PX_TOP + 25, false));
        this.updateButtons();

        EditResponder r = new EditResponder(this);

        this.pageEdit = new GuiTextArea(PAGE_EDIT_ID, this.fontRenderer, xPosition + EDITOR_PX_LEFT, yPosition + EDITOR_PX_TOP, EDITOR_PX_WIDTH, EDITOR_PX_HEIGHT);
        String s = this.pageGetCurrent();
        this.pageEdit.setString(s);
        this.pageEdit.setFocused(true);
        this.pageEdit.setGuiResponder(r);

        this.titleEdit = new GuiVertTextField(TITLE_EDIT_ID, this.fontRenderer, xPosition + TITLE_PX_LEFT, yPosition + TITLE_PX_BOTTOM, 176, 15);
        this.titleEdit.setFocused(false);
        this.titleEdit.setDefaultText(TITLE_PLACEHOLDER);
        this.titleEdit.setText(this.bookTitle);
        this.titleEdit.setEnableBackgroundDrawing(false);
        this.titleEdit.setTextColor(0);
        this.titleEdit.setMaxStringLength(26);
        this.titleEdit.setGuiResponder(r);
    }

    @SideOnly(Side.CLIENT)
    class EditResponder implements GuiPageButtonList.GuiResponder
    {
        private GuiPythonBook parent;

        public EditResponder(GuiPythonBook parent)
        {
            this.parent = parent;
        }

        public void setEntryValue(int id, boolean value)
        {

        }

        public void setEntryValue(int id, float value)
        {

        }

        public void setEntryValue(int id, String value)
        {
            switch (id)
            {
                case PAGE_EDIT_ID: this.parent.pageSetCurrent(value); break;
                case TITLE_EDIT_ID: this.parent.bookTitle = value; break;
            }
            this.parent.bookIsModified = true;
        }
    }

    public void updateScreen()
    {
        super.updateScreen();

        if (this.pageEdit == null) return;

        this.pageEdit.update();
        this.titleEdit.updateCursorCounter();

        if (this.titleEdit.getText().equals(TITLE_PLACEHOLDER))
        {
            this.titleEdit.setTextColor(5592405);
        }
        else
        {
            this.titleEdit.setTextColor(00);
        }

        String content = pageEdit.getString();

        if (!this.oldContent.equals(content))
        {
            this.codeException = null;
            this.timeToCheck = 60;
            this.codeChecked = false;
            this.oldContent = content;
        }

        if (!this.codeChecked && this.timeToCheck-- < 0)
        {
            this.codeChecked = true;

            try
            {
                this.code.check(content);
                this.codeException = null;
            }
            catch (ScriptException e)
            {
                this.codeException = e;
            }
        }
    }

    private void updateButtons()
    {
        this.buttonNextPage.visible = true; // this.currPage < this.bookTotalPages - 1;
        this.buttonPreviousPage.visible = this.currPage > 0;
        this.buttonDone.visible = true;
        this.buttonCancel.visible = true;
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        boolean updateLines = false;
        if (button.enabled)
        {
            switch (button.id)
            {
                case BUTTON_DONE_ID:
                    this.sendBookToServer();
                    this.mc.displayGuiScreen(null);
                    break;
                case BUTTON_NEXT_ID:
                    if (this.currPage < this.bookTotalPages - 1)
                    {
                        ++this.currPage;
                        updateLines = true;
                    }
                    else
                    {
                        this.addNewPage();

                        if (this.currPage < this.bookTotalPages - 1)
                        {
                            ++this.currPage;
                            updateLines = true;
                        }
                    }
                    break;
                case BUTTON_PREV_ID:
                    if (this.currPage > 0)
                    {
                        --this.currPage;
                        updateLines = true;
                    }
                    break;
                case BUTTON_CANCEL_ID:
                    this.mc.displayGuiScreen(null);
                    break;
            }

            if (updateLines)
            {
                this.pageEdit.setString(this.pageGetCurrent());
            }
            this.updateButtons();
        }
    }

    private void sendBookToServer() throws IOException
    {
        if (!this.bookIsModified || this.bookPages == null) return;

        while (this.bookPages.tagCount() > 1)
        {
            String s = this.bookPages.getStringTagAt(this.bookPages.tagCount() - 1);
            if (!s.trim().isEmpty()) break;
            this.bookPages.removeTag(this.bookPages.tagCount() - 1);
        }

        this.bookObj.setTagInfo("pages", this.bookPages);
        String title = this.bookTitle;

        if (title.equals(TITLE_PLACEHOLDER)) title = "";

        this.bookObj.setTagInfo("title", new NBTTagString(title));

        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeItemStack(this.bookObj);
        this.mc.getConnection().sendPacket(new CPacketCustomPayload("MC|BEdit", packetbuffer));
    }

    @Override
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, (x - fontRendererIn.getStringWidth(text) / 2), y, color);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // I have no idea why, but sometimes pageEdit is null when this is invoked!!
        if (this.pageEdit == null) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, BOOK_PX_WIDTH, BOOK_PX_HEIGHT, TEX_WIDTH, TEX_HEIGHT);

        // draw the widgets
        this.pageEdit.drawEditor();
        this.titleEdit.drawTextBox();

        // render the page location
        // TODO consider using GuiLabel
        this.drawCenteredString(this.fontRenderer, "Page",
                xPosition + LOC_PX_LEFT + LOC_PX_WIDTH / 2,
                yPosition + LOC_PX_TOP, 0);

        String page_pos = String.format("%d of %d", this.currPage + 1, this.bookTotalPages);
        this.drawCenteredString(this.fontRenderer, page_pos,
                xPosition + LOC_PX_LEFT + LOC_PX_WIDTH / 2,
                yPosition + LOC_PX_TOP + this.fontRenderer.FONT_HEIGHT, 0);

        if (this.codeException != null)
        {
            String err = this.codeException.getMessage();
            if (err == null)
            {
                err = this.codeException.getClass().getName();
            }
            else
            {
                Pattern p = Pattern.compile("^(\\p{Alpha}+: )(.+) in <script> at");
                Matcher m = p.matcher(err);
                if (m.find())
                {
                    err = m.group(2);
                }

                if (err.startsWith("no viable alternative at input "))
                {
                    err = "unexpected " + err.substring(31);
                }
            }

            int col = this.codeException.getColumnNumber();
            int row = this.codeException.getLineNumber() < col ? this.codeException.getLineNumber() : 0;
            String[] lines = this.pageEdit.getLines();

            if (col > lines[row].length())
            {
               col = lines[row].length();
            }

            int x = this.pageEdit.xPosition + 12 + this.fontRenderer.getStringWidth(lines[row].substring(0, col));
            int y = this.pageEdit.yPosition + (row + 1) * this.fontRenderer.FONT_HEIGHT;

            int w = this.fontRenderer.getStringWidth(err);
            x -= w / 2;
            y += 8;
            Gui.drawRect(x - 2, y - 2, x + w + 2, y + this.fontRenderer.FONT_HEIGHT + 2, 0xfff1e2b8);
            this.fontRenderer.drawString(err, x , y, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.pageEdit.keyTyped(typedChar, keyCode);
        this.titleEdit.textboxKeyTyped(typedChar, keyCode);
    }

    private void addNewPage()
    {
        if (this.bookPages != null && this.bookPages.tagCount() < 50)
        {
            this.bookPages.appendTag(new NBTTagString("\n"));
            ++this.bookTotalPages;
            this.bookIsModified = true;
        }
    }

    /**
     * Returns the entire text of the current page as determined by currPage
     */
    private String pageGetCurrent()
    {
        return this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount() ? this.bookPages.getStringTagAt(this.currPage) : "\n";
    }

    /**
     * Sets the text of the current page as determined by currPage
     */
    private void pageSetCurrent(String text)
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            this.bookPages.set(this.currPage, new NBTTagString(text));
            this.bookIsModified = true;
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.pageEdit.mouseClicked(mouseX, mouseY, mouseButton);
        this.titleEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Executes the click event specified by the given chat component
     */
    public boolean handleComponentClick(ITextComponent component)
    {
        ClickEvent clickevent = component.getStyle().getClickEvent();

        if (clickevent == null)
        {
            return false;
        }
        else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE)
        {
            String s = clickevent.getValue();

            try
            {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < this.bookTotalPages && i != this.currPage)
                {
                    this.currPage = i;
                    this.updateButtons();
                    return true;
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            return false;
        }
        else
        {
            boolean flag = super.handleComponentClick(component);

            if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND)
            {
                this.mc.displayGuiScreen(null);
            }
            return flag;
        }
    }

    @SideOnly(Side.CLIENT)
    static class NextPageButton extends GuiButton
    {
        private final boolean isForward;

        public NextPageButton(int id, int x, int y, boolean isForward)
        {
            super(id, x, y, 18, 10, "");
            this.isForward = isForward;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (this.visible)
            {
                boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiPythonBook.texture);
                int x = 2;
                int y = 215;

                if (flag)  x += 25;

                if (!this.isForward) y += 13;

                drawModalRectWithCustomSizedTexture(this.x, this.y, x, y, 18, 10, TEX_WIDTH, TEX_HEIGHT);
            }
        }
    }
}