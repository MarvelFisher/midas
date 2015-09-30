package com.cyanspring.info.ne;

import java.security.MessageDigest;
import java.util.HashMap;

import javax.crypto.Cipher;

import com.cyanspring.info.ne.util.EncryptHelper;
import com.cyanspring.info.ne.util.HttpHelper;

import org.json.JSONObject;

import com.google.common.base.Strings;

public class NetEaseClient
{
	final String m_sUri;
	final String m_sAppKey;
	final String m_sAppSecret;
	final String m_sTokenSalt;
	final String m_sIV;

	final byte[] m_bTokenBytes;
	final byte[] m_bIVBytes;

	final static String TEXT_MSG_TYPE = "0";
	final static String JPG_MSG_TYPE = "1";
	final static String VOICE_MSG_TYPE = "2";
	final static String VEDIO_MSG_TYPE = "3";
	final static String GEO_MSG_TYPE = "4";
	final static String THIRDPARTY_MSG_TYPE = "100";

	final static String ACCID = "accid";
	final static String NAME = "name";
	final static String TOKEN = "token";
	final static String ICON = "icon";
	final static String PROPS = "props";

	final static String APPKEY = "AppKey";
	final static String NONCE = "Nonce";
	final static String CURTIME = "CurTime";
	final static String CHECKSUM = "CheckSum";

	final static String FROM = "from";
	final static String OPE = "ope";
	final static String TO = "to";
	final static String TYPE = "type";
	final static String BODY = "body";
	final static String ATTACH = "attach";
	final static String PUSHCONTENT = "pushcontent";
	final static String PAYLOAD = "payload";

	final static String MSGTYPE = "msgtype";
	final static String SEND_MSG_OPE = "0";

	final private Cipher m_aesCipher;

	private StringBuffer m_sb = new StringBuffer();

	private MessageDigest m_crypt = MessageDigest.getInstance("SHA-1");
	private EncryptHelper m_encryptHelper = new EncryptHelper();
	final private String m_sNonce = m_encryptHelper.genRandomStr(10);

	final private static String USER_CREATE = "user/create.action";
	final private static String USER_UPDATE = "user/update.action";
	final private static String CHECK_ONLINE = "user/checkOnline.action";
	final private static String REFRESH_TOKEN = "user/refreshToken.action";
	final private static String BLOCK = "user/block.action";
	final private static String UNBLOCK = "user/unblock.action";
	final private static String SENDMSG = "msg/sendMsg.action";
	final private static String SENDATTACHMSG = "msg/sendAttachMsg.action";

	final private String USER_CREATE_URI;
	final private String USER_UPDATE_URI;
	final private String CHECK_ONLINE_URI;
	final private String REFRESH_TOKEN_URI;
	final private String BLOCK_URI;
	final private String UNBLOCK_URI;
	final private String SENDMSG_URI;
	final private String SENDATTACHMSG_URI;

	public NetEaseClient(String uri, String appKey, String appSecret, String tokenSalt, String iv) throws Exception
	{
		m_sUri = uri;
		m_sAppKey = appKey;
		m_sAppSecret = appSecret;
		m_sTokenSalt = tokenSalt;
		m_sIV = iv;

		m_aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		m_bTokenBytes = m_sTokenSalt.getBytes();
		m_bIVBytes = m_sIV.getBytes();

		USER_CREATE_URI = m_sUri + USER_CREATE;
		USER_UPDATE_URI = m_sUri + USER_UPDATE;
		CHECK_ONLINE_URI = m_sUri + CHECK_ONLINE;
		REFRESH_TOKEN_URI = m_sUri + REFRESH_TOKEN;
		BLOCK_URI = m_sUri + BLOCK;
		UNBLOCK_URI = m_sUri + UNBLOCK;
		SENDMSG_URI = m_sUri + SENDMSG;
		SENDATTACHMSG_URI = m_sUri + SENDATTACHMSG;

	}

