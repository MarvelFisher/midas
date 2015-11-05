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
package com.cyanspring.cstw.ui.trader.command;

import com.cyanspring.cstw.ui.views.SpeedDepthViewPart;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/14
 *
 */
public class TraderSpeedDepthViewCommand extends BasicMenuItemCommand {

	public static String ID = "com.cyanspring.ltw.ui.command.stockpooldmin";

	@Override
	protected String getViewId() {
		return SpeedDepthViewPart.ID;
	}
	
	
	public boolean isMultiple() {
		return true;
	}

}
