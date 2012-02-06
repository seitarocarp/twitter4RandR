package jp.co.rough_and_ready.twitter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;

import javax.swing.JOptionPane;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class CreateTwitterAccessTorken {

	public static void main(String args[]) throws Exception {
		String filename = JOptionPane.showInputDialog("保存するAccessTokenファイルの名前を入力してください 例:accessToken.dat");
		filename = filename.trim();

		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(args[0], args[1]);

		RequestToken requestToken = twitter.getOAuthRequestToken();

		String url = requestToken.getAuthorizationURL();

		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(URI.create(url));
		} catch (IOException e) {
			throw new TwitterException(e);
		}

		String pin = JOptionPane.showInputDialog("暗証番号を入力して下さい");
		if (pin == null) {
			throw new TwitterException("暗証番号の入力がキャンセルされました");
		}
		pin = pin.trim();


		AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, pin);

		storeAccessToken(filename,accessToken);

	}



	static 	void storeAccessToken(String filename, AccessToken accessToken) throws IOException {
		File f = createAccessTokenFileName(filename);
		File d = f.getParentFile();
		if (!d.exists()) {
			d.mkdirs();
		}

		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(f));
			os.writeObject(accessToken);

		} finally {
			if (os != null) {
				try { os.close(); } catch (IOException e) { e.printStackTrace(); }
			}
		}
	}

	static File createAccessTokenFileName(String filename) {
		String s = System.getProperty("user.home") + "/.twitter/client/sample/"+filename;
		System.out.println("保存しました:"+s);
		return new File(s);
	}

}
