package ru.ivasik.ivasiklib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextArea extends Gui
{
    private List<String> text = new ArrayList<>();
    private boolean focused;
    private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private int posX, posY, width, height, visibleText = 24, offset, cursorX = 0, cursorY = 0;

    public TextArea(int posX, int posY, int width, int height, int visibleText)
    {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.visibleText = visibleText;
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        for (int i = 0; i < visibleText && i < text.size(); i++)
        {
            String rows = text.get(i);
            fontRenderer.drawString(rows, posX, posY + (i * 8), 0);
        }
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (focused)
        {
            if (GuiScreen.isKeyComboCtrlC(keyCode))
            {

            }
            else if (GuiScreen.isKeyComboCtrlV(keyCode))
            {

            }
            else
            {
                switch (keyCode)
                {
                    case Keyboard.KEY_LEFT: break;
                    case Keyboard.KEY_RIGHT: break;
                    case Keyboard.KEY_UP: break;
                    case Keyboard.KEY_DOWN: break;
                    case Keyboard.KEY_END: break;
                    case Keyboard.KEY_HOME: break;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
                        {
                            this.write(Character.toString(typedChar));
                        }
                }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {

    }

    public void mouseScroll(int mouseX, int mouseY, boolean direction)
    {
        int height = 13;

        if(isMouseInside(mouseX, mouseY, posX, posY, posX + width, posY + visibleText * height + visibleText))
        {
            if(direction)
            {
                if(offset > 0)
                {
                    offset--;
                }
            }
            else
            {
                if(visibleText + offset < text.size())
                {
                    offset++;
                }
            }
        }
    }

    public void update()
    {

    }

    private void write(String text)
    {
        String row = this.text.get(cursorX);
    }

    private static boolean isMouseInside(int mouseX, int mouseY, int minX, int minY, int maxX, int maxY)
    {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public List<String> getText()
    {
        return text;
    }
}