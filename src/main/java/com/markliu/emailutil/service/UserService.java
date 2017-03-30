package com.markliu.emailutil.service;

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
	 * 
	 * @return
	 */
	public List<EmailAccount> getEmailAccount() {

		List<EmailAccount> rs = new ArrayList<EmailAccount>(0);

		/**
		 * 读取自定义配置，同工程路径
		 */
		Properties properties = PropertyConfig.getInstance()
				.getConfig("CLASSPATH:account.properties",
						"/account.properties", "账号信息");
		try {

			if (null == properties) {
				return rs;
			}
			String str = null;

			/**
			 * 解析配置，生成账户信息here
			 */
			for (int i = 1; i <= properties.size(); i++) {
				str = properties.getProperty("user/pwd" + i);
				if (null != str && !"".equals(str)) {
					String[] rsses = str.split("/");
					if (null != rsses && rsses.length != 0) {
						EmailAccount emailAccount = new EmailAccount();
						emailAccount.setMailAddress(rsses[0]);
						emailAccount.setMailPwd(rsses[1]);
						rs.add(emailAccount);
					}
				} else
					continue;
			}
			System.out.println("从配置文件读取了" + rs.size() + "个用户信息。");
			return rs;
		} catch (Exception e) {
			System.err.println("读取用户配置信息发生错误" + e.getMessage());
			return null;
		}
	}

}
