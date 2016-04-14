package com.markliu.emailutil;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.markliu.emailutil.entities.EmailServerHostAndPort;
import com.markliu.emailutil.entities.EmailServerInfo;
import com.markliu.emailutil.util.EmailTemplateUtil;

/**
 * 
 * 
 * @auther SunnyMarkLiu
 * @time Apr 14, 2016 10:34:01 AM
 */
public class DeleteEmailTest {

	@Test
	public void testDeleteEmail() throws FileNotFoundException {
		EmailServerInfo emailServerInfo = new EmailServerInfo();
		emailServerInfo.setMailServerHost(EmailServerHostAndPort.NetEase163_POP3_SERVER);
		emailServerInfo.setMailServerPort(EmailServerHostAndPort.POP3_PORT);
		emailServerInfo.setValidate(true);
		emailServerInfo.setUserName("xxxxxx@163.com");
		emailServerInfo.setPassword("xxxxxx"); // 注意使用的是开通 SMTP 协议的授权码
		emailServerInfo.setMyEmailAddress("xxxxxx@163.com");
		
		if (EmailTemplateUtil.deleteEmailByMsgNum(emailServerInfo, 64)) {
			System.out.println("删除成功！");
		} else {
			System.out.println("删除失败！");
		}
	}
}
