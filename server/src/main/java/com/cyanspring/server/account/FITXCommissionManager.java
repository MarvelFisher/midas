package com.cyanspring.server.account;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class FITXCommissionManager extends CommissionManager {

	private double trxFee = 12;
	private double trxTaxRate = 0.00002;

	public double getTrxFee() {
		return trxFee;
	}

	public void setTrxFee(double trxFee) {
		this.trxFee = trxFee;
	}

	public double getTrxTaxRate() {
		return trxTaxRate;
	}

	public void setTrxTaxRate(double trxTaxRate) {
		this.trxTaxRate = trxTaxRate;
	}

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
        double tax = value * trxTaxRate;
        tax = Math.round(tax);

        double defaultCommission = value * Default.getCommission();

        double commissionFee = (trxFee * quantity) + tax + defaultCommission;
        commissionFee *= accountCom;

        return commissionFee;
	}

}
