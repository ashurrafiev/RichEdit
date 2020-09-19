package com.xrbpowered.zoomui.menu;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIPopupWindow;
import com.xrbpowered.zoomui.UIWindowFactory;
import com.xrbpowered.zoomui.base.UILayersContainer;

public class UIMenuBar extends UIContainer {

	public static Color colorBackground = Color.WHITE;
	public static float itemMargin = 8;

	protected class Bar extends UIMenu {
		public Bar() {
			super(UIMenuBar.this);
		}
		@Override
		public void layout() {
			float x = 0f;
			for(UIElement c : children) {
				UIMenuItem mi = (UIMenuItem) c;
				float w = mi.getMinWidth();
				mi.setLocation(x, 0);
				mi.setSize(w, getHeight());
				x += w;
			}
		}
		@Override
		protected void paintSelf(GraphAssist g) {
			g.fill(this, UIMenuBar.colorBackground);
		}
	}
	
	protected class BarItem extends UIMenuItem {
		public final UIPopupWindow popup;
		public final UIMenu menu;
		
		public BarItem(final String label) {
			super(bar, label);
			popup = UIWindowFactory.instance.createPopup();
			popup.getContainer().setClientBorder(1, UIMenu.colorBorder);
			menu = new UIMenu(popup.getContainer());
		}
		@Override
		public float getMarginLeft() {
			return itemMargin;
		}
		@Override
		public float getTotalMargins() {
			return itemMargin*2;
		}
		@Override
		public void onAction() {
			// reqiures UIManager.PopupMenu.consumeEventOnClose=true for SwingPopup to work,
			// otherwise the same click even will dismiss the popup and instantly call onAction() to reopen it again 
			if(popup.setClientSizeToContent()) {
				float bx = localToBaseX(0);
				float by = localToBaseY(getHeight());
				popup.show(getBase().getWindow(), bx, by);
			}
		}
	}
	
	public final UIMenu bar;
	public final UIContainer content;
	
	public UIMenuBar(UIContainer parent) {
		super(parent);
		this.content = new UILayersContainer(this);
		this.bar = new Bar();
		bar.setSize(0, UIMenuItem.defaultHeight);
	}
	
	public UIMenu addMenu(String title) {
		return new BarItem(title).menu;
	}
	
	@Override
	public void layout() {
		float y = bar.getHeight();
		bar.setSize(getWidth(), y);
		bar.setLocation(0, 0);
		content.setSize(getWidth(), getHeight()-y);
		content.setLocation(0, y);
		super.layout();
	}

}
