package com.cyanspring.server.account;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class FITXCommissionManager extends CommissionManager {

	private final double TRX_FEE = 12;
	private final double TRX_TAX_RATE = 0.00002;

	@Override
	public double getCommission(RefData refData, AccountSetting settings, double value, Execution execution) {
		if (refData != null) {
        	String type = refData.getIType();
        	if (type != null && (IType.FUTURES_CX.getValue().equals(type) || IType.FUTURES_IDX.getValue().equals(type))) {
				return super.getCommission(refData, settings, value, execution);
			}
        }

    	double accountCom = 1;
        if (settings != null && !PriceUtils.isZero(settings.getCommission())) {
			accountCom = settings.getCommission();
		}

        double quantity = execution.getQuantity();
        double tax = value * TRX_TAX_RATE;
        tax = Math.round(tax);

        double commissionFee = (TRX_FEE * quantity) + tax;
        commissionFee *= accountCom;

        return commissionFee;
	}

}
