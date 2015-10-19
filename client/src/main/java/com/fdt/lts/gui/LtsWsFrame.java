package com.fdt.lts.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.StringUtils;

import com.cyanspring.apievent.obj.Account;
import com.cyanspring.apievent.obj.OpenPosition;
import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
import com.cyanspring.apievent.obj.Quote;
import com.cyanspring.apievent.reply.AccountSnapshotReplyEvent;
import com.cyanspring.apievent.reply.AccountUpdateEvent;
import com.cyanspring.apievent.reply.AmendParentOrderReplyEvent;
import com.cyanspring.apievent.reply.CancelParentOrderReplyEvent;
import com.cyanspring.apievent.reply.EnterParentOrderReplyEvent;
import com.cyanspring.apievent.reply.OpenPositionUpdateEvent;
import com.cyanspring.apievent.reply.ParentOrderUpdateEvent;
import com.cyanspring.apievent.reply.QuoteEvent;
import com.cyanspring.apievent.reply.ServerReadyEvent;
import com.cyanspring.apievent.reply.StrategySnapshotEvent;
import com.cyanspring.apievent.reply.SystemErrorEvent;
import com.cyanspring.apievent.reply.UserLoginReplyEvent;
import com.cyanspring.apievent.request.AccountSnapshotRequestEvent;
import com.cyanspring.apievent.request.AmendParentOrderEvent;
import com.cyanspring.apievent.request.CancelParentOrderEvent;
import com.cyanspring.apievent.request.QuoteSubEvent;
import com.cyanspring.apievent.request.StrategySnapshotRequestEvent;
import com.cyanspring.apievent.request.UserLoginEvent;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.event.ClientSocketEventManager;
import com.cyanspring.transport.socket.ClientSocketService;
import com.fdt.lts.client.OrderUtil;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.awt.CardLayout;
import java.awt.FlowLayout;

/**
 * @author dennischen
 * @version 1.0
 * @since 1.0
 */

public class LtsWsFrame extends JFrame {
    private static Logger log = LoggerFactory.getLogger(LtsWsFrame.class);

	private JPanel contentPane;
	private JTextField edSymbol;
	private JTextField edPrice;
	final JComboBox<String> cbSide;
	final JComboBox<String> cbType;
	private JTextField edQty;
	private JTable tblOrder;
	private static String title = "LTS Trader Workstation";
	private DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
    private JTable tblPosition;
    private JTable tblAccount;
    private JLabel lblBidAsk;
    private JTextField edQuote;

