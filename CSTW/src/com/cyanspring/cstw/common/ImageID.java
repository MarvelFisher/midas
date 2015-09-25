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
package com.cyanspring.cstw.common;

public enum ImageID {
	FILTER_ICON ("icons/filter.png"),
	EDIT_ICON("icons/gear.png"),
	NONE_EDIT_ICON("icons/gears.png"),
	TRUE_ICON("icons/true.png"),
	FALSE_ICON("icons/false.png"),
	PLUS_ICON("icons/plus.png"),
	AMEND_ICON("icons/amend.png"),
	PAUSE_ICON("icons/pause.png"),
	STOP_ICON("icons/stop.png"),
	START_ICON("icons/start.png"),
	PIN_ICON("icons/pin.png"),
	CANCEL_ICON("icons/cancel.png"),
	SAVE_ICON("icons/save.png"),
	ACTIVE_ICON("icons/active.png"),
	REFRESH_ICON("icons/refresh.png"),
	LINE_ICON("icons/lineIcon.png"),
	PEOPLE_ICON("icons/people.png"),
	FORWARD_ICON("icons/forward_nav.png"),
	BACKWARD_ICON("icons/backward_nav.png"),
	ROLE_ICON("icons/role.png"),
	FREZZE_ICON("icons/frezze.png"),
	AMEND_OPTIONS_ICON("icons/amendOptions.png"),
	MONEY_ICON("icons/money.png"),
	LOGIN_BG("icons/loginBg.png"),
	SKULL_ICON("icons/skull.png"),
	ALERT_ICON("icons/alert.png"),
	ORDER_CLOSE_ICON("icons/orderclose.png"),
	MANUAL_CLOSE_ICON("icons/manualclose.png"),
	STOP_PROGRESS_ICON("icons/stopProgress.png"),
	MANUAL_PRICE_ICON("icons/manualPrice.png"),
	USER_ICON("icons/user.png"),
	POWER_ICON("icons/power.png"),
	APP_ICON("icons/appIcon16.png"),
	ACCOUNT_ICON("icons/account.png"),
	ORDER_ICON("icons/order.png"),
	QUOTE_ICON("icons/quote.png"),
	MARKET_DATA_ICON("icons/marketData.png"),
	PROPERTY_ICON("icons/property.png")
	;
	private String value;
	ImageID(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}

}
