package com.markliu.emailutil.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.markliu.emailutil.entities.EmailInfo;
import com.markliu.emailutil.entities.EmailServerInfo;

/**
 * 
 * 
 * @auther SunnyMarkLiu
 * @time Apr 13, 2016 2:36:05 PM
 */
public class FetchingEmailUtil {

	private static final Log LOG = LogFactory.getLog("emailLog");

	private EmailServerInfo serverInfo = null;

	public FetchingEmailUtil(EmailServerInfo serverInfo) {
		super();
		this.serverInfo = serverInfo;
	}

	public List<EmailInfo> fetchingAllEmailInfosWithoutDel(Store store,
			boolean closeFolder) throws Exception {
		return this.fetchingAllEmailInfos(store, closeFolder, false, false);
	}

	public List<EmailInfo> fetchingEmailBySubjectWithDel(Store store,
			boolean closeFolder) throws Exception {
		return this.fetchingAllEmailInfos(store, closeFolder, true, true);
	}

	/**
	 * 根据邮件标题前缀从所有邮件获取，获取成功后从收件箱删除
	 * 
	 * @param store
	 * @param closeFolder
	 * @param usePrefix
	 * @param isDel
	 *            是否删除邮件
	 * @return
	 * @throws Exception
	 */
	private List<EmailInfo> fetchingAllEmailInfos(Store store,
			boolean closeFolder, boolean usePrefix, boolean isDel)
			throws Exception {
		List<EmailInfo> emailInfos = new ArrayList<EmailInfo>();

		// create the folder object and open it
		Folder emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_WRITE);// 打开某个收件箱

