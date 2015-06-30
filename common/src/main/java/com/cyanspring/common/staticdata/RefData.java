/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.staticdata;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.fx.FxUtils;

public class RefData extends DataObject {
	
	public RefData() {
		// default lot size to 1
		put(RefDataField.LOT_SIZE.value(), new Integer(1));
	}
	
	public double roundToLots(double qty) {
		int lotSize = this.getLotSize();
		if (qty > 0)
			return ((long)(qty/lotSize)) * lotSize;
		else
			return 0;
	}
	
	// getters
	public String getSymbol() {
		return this.get(String.class, RefDataField.SYMBOL.value());
	}
	
	public int getLotSize() {
		return this.get(int.class, RefDataField.LOT_SIZE.value());
	}
	
	public String getExchange() {
		return this.get(String.class, RefDataField.EXCHANGE.value());
	}
	
	public String getCurrency() {
		String cur = this.get(String.class, RefDataField.CURRENCY.value());
		if(null == cur && null != this.getExchange() && this.getExchange().equals("FX"))
			return FxUtils.getFromCurrency(this.getSymbol());
		return cur;
	}
	
	public String getFxCurrency() {
		String cur = this.get(String.class, RefDataField.FX_CURRENCY.value());
		if(null == cur && null != this.getExchange() && this.getExchange().equals("FX"))
			return FxUtils.getToCurrency(this.getSymbol());
		return cur;
	}
	
	public String getENDisplayName(){
		return this.get(String.class, RefDataField.EN_DISPLAYNAME.value());
	}
	
	public String getTWDisplayName(){
		return this.get(String.class, RefDataField.TW_DISPLAYNAME.value());
	}

	public String getCNDisplayName(){
		return this.get(String.class, RefDataField.CN_DISPLAYNAME.value());
	}
	
	public String getRefSymbol(){
		return this.get(String.class, RefDataField.REF_SYMBOL.value());
	}
	
	public String getType(){
		return this.get(String.class, RefDataField.TYPE.value());
	}
	
	public String getCategory(){
		return this.get(String.class, RefDataField.CATEGORY.value());
	}
	
	public String getTickTable(){
		return this.get(String.class, RefDataField.TICK_TABLE.value());
	}
	
	public String getSettlementDate(){
		return this.get(String.class, RefDataField.SETTLEMENT_DATE.value());
	}
	
	public double getPricePerUnit(){
		return this.get(double.class, RefDataField.PRICE_PER_UNIT.value());
	}
	
	public String getENTradingUnit(){
		return this.get(String.class, RefDataField.EN_TRADING_UNIT.value());
	}
	
	public String getTWTradingUnit(){
		return this.get(String.class, RefDataField.TW_TRADING_UNIT.value());
	}
	
	public String getCNTradingUnit(){
		return this.get(String.class, RefDataField.CN_TRADING_UNIT.value());
	}
	
	public double getMarginRate(){
		return this.get(double.class, RefDataField.MARGIN_RATE.value());
	}

    public void setMarginRate(double marginRate){
        this.set(marginRate, RefDataField.MARGIN_RATE.value());
    }
	public double getCommissionFee(){
		return this.get(double.class, RefDataField.COMMISSION_FEE.value());
	}
	
	public double getMinimalCommissionFee(){
		return this.get(double.class, RefDataField.MINIMAL_COMMISSION_FEE.value());
	}	
//	public int getMaximumLot(){
//		return this.get(int.class, RefDataField.MAXIMUM_LOT.value());
//	}
	public int getLimitMaximumLot(){
		return this.get(int.class, RefDataField.LIMIT_MAXIMUM_LOT.value());
	}
	public int getMarketMaximumLot(){
		return this.get(int.class, RefDataField.MARKET_MAXIMUM_LOT.value());
	}
	public int getMaximumHold(){
		return this.get(int.class, RefDataField.MAXIMUM_HOLD.value());
	}
	
	public double getPriceLimit(){
		return this.get(double.class, RefDataField.PRICE_LIMIT.value());
	}
	
	public int getDeciamlPoint(){
		return this.get(int.class, RefDataField.DECIMALPOINT.value());
	}
	
	public double getDenominator(){
		return this.get(double.class, RefDataField.DENOMINATOR.value());
	}
	
	public double getNumberatorDp(){
		return this.get(double.class, RefDataField.NUMERATOR_DP.value());
	}
	
	public String getTradable(){
		return this.get(String.class, RefDataField.TRADABLE.value());
	}
	
	public String getStrategy(){
		return this.get(String.class, RefDataField.STRATEGY.value());
	}
	
	public String getMarket(){
		return this.get(String.class, RefDataField.MARKET.value());
	}
	
	public String getSpellName(){
		return this.get(String.class, RefDataField.SPELL_NAME.value());
	}
		
	public String getCommodity(){
		return this.get(String.class, RefDataField.COMMODITY.value());
	}
	
	public String getDetailEN(){
		return this.get(String.class, RefDataField.DETAIL_EN.value());
	}
	
	public String getDetailTW(){
		return this.get(String.class, RefDataField.DETAIL_TW.value());
	}
	
	public String getDetailCN(){
		return this.get(String.class, RefDataField.DETAIL_CN.value());
	}
	
	public void setSymbol(String symbol){
		this.set(symbol, RefDataField.SYMBOL.value());
	}
	
	public void setENDisplayName(String enName){
		this.set(enName, RefDataField.EN_DISPLAYNAME.value());
	}
	
	public void setTWDisplayName(String twName){
		this.set(twName, RefDataField.TW_DISPLAYNAME.value());
	}
	
	public void setCNDisplayName(String cnName){
		this.set(cnName, RefDataField.CN_DISPLAYNAME.value());
	}
	
	public void setRefSymbol(String refSymbol){
		this.set(refSymbol, RefDataField.REF_SYMBOL.value());
	}
	
	public void setSettlementDate(String settlementDate){
		this.set(settlementDate, RefDataField.SETTLEMENT_DATE.value());
	}
	
	public void setSpellName(String spellName){
		this.set(spellName, RefDataField.SPELL_NAME.value());
	}
	
	public void setCommodity(String commodity){
		this.set(commodity, RefDataField.COMMODITY.value());
	}
	
	public void setDetailEN(String detailEN){
		this.set(detailEN, RefDataField.DETAIL_EN.value());
	}
	
	public void setDetailTW(String detailTW){
		this.set(detailTW, RefDataField.DETAIL_TW.value());
	}
	
	public void setDetailCN(String detailCN){
		this.set(detailCN, RefDataField.DETAIL_CN.value());
	}
}
