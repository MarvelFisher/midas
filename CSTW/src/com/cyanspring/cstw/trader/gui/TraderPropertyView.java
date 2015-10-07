package com.cyanspring.cstw.trader.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSettingSnapshotRequestEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingReplyEvent;
import com.cyanspring.common.event.account.ChangeAccountSettingRequestEvent;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;

public class TraderPropertyView extends ViewPart implements IAsyncEventListener{
	enum TraderProperty{
		DEFAULT_QUANTITY("Default Quantity");
		private String value;
		private TraderProperty(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
	}
	private static final Logger log = LoggerFactory.getLogger(TraderPropertyView.class);
	public static final String ID = "com.cyanspring.cstw.trader.gui.TraderPropertyViewer"; //$NON-NLS-1$
	private Text txt_defaultQty;
	private TreeItem trtmDefaultQuantity;
	private ImageRegistry imageRegistry;
	private Composite parentComposite = null;
	private AccountSetting accountSetting = Business.getInstance().getAccountSetting();
	private String accountId = Business.getInstance().getAccount();
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof AccountSettingSnapshotReplyEvent) {
			AccountSettingSnapshotReplyEvent e = (AccountSettingSnapshotReplyEvent) event;
			if(null != e.getAccountSetting()){
				setAccountSetting(e.getAccountSetting());
				setAccountSettingParams();
			}
		} else if(event instanceof ChangeAccountSettingReplyEvent) {
			ChangeAccountSettingReplyEvent evt = (ChangeAccountSettingReplyEvent)event;
			log.info("Account setting change: " + evt.isOk() + ", " + evt.getMessage());
			if(evt.isOk()){
				showMessageBox("Account settings updated", parentComposite);
			}else{
				showMessageBox("Account settings update fail:"+evt.getMessage(), parentComposite);
			}
			sendAccountSettingRequestEvent();
		} else {
			log.error("Unhandled event: " + event);
		}
	}
	
	private void setAccountSetting(AccountSetting setting){
		accountSetting = setting;
		Business.getInstance().setAccountSetting(setting);
	}

	private void setAccountSettingParams() {
		if( null == accountSetting)
			return ;
		
		if(null == parentComposite)
			return ;
		
		parentComposite.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				txt_defaultQty.setText(Double.toString(accountSetting.getDefaultQty()));
				trtmDefaultQuantity.setText(TraderProperty.DEFAULT_QUANTITY.getValue()+" : "+txt_defaultQty.getText());
			}
		});
		
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		imageRegistry = Activator.getDefault().getImageRegistry();

		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(container, SWT.NONE);
		
		Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Tree tree = new Tree(composite, SWT.BORDER);
		
		TreeItem trtmAccount = new TreeItem(tree, SWT.NONE);
		trtmAccount.setText("Account Settings");
		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.USER_ICON.toString());
		trtmAccount.setImage(imageDesc.createImage());
		
		trtmDefaultQuantity = new TreeItem(trtmAccount, SWT.NONE);
		trtmDefaultQuantity.setText(TraderProperty.DEFAULT_QUANTITY.getValue());
		trtmAccount.setExpanded(false);
		
		Composite composite_1 = new Composite(sashForm, SWT.NONE);
		composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Group grpAccountSettings = new Group(composite_1, SWT.SHADOW_OUT);
		GridLayout gl_grpAccountSettings = new GridLayout(2, true);
		gl_grpAccountSettings.marginLeft = 1;
		grpAccountSettings.setLayout(gl_grpAccountSettings);
		grpAccountSettings.setText("Account Settings");
		
		Label lblDefaultQuantity = new Label(grpAccountSettings, SWT.NONE);
		lblDefaultQuantity.setText("Default Quantity :");
		
		txt_defaultQty = new Text(grpAccountSettings, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 99;
		txt_defaultQty.setLayoutData(gd_text);
		
		Button btnModify = new Button(grpAccountSettings, SWT.NONE);
		GridData gd_btnModify = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnModify.widthHint = 112;
		btnModify.setLayoutData(gd_btnModify);
		btnModify.setText("Modify");
		btnModify.setImage(imageRegistry.getDescriptor(ImageID.PIN_ICON.toString()).createImage());
		btnModify.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendPropertyModifyEvent();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button btnRestore = new Button(grpAccountSettings, SWT.NONE);
		GridData gd_btnRestore = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRestore.widthHint = 112;
		btnRestore.setLayoutData(gd_btnRestore);
		btnRestore.setText("Restore");
		btnRestore.setImage(imageRegistry.getDescriptor(ImageID.REFRESH_ICON.toString()).createImage());
		sashForm.setWeights(new int[] {119, 472});
		btnRestore.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAccountSettingParams();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
			
		accountId = Business.getInstance().getAccount();
		log.info("request setting:{}",accountId);
		subEvent(AccountSettingSnapshotReplyEvent.class);
		subEvent(ChangeAccountSettingReplyEvent.class);
		sendAccountSettingRequestEvent();
	}

	private void sendAccountSettingRequestEvent(){
		AccountSettingSnapshotRequestEvent settingRequestEvent = new AccountSettingSnapshotRequestEvent(ID, Business.getInstance().getFirstServer(), accountId, null);
		sendRemoteEvent(settingRequestEvent);
	}
	
	private void sendPropertyModifyEvent() {
		if(!isNumberType(txt_defaultQty.getText())){
			showMessageBox("Default Quantity is not number type", parentComposite);
			setAccountSettingParams();
			return;
		}
		double defaultQty = Double.parseDouble(txt_defaultQty.getText());
		if(PriceUtils.LessThan(defaultQty, 0)){
			showMessageBox("Default Quantity can't be nagative number",parentComposite);
			setAccountSettingParams();
			return;
		}
		
		if(PriceUtils.Equal(defaultQty, accountSetting.getDefaultQty())){
			showMessageBox("Default Quantity not modified", parentComposite);
			return;
		}
		AccountSetting tempSetting = new AccountSetting(accountId);
		tempSetting.setDefaultQty(defaultQty);
		ChangeAccountSettingRequestEvent request = new ChangeAccountSettingRequestEvent(ID, Business.getInstance().getFirstServer(), tempSetting);
		sendRemoteEvent(request);
	}
	
	@Override
	public void setFocus() {
		
	}

	private void subEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().subscribe(clazz, this);
	}
	
	private void unSubEvent(Class<? extends AsyncEvent> clazz){
		Business.getInstance().getEventManager().unsubscribe(clazz,ID, this);		
	}
	
	private void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private void showMessageBox(final String msg, Composite parent){
		parent.getDisplay().asyncExec(new Runnable(){

			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(parentComposite.getShell(),
						SWT.ICON_INFORMATION);
				messageBox.setText("Info");
				messageBox.setMessage(msg);
				messageBox.open();				
			}
			
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		unSubEvent(AccountSettingSnapshotReplyEvent.class);
		unSubEvent(ChangeAccountSettingReplyEvent.class);
	}
	
	private boolean isNumberType(String strOld) {
		try{
			Double.parseDouble(strOld);
		}catch(Exception e){
			return false;
		}
		return true;
	}
}
