package com.cyanspring.adaptor.future.wind.test;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Frame.InfoLabel;
import com.cyanspring.id.Library.Frame.InfoString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class Mainframe extends JFrame {

	static Mainframe mainFrame;
	public static boolean IsUsingMsg = false;
	private JSplitPane splitPaneV;
	private JPanel panel1;
	private JPanel inputPanel;
	JScrollPane scrollLog, scrollMsg;

	private DefaultListModel<InfoString> listLogModel = null;
	private JList<InfoString> listLog = null;
	private DefaultListModel<String> listMsgModel = null;
	private JList<String> listMsg = null;

	IFrameClose caller;

	@SuppressWarnings("unchecked")
	public Mainframe(IFrameClose caller) {
		this.caller = caller;
		setTitle("Split Pane Application");
		setBackground(Color.gray);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);

		// Create the panels
		// createPanel1();
		createinputPanel();

		listLogModel = new DefaultListModel<InfoString>();
		listLog = new JList<InfoString>(listLogModel);
		listLog.setCellRenderer(new InfoLabel());
		scrollLog = createPanelLog(listLog);

		if (IsUsingMsg) {
			listMsgModel = new DefaultListModel<String>();
			listMsg = new JList<String>(listMsgModel);
			scrollMsg = createPanelMsg(listMsg);
		}

		// Create a splitter pane

		// splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// splitPaneH.setLeftComponent(panel1);
		// splitPaneH.setRightComponent(inputPanel);

		if (IsUsingMsg) {
			JSplitPane v = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollMsg, scrollLog);
			splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, v);
		} else {
			splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, scrollLog);
		}
		// splitPaneV.setLeftComponent(inputPanel); //splitPaneH);
		// splitPaneV.setRightComponent(panel3);
		setPreferredSize(new Dimension(600, 400));
		topPanel.add(splitPaneV, BorderLayout.CENTER);

		// ImageIcon icon = new ImageIcon("Image/png17.png");
		ImageIcon icon2 = new ImageIcon("Image/client.png");
		this.setIconImage(icon2.getImage());
	}

	public void addButton(JButton button) {
		inputPanel.add(button);
	}
	
	public void addCheckBox(JCheckBox check) {
		inputPanel.add(check);
	}

	public JLabel addLabel(JLabel label) {
		inputPanel.add(label);
		return label;
	}

	public void addTextField(JTextField text) {
		inputPanel.add(text);
	}
	
	public void addSpace(){ 
		inputPanel.add(Box.createHorizontalGlue());
	}	

	public void createPanel1() {
		panel1 = new JPanel();

		panel1.setLayout(new BorderLayout());
		// Add some buttons
		panel1.add(new JButton("North"), BorderLayout.NORTH);
		panel1.add(new JButton("South"), BorderLayout.SOUTH);
		panel1.add(new JButton("East"), BorderLayout.EAST);
		panel1.add(new JButton("West"), BorderLayout.WEST);
		panel1.add(new JButton("Center"), BorderLayout.CENTER);

	}

	JTextArea area = null;

	public void setQuoteText(final String text) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				area.setText(text);
			}
		});
	}

	public void createinputPanel() {
		inputPanel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		// inputPanel.setSize(500, 60);
		inputPanel.setPreferredSize(new Dimension(800, 400));
		// inputPanel.setLayout(new FlowLayout());
		// inputPanel.setLayout(new GridLayout(3, 3));
		addSpace();
		area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setPreferredSize(new Dimension(580, 200));
		// area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		inputPanel.add(area);
		String s = String.format("     \n     ");
		area.setText(s);
		//inputPanel.add(new JLabel("Hello"));
		//inputPanel.add(new JTextField(10));
		//inputPanel.add(new JButton("Button 1"));
		//inputPanel.add(new JButton("Button 2"));
		//inputPanel.add(new JButton("Button 3"));
	}

	public JScrollPane createPanelMsg(JList<String> listMsg2) {

		// list = new JList<String>(listLogModel);
		listMsg2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMsg2.setSelectedIndex(listLogModel.size() - 1);

		JScrollPane tmpscroll = new JScrollPane(listMsg2);

		return tmpscroll;

	}
	
	public JScrollPane createPanelLog(JList<InfoString> list) {

		// list = new JList<String>(listLogModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(listLogModel.size() - 1);

		JScrollPane tmpscroll = new JScrollPane(list);

		return tmpscroll;

	}
	public void addLog(Integer nLevel, String f, Object... args) {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String s = String.format("[%s] %-10s %s", sdf.format(new java.util.Date()), InfoString.getString(nLevel),
				String.format(f, args));

		final InfoString info = new InfoString(nLevel, s);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (listLogModel != null) {
					if (listLogModel.size() > 500) {
						for (int i = 0; i < 5; i++) {
							listLogModel.removeElementAt(0);				
						}
					}
					listLogModel.addElement(info);
				}
				if (listLog != null) {
					int lastIndex = listLog.getModel().getSize() - 1;
					if (lastIndex >= 0) {
						listLog.setSelectedIndex(lastIndex);
						listLog.ensureIndexIsVisible(lastIndex);
					}
					listLog.updateUI();
				}
			}
		});
	}
	public void addMsg(String f, Object... args) {

		// SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ");
		String s = String.format(f, args);

		listMsgModel.addElement(s);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (listMsg != null) {
					int lastIndex = listMsg.getModel().getSize() - 1;
					if (lastIndex >= 0) {
						listMsg.setSelectedIndex(lastIndex);
						listMsg.ensureIndexIsVisible(lastIndex);
					}
				}
			}
		});
	}

	public static Mainframe instance(IFrameClose caller, String title) {
		try {

			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

		} catch (Exception evt) {
		}

		mainFrame = new Mainframe(caller);
		mainFrame.setTitle(title);
		// IsUsingMsg = true;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create an instance of the test application

				// mainFrame.setLayout(new GridLayout(2, 1));
				// mainFrame.setSize(800, 600);
				mainFrame.pack();
				mainFrame.setVisible(true);
				mainFrame.setLocationRelativeTo(null);
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});

		return mainFrame;
	}

}
