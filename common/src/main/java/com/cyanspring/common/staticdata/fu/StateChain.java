package com.cyanspring.common.staticdata.fu;

public class StateChain {
	private StateChain nextState;
	private int state;
	
	public StateChain(int month){
		this.state = month;
	}
	
	public int nowState(){
		return state;
	}

    public int nextState(){
        return nextState.nowState();
    }
	
	public StateChain getNextState(){
		return nextState;
	}
	
	public void addState(StateChain state){
		if(nextState == null)
			nextState = state;
		else
			nextState.addState(state);
	}
}
