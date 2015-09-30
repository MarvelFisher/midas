package com.cyanspring.info.ne.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptHelper
{
	final private static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	public String sha1Hash(MessageDigest md, String iptString) throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException
	{
		md.update(iptString.getBytes());
		return bytesToHex(md.digest());
	}

	public String aesEncrypt(String iptString, byte keyBytes[], byte ivBytes[], Cipher cipher)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException
	{
		SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
		byte[] encryptData = cipher.doFinal(iptString.getBytes());
		return bytesToHex(encryptData);
	}

	public String aesDecrypt(String iptString, byte keyBytes[], byte ivBytes[], Cipher cipher) throws Exception
	{
		byte[] input = hexToByte(iptString);
		final SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
		byte[] decryptData = cipher.doFinal(input);
		return new String(decryptData, "UTF-8");
	}

	public String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public byte[] hexToByte(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public String genRandomStr(int length)
	{
		String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int chartLen = chars.length() - 1;
		StringBuffer sb = new StringBuffer();
		for (int i = length; i > 0; --i)
		{
			sb.append(chars.charAt((int) (Math.round(Math.random() * (chartLen)))));
		}
		return sb.toString();
	}

}
