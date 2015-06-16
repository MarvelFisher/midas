package com.cyanspring.adaptor.future.ctp.trader.generated;
import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Array;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * \u54cd\u5e94\u4fe1\u606f<br>
 * <i>native declaration : ThostFtdcUserApiStruct.h:297</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("Trader") 
public class CThostFtdcRspInfoField extends StructObject {
	static {
		BridJ.register();
	}
	/** C type : TThostFtdcErrorIDType */
	@Field(0) 
	public int ErrorID() {
		return this.io.getIntField(this, 0);
	}
	/** C type : TThostFtdcErrorIDType */
	@Field(0) 
	public CThostFtdcRspInfoField ErrorID(int ErrorID) {
		this.io.setIntField(this, 0, ErrorID);
		return this;
	}
	/** C type : TThostFtdcErrorMsgType */
	@Array({81}) 
	@Field(1) 
	public Pointer<Byte > ErrorMsg() {
		return this.io.getPointerField(this, 1);
	}
	public CThostFtdcRspInfoField() {
		super();
	}
	public CThostFtdcRspInfoField(Pointer pointer) {
		super(pointer);
	}
}