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
package com.cyanspring.server.sim;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.stream.IStreamAdaptor;

public class HyperDownStreamAdaptor implements IStreamAdaptor<IDownStreamConnection> {
	@Autowired
	private HyperDownStreamConnection hyperDownStreamConnection;
	
	private List<IDownStreamConnection> connections = new ArrayList<IDownStreamConnection>();
	
	public HyperDownStreamAdaptor() {
	}
	@Override
	public List<IDownStreamConnection> getConnections() {
		return connections;
	}
	public int getNumberOfConnections() {
		return connections.size();
	}
	public void setNumberOfConnections(int numberOfConnections) {
	}
	@Override
	public void init() throws Exception {
		connections.add(hyperDownStreamConnection);
		hyperDownStreamConnection.init();
	}

	@Override
	public void uninit() {
		hyperDownStreamConnection.uninit();
	}
}
