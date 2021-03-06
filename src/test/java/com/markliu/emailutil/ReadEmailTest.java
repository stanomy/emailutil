package com.markliu.emailutil;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import com.markliu.emailutil.entities.EmailAccount;
import com.markliu.emailutil.entities.EmailServerInfo;
import com.markliu.emailutil.entities.EmailInfo;
import com.markliu.emailutil.util.EmailTemplateUtil;

/**
 * 
 * 
 * @auther sy
 * @time Apr 12, 2016 10:57:13 PM
 */
public class ReadEmailTest {

	@Test
	public void testReadAllEmails() throws FileNotFoundException {

		// 获取配置的登陆邮件服务器的信息
		EmailServerInfo emailServerInfo = EmailTemplateUtil
				.getConfigEmailServerInfo();

		List<EmailInfo> emailInfos = EmailTemplateUtil
				.getAllEmailInfos(emailServerInfo);
		System.out.println("邮件数目：" + emailInfos.size());
		for (EmailInfo emailInfo : emailInfos) {
			System.out.println(emailInfo.toString());
		}
	}

	@Test
	public void testReadEmailTemplate() throws FileNotFoundException {

		// 获取配置的登陆邮件服务器的信息
		EmailServerInfo emailServerInfo = EmailTemplateUtil
				.getConfigEmailServerInfo();

		EmailInfo emailInfo = EmailTemplateUtil
				.getLatestOneEmailInfo(emailServerInfo);
		System.out.println(emailInfo.toString());
	}

	@Test
	public void testReadEmailBySubjectTemplate() throws FileNotFoundException {

		// 获取配置的登陆邮件服务器的信息
		EmailServerInfo emailServerInfo = EmailTemplateUtil
				.getConfigEmailServerInfo();

		List<EmailInfo> emailInfos = EmailTemplateUtil
				.getEmailBySubjectPrefix(emailServerInfo);
		System.out.println("邮件数目：" + emailInfos.size());
		for (EmailInfo emailInfo : emailInfos) {
			System.out.println(emailInfo.toString());
		}
		;
	}
	
	@Test
	public void tesetReadEmailBySubFromUserList() {
		// 获取配置的登陆邮件服务器的信息
		EmailServerInfo emailServerInfo = EmailTemplateUtil
				.getConfigEmailServerInfo();

		List<EmailAccount> accountList = EmailTemplateUtil.loadUser();

		if (null == accountList || accountList.isEmpty()
				|| null == emailServerInfo)
			return;

		for (EmailAccount ac : accountList) {
			emailServerInfo.setUserName(ac.getMailAddress());
			emailServerInfo.setPassword(ac.getMailPwd());
			emailServerInfo.setMyEmailAddress(ac.getMailAddress());
			List<EmailInfo> emailInfos = EmailTemplateUtil
					.getEmailBySubjectPrefix(emailServerInfo);
			System.out.println("邮件数目：" + (null!=emailInfos?emailInfos.size():"0"));
			for (EmailInfo emailInfo : emailInfos) {
				System.out.println(emailInfo.toString());
			}
		}

	}
}
