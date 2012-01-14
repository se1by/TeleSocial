package me.se1by.TeleSocial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.bukkit.configuration.file.YamlConfiguration;



public class ApiAccess {
	static YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/TeleSocial/config.yml"));
	static String appkey = config.getString("AppKey");
	static String baseUrl = config.getString("BaseUrl");
	/**
	 * This method sends a post request to the rest api
	 * @param adress Part of the url(behind baseUrl)
	 * @param key1 First key
	 * @param value1 First value
	 * @param key2 Second key
	 * @param value2 Second value
	 * @return
	 */
	public static String apiPost(String adress, String key1, String value1, String key2, String value2) {
		String response1 = "";
		try {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(baseUrl + adress);
		List<NameValuePair>nvpairs = new ArrayList<NameValuePair>();
		nvpairs.add(new BasicNameValuePair("appkey", appkey));
		if(key1 != null)
			nvpairs.add(new BasicNameValuePair(key1, value1));
		if(key2 != null)
			nvpairs.add(new BasicNameValuePair(key2, value2));
		
		post.setEntity(new UrlEncodedFormEntity(nvpairs));
		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			response1 += line;
		}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response1;
	}
}
