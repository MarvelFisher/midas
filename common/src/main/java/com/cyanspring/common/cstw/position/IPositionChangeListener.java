package com.cyanspring.common.cstw.position;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.OverallPosition;

public interface IPositionChangeListener {
	
	public void OverAllPositionChange(List<OverallPosition> allPositionList);
		
	public void OpenPositionChange(OpenPosition position);
	
	public void ClosedPositionChange(ClosedPosition position);
	
	public void notifyInitStatus(boolean sucess);
}