	/**
	 * Create the frame.
	 */
	public LtsWsFrame() {
		this.setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1054, 633);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	setLocation((dim.width-getWidth())/2, (dim.height-getHeight())/2);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.16);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JSplitPane splitPane2 = new JSplitPane();
		splitPane2.setResizeWeight(0.3);
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(splitPane2);
		
		JPanel panel_1 = new JPanel();
		splitPane2.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1);
		
		tblPosition = new JTable();
		tblPosition.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"Symbol", "Price", "Qty", "AC P&L", "P&L"
				}
			));
		scrollPane_1.setViewportView(tblPosition);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		edQuote = new JTextField();
		edQuote.setText("USDJPY");
		panel_3.add(edQuote);
		edQuote.setColumns(10);
		
		JButton btnQuote = new JButton("Get Quote");
		btnQuote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        sendEvent(new QuoteSubEvent(getId(), null, edQuote.getText()));
			}
		});
		panel_3.add(btnQuote);
		
		lblBidAsk = new JLabel("");
		panel_3.add(lblBidAsk);
		
		JPanel panel_2 = new JPanel();
		splitPane2.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane);
		
		tblOrder = new JTable();
		tblOrder.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"ID", "Symbol", "Side", "Type", "Price", "Qty", "Status", "CumQty", "AvgPx", "Time"
			}
		));
		scrollPane.setViewportView(tblOrder);
		
		tblOrder.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	if(tblOrder.getRowCount() <= 0)
	        		return;
	        	
	        	if(tblOrder.getSelectedRow() < 0)
	        		return;
	        	
	        	log.debug("Selected: " + tblOrder.getValueAt(tblOrder.getSelectedRow(), 0).toString());
	            edSymbol.setText(tblOrder.getValueAt(tblOrder.getSelectedRow(), 1).toString());
	            String side = tblOrder.getValueAt(tblOrder.getSelectedRow(), 2).toString();
	            cbSide.setSelectedItem(side);
	            String type = tblOrder.getValueAt(tblOrder.getSelectedRow(), 3).toString();
	            cbSide.setSelectedItem(type);
	            String price = tblOrder.getValueAt(tblOrder.getSelectedRow(), 4).toString();
	            edPrice.setText(price);
	            String qty = tblOrder.getValueAt(tblOrder.getSelectedRow(), 5).toString();
	            edQty.setText(qty);
	        }
	    });
		
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_2 = new JScrollPane();
		panel.add(scrollPane_2);
		
		tblAccount = new JTable();
		tblAccount.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"Account Value", "Account Cash", "Cash Available", "P&L", "Ur P&L", "Daily P&L"
				}
			));
		scrollPane_2.setViewportView(tblAccount);
		
		JPanel plTrade = new JPanel();
		contentPane.add(plTrade, BorderLayout.SOUTH);
		
		JLabel lblSymbol = new JLabel("Symbol:");
		plTrade.add(lblSymbol);
		
		edSymbol = new JTextField();
		edSymbol.setText("USDJPY");
		plTrade.add(edSymbol);
		edSymbol.setColumns(6);
		
		JLabel lblPrice = new JLabel("Price:");
		plTrade.add(lblPrice);
		
		edPrice = new JTextField();
		plTrade.add(edPrice);
		edPrice.setColumns(6);
		
		JLabel lblQty = new JLabel("Qty:");
		plTrade.add(lblQty);
		
		edQty = new JTextField();
		edQty.setText("100000");
		plTrade.add(edQty);
		edQty.setColumns(6);
		
		cbType = new JComboBox();
		cbType.setModel(new DefaultComboBoxModel(new String[] {"Market", "Limit"}));
		plTrade.add(cbType);
		
		cbSide = new JComboBox();
		cbSide.setModel(new DefaultComboBoxModel(new String[] {"Buy", "Sell"}));
		plTrade.add(cbSide);
		
		JButton btnEnter = new JButton("Enter");
		btnEnter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		
				if(cbType.getSelectedItem().toString().equals("Market")) {
			        sendEvent(OrderUtil.createMarketOrder(
			        		edSymbol.getText(), 
			        		OrderSide.valueOf(cbSide.getSelectedItem().toString()), 
			        		Long.parseLong(edQty.getText()), 
			        		user, 
			        		account));		

				} else {
			        sendEvent(OrderUtil.createLimitOrder(
			        		edSymbol.getText(), 
			        		OrderSide.valueOf(cbSide.getSelectedItem().toString()), 
			        		Double.parseDouble(edPrice.getText()), 
			        		Long.parseLong(edQty.getText()), 
			        		user, 
			        		account));		
				}
			}
		});
		plTrade.add(btnEnter);
		
		JButton btnAmend = new JButton("Amend");
		btnAmend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tblOrder.getSelectedRow() < 0)
					return;
				
	            String orderId = tblOrder.getValueAt(tblOrder.getSelectedRow(), 0).toString();
	            log.debug("Amending: " + getId());
	            AmendParentOrderEvent event = 
	            		OrderUtil.amendOrder(getId(), orderId, Double.parseDouble(edPrice.getText()),
	            				Long.parseLong(edQty.getText()));
	            sendEvent(event);
			}
		});
		plTrade.add(btnAmend);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	            String orderId = tblOrder.getValueAt(tblOrder.getSelectedRow(), 0).toString();
	            CancelParentOrderEvent cancelEvent = new CancelParentOrderEvent(getId(), null,
	                    orderId, false, IdGenerator.getInstance().getNextID());
	            sendEvent(cancelEvent);
			}
		});
		plTrade.add(btnCancel);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		plTrade.add(separator);
		
		JButton btRefresh = new JButton("Update P&L");
		btRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
			}
		});
		plTrade.add(btRefresh);
	}
	
	private void updateAllOrder(List<Order> orders) {
        this.orders.clear();
        for (Order order: orders){
    		this.orders.put(order.getId(), order);
        }   
		refreshOrders();
	}
	
	private void updateOrder(Order order) {
		this.orders.put(order.getId(), order);
		refreshOrders();
	}

	private void refreshOrders() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		    	DefaultTableModel model = (DefaultTableModel)tblOrder.getModel();
		        model.setRowCount(0);
		        for (Order order: orders.values())
		        {
		//			"ID", "Symbol", "Side", "Type", "Price", "Qty", "Status", "CumQty", "AvgPx", "Time"
		        	model.addRow( new Object[]{
		            		order.getId(),
		            		order.getSymbol(),
		            		order.getSide(),
		            		order.getType(),
		            		order.getPrice(),
		            		order.getQuantity(),
		            		order.getStatus(),
		            		order.getCumQty(),
		            		order.getAvgPx(),
		            		order.getCreated()
		                });
		        }   
            }
        });
	}
	
	private void updateAccount(final Account account) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		    	DefaultTableModel model = (DefaultTableModel)tblAccount.getModel();
		        model.setRowCount(0);
		//		"Account Value", "Account Cash", "Cash Available", "P&L", "Ur P&L", "Daily P&L"
		        model.addRow(new Object[] {
		        		decimalFormat.format(account.getValue()), 
		        		decimalFormat.format(account.getCashDeduct()),
		        		decimalFormat.format(account.getCashAvailable()),
		        		decimalFormat.format(account.getPnL()),
		        		decimalFormat.format(account.getUrPnL()),
		        		decimalFormat.format(account.getDailyPnL())
		        });
            }
        });
	}
	
	private void updateAllOpenPositions(List<OpenPosition> positions){
        this.positions.clear();
        for (OpenPosition position: positions){
    		this.positions.put(position.getSymbol(), position);
        }   
        refreshPositions();
	}
	
	private void updateOpenPosition(OpenPosition position) {
		if (PriceUtils.isZero(position.getQty())) {
			positions.remove(position.getSymbol());
		} else {
			positions.put(position.getSymbol(), position);
		}
		refreshPositions();
	}
	
	private void refreshPositions() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		    	DefaultTableModel model = (DefaultTableModel)tblPosition.getModel();
		        model.setRowCount(0);
		        for (OpenPosition position: positions.values())
		        {        	
		//			"Symbol", "Price", "Qty", "Time"
		        	model.addRow( new Object[]{
		        			position.getSymbol(),
		            		position.getPrice(),
		            		position.getQty(),
		            		LtsWsFrame.this.decimalFormat.format(position.getAcPnL()),
		            		LtsWsFrame.this.decimalFormat.format(position.getPnL()),
		                });
		        }   
            }
        });
	}

	private String user = "test1";
    private String account = "test1-FX";
    private String password = "xxx";
    
    private TreeMap<String, Order> orders = new TreeMap<String, Order>(Collections.reverseOrder());
    private TreeMap<String, OpenPosition> positions = new TreeMap<String, OpenPosition>(Collections.reverseOrder());
    private Boolean serverReady;
    private Boolean login;
    
    @Autowired
    private IRemoteEventManager eventManager = new ClientSocketEventManager();

    protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(ServerReadyEvent.class, null);
            subscribeToEvent(QuoteEvent.class, null);
            subscribeToEvent(EnterParentOrderReplyEvent.class, getId());
            subscribeToEvent(AmendParentOrderReplyEvent.class, getId());
            subscribeToEvent(CancelParentOrderReplyEvent.class, getId());
            subscribeToEvent(ParentOrderUpdateEvent.class, null);
            subscribeToEvent(StrategySnapshotEvent.class, null);
            subscribeToEvent(UserLoginReplyEvent.class, null);
            subscribeToEvent(AccountSnapshotReplyEvent.class, null);
            subscribeToEvent(AccountUpdateEvent.class, null);
            subscribeToEvent(OpenPositionUpdateEvent.class, null);
            subscribeToEvent(ClosedPositionUpdateEvent.class, null);
            subscribeToEvent(SystemErrorEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }

    };

    public void init() throws Exception {
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("LtsApiAdaptor");

        eventManager.init(null, null);
    }

    private void sendEvent(RemoteAsyncEvent event) {
        try {
            eventManager.sendRemoteEvent(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processServerReadyEvent(ServerReadyEvent event) {
        log.debug("Received ServerReadyEvent: " + event.getSender() + ", " + event.isReady());
        serverReady = event.isReady();
        if (event.isReady()) {
            sendEvent(new UserLoginEvent(getId(), null, user, password, IdGenerator.getInstance().getNextID()));
        }
    }
    
    public void processAccountSnapshotReplyEvent(AccountSnapshotReplyEvent event) {
        log.debug("### Account Snapshot Start ###");
        log.debug("Account: " + event.getKey());
        updateAccount(event.getAccount());
        log.debug("Open positions: " + event.getOpenPositions());
        updateAllOpenPositions(event.getOpenPositions());
        log.debug("Trades :" + event.getExecutions());
        log.debug("### Account Snapshot End ###");
    }

    public void processAccountUpdateEvent(AccountUpdateEvent event) {
        log.debug("Account: " + event.getAccount());
        updateAccount(event.getAccount());
    }

    public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
        log.debug("Position: " + event.getPosition());
        updateOpenPosition(event.getPosition());
    }

    public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
        log.debug("Closed Position: " + event.getPosition());
    }

    public void processQuoteEvent(QuoteEvent event) {
        log.debug("Received QuoteEvent: " + event.getKey() + ", " + event.getQuote());
        final Quote quote = event.getQuote();
        
        String symbol = edQuote.getText()!=null?edQuote.getText().trim():"";
        
        if(!symbol.equals(quote.getSymbol()))
        	return;
        	
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	lblBidAsk.setText("    "+ quote.getBid() + " / " + quote.getAsk());
            }
        });
    }

    public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
        log.debug("User login is " + event.isOk() + ", " + event.getMessage());

        login = event.isOk();
        if (!event.isOk())
            return;

        this.account = event.getAccount();
        sendEvent(new QuoteSubEvent(getId(), null, "AUDUSD"));
        sendEvent(new QuoteSubEvent(getId(), null, "USDJPY"));
        sendEvent(new StrategySnapshotRequestEvent(account, null, null));
        sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
        this.setTitle(title + " - " + this.user);
     }

    public void processStrategySnapshotEvent(StrategySnapshotEvent event) {
        List<Order> orders = event.getOrders();
        log.debug("### Start order list ###");
        for (Order order : orders) {
            log.debug("Order: " + order);
        }
        log.debug("### End order list ###");
        updateAllOrder(orders);
    }

    public void processEnterParentOrderReplyEvent(
            EnterParentOrderReplyEvent event) {
        if (!event.isOk()) {
            log.debug("Received EnterParentOrderReplyEvent(NACK): " + event.getMessage());
        } else {
            log.debug("Received EnterParentOrderReplyEvent(ACK)");
        }
    }

    public void processAmendParentOrderReplyEvent(
            AmendParentOrderReplyEvent event) {
        if (event.isOk()) {
            log.debug("Received AmendParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
        } else {
            log.debug("Received AmendParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
        }
    }

    public void processCancelParentOrderReplyEvent(
            CancelParentOrderReplyEvent event) {
        if (event.isOk()) {
            log.debug("Received CancelParentOrderReplyEvent(ACK): " + event.getKey() + ", order: " + event.getOrder());
        } else {
            log.debug("Received CancelParentOrderReplyEvent(NACK): " + event.isOk() + ", message: " + event.getMessage());
        }
    }

    public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
        log.debug("Received ParentOrderUpdateEvent: " + event.getOrder());
        updateOrder(event.getOrder());
    }

    public void processSystemErrorEvent(SystemErrorEvent event) {
        log.error("Error code: " + event.getErrorCode() + " - " + event.getMessage());
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getId() {
        return user;
    }

	public Boolean isServerReady() {
		return serverReady;
	}

	public Boolean isLogin() {
		return login;
	}

	/**
	 * Launch the application.
	 */
    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/apilog4j.xml");
        String configFile = "conf/apigui.xml";
        if (args.length > 0)
            configFile = args[0];
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);

        // start server
        final LtsWsFrame apiGui = (LtsWsFrame)context.getBean("apiGui");
        ClientSocketService socketService = (ClientSocketService)context.getBean("socketService");
        LoginDlg dlg = new LoginDlg();
        
        dlg.setHost(socketService.getHost());
        dlg.setPort(""+socketService.getPort());
        dlg.setUser(apiGui.getUser());
        dlg.setLogin(false);
        dlg.setVisible(true);
        if(!dlg.isLogin())
        	System.exit(0);
 		socketService.setHost(dlg.getHost());
 		socketService.setPort(Integer.parseInt(dlg.getPort()));
 		apiGui.setUser(dlg.getUser());
 		apiGui.setPassword(dlg.getPassword());
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
		 		try {
	 			apiGui.init();
		 		} catch (Exception e) {
		 			log.error(e.getMessage(), e);
		 		}
			}
		});
		thread.start();
		
		// wait for connection successful
 		int maxCount = 40;
 		int count= 0;
 		while(apiGui.isServerReady() == null && count < maxCount) {
	 		Thread.sleep(100);
	 		count++;
 		}

 		if(apiGui.isServerReady() == null || !apiGui.isServerReady()) {
 			JOptionPane.showMessageDialog(null, "Connection failed");
			System.exit(-1);
 		}

 		// wait for login successful
 		maxCount = 30;
 		count= 0;
 		while(apiGui.isLogin() == null && count < maxCount) {
	 		Thread.sleep(100);
	 		count++;
 		}
 		if(apiGui.isLogin() == null || !apiGui.isLogin()) {
 			JOptionPane.showMessageDialog(null, "Login failed");
 			System.exit(-1);
 		}

 		apiGui.setVisible(true);
    }

}
