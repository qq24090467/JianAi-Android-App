package com.vibexie.jianai.Utils.BlowfishUtils;

/**
 * Blowfish加密需要实现的接口，可以参看BlowfishUtil的说明
 * Title  : Encryptor.java 
 * Company: ZhenBot
 * Author : Vibe Xie  @www.vibexie.com
 * Time   : Apr 19, 2015 3:05:32 PM
 * Copyright: Copyright (c) 2015
 * Description:
 */
public interface Encryptor {

	/**
	 * Encrypt a clear text String. 
	 *
	 * @param value The clear text attribute
	 * @return The encrypted attribute, or null
	 */
	public abstract String encrypt(String value);

	/**
	 * Decrypt an encrypted String. 
	 *
	 * @param value The encrypted attribute in Base64 encoding
	 * @return The clear text attribute, or null
	 */
	public abstract String decrypt(String value);

	/**
	 * Set the encryption key. This will apply the user-defined key,
	 * truncated or filled (via the default key) as needed  to meet
	 * the key length specifications.
	 *
	 * @param key The encryption key
	 */
	public abstract void setKey(String key);

}