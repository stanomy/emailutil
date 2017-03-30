package com.markliu.emailutil.entities;

/**
 * 
 * @author stanomy
 *
 */
public class EmailAccount {

	/**
	 * 邮箱地址
	 * eg:xxxx@163.com
	 */
	protected String mailAddress;
	/**
	 * 邮箱密码
	 */
	protected String mailPwd;
	/**
	 * 邮箱服务名称，截取邮箱地址后缀
	 * eg:@163.com
	 */
	protected String serverName;
	
	
	
	
	public EmailAccount() {
		super();
	}
	public EmailAccount(String mailAddress, String mailPwd, String serverName) {
		super();
		this.mailAddress = mailAddress;
		this.mailPwd = mailPwd;
		this.serverName = serverName;
	}
	public String getMailAddress() {
		return mailAddress;
	}
	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}
	public String getMailPwd() {
		return mailPwd;
	}
	public void setMailPwd(String mailPwd) {
		this.mailPwd = mailPwd;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	
}
