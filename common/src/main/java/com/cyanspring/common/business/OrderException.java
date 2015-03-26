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
package com.cyanspring.common.business;

import com.cyanspring.common.message.ErrorMessage;

public class OrderException extends Exception {
	private static final long serialVersionUID = 8892272831263712497L;
	private ErrorMessage clientMessage;

	public OrderException(String message) {
		super(message);
	}
	public OrderException(String localMessage,ErrorMessage clientMessage) {
		super(localMessage);
		this.clientMessage = clientMessage;
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