		// retrieve all messages from the folder in an array
		Message[] messages = emailFolder.getMessages();
		for (Message message : messages) {
			EmailInfo emailInfo = new EmailInfo();
			if (usePrefix) {
				if (null != this.serverInfo
						&& null != this.serverInfo.getMailSubjectPrefix()
						&& message.getSubject().indexOf(
								this.serverInfo.getMailSubjectPrefix()) >= 0) {
					writePart(message, emailInfo);
					// 读取后删除邮件
					if (isDel) {
						message.setFlag(Flags.Flag.DELETED, true);
					}
					emailInfos.add(emailInfo);
				}
			} else {
				writePart(message, emailInfo);
				// 读取后删除邮件
				if (isDel) {
					message.setFlag(Flags.Flag.DELETED, true);
				}
				emailInfos.add(emailInfo);
			}
		}
		if (closeFolder) {
			// 修改为true才会触发删除
			emailFolder.close(true);
		}
		return emailInfos;
	}

	/**
	 * 获取一份最新的邮件
	 * 
	 * @return
	 * @throws Exception
	 */
	public EmailInfo fetchingLatestEmailFromStore(Store store,
			boolean closeFolder) throws Exception {
		EmailInfo emailInfo = new EmailInfo();

		// create the folder object and open it
		Folder emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_ONLY);

		// retrieve the latest messages from the folder in an array
		Message message = emailFolder.getMessage(emailFolder.getMessageCount());
		writePart(message, emailInfo);

		if (closeFolder) {
			emailFolder.close(false);
		}
		return emailInfo;
	}

	/*
	 * This method checks for content-type based on which, it processes and
	 * fetches the content of the message
	 */
	private void writePart(Part p, EmailInfo emailInfo) throws Exception {
		if (p instanceof Message)
			// Call methos writeEnvelope
			writeEnvelope((Message) p, emailInfo);
		LOG.info("-------------Body---------------");
		LOG.info("CONTENT-TYPE: " + p.getContentType());

		// check if the content is plain text
		if (p.isMimeType("text/plain")) {
			// System.out.println("邮件正文");
			// 设置文本内容的正文
			emailInfo.setContent(MimeUtility.decodeText(p.getContent()
					.toString()));
		}
		// check if the content has attachment
		else if (p.isMimeType("multipart/*")) {
			emailInfo.setContainsAttachments(true);
			LOG.info("--------------包含附件-------------");
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
				writePart(mp.getBodyPart(i), emailInfo);
		}
		// check if the content is a nested message // 包含内嵌的内容
		else if (p.isMimeType("message/rfc822")) {
			// System.out.println("This is a Nested Message");
			// System.out.println("---------------------------");
			writePart((Part) p.getContent(), emailInfo);
		}
		// check if the content is an inline image
		else if (p.isMimeType("image/jpeg")) { // emailInfo
			Object o = p.getContent();
			InputStream x = (InputStream) o;
			// Construct the required byte array
			LOG.info("x.length = " + x.available());

			// 开启线程保存文件
			new SaveFileThread(x, "image.jpg",
					this.serverInfo.getDownloadPath()).start();

		} else if (p.getContentType().contains("image/")) {
			LOG.info("content type" + p.getContentType());
			File f = new File(this.serverInfo.getDownloadPath() + "image"
					+ new Date().getTime() + ".jpg");
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(f)));
			com.sun.mail.util.BASE64DecoderStream test = (com.sun.mail.util.BASE64DecoderStream) p
					.getContent();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = test.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			output.flush();
			output.close();
		} else {
			Object o = p.getContent();
			if (o instanceof String) {
				// 设置文本内容的正文
				emailInfo.setContent(MimeUtility.decodeText(p.getContent()
						.toString()));
			} else if (o instanceof InputStream) {

				String attachmentFileName = p.getDataHandler().getDataSource()
						.getName();
				if (attachmentFileName != null) {
					attachmentFileName = MimeUtility
							.decodeText(attachmentFileName);
					// System.out.println("附件文件名：" + attachmentFileName);
					InputStream fileIn = p.getDataHandler().getDataSource()
							.getInputStream();
					// 附件大小限制
					LOG.info("附件大小" + fileIn.available());
					if (fileIn.available() > 1024 * 1024 * Integer
							.valueOf(this.serverInfo.getFileLimitSize())) {
						LOG.info("附件大小超过最大限制:"
								+ this.serverInfo.getFileLimitSize() + "m");
						return;
					}

					LOG.info("-------开始下载附件-------");
					List<String> attachmentFiles = emailInfo
							.getAttachmentFiles();

					String downloadPath = this.serverInfo.getDownloadPath()
							+ File.separator + this.serverInfo.getUserName()
							+ File.separator;

					// 下载附件
					attachmentFiles.add(downloadPath + attachmentFileName);

					// 保存附件路径及名称
					emailInfo.setAttachmentFiles(attachmentFiles);
					// 开启线程保存文件
					new SaveFileThread(fileIn, attachmentFileName, downloadPath)
							.start();
				}

			} else {
				LOG.info("未知类型:" + o.toString());
			}
		}

	}

	/*
	 * This method would print FROM,TO and SUBJECT of the message
	 */
	private static void writeEnvelope(Message m, EmailInfo emailInfo)
			throws Exception {
		LOG.info("------------HEADER---------------");
		Address[] a;

		// 设置发送时间
		emailInfo.setSentDate(m.getSentDate());

		// FROM
		if ((a = m.getFrom()) != null) {
			// 注意需要 decode
			LOG.info("From address: " + MimeUtility.decodeText(a[0].toString()));
			emailInfo.setFromAddress(MimeUtility.decodeText(a[0].toString()));
		}

		// TO
		try {
			a = m.getRecipients(Message.RecipientType.TO);
		} catch (AddressException e) {
			LOG.info("*********** TO Illegal semicolon *************");
			LOG.info(e.getMessage());
		}
		if (a != null) {
			String[] toes = new String[a.length];
			for (int j = 0; j < a.length; j++) {
				LOG.info("TO address: "
						+ MimeUtility.decodeText(a[j].toString()));
				toes[j] = MimeUtility.decodeText(a[j].toString());
			}
			emailInfo.setToAddress(toes);
		}

		// CC
		try {
			a = m.getRecipients(Message.RecipientType.CC);
		} catch (Exception e) {
			LOG.info("*********** CC Illegal semicolon *************");
			LOG.info(e.getMessage());
		}
		if (a != null) {
			String[] toes = new String[a.length];
			for (int j = 0; j < a.length; j++) {
				LOG.info("TO CC: " + MimeUtility.decodeText(a[j].toString()));
				toes[j] = MimeUtility.decodeText(a[j].toString());
			}
			emailInfo.setCarbonCopy(toes);
		}

		// BCC
		try {
			a = m.getRecipients(Message.RecipientType.BCC);
		} catch (Exception e) {
			LOG.info("*********** BCC Illegal semicolon *************");
			LOG.info(e.getMessage());
		}
		if (a != null) {
			String[] toes = new String[a.length];
			for (int j = 0; j < a.length; j++) {
				LOG.info("TO BCC: " + MimeUtility.decodeText(a[j].toString()));
				toes[j] = MimeUtility.decodeText(a[j].toString());
			}
			emailInfo.setDarkCopy(toes);
		}

		// SUBJECT
		if (m.getSubject() != null) {
			LOG.info("SUBJECT: " + MimeUtility.decodeText(m.getSubject()));
			emailInfo.setSubject(MimeUtility.decodeText(m.getSubject()));
		}
		// 判断邮件是否已读
		/**
		 * pop3不能识别已读未读，所以注释掉
		 */
		// boolean isNew = false;
		// Flags flags = m.getFlags();
		// Flags.Flag[] flag = flags.getSystemFlags();
		// System.out.println("flags的长度:　" + flag.length);
		// for (int i = 0; i < flag.length; i++) {
		// if (flag[i] == Flags.Flag.SEEN) {
		// isNew = true;
		// System.out.println("seen email...");
		// // break;
		// }
		// }
		// emailInfo.setReaded(isNew);
		/*
		 * This message is seen. This flag is implicitly set by the
		 * implementation when the this Message's content is returned to the
		 * client in some form. The getInputStream and getContent methods on
		 * Message cause this flag to be set.
		 */
		emailInfo.setReaded(false);

		// 判断是否需要回执
		boolean needReply = m.getHeader("Disposition-Notification-To") != null ? true
				: false;
		emailInfo.setNeedReply(needReply);

		// 获取该邮件的Message-ID
		String messageID = ((MimeMessage) m).getMessageID();
		emailInfo.setMessageID(messageID);
	}

	/**
	 * 保存附件的线程
	 * 
	 * @author dell
	 *
	 */
	private class SaveFileThread extends Thread {

		private String filename;
		private InputStream fileIn;
		private String path;

		public SaveFileThread(InputStream fileIn, String filename, String path) {
			this.filename = filename;
			this.fileIn = fileIn;
			this.path = path;
		}

		/**
		 * 判断用户下载文件夹是否存在，不存在则创建
		 * <p>
		 * eg:用户下载文件夹格式: xxx@xxx.com,即邮箱地址
		 * 
		 * @param serverInfo
		 */
		private void createUserDownloadFolder(String downLoadPath) {
			File file = new File(downLoadPath);
			// 如果文件不存在且不是目录，则创建
			if (!file.exists() && !file.isDirectory()) {
				file.mkdir();
				LOG.info("文件夹" + downLoadPath + "不存在，创建");
			}
		}

		@Override
		public void run() {
			FileOutputStream out = null;
			try {
				LOG.info("----开始下载" + path + "----");
				// 判断下载文件夹是否存在
				this.createUserDownloadFolder(path);
				out = new FileOutputStream(path + File.separator + filename);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = fileIn.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.flush();
				LOG.info("----下载结束----");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info("下载发生错误" + e.getMessage());
			} finally {
				try {
					if (out != null) {
						out.close();
						out = null;
					}
					if (fileIn != null) {
						fileIn.close();
						fileIn = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
