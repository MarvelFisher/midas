package com.cyanspring.common.staticdata.fu;


public class SeasonState {
	private SeasonState nextState;
	private int season;
	
	public SeasonState(int month){
		this.season = month;
	}
	
	public int nextState(){
		season = nextState.nowState();
		nextState = nextState.getNexState();
		return season;
	}
	
	public int nowState(){
		return season;
	}
	
	public SeasonState getNexState(){
		return nextState;
	}
	
	public void addState(SeasonState state){
		if(nextState == null)
			nextState = state;
		else
			nextState.addState(state);
	}
	
	public int searchNearestSeason(int month, int depth){
		int i = 0;
        while (month >= this.nextState() && i < depth){
        	i++;
        }
        
        if(i != depth)
        	return this.season;
        else
        	return 0;
	}
}
