package com.markliu.emailutil.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.markliu.emailutil.entities.EmailAccount;

/**
 * 
 * 邮箱账户业务类
 * 
 * @author stanomy
 *
 */
public class UserService {

	/**
	 * 从配置文件读取账号，密码
	 * @return
	 */
	public List<EmailAccount> getEmailAccount() {

		List<EmailAccount> rs = new ArrayList<EmailAccount>(0);

		Properties properties = new Properties();
		InputStream inStream = null;
		try {
			// 获取类路径(/)下的配置文件
			inStream = getClass().getResourceAsStream("/account.properties");
		} catch (Exception e) {
			System.out.println("用户邮箱账号加载失败!");
			return null;
		}
		try {
			properties.load(inStream);

			String str = properties.getProperty("user/pwd");

			if (null != str && !"".equals(str)) {
				String[] strs=str.split(",");
				if(null!=strs && strs.length!=0){
					for (String rss : strs) {
						/**
						 * 解析配置，生成账户信息here
						 */
						if(null!=rss && !"".equals(rss)){
							String[] rsses=rss.split("/");
							if(null!=rsses && rsses.length!=0){
								EmailAccount emailAccount=new EmailAccount();
								emailAccount.setMailAddress(rsses[0]);
								emailAccount.setMailPwd(rsses[1]);
								rs.add(emailAccount);
							}
						}
						
					}
				}
				
			}
			return rs;
		} catch (Exception e) {
			return null;
		}
	}

}
