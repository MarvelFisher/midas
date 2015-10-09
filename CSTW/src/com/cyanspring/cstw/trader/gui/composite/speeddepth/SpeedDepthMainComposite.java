package com.cyanspring.cstw.trader.gui.composite.speeddepth;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.cstw.business.Business;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthMainComposite extends Composite implements
		IAsyncEventListener {

	private SpeedDepthTableComposite speedDepthComposite;
	private Text symbolText;
	private Text defaultQuantityText;
	private String symbol = "";

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpeedDepthMainComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
		initListener();
	}

	private void initComponent() {
		this.setLayout(new GridLayout(3, false));
		symbolText = new Text(this, SWT.BORDER);
		GridData gd_symbolText = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_symbolText.widthHint = 132;
		symbolText.setLayoutData(gd_symbolText);

		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblNewLabel.setText("Default Quantity:");

		defaultQuantityText = new Text(this, SWT.BORDER);
		defaultQuantityText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		defaultQuantityText.setText("1000");
		speedDepthComposite = new SpeedDepthTableComposite(this, SWT.NONE);
		speedDepthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 3, 1));

	}

	private void initListener() {
		symbolText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					Business.getInstance()
							.getEventManager()
							.unsubscribe(QuoteEvent.class,
									SpeedDepthMainComposite.this.symbol,
									SpeedDepthMainComposite.this);

					SpeedDepthMainComposite.this.symbol = symbolText.getText();

					Business.getInstance()
							.getEventManager()
							.subscribe(QuoteEvent.class,
									SpeedDepthMainComposite.this.symbol,
									SpeedDepthMainComposite.this);
				}
			}
		});
	}

	@Override
	public void onEvent(final AsyncEvent event) {
		if (event instanceof QuoteEvent) {
			speedDepthComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					speedDepthComposite.setQuote(((QuoteEvent) event)
							.getQuote());
				}
			});
		}
	}

	public Text getDefaultQuantityText() {
		return defaultQuantityText;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
