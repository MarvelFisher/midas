package com.cyanspring.cstw.gui.command.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cyanspring.cstw.gui.SingleOrderStrategyView;

public class AuthMenuManager extends MenuManager{
	
	private static final Logger log = LoggerFactory
			.getLogger(AuthMenuManager.class);
	
	private static Map<String,List<AuthMenuManager>> menuMap = new HashMap<String,List<AuthMenuManager>>(); 
	
	private AuthMenuManager(){
	}
	
	public static AuthMenuManager newInstance(String partName){
		AuthMenuManager authMenuManager = new AuthMenuManager();
		
		if(menuMap.containsKey(partName)){
			List <AuthMenuManager>list = menuMap.get(partName);
			list.add(authMenuManager);
		}else{
			List <AuthMenuManager>list = new ArrayList<AuthMenuManager>();
			list.add(authMenuManager);
			menuMap.put(partName, list);
		}

		return authMenuManager;
	}
	
	public static List<ActionContributionItem>  getViewMenuActions(String partName){
		
		List<ActionContributionItem> list = new ArrayList<ActionContributionItem>();
		
		if(!StringUtils.hasText(partName) || null == menuMap.get(partName)){
			log.warn("this view doesn't maintain any menu actions:{}",partName);
			return list;
		}else{
			List<AuthMenuManager> tempList= menuMap.get(partName);
			for(AuthMenuManager amm : tempList){
				IContributionItem items []=  amm.getItems();
				for(IContributionItem item : items){
					ActionContributionItem actionItem = (ActionContributionItem)item;
					list.add(actionItem);
				}
			}
		}
		
		
		return list;
	}
	

}
