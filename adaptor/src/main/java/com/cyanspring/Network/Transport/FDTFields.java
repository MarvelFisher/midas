package com.cyanspring.Network.Transport;

public class FDTFields {
	
	// Packet Relative Fields
	public final static int PacketType = 1;	
	public final static int PacketArray = 2001;
	public final static int Heartbeat = 5;
	public final static int SerialNumber = 2;
	public final static int WindCodeTable = 2013;
	public final static int WindMarketData = 2015;
	public final static int WindIndexData = 2016;
	public final static int WindFutureData = 2017;
	public final static int WindTransaction = 2019;
	public final static int WindMarkets = 2021;
	public final static int WindQuotationDateChange = 2022;
	public final static int WindMarketClose = 20023;


	public final static int ArrayOfString = 2501;
	public final static int ArrayOfPacket = 2502;
	
	
	// Quote Relative Fields
	public final static int WindSymbolCode = 55;
	public final static int ShortName = 20006;
	public final static int ActionDay = 51;
	public final static int AskPriceArray = 20020;
	public final static int AskVolumeArray = 20021;
	public final static int BidPriceArray = 20018;
	public final static int BidVolumeArray = 20019;
	public final static int Open = 1025;
	public final static int High = 332;
	public final static int HighLimit = 1049;
	public final static int Low = 333;
	public final static int LowLimit = 1048;
	public final static int Last = 31;
	public final static int Close = 20001;
	public final static int PreClose = 140;
	public final static int Status = 20011;
	public final static int Time = 273;
	public final static int TradingDay = 75;
	public final static int Turnover = 20013;
	public final static int Volume = 14;
	public final static int NumberOfTrades = 20022;
	public final static int TotalBidVolume = 20023;
	public final static int TotalAskVolume = 20024;
	public final static int SecurityType = 167;    
	public final static int SecurityExchange = 207;	
	public final static int OpenInterest = 20014;
	public final static int SettlePrice = 20015;
	public final static int PreSettlePrice = 20012;
	public final static int LastTradingDay = 20036;
	public final static int Information = 20037;
	public final static int IndexNumber = 20038;
	public final static int BuySellFlag = 20039;
	public final static int BuyVolume = 20040;
	public final static int BuyTurnover = 20041;
	public final static int SellVolume = 20042;
	public final static int SellTurnover = 20043;
	public final static int UnclassifiedVolume = 20044;
	public final static int UnclassifiedTurnover = 20045;
	public final static int WgtAvgAskPrice = 20046;
	public final static int WgtAvgBidPrice = 20047;
	public final static int YieldToMaturity = 20048;
	public final static int Prefix = 20049;
	public final static int Syl1 = 20050;
	public final static int Syl2 = 20051;
	public final static int SD2 = 20052;
}
