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
package com.cyanspring.sample.singleorder.sdma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class SDMAStrategy extends SingleOrderStrategy {
	private static final Logger log = LoggerFactory
		.getLogger(SDMAStrategy.class);
	
	@Override
	protected Logger getLog() {
		return log;
	}

	@Override
	protected void processQuoteEvent(QuoteEvent event) {
		// reject market order if there is no marketable price
		if(parentOrder.getOrderType().equals(OrderType.Market) &&
			null ==	this.pendingExecInstrEvent &&	
			!parentOrder.getState().equals(StrategyState.Terminated)) {
			double marketablePrice = QuoteUtils.getMarketablePrice(event.getQuote(), parentOrder.getSide());
			if(!PriceUtils.validPrice(marketablePrice)) {
				log.debug("Rejecting market order since there is no marketable price: "
							+ parentOrder.getId() + ", " + event.getQuote());
				parentOrder.setOrdStatus(OrdStatus.REJECTED);
				String txId = parentOrder.get(String.class, OrderField.CLORDERID.value());
				container.sendEvent(new UpdateParentOrderEvent(parentOrder.getId(), ExecType.REJECTED, txId, parentOrder, null));
				terminate();
				return;
			}
		}
		super.processQuoteEvent(event);
	}

	@Override
	public void init() throws StrategyException {
		if(this.isSimMarketOrder())
			setQuoteRequired(true);
		else
			setQuoteRequired(false);
			
		setTimerEventRequired(false);
		super.init();
	}
	

}
