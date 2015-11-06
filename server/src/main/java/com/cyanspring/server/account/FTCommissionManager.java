package com.cyanspring.server.account;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;

public class FTCommissionManager extends CommissionManager {

	private double trxTaxRate = 0.00002;

	public double getTrxTaxRate() {
		return trxTaxRate;
	}

	public void setTrxTaxRate(double trxTaxRate) {
		this.trxTaxRate = trxTaxRate;
	}

	@Override
	public double getCommission(RefData refData, AccountSetting settings, double value, Execution execution) {
		// Currently FT only set Lot CF, call super to get Lot CF (already applied accountCommission)
		double lotCF = super.getCommission(refData, settings, value, execution);

		double accountCommission = 1;
		if (settings != null && !PriceUtils.isZero(settings.getCommission())) {
			accountCommission = settings.getCommission();
		}

        double tax = value * trxTaxRate;
        tax = Math.round(tax * accountCommission);

        double commissionFee = tax + lotCF;

        return Math.ceil(commissionFee);
	}

}
