package com.cyanspring.cstw.trader.gui.composite.speeddepth;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.Constants;
import com.cyanspring.cstw.common.GUIUtils;
import com.cyanspring.cstw.preference.PreferenceStoreManager;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthMainComposite extends Composite implements
		IAsyncEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(SpeedDepthMainComposite.class);

	private String receiverId = IdGenerator.getInstance().getNextID();

	private SpeedDepthTableComposite speedDepthComposite;
	private Text symbolText;
	private Text defaultQuantityText;
	private String symbol = "";
	private String defaultQuantity;

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
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		lblNewLabel.setText("Default Quantity:");

		defaultQuantityText = new Text(this, SWT.BORDER);
		GridData gd_defaultQuantityText = new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1);
		gd_defaultQuantityText.widthHint = 80;
		gd_defaultQuantityText.minimumWidth = 80;
		defaultQuantityText.setLayoutData(gd_defaultQuantityText);
		if (PreferenceStoreManager.getInstance().getDefaultQty() == null
				|| PreferenceStoreManager.getInstance().getDefaultQty()
						.equals("")) {
			defaultQuantityText.setText("1");
		} else {
			defaultQuantityText.setText(PreferenceStoreManager.getInstance()
					.getDefaultQty());
		}
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		speedDepthComposite = new SpeedDepthTableComposite(this, SWT.NONE);
		speedDepthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 4, 1));

		changeFont(SpeedDepthMainComposite.this);
		SpeedDepthMainComposite.this.layout();

	}

	private void initListener() {
		Business.getInstance()
				.getEventManager()
				.subscribe(EnterParentOrderReplyEvent.class,
						SpeedDepthMainComposite.this);
		Business.getInstance()
				.getEventManager()
				.subscribe(ParentOrderUpdateEvent.class,
						SpeedDepthMainComposite.this);

		symbolText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {

					Business.getInstance()
							.getEventManager()
							.unsubscribe(QuoteEvent.class, symbol,
									SpeedDepthMainComposite.this);

					symbol = symbolText.getText();

					log.info("subscrib QuoteEvent symbol:"
							+ SpeedDepthMainComposite.this.symbol);

					Business.getInstance()
							.getEventManager()
							.subscribe(QuoteEvent.class, symbol,
									SpeedDepthMainComposite.this);

					Business.getInstance()
							.getEventManager()
							.subscribe(QuoteEvent.class, receiverId,
									SpeedDepthMainComposite.this);

					QuoteSubEvent quoteSubEvent = new QuoteSubEvent(receiverId,
							Business.getInstance().getFirstServer(), symbol);
					try {
						Business.getInstance().getEventManager()
								.sendRemoteEvent(quoteSubEvent);
					} catch (Exception en) {
						log.error(en.getMessage(), en);
					}
				}
			}
		});

		defaultQuantityText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				defaultQuantity = defaultQuantityText.getText();
			}
		});
	}

	private void changeFont(Control control) {
		control.setFont(Constants.M_FONT);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			for (Control subControl : composite.getChildren()) {
				changeFont(subControl);
			}
		}

	}

	@Override
	public void onEvent(final AsyncEvent event) {
		if (event instanceof QuoteEvent) {
			speedDepthComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					Quote quote = ((QuoteEvent) event).getQuote();
					if (symbol.equals(quote.getSymbol())) {
						speedDepthComposite.setQuote(quote);
					}
				}
			});
		} else if (event instanceof EnterParentOrderReplyEvent) {
			EnterParentOrderReplyEvent replyEvent = (EnterParentOrderReplyEvent) event;
			if (!replyEvent.isOk()) {
				GUIUtils.showMessageBox(replyEvent.getMessage(),
						SpeedDepthMainComposite.this);
			}
		}

		else if (event instanceof ParentOrderUpdateEvent) {

			ParentOrderUpdateEvent updateEvent = (ParentOrderUpdateEvent) event;

			String orderSymbol = updateEvent.getOrder().getSymbol();

			if (symbol == null || !symbol.equals(orderSymbol)) {
				return;
			}

			speedDepthComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					speedDepthComposite.refresh();
				}
			});

		}
	}

	public Text getDefaultQuantityText() {
		return defaultQuantityText;
	}

	@Override
	public void dispose() {

		PreferenceStoreManager.getInstance().setDefayltQty(defaultQuantity);

		Business.getInstance()
				.getEventManager()
				.unsubscribe(QuoteEvent.class, symbol,
						SpeedDepthMainComposite.this);

		Business.getInstance()
				.getEventManager()
				.unsubscribe(QuoteEvent.class, receiverId,
						SpeedDepthMainComposite.this);

		Business.getInstance()
				.getEventManager()
				.unsubscribe(EnterParentOrderReplyEvent.class,
						SpeedDepthMainComposite.this);

		Business.getInstance()
				.getEventManager()
				.unsubscribe(ParentOrderUpdateEvent.class,
						SpeedDepthMainComposite.this);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
