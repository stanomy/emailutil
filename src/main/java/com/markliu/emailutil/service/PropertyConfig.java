package com.markliu.emailutil.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 配置文件读取工具类
 * 
 * @author stanomy
 *
 */
public class PropertyConfig {

	private static final Log LOG = LogFactory.getLog("emailLog");
	
	private static class PropertyConfigHelper {
		private static final PropertyConfig config = new PropertyConfig();
	}

	private PropertyConfig() {
	}

	public static PropertyConfig getInstance() {
		return PropertyConfigHelper.config;
	}

	/**
	 * 读取自定义配置文件，失败读取项目默认配置文件
	 * 
	 * @param customizeConfig
	 * @param defaultConfig
	 * @param logName
	 *            业务日志名称
	 * @return
	 */
	public Properties getConfig(String customizeConfig, String defaultConfig,
			String logName) {
		Properties properties = new Properties();
		InputStreamReader inStream = null;
		try {
			// 获取类路径(/)下的配置文件
			inStream = new InputStreamReader(new FileInputStream(customizeConfig), "UTF-8");
			properties.load(inStream);
			LOG.info("加载用户自定义配置文件成功-" + logName);
		} catch (Exception e) {
			try {
				LOG.info("加载用户自定义配置文件失败!-" + logName);
				LOG.info("开始加载默认配置文件-" + logName);
				inStream = new InputStreamReader(getClass()
						.getResourceAsStream(defaultConfig), "UTF-8");
				properties.load(inStream);
			} catch (Exception e1) {
				LOG.error("加载默认配置文件失败!-" + logName);
				return null;
			}
		} finally {
			if (null != inStream)
				try {
					inStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error("关闭文件流错误-" + logName);
					return null;
				}
		}
		return properties;
	}
}
