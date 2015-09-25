package com.cyanspring.server.account;

import webcurve.util.PriceUtils;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IType;

public class FITXCommissionManager extends CommissionManager {

	private double trxFee = 80;
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
		double accountCommission = 1;
        if (settings != null && !PriceUtils.isZero(settings.getCommission())) {
        	accountCommission = settings.getCommission();
		}

		if (refData != null) {
        	String type = refData.getIType();
        	if (type != null && (IType.FUTURES_CX.getValue().equals(type) || IType.FUTURES_IDX.getValue().equals(type))) {
				return super.getCommission(refData, settings, value, execution);
			}
        	double lotCommissionFee = refData.getLotCommissionFee();
			if (!nullOrZero(lotCommissionFee)) {
				double quantity = execution.getQuantity();
				return lotCommissionFee * quantity * accountCommission;
			}
        }

        double tax = value * trxTaxRate;
        tax = Math.round(tax);

        double defaultCommission = value * Default.getCommission();

        double commissionFee = trxFee + tax + defaultCommission;
        commissionFee *= accountCommission;

        return commissionFee;
	}

	private boolean nullOrZero(Double commission) {
		return commission == null || PriceUtils.isZero(commission);
	}

}
