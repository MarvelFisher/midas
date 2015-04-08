package com.cyanspring.common.message;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageLookupTest {

	@Test
	public void testGetMsgBeanFromEventMessage() {
		String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "account not exist");
		MessageBean bean = MessageLookup.getMsgBeanFromEventMessage(msg);
		
		assertEquals(406, bean.getCode());
		assertEquals("account doesn't exist", bean.getMsg());
		assertEquals("account not exist", bean.getLocalMsg() );
		
	}

	@Test
	public void testBuildEventMessage() {
		String msg = MessageLookup.buildEventMessage(ErrorMessage.ACCOUNT_NOT_EXIST, "account not exist");
		assertNotNull(msg);
		String msgs[] = msg.split("\\|"+"&"+"\\|");
		assertEquals(3, msgs.length);
		assertEquals("406", msgs[0]);
		assertEquals("account doesn't exist", msgs[1]);
		assertEquals("account not exist", msgs[2] );


		
	}
	@Test
	public void testBuildEventMessageWithCode() {
		String msg = MessageLookup.buildEventMessageWithCode(ErrorMessage.ACCOUNT_NOT_EXIST, "account not exist");
		assertNotNull(msg);
		String msgs[] = msg.split("\\|"+"&"+"\\|");
		assertEquals(3, msgs.length);
		assertEquals("406", msgs[0]);
		assertEquals("account not exist", msgs[1]);
		assertEquals("account not exist", msgs[2] );
		System.out.println("msg:"+msgs[2]+msg);

		
	}
	@Test
	public void testLookup() {
		MessageBean bean = MessageLookup.lookup(ErrorMessage.ACCOUNT_NOT_EXIST);
		assertNotNull(bean);
		assertNotNull(bean.getCode());
		assertNotNull(bean.getMsg());
	}

}
