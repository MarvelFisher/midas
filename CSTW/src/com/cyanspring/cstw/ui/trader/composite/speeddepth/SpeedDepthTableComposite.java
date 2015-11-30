package com.cyanspring.cstw.ui.trader.composite.speeddepth;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.wb.swt.SWTResourceManager;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.cstw.common.CustomOrderType;
import com.cyanspring.cstw.model.trader.ParentOrderModel;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.trader.IParentOrderService;
import com.cyanspring.cstw.ui.trader.composite.speeddepth.model.SpeedDepthModel;
import com.cyanspring.cstw.ui.trader.composite.speeddepth.provider.SpeedDepthContentProvider;
import com.cyanspring.cstw.ui.trader.composite.speeddepth.provider.SpeedDepthLabelProvider;
import com.cyanspring.cstw.ui.trader.composite.speeddepth.service.SpeedDepthService;
import com.cyanspring.cstw.ui.utils.TableUtils;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthTableComposite extends Composite {

	private IParentOrderService parentOrderService;

	private SpeedDepthService speedDepthService;

	private Quote currentQuote;

	private Table table;
	private TableViewer tableViewer;

	private SpeedDepthContentProvider speedDepthContentProvider;
	private SpeedDepthLabelProvider labelProvider;

	private TableColumn tblclmnAskVol;
	private TableColumn tblBidsVol;

	private TableItem currentMouseMoveItem;
	private TableItem currentSelectedItem;

	private Composite buttonBarComposite;

	private Button cancelButton;
	private Button lockButton;

	private boolean isLock = false;
	private String receiverId;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpeedDepthTableComposite(Composite composite, String receiverId,
			int style) {
		super(composite, style);
		parentOrderService = ServiceFactory.createParentOrderService();
		speedDepthService = new SpeedDepthService();
		this.receiverId = receiverId;
		initComponent();
		initProvider();
		initListener();
	}

	private void initComponent() {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		toolBar.setVisible(false);

		buttonBarComposite = new Composite(this, SWT.NONE);
		buttonBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		buttonBarComposite.setLayout(new GridLayout(2, false));

		lockButton = new Button(buttonBarComposite, SWT.CHECK);
		lockButton.setText("LOCK");

		cancelButton = new Button(buttonBarComposite, SWT.NONE);
		cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				true, 1, 1));
		cancelButton.setText("Cancel ALL");

		tableViewer = new TableViewer(this, SWT.MULTI | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 85, 85);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn.setWidth(100);

		tblclmnAskVol = new TableColumn(table, SWT.CENTER);
		tblclmnAskVol.setWidth(100);
		tblclmnAskVol.setText("Volume");

		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Bid/Ask");

		tblBidsVol = new TableColumn(table, SWT.CENTER);
		tblBidsVol.setWidth(100);
		tblBidsVol.setText("Volume");

		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_4.setWidth(100);

		TableColumn tblclmnNewColumn_5 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_5.setWidth(1000);
	}

	private void initProvider() {
		speedDepthContentProvider = new SpeedDepthContentProvider();
		tableViewer.setContentProvider(speedDepthContentProvider);
		labelProvider = new SpeedDepthLabelProvider();
		tableViewer.setLabelProvider(labelProvider);
	}

	private void initListener() {
		lockButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isLock = lockButton.getSelection();
				if (currentQuote != null) {
					labelProvider.setKeyselectIndex(-1);
					labelProvider.setMouseselectIndex(-1);
					labelProvider.clearSelectedIndex();
					currentSelectedItem = null;
					currentMouseMoveItem = null;
					tableViewer.setInput(speedDepthService.getSpeedDepthList(
							currentQuote, isLock));
				}
			}
		});

		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentQuote == null) {
					return;
				}
				parentOrderService.cancelAllOrder(currentQuote.getSymbol());
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
				if (cell != null
						&& cell.getElement() instanceof SpeedDepthModel) {
					SpeedDepthModel model = (SpeedDepthModel) cell.getElement();
					int columnIndex = cell.getColumnIndex();
					SpeedDepthMainComposite mainComposite = (SpeedDepthMainComposite) SpeedDepthTableComposite.this
							.getParent();

					ParentOrderModel parentOrderModel = new ParentOrderModel();
					parentOrderModel.setSymbol(model.getSymbol());
					parentOrderModel.setReceiverId(receiverId);
					parentOrderModel.setQuantity(mainComposite
							.getDefaultQuantityText().getText());
					parentOrderModel.setPrice(model.getPrice());

					CustomOrderType orderType;
					if (e.button == 1) {
						orderType = CustomOrderType.Limit;
					} else {
						orderType = CustomOrderType.Stop;
					}

					// ask
					if (columnIndex == 1) {
						parentOrderModel.setSide("Buy");
						parentOrderService.quickEnterOrder(parentOrderModel,
								orderType);
					}
					// bids
					else if (columnIndex == 3) {
						parentOrderModel.setSide("Sell");
						parentOrderService.quickEnterOrder(parentOrderModel,
								orderType);
					} else if (columnIndex == 0 || columnIndex == 4) {
						parentOrderService.cancelOrder(model.getSymbol(),
								Double.valueOf(model.getPrice()), orderType);
					}
				}
			}
		});

		table.addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (currentMouseMoveItem != null
						&& currentMouseMoveItem != currentSelectedItem) {
					changeItemColor(currentMouseMoveItem, SWT.COLOR_WHITE);
					labelProvider.setMouseselectIndex(-1);
					currentMouseMoveItem = null;
					return;
				}
			}
		});

		table.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null) {
					changeItemColor(item, SWT.COLOR_BLACK);
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					if (currentMouseMoveItem != null
							&& !currentMouseMoveItem.isDisposed()
							&& currentMouseMoveItem != item) {
						if (isLock
								&& currentMouseMoveItem != currentSelectedItem) {
							changeItemColor(currentMouseMoveItem,
									SWT.COLOR_WHITE);
						} else {
							changeItemColor(currentMouseMoveItem,
									SWT.COLOR_WHITE);
						}
					}
					SpeedDepthModel model = (SpeedDepthModel) item.getData();
					labelProvider.setMouseselectIndex(model.getIndex());
				} else {
					labelProvider.setMouseselectIndex(-1);
				}
				currentMouseMoveItem = item;
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem item : table.getItems()) {
					if (!TableUtils.isItemSelected(item, table)) {
						changeItemColor(item, SWT.COLOR_WHITE);
					}
				}
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
					TableItem item = table.getItem(table.getSelectionIndex());
					checkSelectedItem(item);
				}
			}
		});
	}

	private void checkSelectedItem(TableItem item) {
		if (item == null) {
			return;
		}
		if (item != currentMouseMoveItem) {
			changeItemColor(item, SWT.COLOR_BLACK);
		}
		if (currentSelectedItem != null && !currentSelectedItem.isDisposed()
				&& currentSelectedItem != item
				&& currentSelectedItem != currentMouseMoveItem) {
			changeItemColor(currentSelectedItem, SWT.COLOR_WHITE);
		}
		currentSelectedItem = item;
		SpeedDepthModel model = (SpeedDepthModel) item.getData();
		labelProvider.setKeyselectIndex(model.getIndex());
	}

	private void changeItemColor(TableItem item, int color) {
		if (item.getText(1).length() > 0 || item.getText(3).length() > 0) {
			item.setForeground(1, SWTResourceManager.getColor(color));
			item.setForeground(3, SWTResourceManager.getColor(color));
		}
	}

	public void clear() {
		currentQuote = null;
		lockButton.setSelection(false);
		labelProvider.setMouseselectIndex(-1);
		labelProvider.setKeyselectIndex(-1);
		isLock = false;
		speedDepthService.setRowLength(20);
		tableViewer.setInput(null);
	}

	public void setQuote(Quote quote) {
		if (tableViewer.getTable().isDisposed()) {
			return;
		}
		currentQuote = quote;
		List<SpeedDepthModel> list = speedDepthService.getSpeedDepthList(
				currentQuote, isLock);
		labelProvider.setKeyselectIndex(-1);
		tableViewer.setInput(list);

		// 每次刷新后key选择失效
		currentSelectedItem = null;

	}

	/**
	 * 当rowLength=0时，仅做刷新。
	 * 
	 * @param rowLength
	 */
	public void refresh(int rowLength) {
		if (rowLength > 0) {
			speedDepthService.setRowLength(rowLength);
		}
		tableViewer.setInput(speedDepthService.getSpeedDepthList(currentQuote,
				isLock));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
