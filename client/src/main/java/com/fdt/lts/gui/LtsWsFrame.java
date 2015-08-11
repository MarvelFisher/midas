package com.fdt.lts.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.apievent.obj.Account;
import com.cyanspring.apievent.obj.OpenPosition;
import com.cyanspring.apievent.obj.Order;
import com.cyanspring.apievent.obj.OrderSide;
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
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.event.ClientSocketEventManager;
import com.fdt.lts.client.LtsApiAdaptor;
import com.fdt.lts.client.OrderUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;

public class LtsWsFrame extends JFrame {
    private static Logger log = LoggerFactory.getLogger(LtsWsFrame.class);

	private JPanel contentPane;
	private JTextField edSymbol;
	private JTextField edPrice;
	private JTextField edQty;
	private JTable tblOrder;

	/**
	 * Create the frame.
	 */
	public LtsWsFrame() {
		this.setTitle("LTS Trader Workstation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1054, 633);
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
					"Symbol", "Price", "Qty", "Time"
				}
			));
		scrollPane_1.setViewportView(tblPosition);
		
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
		edSymbol.setColumns(10);
		
		JLabel lblPrice = new JLabel("Price:");
		plTrade.add(lblPrice);
		
		edPrice = new JTextField();
		plTrade.add(edPrice);
		edPrice.setColumns(10);
		
		JLabel lblQty = new JLabel("Qty:");
		plTrade.add(lblQty);
		
		edQty = new JTextField();
		edQty.setText("100000");
		plTrade.add(edQty);
		edQty.setColumns(10);
		
		final JComboBox cbType = new JComboBox();
		cbType.setModel(new DefaultComboBoxModel(new String[] {"Limit", "Market"}));
		plTrade.add(cbType);
		
		final JComboBox cbSide = new JComboBox();
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
		plTrade.add(btnAmend);
		
		JButton btnCancel = new JButton("Cancel");
		plTrade.add(btnCancel);
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
	
	private void updateAccount(Account account) {
    	DefaultTableModel model = (DefaultTableModel)tblAccount.getModel();
        model.setRowCount(0);
//		"Account Value", "Account Cash", "Cash Available", "P&L", "Ur P&L", "Daily P&L"
        model.addRow(new Object[] {
        		account.getValue(), 
        		account.getCash() + account.getUrPnL(),
        		account.getCashAvailable(),
        		account.getPnL(),
        		account.getUrPnL(),
        		account.getDailyPnL()
        });
	}
	
	private void updateAllOpenPositions(List<OpenPosition> positions){
        this.positions.clear();
        for (OpenPosition position: positions){
    		this.positions.put(position.getSymbol(), position);
        }   
		refreshOrders();
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
    	DefaultTableModel model = (DefaultTableModel)tblPosition.getModel();
        model.setRowCount(0);
        for (OpenPosition position: positions.values())
        {
//			"Symbol", "Price", "Qty", "Time"
        	model.addRow( new Object[]{
        			position.getSymbol(),
            		position.getPrice(),
            		position.getQty(),
            		position.getCreated(),
                });
        }   
		
	}

	private String user = "test1";
    private String account = "test1-FX";
    private String password = "xxx";
    
    private TreeMap<String, Order> orders = new TreeMap<String, Order>(Collections.reverseOrder());
    private TreeMap<String, OpenPosition> positions = new TreeMap<String, OpenPosition>(Collections.reverseOrder());
    
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
    private JTable tblPosition;
    private JTable tblAccount;

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
    }

    public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
        log.debug("User login is " + event.isOk() + ", " + event.getMessage());

        if (!event.isOk())
            return;

        sendEvent(new QuoteSubEvent(getId(), null, "AUDUSD"));
        sendEvent(new QuoteSubEvent(getId(), null, "USDJPY"));
        sendEvent(new StrategySnapshotRequestEvent(account, null, null));
        sendEvent(new AccountSnapshotRequestEvent(account, null, account, null));
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
        log.debug("Received ParentOrderUpdateEvent: " + ", order: " + event.getOrder());
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
        LtsWsFrame apiGui = (LtsWsFrame)context.getBean("apiGui");
 		apiGui.setVisible(true);
        apiGui.init();
    }

}
