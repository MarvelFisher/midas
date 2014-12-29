package com.cyanspring.id.gateway;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.cyanspring.id.Library.Frame.FrameUtil;
import com.cyanspring.id.Library.Frame.IFrameClose;
import com.cyanspring.id.Library.Frame.InfoLabel;
import com.cyanspring.id.Library.Frame.InfoString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class IDGateWayDialog extends JFrame {

	public static final int TXT_InSize = 1;
	public static final int TXT_OutSize = 2;

	// const data
	static final int KB = 1024;
	static final int MB = 1024 * KB;
	static final int GB = KB * MB;
	static final double MaxSize = 10 * GB;
	static final String _defSizeValue = "       0";

	IDGateWayDialog mainFrame;
	final static int FRAME_WIDTH = 600;
	final static int FRAME_HEIGHT = 400;
	final static int FRAME_UI_01_HEIGHT = 60;

	// Data
	private DefaultListModel<InfoString> listLogModel = null;
	private DefaultListModel<String> listClientModel = null;
	double _dInSize = 0, _dOutSize = 0;
	String _strStatusMsg = "";

	// View
	private JSplitPane splitPaneV, splitPaneV2;
	private JPanel defaultPanel, inputPanel;
	JScrollPane scrollLog, scrollClient;

	private JList<InfoString> listLog = null;
	private JList<String> listClient = null;
	JLabel _inSize, _outSize;
	JLabel _statusbar;

	IFrameClose _caller;

	@SuppressWarnings("unchecked")
	public IDGateWayDialog(IFrameClose caller) {
		_caller = caller;
		setBackground(Color.LIGHT_GRAY);

		_statusbar = new JLabel(" Statusbar");
		_statusbar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		add(_statusbar, BorderLayout.SOUTH);

		defaultPanel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		defaultPanel.setLayout(new BoxLayout(defaultPanel, BoxLayout.LINE_AXIS));

		defaultPanel.setPreferredSize(new Dimension(FRAME_WIDTH, 30));

		inputPanel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
		inputPanel.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_UI_01_HEIGHT - 30));

		// show client list
		listClientModel = new DefaultListModel<String>();
		listClient = new JList<String>(listClientModel);
		scrollClient = createPanel3(listClient);
		scrollClient.setPreferredSize(new Dimension(150, 0));

		// show log list
		listLogModel = new DefaultListModel<InfoString>();
		listLog = new JList<InfoString>(listLogModel);
		listLog.setCellRenderer(new InfoLabel());
		scrollLog = createLogPanel(listLog);

		// split panel to 2 list
		final JSplitPane bottomv = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollClient, scrollLog);

		bottomv.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				bottomv.setDividerLocation(1.0 / 4.0);
			}
		});

		splitPaneV2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, defaultPanel, inputPanel);
		FrameUtil.flattenSplitPane(splitPaneV2);

		splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneV2, bottomv);

		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		getContentPane().add(splitPaneV);

		ImageIcon icon2 = new ImageIcon("Image/client.png");
		this.setIconImage(icon2.getImage());

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				_caller.onCloseAction();
				System.exit(0);
			}
		});
	}

	/**
	 * set size format
	 * 
	 * @param dSize
	 * @return
	 */
	static String formatSize(double dSize) {

		String strRet = _defSizeValue;
		if (dSize > GB) {
			strRet = String.format("%.5f GB", dSize / GB);
		} else if (dSize > MB) {
			strRet = String.format("%.4f MB", dSize / MB);
		} else if (dSize > KB) {
			strRet = String.format("%.3f KB", dSize / KB);
		} else if (dSize != 0)
			strRet = String.format("%d bytes", (int)dSize);

		return strRet;
	}

	public void setSize(int nID, double dSize) {
		switch (nID) {
		case IDGateWayDialog.TXT_InSize: {
			_dInSize = dSize;
		}
			break;
		case IDGateWayDialog.TXT_OutSize: {
			_dOutSize = dSize;
		}
		default:
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_outSize.setText(formatSize(_dOutSize));
				_inSize.setText(formatSize(_dInSize));
			}
		});
	}
	
	/**
	 * add button to input panel
	 * 
	 * @param button
	 */
	public void addButton(JButton button) {
		inputPanel.add(button);
	}

	/**
	 * add label to input panel
	 * 
	 * @param label
	 * @return
	 */
	public JLabel addLabel(JLabel label) {
		inputPanel.add(label);
		return label;
	}

	/**
	 * add text field to input panel
	 * 
	 * @param text
	 */
	public void addTextField(JTextField text) {
		inputPanel.add(text);
	}

	/**
	 * input panel add space and the next ui will be in right side
	 */
	public void addSpace() {
		inputPanel.add(Box.createHorizontalGlue());
	}

	/**
	 * create default ui Panel
	 */
	public void createDefaultPanel() {

		defaultPanel.add(new JLabel("In Size:"));
		_inSize = new JLabel(_defSizeValue);
		defaultPanel.add(_inSize);
		defaultPanel.add(new JLabel("         "));
		defaultPanel.add(new JLabel("Out Size:"));
		_outSize = new JLabel(_defSizeValue);
		defaultPanel.add(_outSize);
		defaultPanel.add(Box.createHorizontalGlue());

		JButton close = new JButton("Close");
		close.setToolTipText("A button component : Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_caller.onCloseAction();
				System.exit(0);
			}
		});
		defaultPanel.add(close);
		defaultPanel.add(new JLabel("  "));
	}

	/**
	 * create scroll Log List Panel
	 * 
	 * @param list
	 * @return scroll panel
	 */
	public JScrollPane createLogPanel(JList<InfoString> list) {

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(list.getModel().getSize() - 1);
		JScrollPane tmpscroll = new JScrollPane(list);

		return tmpscroll;

	}

	/**
	 * create scroll ip list panel
	 * 
	 * @param list
	 * @return scroll panel
	 */
	public JScrollPane createPanel3(JList<String> list) {

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(list.getModel().getSize() - 1);
		JScrollPane tmpscroll = new JScrollPane(list);

		return tmpscroll;
	}

	/**
	 * set status bar message
	 * 
	 * @param strTxt
	 */
	public void setStatus(String strTxt) {
		_strStatusMsg = strTxt;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_statusbar.setText(_strStatusMsg);
			}
		});
	}

	/**
	 * add log message to log panel
	 * 
	 * @param f
	 * @param args
	 */
	public void addLog(String f, Object... args) {
		addLog(InfoString.Info, f, args);
	}

	/**
	 * add log message with log level to log panel
	 * 
	 * @param nLevel
	 * @param f
	 * @param args
	 */
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

	/**
	 * update client panel
	 * 
	 * @param list
	 */
	public void updateClient(ArrayList<String> list) {

		listClientModel.removeAllElements();

		for (String ip : list) {
			listClientModel.addElement(ip);
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (listClient != null) {
					int lastIndex = listClient.getModel().getSize() - 1;
					if (lastIndex >= 0) {
						listClient.setSelectedIndex(lastIndex);
						listClient.ensureIndexIsVisible(lastIndex);
					}
					listClient.updateUI();
				}
			}
		});
	}

	/**
	 * add client IP
	 * 
	 * @param strDetail
	 */
	public void addClient(String strDetail) {

		if (listClientModel.contains(strDetail)) {
			return;
		}

		listClientModel.addElement(strDetail);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (listClient != null) {
					int lastIndex = listClient.getModel().getSize() - 1;
					if (lastIndex >= 0) {
						listClient.setSelectedIndex(lastIndex);
						listClient.ensureIndexIsVisible(lastIndex);
					}
				}
			}
		});
	}

	/**
	 * remove client IP
	 * 
	 * @param strDetail
	 */
	public void removeClient(String strDetail) {

		if (!listClientModel.contains(strDetail)) {
			return;
		}

		listClientModel.removeElement(strDetail);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (listClient != null) {
					int lastIndex = listClient.getModel().getSize() - 1;
					if (lastIndex >= 0) {
						listClient.setSelectedIndex(lastIndex);
						listClient.ensureIndexIsVisible(lastIndex);
					}
				}
			}
		});
	}

	/**
	 * 
	 * @param caller
	 * @param strTitle
	 * @return
	 */
	public static IDGateWayDialog Instance(IFrameClose caller, String strTitle) {
		try {

			// UIManager
			// .setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		} catch (Exception evt) {
		}

		if (strTitle.isEmpty()) {
			strTitle = "GateWayDialog";
		}
		final IDGateWayDialog mainFrame = new IDGateWayDialog(caller);
		mainFrame.setTitle(strTitle);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create an instance of the test application
				mainFrame.createDefaultPanel();
				mainFrame.pack();
				mainFrame.setVisible(true);
				mainFrame.setLocationRelativeTo(null);
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			}
		});
		return mainFrame;
	}


}
