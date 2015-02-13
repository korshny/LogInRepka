package login;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Connection {

	private HttpsURLConnection conn;

	public static void main(String[] args) throws Exception {

		String urlToLogIn = "https://repka.ua/personal/";
		String urlToChangeName = "https://repka.ua/custom_ajax/cabinet/save.php/";
		
		String fNamePostParams = "FIELD=NAME&VALUE=Andrew";
		String sNamePostParams = "FIELD=LAST_NAME&VALUE=Korshny";

		if (args.length > 0) fNamePostParams = "FIELD=NAME&VALUE=" + args[0];
		if (args.length > 1) sNamePostParams = "FIELD=LAST_NAME&VALUE=" + args[1];
		

		Connection http = new Connection();

		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		// send a "GET" request to extract the form's data.
		String page = http.GetPageContent(urlToLogIn);
		String logInPostParams = http.getFormParams(page, "user123", "qwerty123");
		
		// 1. Send a POST request for authentication.
		http.sendPost(urlToLogIn, logInPostParams);

		// 2. Get name of current user.
		String result = http.GetPageContent(urlToLogIn);
		System.out.println("Old name is " + http.getName(result));
	
		// 3. Change user name.
		http.sendPost(urlToChangeName, fNamePostParams);
		http.sendPost(urlToChangeName, sNamePostParams);
		result = http.GetPageContent(urlToLogIn);
		System.out.println("New name is " + http.getName(result));
	}
	
	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		
		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		System.out.println("\n Sending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());

	}

	private String GetPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		conn.setUseCaches(false);

		System.out.println("\n Sending 'GET' request to URL : " + url);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();

	}
	
	public String getName(String html) {
		String fName = null;
		String lName = null;
		Document doc = Jsoup.parse(html);
		Elements inputElements = doc.getElementsByTag("input");
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			if (key.equals("NAME")) fName = value;
			else if (key.equals("LAST_NAME")) {
				lName = value;
				break;
			}
		}
		return fName + " " + lName;
	}

	public String getFormParams(String html, String username, String password)
			throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");
		
		Document doc = Jsoup.parse(html);
		
		//Get div-element with id
		Element loginDiv = doc.getElementById("at_bitrix");
		Elements inputElements = loginDiv.getElementsByTag("input");		
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("USER_LOGIN"))
				value = username;
			else if (key.equals("USER_PASSWORD"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

}