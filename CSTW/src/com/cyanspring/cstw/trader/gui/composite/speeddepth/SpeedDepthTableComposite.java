package com.cyanspring.cstw.trader.gui.composite.speeddepth;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.wb.swt.SWTResourceManager;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.model.SpeedDepthModel;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.provider.SpeedDepthContentProvider;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.provider.SpeedDepthLabelProvider;
import com.cyanspring.cstw.trader.gui.composite.speeddepth.service.SpeedDepthService;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/08
 *
 */
public final class SpeedDepthTableComposite extends Composite {

	private SpeedDepthService speedDepthService;

	private Quote currentQuote;

	private Table table;
	private TableViewer tableViewer;

	private SpeedDepthContentProvider speedDepthContentProvider;
	private SpeedDepthLabelProvider labelProvider;
	private SpeedDepthMainComposite mainComposite;
	private TableColumn tblclmnAskVol;
	private TableColumn tblBidsVol;
	private Composite composite;
	private Button cancelButton;
	private Button lockButton;

	private boolean isLock = false;

	private TableItem currentMouseSelectedItem;

	private TableItem currentKeySelectedItem;

	private String receiverId;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpeedDepthTableComposite(SpeedDepthMainComposite mainComposite,
			String receiverId, int style) {
		super(mainComposite, style);
		speedDepthService = new SpeedDepthService();
		this.mainComposite = mainComposite;
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

		composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		composite.setLayout(new GridLayout(2, false));

		lockButton = new Button(composite, SWT.CHECK);
		lockButton.setText("LOCK");

		cancelButton = new Button(composite, SWT.NONE);
		cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				true, 1, 1));
		cancelButton.setText("Cancel ALL");

		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
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
				speedDepthService.cancelOrder(currentQuote.getSymbol());
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
					// ask
					if (columnIndex == 1) {
						speedDepthService.quickEnterOrder(model, "Buy",
								mainComposite.getDefaultQuantityText()
										.getText(), receiverId);
					}
					// bids
					else if (columnIndex == 3) {
						speedDepthService.quickEnterOrder(model, "Sell",
								mainComposite.getDefaultQuantityText()
										.getText(), receiverId);
					} else if (columnIndex == 0 || columnIndex == 4) {
						speedDepthService.cancelOrder(model.getSymbol(),
								Double.valueOf(model.getPrice()));
					}
				}
			}
		});

		table.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item == null) {
					return;
				}
				if (currentMouseSelectedItem != null
						&& !currentMouseSelectedItem.isDisposed()
						&& currentMouseSelectedItem != item) {
					if (isLock
							|| (!isLock && currentMouseSelectedItem != currentKeySelectedItem)) {
						currentMouseSelectedItem.setForeground(1,
								SWTResourceManager.getColor(SWT.COLOR_WHITE));
						currentMouseSelectedItem.setForeground(3,
								SWTResourceManager.getColor(SWT.COLOR_WHITE));
					}
				}
				item.setForeground(1,
						SWTResourceManager.getColor(SWT.COLOR_BLACK));
				item.setForeground(3,
						SWTResourceManager.getColor(SWT.COLOR_BLACK));
				SpeedDepthModel model = (SpeedDepthModel) item.getData();
				labelProvider.setSelectIndex(model.getIndex());
				currentMouseSelectedItem = item;
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem item = table.getItem(table.getSelectionIndex());
				if (currentKeySelectedItem != null
						&& !currentKeySelectedItem.isDisposed()
						&& currentKeySelectedItem != item
						&& currentKeySelectedItem != currentMouseSelectedItem) {
					currentKeySelectedItem.setForeground(1,
							SWTResourceManager.getColor(SWT.COLOR_WHITE));
					currentKeySelectedItem.setForeground(3,
							SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				item.setForeground(1,
						SWTResourceManager.getColor(SWT.COLOR_BLACK));
				item.setForeground(3,
						SWTResourceManager.getColor(SWT.COLOR_BLACK));
				currentKeySelectedItem = item;

			}
		});
	}

	public void clear() {
		currentQuote = null;
		lockButton.setSelection(false);
		isLock = false;
		tableViewer.setInput(null);
	}

	public void setQuote(Quote quote) {
		currentQuote = quote;
		tableViewer.setInput(speedDepthService.getSpeedDepthList(currentQuote,
				isLock));
	}

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
