package com.github.vfyjxf.neiutilities.nei;

import static com.github.vfyjxf.neiutilities.config.NeiUtilitiesConfig.useRows;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.github.vfyjxf.neiutilities.config.NeiUtilitiesConfig;
import com.github.vfyjxf.neiutilities.config.SplittingMode;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.ItemPanel;

public class AdvancedItemPanel extends ItemPanel {

    public static final AdvancedItemPanel INSTANCE = new AdvancedItemPanel();

    public boolean isMouseOverHistory = false;

    public AdvancedItemPanel() {
        this.grid = new AdvancedItemPanelGrid();
    }

    public void addHistoryItem(Object... results) {
        if (results.length > 0 && results[0] instanceof ItemStack) {
            this.getAdvancedGrid()
                .addHistoryItem((ItemStack) results[0]);
        }
    }

    public AdvancedItemPanelGrid getAdvancedGrid() {
        return (AdvancedItemPanelGrid) this.grid;
    }

    protected static class AdvancedItemPanelGrid extends ItemPanelGrid {

        private int startIndex;
        private final List<ItemStack> historyItems = new ArrayList<>();

        @Override
        public void setItems(ArrayList<ItemStack> items) {
            startIndex = items.size();
            items.addAll(historyItems);
            super.setItems(items);
        }

        public void addHistoryItem(ItemStack itemStack) {
            if (itemStack != null) {
                ItemStack is = itemStack.copy();
                is.stackSize = 1;
                historyItems.removeIf(stack -> stack.isItemEqual(is));
                historyItems.add(0, is);

                if (historyItems.size() > (useRows * columns)) {
                    historyItems.remove(useRows * columns);
                }

                setItems(new ArrayList<>(this.realItems.subList(0, startIndex)));
            }
        }

        @Override
        protected List<Integer> getMask() {

            if (this.gridMask == null) {
                this.gridMask = new ArrayList<>();
                int idx = page * perPage;
                int limit = (rows - useRows) * columns;
                int index = 0;

                while (index < rows * columns) {

                    if (isInvalidSlot(index)) {
                        this.gridMask.add(null);
                    } else if (idx < startIndex && index < limit) {
                        this.gridMask.add(idx++);
                    } else if (index >= limit && (startIndex + (index - limit)) < size()) {
                        this.gridMask.add(startIndex + (index - limit));
                    } else {
                        this.gridMask.add(null);
                    }

                    index++;
                }
            }

            return this.gridMask;
        }

        @Override
        public void draw(int mouseX, int mouseY) {
            super.draw(mouseX, mouseY);

            // draw history highlighted area
            Rectangle4i firstRect = getSlotRect(rows - useRows, 0);
            if (NeiUtilitiesConfig.getSplittingMode() == SplittingMode.BACKGROUND) {
                GuiDraw.drawRect(
                    firstRect.x,
                    firstRect.y,
                    this.columns * firstRect.w,
                    useRows * firstRect.h,
                    NeiUtilitiesConfig.historyColor);
            } else {
                drawSplittingArea(
                    firstRect.x,
                    firstRect.y,
                    this.columns * firstRect.w,
                    useRows * firstRect.h,
                    NeiUtilitiesConfig.historyColor);
            }
        }

        private void drawSplittingArea(int x, int y, int width, int height, int color) {

            float alpha = (float) (color >> 24 & 255) / 255.0F;
            float red = (float) (color >> 16 & 255) / 255.0F;
            float green = (float) (color >> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;

            GL11.glPushMatrix();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glColor4f(red, green, blue, alpha);
            GL11.glLineWidth(2F);
            GL11.glLineStipple(2, (short) 0x00FF);

            GL11.glBegin(GL11.GL_LINE_LOOP);

            GL11.glVertex2i(x, y);
            GL11.glVertex2i(x + width, y);
            GL11.glVertex2i(x + width, y + height);
            GL11.glVertex2i(x, y + height);

            GL11.glEnd();

            GL11.glLineStipple(1, (short) 0xFFFF);
            GL11.glLineWidth(1F);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            GL11.glPopMatrix();

        }

    }

}
