package com.cyanspring.server.account;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataBitUtil;
import com.cyanspring.common.staticdata.fu.IType;
import com.cyanspring.common.type.OrderSide;

import webcurve.util.PriceUtils;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class StockCommissionManager extends CommissionManager {

    private double stampTax = 0.001;
    private double brokerage = 0.0000696;
    private double secFee = 0.00002;
    private double commission = 0.0002;
    private double transferFee = 0.0001;

    @Override
    public double getCommission(RefData refData, AccountSetting settings, double value, Execution execution) {
    	if (refData != null) {
        	if (!RefDataBitUtil.isStock(refData.getInstrumentType())) {
				return super.getCommission(refData, settings, value, execution);
			}
        }
    	double accountCom = 1;
        if (settings != null && !PriceUtils.isZero(settings.getCommission())) {
			accountCom = settings.getCommission();
		}

        OrderSide side = execution.getSide();
        if (side == null) {
			return Default.getCommission() * value * accountCom;
		}

        double stampValue = 0.0;
        if (side.equals(OrderSide.Sell)) {
            stampValue = Math.ceil(stampTax * value * accountCom);
        }
        double brokerageValue = Math.ceil(brokerage * value * accountCom);
        double secValue = Math.ceil(secFee * value * accountCom);
        double commissionValue = Math.ceil(commission * value * accountCom);
        if (PriceUtils.LessThan(commissionValue, 5)) {
			commissionValue = 5;
		}
        double transferValue = Math.ceil(transferFee * value * accountCom);

        return stampValue + brokerageValue + secValue + commissionValue + transferValue;
    }

    @Override
    public double getCommission(RefData refData, AccountSetting settings, Execution execution) {
    	if (refData != null) {
        	if (!RefDataBitUtil.isStock(refData.getInstrumentType())) {
				return super.getCommission(refData, settings, execution);
			}
        }
        double accountCom = 1;
        if (settings != null && !PriceUtils.isZero(settings.getCommission())) {
			accountCom = settings.getCommission();
		}

        OrderSide side = execution.getSide();
        if (side == null) {
			return Default.getCommission() * accountCom;
		}
        double totalCom = brokerage + secFee + commission + transferFee;
        if (side.equals(OrderSide.Sell)) {
			totalCom += stampTax;
		}
        return totalCom * accountCom;
    }

    public void setStampTax(double stampTax) {
        this.stampTax = stampTax;
    }

    public void setBrokerage(double brokerage) {
        this.brokerage = brokerage;
    }

    public void setSecFee(double secFee) {
        this.secFee = secFee;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public void setTransferFee(double transferFee) {
        this.transferFee = transferFee;
    }
}
