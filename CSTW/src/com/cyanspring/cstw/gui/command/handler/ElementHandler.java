package com.cyanspring.cstw.gui.command.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import com.cyanspring.cstw.business.Business;

public class ElementHandler extends AbstractHandler implements IElementUpdater{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters) {
		element.setText("User : "+Business.getInstance().getUser()+" - "+Business.getInstance().getUserGroup().getRole());	
	}

}
