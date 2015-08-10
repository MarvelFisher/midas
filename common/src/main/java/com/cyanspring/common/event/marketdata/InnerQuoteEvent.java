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
package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;

public final class InnerQuoteEvent extends RemoteAsyncEvent {
	QuoteEvent quoteEvent;
	private QuoteSource quoteSource;
	private String contributor;

	public InnerQuoteEvent(String key, String receiver, Quote quote, QuoteSource quoteSource, String contributor) {
		super(key, receiver);
		this.quoteEvent = new QuoteEvent(key, receiver, quote);
		this.quoteSource = quoteSource;
		this.contributor = contributor;
	}

	public Quote getQuote() {
		return quoteEvent.getQuote();
	}
	
	public QuoteEvent getQuoteEvent() {
		return quoteEvent;
	}

	public QuoteSource getQuoteSource() {
		return quoteSource;
	}

	public String getContributor() {
		return contributor;
	}
}
