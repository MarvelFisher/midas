package com.fdt.lts.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DropMode;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author dennischen
 * @version 1.0
 * @since 1.0
 */

public class LoginDlg extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField edUser;
	private final JLabel lblUser = new JLabel("User:");
	private final JLabel lblPassword = new JLabel("Password:");
	private JPasswordField edPassword;
	private JLabel lblHost;
	private JTextField edHost;
	private JLabel lblPort;
	private JTextField edPort;
	private JCheckBox chkAdvanced;
	private static final int x = 150;
	private static final int y = 150;
	private static final int width = 400;
	private static final int height = 264;
	private static final int diff = 80;
	private boolean login;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			LoginDlg dialog = new LoginDlg();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public LoginDlg() {
		setModal(true);
		setTitle("Login");
		setBounds(x, y, width, height - diff);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	setLocation((dim.width-getWidth())/2, (dim.height-getHeight())/2);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(2, 2, 20, 10));
		contentPanel.add(lblUser);
		{
			edUser = new JTextField();
			contentPanel.add(edUser);
			edUser.setColumns(5);
		}
		contentPanel.add(lblPassword);
		{
			edPassword = new JPasswordField();
			contentPanel.add(edPassword);
		}
		{
			lblHost = new JLabel("Host:");
			//contentPanel.add(lblHost);
		}
		{
			edHost = new JTextField();
			//contentPanel.add(edHost);
			edHost.setColumns(10);
		}
		{
			lblPort = new JLabel("Port:");
			//contentPanel.add(lblPort);
		}
		{
			edPort = new JTextField();
			//contentPanel.add(edPort);
			edPort.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new GridLayout(1, 4, 0, 0));
			{
				chkAdvanced = new JCheckBox("Advanced");
				chkAdvanced.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if(!chkAdvanced.isSelected()) {
							contentPanel.remove(lblHost);
							contentPanel.remove(edHost);
							contentPanel.remove(lblPort);
							contentPanel.remove(edPort);
							contentPanel.setLayout(new GridLayout(2, 2, 20, 10));
							Rectangle rect = LoginDlg.this.getBounds();
							rect.width = width;
							rect.height = height - diff;
							LoginDlg.this.setBounds(rect);
							contentPanel.doLayout();
						} else {
							contentPanel.add(lblHost);
							contentPanel.add(edHost);
							contentPanel.add(lblPort);
							contentPanel.add(edPort);
							contentPanel.setLayout(new GridLayout(4, 2, 20, 10));
							Rectangle rect = LoginDlg.this.getBounds();
							rect.width = width;
							rect.height = height;
							LoginDlg.this.setBounds(rect);
							contentPanel.doLayout();
						}
					}
				});
				buttonPane.add(chkAdvanced);
			}
			{
				JLabel lblNewLabel = new JLabel("");
				buttonPane.add(lblNewLabel);
			}
			{
				JButton btLogin = new JButton("Login");
				btLogin.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						LoginDlg.this.setVisible(false);
						login = true;
					}
				});
				btLogin.setActionCommand("OK");
				buttonPane.add(btLogin);
				getRootPane().setDefaultButton(btLogin);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public String getUser() {
		return edUser.getText();
	}

	public void setUser(String user) {
		this.edUser.setText(user);
	}

	public String getPassword() {
		return new String(edPassword.getPassword());
	}

	public String getHost() {
		return edHost.getText();
	}

	public void setHost(String host) {
		edHost.setText(host);;
	}

	public String getPort() {
		return edPort.getText();
	}

	public void setPort(String port) {
		edPort.setText(port);;
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

}
