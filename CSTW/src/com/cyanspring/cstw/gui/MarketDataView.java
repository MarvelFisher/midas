package com.cyanspring.cstw.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.InstrumentSelectionEvent;
import com.cyanspring.cstw.event.MarketDataReplyEvent;
import com.cyanspring.cstw.event.MarketDataRequestEvent;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.ObjectSelectionEvent;
import com.cyanspring.cstw.event.QuoteSymbolSelectEvent;
import com.cyanspring.cstw.event.SingleInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;

public class MarketDataView extends ViewPart implements IAsyncEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(MarketDataView.class);
	public static final String ID = "com.cyanspring.cstw.gui.MarketDataView"; //$NON-NLS-1$
	private static final DecimalFormat priceFormat = new DecimalFormat(
			"#0.####");
	private ImageRegistry imageRegistry;
	private Composite topComposite;
	private Table table;
	private TableViewer tableViewer;
	private String symbol = "";
	private CCombo cbSymbol;
	private Label lbBid;
	private Label lbBidVol;
	private Label lbAsk;
	private Label lbAskVol;
	private Label lbLast;
	private Label lbLastVol;
	private Label lbOpen;
	private Label lbClose;
	private Label lbHigh;
	private Label lbLow;
	private Map<String, String> symbolServer = new HashMap<String, String>();
	private Label lblMktVol;
	private Label lbMktVol;
	private Label lblNewLabel_1;
	private Label lbChange;
	private Label lbChangePercent;
	private Label lbStale;
	private Quote nowQuote = null;
	private ExpandBar expandBar = null;
	private Composite askBidComposite = null;
	private Composite detailComposite = null;
	private Label lblBidvolume = null;
	private Composite mainComposite = null;
	public MarketDataView() {
	}

	class DepthItem {
		Double bid, ask, bidVol, askVol;
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof Quote) {
				Quote quote = (Quote) parent;
				List<DepthItem> list = new ArrayList<DepthItem>();
				for (int i = 0; i < Math.max(quote.getBids().size(), quote
						.getAsks().size()); i++) {
					DepthItem item = new DepthItem();
					if (i < quote.getBids().size()) {
						QtyPrice qp = quote.getBids().get(i);
						item.bid = qp.price;
						item.bidVol = qp.quantity;
					}

					if (i < quote.getAsks().size()) {
						QtyPrice qp = quote.getAsks().get(i);
						item.ask = qp.price;
						item.askVol = qp.quantity;
					}
					list.add(item);
				}
				return list.toArray();
			}
			return null;
		}
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		imageRegistry = Activator.getDefault().getImageRegistry();
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayout(new GridLayout(1, true));
		topComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout gl_topComposite = new GridLayout(3, true);
		topComposite.setLayout(gl_topComposite);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		cbSymbol = new CCombo(topComposite, SWT.BORDER);
		List<String> symbolList = Business.getInstance().getSymbolList();
		if(null != symbolList && !symbolList.isEmpty()){	
			cbSymbol.setItems(symbolList.toArray(new String[symbolList.size()]));
			cbSymbol.setText(cbSymbol.getItem(0));
		}else{
			cbSymbol.setText("");
		}
		
		cbSymbol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		cbSymbol.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String symbol = cbSymbol.getText();
				if (!symbol.equals(MarketDataView.this.symbol)) {
					String server = symbolServer.get(symbol);
					if (null != server)
						subscribeMD(symbol, server);
				}
			}
		});

		cbSymbol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					String symbol = cbSymbol.getText();
					String server = symbolServer.get(symbol);
					if (null == server) {
						ArrayList<String> servers = Business.getInstance()
								.getOrderManager().getServers();
						if (servers.size() == 0) {
							log.warn("No server is available for market data request");
							return;
						} else {
							server = servers.get(0);
							symbolServer.put(symbol, server);
						}
					}
					subscribeMD(symbol, server);
					addSymbol(symbol);
				}

			}

		});

		new Label(topComposite, SWT.NONE);
		
		expandBar = new ExpandBar(mainComposite, SWT.NONE);
		GridData gd_expandBar = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		expandBar.setLayoutData(gd_expandBar);		
		expandBar.addExpandListener(new ExpandListener() {
			private Display display = Display.getCurrent();			
			private void layout(){
				display.asyncExec(new Runnable(){

					@Override
					public void run() {
						
						Rectangle rec= expandBar.getBounds();
						int height = rec.y+rec.height;
						Rectangle rec2=tableViewer.getTable().getBounds();
						rec2.y = height+10;				
						tableViewer.getTable().setBounds(rec2);
						tableViewer.refresh();
						mainComposite.layout();
					}
					
				});
			}
			
			@Override
			public void itemExpanded(ExpandEvent e) {
				layout();
			}
			
			@Override
			public void itemCollapsed(ExpandEvent e) {
				layout();
			}
		});
		expandBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		{
			ExpandItem xpndtmAskbid = new ExpandItem(expandBar, SWT.NONE);
			xpndtmAskbid.setImage(imageRegistry.getDescriptor(ImageID.ASKBID_ICON.toString()).createImage());
			xpndtmAskbid.setExpanded(false);
			xpndtmAskbid.setText("Ask / Bid");
			{
				askBidComposite = new Composite(expandBar,  SWT.BORDER);
				GridLayout gl_composite_1 = new GridLayout(6, true);
				gl_composite_1.makeColumnsEqualWidth = true;
				
				askBidComposite.setLayout(gl_composite_1);
				askBidComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
						false, 1, 1));
				
				xpndtmAskbid.setControl(askBidComposite);
				lblBidvolume = new Label(askBidComposite, SWT.NONE);
				lblBidvolume.setText("Bid/Vol");
				new Label(askBidComposite, SWT.NONE);
				lbBid = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
				lbBidVol = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
			
				Label lblNewLabel_5 = new Label(askBidComposite, SWT.NONE);
				lblNewLabel_5.setText("Ask/Vol");
				new Label(askBidComposite, SWT.NONE);
				lbAsk = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
				lbAskVol = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
				
				Label lblLastvolume = new Label(askBidComposite, SWT.NONE);
				lblLastvolume.setText("Last/Vol");
				new Label(askBidComposite, SWT.NONE);
				lbLast = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
				lbLastVol = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
							
				lblMktVol = new Label(askBidComposite, SWT.NONE);
				lblMktVol.setText("Mkt Vol");
				new Label(askBidComposite, SWT.NONE);
				lbMktVol = new Label(askBidComposite, SWT.NONE);
				new Label(askBidComposite, SWT.NONE);
				
			}
			xpndtmAskbid.setHeight(xpndtmAskbid.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
		{
			ExpandItem xpndtmDetail = new ExpandItem(expandBar, SWT.NONE);
			xpndtmDetail.setExpanded(true);
			xpndtmDetail.setText("Detail");
			xpndtmDetail.setImage(imageRegistry.getDescriptor(ImageID.DETAILS_ICON.toString()).createImage());

			{
				detailComposite = new Composite(expandBar, SWT.BORDER);
				xpndtmDetail.setControl(detailComposite);
				detailComposite.setLayout(new GridLayout(6, true));
				
				Label lblOpenclose = new Label(detailComposite, SWT.NONE);		
				lblOpenclose.setText("Open/Close");
				new Label(detailComposite, SWT.NONE);
				lbOpen = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);			
				lbClose = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);
				
				Label lblHighlow = new Label(detailComposite, SWT.NONE);
				lblHighlow.setText("High/Low");
				new Label(detailComposite, SWT.NONE);
				lbHigh = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);
				lbLow = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);
				
				lblNewLabel_1 = new Label(detailComposite, SWT.NONE);
				lblNewLabel_1.setText("Change/%");
				new Label(detailComposite, SWT.NONE);
				lbChange = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);
				lbChangePercent = new Label(detailComposite, SWT.NONE);
				new Label(detailComposite, SWT.NONE);		
				Label lbLabelStale = new Label(detailComposite, SWT.NONE);
				
				lbLabelStale.setText("Stale");
				new Label(detailComposite, SWT.NONE);
				lbStale = new Label(detailComposite, SWT.NONE);	
			}
			xpndtmDetail.setHeight(xpndtmDetail.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
		
		tableViewer = new TableViewer(mainComposite, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		createActions();
		initializeToolBar();
		initializeMenu();
		createColumns();

		tableViewer.setContentProvider(new ViewContentProvider());
		Business.getInstance().getEventManager()
				.subscribe(SingleOrderStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(SingleInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(QuoteEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(InstrumentSelectionEvent.class, this);
		Business.getInstance().getEventManager()
				.subscribe(QuoteSymbolSelectEvent.class, this);

	}

	private void createColumns() {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("Volume");
		column.setWidth(table.getBorderWidth() / 4);
		column.setResizable(true);
		column.setMoveable(false);

		TableViewerColumn tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem)
					try {
						Object value = ((DepthItem) obj).bidVol;
						if (value != null)
							return BeanHolder.getInstance().getDataConverter()
									.toString("Qty", value);
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
					}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Bid");
		column.setWidth(table.getBorderWidth() / 4);
		column.setResizable(true);
		column.setMoveable(false);

		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem) {
					Object value = ((DepthItem) obj).bid;
					if (value != null)
						return value.toString();
				}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Ask");
		column.setWidth(table.getBorderWidth() / 4);
		column.setResizable(true);
		column.setMoveable(false);

		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem) {
					Object value = ((DepthItem) obj).ask;
					if (value != null)
						return value.toString();
				}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Volume");
		column.setWidth(table.getBorderWidth() / 4);
		column.setResizable(true);
		column.setMoveable(false);

		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem)
					try {
						Object value = ((DepthItem) obj).askVol;
						if (value != null)
							return BeanHolder.getInstance().getDataConverter()
									.toString("Qty", value);
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
					}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		table.setHeaderVisible(true);
		table.setRedraw(true);
		tableViewer.refresh();

		// set to same size
		table.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				for (int i = 0; i < table.getColumnCount(); i++) {
					table.getColumn(i).setWidth(
							table.getClientArea().width
									/ table.getColumnCount());
				}
				table.removeListener(SWT.Paint, this);
			}
		});

	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	@SuppressWarnings("unused")
	private void initializeToolBar() {
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	@SuppressWarnings("unused")
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	private synchronized void subscribeMD(String symbol, String server) {

		if(!this.symbol.equals(symbol))
			Business.getInstance().getEventManager().unsubscribe(QuoteEvent.class, this.symbol, this);
			
		Business.getInstance().getEventManager().subscribe(QuoteEvent.class, this.symbol, this);
		Business.getInstance().getEventManager().subscribe(MarketDataRequestEvent.class, this);

		if (!this.symbol.equals(symbol))
			Business.getInstance().getEventManager()
					.unsubscribe(QuoteEvent.class, this.symbol, this);

		Business.getInstance().getEventManager()
				.subscribe(QuoteEvent.class, this.symbol, this);

		this.symbol = symbol;

		if (!StringUtils.hasText(server))
			server = Business.getInstance().getFirstServer();

		QuoteSubEvent request = new QuoteSubEvent(ID, server, symbol);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	private String blankZero(double value) {
		return PriceUtils.isZero(value) ? "" : "" + value;
	}

	private void showQuote(final Quote quote) {
		if (!quote.getSymbol().equals(this.symbol)) {
			return;
		
		} else if (tableViewer.getControl().isDisposed()) {
			return;
		}
		nowQuote = quote;
		tableViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				lbBid.setText(blankZero(quote.getBid()));
				try {
					lbBidVol.setText(BeanHolder.getInstance()
							.getDataConverter()
							.toString("Qty", quote.getBidVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}

				lbAsk.setText(blankZero(quote.getAsk()));
				try {
					lbAskVol.setText(BeanHolder.getInstance()
							.getDataConverter()
							.toString("Qty", quote.getAskVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}
				lbLast.setText(blankZero(quote.getLast()));
				try {
					lbLastVol.setText(BeanHolder.getInstance()
							.getDataConverter()
							.toString("Qty", quote.getLastVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}

				try {
					lbMktVol.setText(BeanHolder.getInstance()
							.getDataConverter()
							.toString("Qty", quote.getTotalVolume()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}

				lbHigh.setText(blankZero(quote.getHigh()));
				lbLow.setText(blankZero(quote.getLow()));
				lbOpen.setText(blankZero(quote.getOpen()));
				lbClose.setText(blankZero(quote.getClose()));

				if (!PriceUtils.isZero(quote.getClose())
						&& !PriceUtils.isZero(quote.getLast())) {
					String change = priceFormat.format(quote.getLast()
							- quote.getClose());
					String changePercent = priceFormat.format((quote.getLast() - quote
							.getClose()) * 100 / quote.getClose())
							+ "%";
					lbChange.setText(change);
					lbChangePercent.setText(changePercent);
				} else {
					lbChange.setText("");
					lbChangePercent.setText("");
				}
				lbStale.setText("" + quote.isStale());

				topComposite.layout();

				tableViewer.setInput(quote);
				tableViewer.refresh();
				askBidComposite.layout(true, true);
				detailComposite.layout(true, true);
				expandBar.layout(true, true);
			}
		});
		
	}

	
	
	private void addSymbol(String symbol) {
		boolean found = false;
		for (String str : cbSymbol.getItems()) {
			if (str.equals(symbol)) {
				found = true;
				break;
			}
		}
		if (!found)
			cbSymbol.add(symbol, 0);
	}

	private void setSymbol(final String symbol) {
		tableViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				cbSymbol.setText(symbol);
				addSymbol(symbol);
			}
		});
	}

	@Override
	public void onEvent(AsyncEvent e) {
		if (e instanceof ObjectSelectionEvent) {
			ObjectSelectionEvent event = (ObjectSelectionEvent) e;
			if (e instanceof MultiInstrumentStrategySelectionEvent)
				return;

			Map<String, Object> map = event.getData();
			String server = (String) map.get(OrderField.SERVER_ID.value());
			String symbol = (String) map.get(OrderField.SYMBOL.value());
			symbolServer.put(symbol, server);
			subscribeMD(symbol, server);
			setSymbol(symbol);

		} else if (e instanceof QuoteEvent) {
			showQuote(((QuoteEvent) e).getQuote());
		} else if (e instanceof QuoteSymbolSelectEvent) {			
			QuoteSymbolSelectEvent event = (QuoteSymbolSelectEvent)e;
			String symbol = event.getSymbol();
			subscribeMD(symbol, null);
			setSymbol(symbol);
			
		}else if (e instanceof MarketDataRequestEvent) {
			log.info("get market data request event");
			MarketDataRequestEvent event = (MarketDataRequestEvent)e;
			String symbol = event.getSymbol();
			if(null == symbol){
				MarketDataReplyEvent reply = new MarketDataReplyEvent(nowQuote);
				Business.getInstance().getEventManager().sendEvent(reply);
			}else{
				subscribeMD(symbol, null);
				setSymbol(symbol);
			}
		} else {
			log.error("Unhandled event: " + e.getClass());
		}
	}

}
