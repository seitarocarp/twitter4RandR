package jp.co.rough_and_ready.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;


/**
 * @author seitarocarp
 *
 */
public class MessageWriteToTwitter {
	Connection conn = null;

	private String accessTokenFilePath;
	private String tweetTextFilePath;
	private String imgFilePath;
	private String consumerKey;
	private String consumerSecret;
	private String twitpicAPIKey;
	private String weekNo;
	private String dataBeseServerURL;
	private String dataBeseUserName;
	private String dataBeseUserPass;

	/**
	 *
	 * @param args
	 * @throws TwitterException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws TwitterException,
			FileNotFoundException, IOException, ClassNotFoundException,
			SQLException {
		new MessageWriteToTwitter().execute(args);
	}

	void execute(String[] args) throws FileNotFoundException, IOException,
			ClassNotFoundException, TwitterException, SQLException {

		initialize(args);

		AccessToken accessToken = loadAccessToken(accessTokenFilePath);

		Twitter twitter = buildTwitterObject(accessToken);

		List<String> messageList = readTweetTextFile(tweetTextFilePath);

		try {
			conn = DriverManager.getConnection(dataBeseServerURL,
					dataBeseUserName, dataBeseUserPass);
			conn.setAutoCommit(false);

			storeDataBase(messageList);
			tweetImage(consumerKey, consumerSecret, accessToken, twitter);
			tweet(twitter, messageList);

			conn.commit();
		} catch (Throwable e) {
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (Throwable t) {
				if (conn != null) {
					conn.close();
				}
				throw new RuntimeException(t);
			}

			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	Twitter buildTwitterObject(AccessToken accessToken) {
		Twitter twitter = new TwitterFactory().getInstance();

		if (accessToken != null) {
			twitter.setOAuthConsumer(consumerKey, consumerSecret);
			twitter.setOAuthAccessToken(accessToken);
		} else {
			throw new RuntimeException("AccessTokenƒtƒ@ƒCƒ‹‚ª‚ ‚è‚Ü‚¹‚ñ:"
					+ accessTokenFilePath);
		}
		return twitter;
	}

	void initialize(String[] args) {
		accessTokenFilePath = args[0].trim();
		System.out.println(accessTokenFilePath);
		tweetTextFilePath = args[1].trim();
		System.out.println(tweetTextFilePath);
		imgFilePath = args[2].trim();
		System.out.println(imgFilePath);
		consumerKey = args[3].trim();
		System.out.println(consumerKey);
		consumerSecret = args[4].trim();
		System.out.println(consumerSecret);
		twitpicAPIKey = args[5].trim();
		System.out.println(twitpicAPIKey);
		weekNo = args[6].trim();
		System.out.println(weekNo);
		dataBeseServerURL = args[7].trim();
		System.out.println(dataBeseServerURL);
		dataBeseUserName = args[8].trim();
		System.out.println(dataBeseUserName);
		dataBeseUserPass = (args.length < 10 ? new String(): args[9].trim());
		System.out.println(dataBeseUserPass);
	}

	void storeDataBase(List<String> messageList) throws ClassNotFoundException,
			SQLException {
		for (String message : messageList) {
			storeDataBase(message);
		}

	}

	void storeDataBase(String message) throws ClassNotFoundException,
			SQLException {
		String insertQuery = buildInsertQuery(message);
		System.out.println(insertQuery);
		Class.forName("org.postgresql.Driver");

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			int status = stmt.executeUpdate(insertQuery);
			System.out.println("update "+status);
		} catch (Throwable e) {
			if (conn != null) {
				conn.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	String buildInsertQuery(String message) {
		StringBuffer query = new StringBuffer(
				"INSERT INTO magazine(week,published,title,content,updated, author_name)VALUES('");
		query.append(weekNo).append("',now(),'").append(message).append("','")
				.append(message).append("',now(),'").append(
						MessageWriteToTwitter.class.toString()).append("')");

		return query.toString();
	}

	void tweetImage(String consumerKey, String consumerSecret,
			AccessToken accessToken, Twitter twitter) throws TwitterException, InterruptedException {

		ConfigurationBuilder configurationbuilder = new ConfigurationBuilder()
				.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(
						consumerSecret).setOAuthAccessToken(
						accessToken.getToken()).setOAuthAccessTokenSecret(
						accessToken.getTokenSecret()).setMediaProviderAPIKey(
						twitpicAPIKey);

		File[] imageFiles = createImageFileList(imgFilePath);

		ImageUpload upload = new ImageUploadFactory(configurationbuilder
				.build()).getInstance(MediaProvider.TWITPIC);

		for(int i = 0; i < imageFiles.length; i++){
			String url = upload.upload(imageFiles[i]);
			System.out.println(imageFiles[i]);
			System.out.println(url);
			tweet(twitter, url);
		}

	}

	private File[] createImageFileList(String imgFilePath) {
		FileFilter fileFilter = new WildcardFileFilter("img*.jpg");
		File[] files = new File(imgFilePath).listFiles(fileFilter);
		Arrays.sort(files, DirectoryFileComparator.DIRECTORY_COMPARATOR);

		return files;
	}

	List<String> readTweetTextFile(String tweetTextFileName) throws IOException,
			TwitterException {
		FileInputStream is = new FileInputStream(tweetTextFileName);
        InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		List<String> messageList = new ArrayList<String>();

		try {

			String str = br.readLine();
			while (str != null) {

				System.out.println(str);
				messageList.add(str);

				str = br.readLine();
			}
			return messageList;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}

	}

	void tweet(Twitter twitter, List<String> messageList) throws TwitterException {
		for (String message : messageList) {
			tweet(twitter, message);
		}
	}

	void tweet(Twitter twitter, String message) throws TwitterException {
		twitter.updateStatus(message);
	}

	AccessToken loadAccessToken(String filename) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		File f = new File(filename);

		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(f));
			AccessToken accessToken = (AccessToken) is.readObject();
			System.out.println(accessToken);
			return accessToken;

		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}
	}

}
