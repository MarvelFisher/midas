package com.cyanspring.cstw.ui.shortcutkey;

import org.eclipse.swt.widgets.Composite;

public interface IShortCutKeyListener {

	Composite getComposite();

	String getObserverName();

	void runByKeyFunction(String functionName);

}
