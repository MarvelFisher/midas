package com.cyanspring.common.business;

import java.io.Serializable;
import java.util.Date;

public class CoinControl implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String AccountId;
	private Date checkPositionStopLossStart;
	private Date checkPositionStopLossEnd;
	private Date checkDailyStopLossStart;
	private Date checkDailyStopLossEnd;
	private Date checkTrailingStopStart;
	private Date checkTrailingStopEnd;
	private Date checkDayTradingModeStart;
	private Date checkDayTradingModeEnd;
	
	public static CoinControl createDefaultCoinControl(String accountId){
		CoinControl control = new CoinControl();
		control.setAccountId(accountId);
		control.setCheckPositionStopLossStart(null);
		control.setCheckPositionStopLossEnd(null);
		control.setCheckDailyStopLossStart(null);
		control.setCheckDailyStopLossEnd(null);
		control.setCheckTrailingStopStart(null);
		control.setCheckTrailingStopEnd(null);
		control.setCheckDayTradingModeStart(null);
		control.setCheckDayTradingModeEnd(null);
		return control;
	}
	
	public boolean canCheckPositionStopLoss(){
					
		if(inPermissionInterval(getCheckPositionStopLossStart(),getCheckPositionStopLossEnd()))
			return true;

		return false;
	}
	
	public boolean canCheckDailyStopLoss(){
			
		if(inPermissionInterval(getCheckDailyStopLossStart(),getCheckDailyStopLossEnd()))
				return true;
		
		return false;
	}
	
	public boolean canCheckTrailingStop(){
		
		if(inPermissionInterval(getCheckTrailingStopStart(),getCheckTrailingStopEnd()))
				return true;

		return false;
	}
	
	public boolean canCheckDayTradingMode(){
		
		if(inPermissionInterval(getCheckDayTradingModeStart(),getCheckDayTradingModeEnd()))
				return true;

		return false;
	}
	
	private boolean inPermissionInterval(Date start,
			Date end) {
		
		if(null == start || null == end)
			return false;
		
		Date now = new Date();
		if(start.getTime() <= now.getTime() && end.getTime() >= now.getTime()){
			return true;
		}
		return false;
	}

	public String getAccountId() {
		return AccountId;
	}
	
	public void setAccountId(String accountId) {
		AccountId = accountId;
	}

	public Date getCheckPositionStopLossStart() {
		return checkPositionStopLossStart;
	}
	
	public void setCheckPositionStopLossStart(Date checkPositionStopLossStart) {
		this.checkPositionStopLossStart = checkPositionStopLossStart;
	}
	
	public Date getCheckPositionStopLossEnd() {
		return checkPositionStopLossEnd;
	}
	
	public void setCheckPositionStopLossEnd(Date checkPositionStopLossEnd) {
		this.checkPositionStopLossEnd = checkPositionStopLossEnd;
	}

	public Date getCheckDailyStopLossStart() {
		return checkDailyStopLossStart;
	}
	
	public void setCheckDailyStopLossStart(Date checkDailyStopLossStart) {
		this.checkDailyStopLossStart = checkDailyStopLossStart;
	}
	
	public Date getCheckDailyStopLossEnd() {
		return checkDailyStopLossEnd;
	}
	
	public void setCheckDailyStopLossEnd(Date checkDailyStopLossEnd) {
		this.checkDailyStopLossEnd = checkDailyStopLossEnd;
	}

	public Date getCheckTrailingStopStart() {
		return checkTrailingStopStart;
	}
	
	public void setCheckTrailingStopStart(Date checkTrailingStopStart) {
		this.checkTrailingStopStart = checkTrailingStopStart;
	}
	
	public Date getCheckTrailingStopEnd() {
		return checkTrailingStopEnd;
	}
	
	public void setCheckTrailingStopEnd(Date checkTrailingStopEnd) {
		this.checkTrailingStopEnd = checkTrailingStopEnd;
	}

	public Date getCheckDayTradingModeStart() {
		return checkDayTradingModeStart;
	}

	public void setCheckDayTradingModeStart(Date checkDayTradingModeStart) {
		this.checkDayTradingModeStart = checkDayTradingModeStart;
	}

	public Date getCheckDayTradingModeEnd() {
		return checkDayTradingModeEnd;
	}

	public void setCheckDayTradingModeEnd(Date checkDayTradingModeEnd) {
		this.checkDayTradingModeEnd = checkDayTradingModeEnd;
	}
}
