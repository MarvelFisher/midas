package com.cyanspring.cstw.service.eventadapter.riskcontrol.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.OverallPosition;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel.RCPositionDirection;
import com.cyanspring.cstw.model.riskmgr.RCOpenPositionModel.RCPositionType;
import com.cyanspring.cstw.service.eventadapter.riskcontrol.IRCOpenPositionEventAdapter;
import com.cyanspring.cstw.service.helper.transfer.ModelTransfer;
import com.cyanspring.cstw.service.localevent.riskmgr.caching.BasicRCPositionUpdateCachingLocalEvent;
/**
 * @author Yu-Junfeng
 * @create 14 Sep 2015
 */
public final class BRCOpenPositionEventAdapterImpl implements
		IRCOpenPositionEventAdapter {

	@Override
	public List<RCOpenPositionModel> getOpenPositionModelListByEvent(
			BasicRCPositionUpdateCachingLocalEvent event) {
		List<RCOpenPositionModel> result = new ArrayList<RCOpenPositionModel>();
		Map<String, Map<String, OverallPosition>> accountPositionMap = event
				.getAccountPositionMap();
		if (accountPositionMap == null) {
			return result;
		}
		// re-organise position map
		// sub-account -> symbol -> position
		Map<String, Map<String, RCOpenPositionModel>> adjustOpenPositionMap = new HashMap<String, Map<String, RCOpenPositionModel>>();
		Map<String, Map<String, RCOpenPositionModel>> adjustClosePositionMap = new HashMap<String, Map<String, RCOpenPositionModel>>();
		for (Map<String, OverallPosition> accountCollection : accountPositionMap
				.values()) {
			for (OverallPosition position : accountCollection.values()) {
				RCOpenPositionModel model = ModelTransfer
						.parseCurrentPositionRecord(position);
				if (model.getType() == RCPositionType.Open) {
					addPosition2Map(model, adjustOpenPositionMap,
							RCPositionType.Open);
				} else {
					addPosition2Map(model, adjustClosePositionMap,
							RCPositionType.Close);
				}
			}
		}

		// generate result
		for (Map<String, RCOpenPositionModel> map : adjustOpenPositionMap
				.values()) {
			for (RCOpenPositionModel m : map.values()) {
				result.add(m);
			}
		}
		for (Map<String, RCOpenPositionModel> map : adjustClosePositionMap
				.values()) {
			for (RCOpenPositionModel m : map.values()) {
				result.add(m);
			}
		}
		return result;
	}

	private void addPosition2Map(RCOpenPositionModel position,
			Map<String, Map<String, RCOpenPositionModel>> adjustMap,
			RCPositionType type) {
		String subAccount = position.getSubAccount();
		String symbol = position.getInstrumentCode();
		if (!adjustMap.containsKey(subAccount)) {
			Map<String, RCOpenPositionModel> symbolMap = new HashMap<String, RCOpenPositionModel>();
			symbolMap.put(symbol, position);
			adjustMap.put(subAccount, symbolMap);
		} else {
			Map<String, RCOpenPositionModel> symbolMap = adjustMap
					.get(subAccount);
			if (!symbolMap.containsKey(symbol)) {
				symbolMap.put(symbol, position);
			} else {
				RCOpenPositionModel existPosition = symbolMap.get(symbol);
				RCOpenPositionModel newPosition = mergePosition(existPosition,
						position, type);
				symbolMap.put(symbol, newPosition);
			}
		}

	}

	private RCOpenPositionModel mergePosition(RCOpenPositionModel exist,
			RCOpenPositionModel position, RCPositionType type) {

		double quality = exist.getPositionDirection() == RCPositionDirection.Long ? exist
				.getInstrumentQuality() : 0 - exist.getInstrumentQuality();
		double urPnl = exist.getUrPnl();
		double pnl = exist.getPnl();
		double avgPrice = 0.0;
		RCPositionDirection direction = exist.getPositionDirection();

		quality += position.getPositionDirection() == RCPositionDirection.Long ? position
				.getInstrumentQuality() : 0 - position.getInstrumentQuality();
		urPnl += position.getUrPnl();
		pnl += position.getPnl();
		if (exist.getPositionDirection() == position.getPositionDirection()) {
			avgPrice = (exist.getAveragePrice() * exist.getInstrumentQuality() + position
					.getAveragePrice() * position.getInstrumentQuality())
					/ (exist.getInstrumentQuality() + position
							.getInstrumentQuality());
			direction = exist.getPositionDirection();
		} else {
			if (PriceUtils.GreaterThan(exist.getInstrumentQuality(),
					position.getInstrumentQuality())) {
				avgPrice = exist.getAveragePrice();
				direction = exist.getPositionDirection();
			} else {
				avgPrice = position.getAveragePrice();
				direction = position.getPositionDirection();
			}
		}

		RCOpenPositionModel model = new RCOpenPositionModel.Builder()
				.id(exist.getId()).subAccount(exist.getSubAccount())
				.instrumentCode(exist.getInstrumentCode())
				.instrumentName(exist.getInstrumentName()).type(type)
				.quality(quality).urPnl(urPnl).pnl(pnl).avgPrice(avgPrice)
				.direction(direction).build();

		return model;
	}

}