	public String createAccount(String userID, String name, String props, String icon) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String token = m_encryptHelper.aesEncrypt(userID, m_bTokenBytes, m_bIVBytes, m_aesCipher);
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();

		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);

		forms.put(ACCID, accid);
		forms.put(NAME, name);
		forms.put(TOKEN, token);
		if (!Strings.isNullOrEmpty(icon))
			forms.put(ICON, icon);
		if (!Strings.isNullOrEmpty(props))
			forms.put(PROPS, props);

		return HttpHelper.post(USER_CREATE_URI, headers, forms);

	}

	public String updateAccount(String userID, String name, String props, String icon) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String token = m_encryptHelper.aesEncrypt(userID, m_bTokenBytes, m_bIVBytes, m_aesCipher);
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();

		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);

		forms.put(ACCID, accid);
		forms.put(NAME, name);
		forms.put(TOKEN, token);
		if (!Strings.isNullOrEmpty(icon))
			forms.put(ICON, icon);
		if (!Strings.isNullOrEmpty(props))
			forms.put(PROPS, props);

		return HttpHelper.post(USER_UPDATE_URI, headers, forms);

	}

	public String checkOnline(String userID) throws Exception

	{

		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(ACCID, accid);

		return HttpHelper.post(CHECK_ONLINE_URI, headers, forms);
	}

	public String refreshToken(String userID) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(ACCID, accid);

		return HttpHelper.post(REFRESH_TOKEN_URI, headers, forms);
	}

	public String block(String userID) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(ACCID, accid);

		return HttpHelper.post(BLOCK_URI, headers, forms);
	}

	public String unblock(String userID) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = userID;
		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(ACCID, accid);

		return HttpHelper.post(UNBLOCK_URI, headers, forms);
	}

	public String sendTextMsg(String fromID, String toID, JSONObject body) throws Exception
	{
		return sendMsg(fromID, toID, TEXT_MSG_TYPE, body);
	}

	public String sendJpgMsg(String fromID, String toID, JSONObject body) throws Exception
	{
		return sendMsg(fromID, toID, JPG_MSG_TYPE, body);
	}

	public String sendVoiceMsg(String fromID, String toID, JSONObject body) throws Exception
	{
		return sendMsg(fromID, toID, VOICE_MSG_TYPE, body);
	}

	public String sendVedioMsg(String fromID, String toID, JSONObject body) throws Exception
	{
		return sendMsg(fromID, toID, VEDIO_MSG_TYPE, body);
	}

	public String sendGeoMsg(String fromID, String toID, JSONObject body) throws Exception
	{

		return sendMsg(fromID, toID, GEO_MSG_TYPE, body);
	}

	public String sendThirdPartyMsg(String fromID, String toID, JSONObject body) throws Exception
	{

		return sendMsg(fromID, toID, THIRDPARTY_MSG_TYPE, body);
	}

	public String sendAttachMsg(String fromID, String toID, String attach, String pushcontent, String payload)
			throws Exception
	{

		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = fromID;
		String accidToID = toID;

		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(FROM, accid);
		forms.put(MSGTYPE, SEND_MSG_OPE);
		forms.put(TO, accidToID);
		forms.put(ATTACH, attach);

		if (!Strings.isNullOrEmpty(pushcontent))
			forms.put(PUSHCONTENT, pushcontent);
		if (!Strings.isNullOrEmpty(payload))
			forms.put(PAYLOAD, payload);

		return HttpHelper.post(SENDATTACHMSG_URI, headers, forms);
	}

	private String sendMsg(String fromID, String toID, String type, JSONObject body) throws Exception
	{
		String currTm = String.valueOf(System.currentTimeMillis());
		String accid = fromID;
		String accidToID = toID;

		String checkSum = m_encryptHelper.sha1Hash(m_crypt, genVerifyStr(m_sAppSecret, m_sNonce, currTm, m_sb));

		HashMap<String, String> forms = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		genHeaders(headers, m_sAppKey, m_sNonce, currTm, checkSum);
		forms.put(FROM, accid);
		forms.put(OPE, SEND_MSG_OPE);
		forms.put(TO, accidToID);
		forms.put(TYPE, type);
		forms.put(BODY, body.toString());
		return HttpHelper.post(SENDMSG_URI, headers, forms);
	}

	private HashMap<String, String> genHeaders(HashMap<String, String> map, String appKey, String nonce, String curTm,
			String checkSum)
	{
		map.put(APPKEY, appKey);
		map.put(NONCE, nonce);
		map.put(CURTIME, curTm);
		map.put(CHECKSUM, checkSum);

		return map;
	}

	private String genVerifyStr(String appSecret, String nonce, String currTm, StringBuffer sb)
	{
		sb.setLength(0);
		sb.append(appSecret).append(nonce).append(currTm);
		return sb.toString();
	}

}
