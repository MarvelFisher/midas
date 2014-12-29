package com.cyanspring.id.Library.Frame;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

@SuppressWarnings({ "serial", "rawtypes" })
public class InfoLabel extends JLabel implements ListCellRenderer {
	
	public InfoLabel() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if ((value instanceof InfoString) == false)
			return null;
			
		InfoString info = (InfoString)value;
		setText(info.getContext());

		Color background = Color.WHITE;		
		Color foreground = Color.BLACK;
		
		JList.DropLocation dropLocation = list.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsert()
				&& dropLocation.getIndex() == index) {

			 background = FrameUtil.getColorRGB(ColorName.CornflowerBlue);
			 foreground = Color.WHITE;

			// check if this cell is selected
		} else if (isSelected) {
			 background = FrameUtil.getColorRGB(ColorName.CornflowerBlue);
			 foreground = Color.WHITE;

			// unselected, and not the DnD drop location
		} else {

			
			switch (info.getLevel()) {
			case InfoString.Error:
				foreground = FrameUtil.getColorRGB(ColorName.DeepPink);
				break;
			case InfoString.ALert:
				foreground = FrameUtil.getColorRGB(ColorName.DarkOrchid);
				break;
			case InfoString.Warn:
				foreground = FrameUtil.getColorRGB(ColorName.Brown3); 	
				break;
			case InfoString.Info:
			default:
				break;
			}
		}	
		setBackground(background);
		setForeground(foreground);	

		return this;
	}

}
