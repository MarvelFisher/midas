package com.cyanspring.cstw.ui.trader.composite.speeddepth;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.cachingmanager.quote.IQuoteChangeListener;
import com.cyanspring.cstw.cachingmanager.quote.QuoteCachingManager;
import com.cyanspring.cstw.common.Constants;
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

	private IQuoteChangeListener quoteChangeListener;

	private SpeedTimer timer;

	private SpeedDepthTableComposite tableComposite;

	private Text symbolText;
	private Combo rowLengthCombo;
	private Text defaultQuantityText;
	private String symbol;
	private String defaultQuantity;
	private Composite composite_1;
	private Label lblErrorMessage;

	private volatile boolean isTableRefreshIng = false;

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
		timer = new SpeedTimer();
	}

	private void initComponent() {
		this.setLayout(new GridLayout(5, false));
		symbolText = new Text(this, SWT.BORDER);
		GridData gd_symbolText = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_symbolText.widthHint = 132;
		symbolText.setLayoutData(gd_symbolText);

		Label rowLabel = new Label(this, SWT.NONE);
		rowLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false,
				1, 1));
		rowLabel.setText("Display Rows:");

		rowLengthCombo = new Combo(this, SWT.NONE);
		rowLengthCombo.add("20");
		rowLengthCombo.add("30");
		rowLengthCombo.add("40");
		rowLengthCombo.add("60");
		rowLengthCombo.add("80");
		rowLengthCombo.add("100");
		rowLengthCombo.select(0);

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
		defaultQuantityText.setText("1");
		tableComposite = new SpeedDepthTableComposite(this, receiverId,
				SWT.NONE);
		GridData gd_tableComposite = new GridData(SWT.FILL, SWT.FILL, true,
				true, 5, 1);
		gd_tableComposite.heightHint = 232;
		tableComposite.setLayoutData(gd_tableComposite);

		composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.BOTTOM, true,
				false, 5, 0);
		gd_composite_1.heightHint = 30;
		composite_1.setLayoutData(gd_composite_1);

		lblErrorMessage = new Label(composite_1, SWT.NONE);
		lblErrorMessage.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_RED));
		lblErrorMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblErrorMessage.setText("");
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
					// check if the same symbol
					if (symbol != null && symbolText.getText().length() > 0
							&& symbol.equals(symbolText.getText())) {
						return;
					}
					symbol = symbolText.getText();

					String qty = PreferenceStoreManager.getInstance()
							.getDefaultQty(symbol);

					if (qty == null || qty.length() == 0) {
						qty = "1";
					}

					defaultQuantityText.setText(qty);
					rowLengthCombo.select(0);
					tableComposite.clear();

					QuoteSubEvent quoteSubEvent = new QuoteSubEvent(receiverId,
							Business.getInstance().getFirstServer(), symbol);
					try {
						Business.getInstance().getEventManager()
								.sendRemoteEvent(quoteSubEvent);
					} catch (Exception en) {
						log.error(en.getMessage(), en);
					}
					registerQuoteChangeListener(symbol);
				}
			}
		});

		defaultQuantityText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				defaultQuantity = defaultQuantityText.getText();
			}
		});

		rowLengthCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String lengthValue = rowLengthCombo.getItem(rowLengthCombo
						.getSelectionIndex());
				tableComposite.refresh(Integer.valueOf(lengthValue));
				rowLengthCombo.setFocus();

			}
		});

	}

	private void registerQuoteChangeListener(final String quoteSymbol) {
		if (quoteChangeListener != null) {
			QuoteCachingManager.getInstance().removeIQuoteChangeListener(
					quoteChangeListener);
		}

		quoteChangeListener = new IQuoteChangeListener() {
			@Override
			public Set<String> getQuoteSymbolSet() {
				Set<String> symbolSet = new HashSet<String>();
				symbolSet.add(quoteSymbol);
				return symbolSet;
			}

			@Override
			public void refreshByQuote(final Quote quote) {
				if (tableComposite.isDisposed()) {
					return;
				}
				tableComposite.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isTableRefreshIng) {
							return;
						} else {
							if (symbol != null
									&& symbol.equals(quote.getSymbol())) {
								isTableRefreshIng = true;
								tableComposite.setQuote(quote);
								isTableRefreshIng = false;
							}
						}
					}
				});
			}
		};
		QuoteCachingManager.getInstance().addIQuoteChangeListener(
				quoteChangeListener);
	}

	private void refreshErrorMessage(String message) {
		lblErrorMessage.setText(message);
		timer.cleanMessage();
		this.layout();
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
		if (event instanceof EnterParentOrderReplyEvent) {
			final EnterParentOrderReplyEvent replyEvent = (EnterParentOrderReplyEvent) event;
			if (!replyEvent.isOk() && replyEvent.getTxId().equals(receiverId)) {
				tableComposite.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshErrorMessage(replyEvent.getMessage());
					}
				});
			}
		} else if (event instanceof ParentOrderUpdateEvent) {
			ParentOrderUpdateEvent updateEvent = (ParentOrderUpdateEvent) event;
			String orderSymbol = updateEvent.getOrder().getSymbol();
			if (symbol == null || !symbol.equals(orderSymbol)) {
				return;
			}
			if (tableComposite.isDisposed()) {
				return;
			}
			tableComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tableComposite.refresh(0);
				}
			});

		}
	}

	public Text getDefaultQuantityText() {
		return defaultQuantityText;
	}

	@Override
	public void dispose() {
		if (defaultQuantity != null && symbol != null) {
			PreferenceStoreManager.getInstance().setDefayltQty(symbol,
					defaultQuantity);
		}

		if (quoteChangeListener != null) {
			QuoteCachingManager.getInstance().removeIQuoteChangeListener(
					quoteChangeListener);
		}

		Business.getInstance()
				.getEventManager()
				.unsubscribe(EnterParentOrderReplyEvent.class,
						SpeedDepthMainComposite.this);
		Business.getInstance()
				.getEventManager()
				.unsubscribe(ParentOrderUpdateEvent.class, receiverId,
						SpeedDepthMainComposite.this);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private class SpeedTimer implements IAsyncEventListener {

		private ScheduleManager scheduleManager = new ScheduleManager();
		private AsyncTimerEvent timerEvent = new AsyncTimerEvent();

		private boolean isRuning = false;

		public void cleanMessage() {
			if (isRuning) {
				scheduleManager.cancelTimerEvent(timerEvent);
			}
			isRuning = true;
			scheduleManager.scheduleTimerEvent(5000, this, timerEvent);
		}

		public void onEvent(AsyncEvent event) {
			if (event instanceof AsyncTimerEvent) {
				tableComposite.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						lblErrorMessage.setText("");
						isRuning = false;
					}
				});

			}
		}
	}
}
