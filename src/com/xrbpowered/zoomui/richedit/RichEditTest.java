package com.xrbpowered.zoomui.richedit;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.menu.UIMenu;
import com.xrbpowered.zoomui.menu.UIMenuBar;
import com.xrbpowered.zoomui.menu.UIMenuItem;
import com.xrbpowered.zoomui.menu.UIMenuSeparator;
import com.xrbpowered.zoomui.richedit.java.JavaContext;
import com.xrbpowered.zoomui.std.file.UIFileBrowser;
import com.xrbpowered.zoomui.swing.SwingFrame;
import com.xrbpowered.zoomui.swing.SwingPopup;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class RichEditTest {
	
	public static byte[] loadBytes(InputStream s) throws IOException {
		DataInputStream in = new DataInputStream(s);
		byte[] bytes = new byte[in.available()];
		in.readFully(bytes);
		in.close();
		return bytes;
	}
	
	public static String loadString(File file) {
		try {
			return new String(loadBytes(new FileInputStream(file)));
		} catch(IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static void addEditMenuItems(final UIMenu menu, final UIRichEditArea text) {
		new UIMenuItem(menu, "Undo") {
			@Override
			public boolean isEnabled() {
				return text.editor.history.canUndo();
			}
			@Override
			public void onAction() {
				text.editor.history.undo();
				text.repaint();
			}
		};
		new UIMenuItem(menu, "Redo") {
			@Override
			public boolean isEnabled() {
				return text.editor.history.canRedo();
			}
			@Override
			public void onAction() {
				text.editor.history.redo();
				text.repaint();
			}
		};
		new UIMenuSeparator(menu);
		new UIMenuItem(menu, "Cut") {
			@Override
			public boolean isEnabled() {
				return text.editor.hasSelection();
			}
			@Override
			public void onAction() {
				text.editor.cutSelection();
				text.repaint();
			}
		};
		new UIMenuItem(menu, "Copy") {
			@Override
			public boolean isEnabled() {
				return text.editor.hasSelection();
			}
			@Override
			public void onAction() {
				text.editor.copySelection();
				text.repaint();
			}
		};
		new UIMenuItem(menu, "Paste") {
			@Override
			public boolean isEnabled() {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
			}
			@Override
			public void onAction() {
				text.editor.pasteAtCursor();
				text.repaint();
			}
		};
		new UIMenuSeparator(menu);
		new UIMenuItem(menu, "Delete") {
			@Override
			public boolean isEnabled() {
				return text.editor.hasSelection();
			}
			@Override
			public void onAction() {
				text.editor.deleteSelection();
				text.repaint();
			}
		};
		new UIMenuItem(menu, "Select All") {
			@Override
			public void onAction() {
				text.editor.selectAll();
				text.repaint();
			}
		};
	}
	
	public static void main(String[] args) {
		final SwingFrame frame = new SwingFrame(SwingWindowFactory.use(), "RichEditTest", 1600, 900, true, false) {
			@Override
			public boolean onClosing() {
				confirmClosing();
				return false;
			}
		};
		
		final SwingPopup popup = new SwingPopup(SwingWindowFactory.use());
		popup.getContainer().setClientBorder(1, UIMenu.colorBorder);

		UIMenuBar menuBar = new UIMenuBar(frame.getContainer());

		final UIRichEditArea text = new UIRichEditArea(menuBar.content) {
			@Override
			protected UIRichEdit createEditor() {
				return new UIRichEdit(getView(), false) {
					@Override
					public boolean onMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
						if(button==Button.right) {
							float bx = localToBaseX(x);
							float by = localToBaseY(y);
							editor.checkPushHistory();
							popup.show(frame, bx, by);
							return true;
						}
						else
							return super.onMouseUp(x, y, button, mods, initiator);
					}
				};
			}
			@Override
			protected void paintBorder(GraphAssist g) {
				g.hborder(this, GraphAssist.TOP, colorBorder);
			}
		};
		text.editor.setFont(new Font("Verdana", Font.PLAIN, 10), 10f);
		text.editor.setTokeniser(new LineTokeniser(new JavaContext()));

		final UIModalWindow<File> openDlg = SwingWindowFactory.use().createModal("Open file", 840, 480, true, null);
		openDlg.onResult = new ResultHandler<File>() {
			@Override
			public void onResult(File result) {
				text.editor.setText(loadString(result));
			}
			@Override
			public void onCancel() {
			}
		};
		new UIFileBrowser(openDlg.getContainer(), openDlg.wrapInResultHandler());
		
		UIMenu fileMenu = menuBar.addMenu("File");
		new UIMenuItem(fileMenu, "New") {
			@Override
			public void onAction() {
				text.editor.setText("");
				text.repaint();
			}
		};
		new UIMenuItem(fileMenu, "Open...") {
			@Override
			public void onAction() {
				openDlg.show();
			}
		};
		new UIMenuSeparator(fileMenu);
		new UIMenuItem(fileMenu, "Save").disable();
		new UIMenuItem(fileMenu, "Save As...").disable();
		new UIMenuSeparator(fileMenu);
		new UIMenuItem(fileMenu, "Exit") {
			@Override
			public void onAction() {
				frame.requestClosing();
			}
		};
		
		UIMenu editMenu = menuBar.addMenu("Edit");
		addEditMenuItems(editMenu, text);

		addEditMenuItems(new UIMenu(popup.getContainer()), text);
		popup.setClientSizeToContent();
		
		frame.show();
	}

}
