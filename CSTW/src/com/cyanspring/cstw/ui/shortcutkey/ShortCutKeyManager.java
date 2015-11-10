package com.cyanspring.cstw.ui.shortcutkey;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.cyanspring.cstw.gui.assist.OpenSingleOrderStrategyViewAssist;

/**
 * 
 * @author NingXiaofeng
 * 
 * @date 2015.11.06
 *
 */
public final class ShortCutKeyManager {

	private static ShortCutKeyManager instance;

	private ShortCutKeyManager() {

	}

	public static ShortCutKeyManager getInstance() {
		if (instance == null) {
			instance = new ShortCutKeyManager();
		}
		return instance;
	}

	public void init() {
		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.keyCode == SWT.F1 || e.keyCode == SWT.F2
						|| e.keyCode == SWT.ESC || e.keyCode == SWT.F3 || e.keyCode == SWT.F4) {
					new OpenSingleOrderStrategyViewAssist().run(e.keyCode);
				}
			}
		});
	}

	public void addShortCutKeyListener(final IShortCutKeyListener listener) {
		KeyListener keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyEvent(e, listener);
			}
		};
		addKeyListenerToComposite(listener.getComposite(), keyListener);
	}

	private void addKeyListenerToComposite(Composite composite,
			KeyListener keyListener) {
		composite.addKeyListener(keyListener);
		for (Control control : composite.getChildren()) {
			if (control instanceof Composite) {
				Composite subComposite = (Composite) control;
				addKeyListenerToComposite(subComposite, keyListener);
			} else {
				control.addKeyListener(keyListener);
			}
		}
	}

	private void handleKeyEvent(KeyEvent event, IShortCutKeyListener listener) {
		if (event.keyCode == SWT.F1 && event.stateMask == SWT.CTRL) {
			listener.runByKeyFunction(ShortCutKeyConstants.TABLE_EXPORT);
		}
	}

}
