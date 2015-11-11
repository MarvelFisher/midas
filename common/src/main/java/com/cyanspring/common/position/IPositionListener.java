package com.cyanspring.common.position;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;

public interface IPositionListener {
	void onRemoveDetailOpenPosition(OpenPosition position);
	void onUpdateDetailOpenPosition(OpenPosition position);
	void onOpenPositionUpdate(OpenPosition position);
	void onOpenPositionDynamiceUpdate(OpenPosition position);
	void onClosedPositionUpdate(ClosedPosition position);
	void onAccountUpdate(Account account);
	void onAccountDynamicUpdate(Account account);
}
