package com.lateralthoughts.vue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.lateralthoughts.vue.DataEntryActivity;
import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.R;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class GetImagesTask extends
		AsyncTask<String, String, ArrayList<GoogleImageBean>> {

	private String mSourceUrl = null;

	public GetImagesTask(String sourceUrl) {
		mSourceUrl = sourceUrl;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected ArrayList<GoogleImageBean> doInBackground(String... arg0) {
		/*
		 * URL url; try { url = new URL(
		 * "https://ajax.googleapis.com/ajax/services/search/images?" +
		 * "v=1.0&q=" + "site:" + searchString + "&rsz=8");
		 * 
		 * URLConnection connection = url.openConnection();
		 * 
		 * String line; StringBuilder builder = new StringBuilder();
		 * BufferedReader reader = new BufferedReader( new
		 * InputStreamReader(connection.getInputStream())); while ((line =
		 * reader.readLine()) != null) { builder.append(line); }
		 * 
		 * System.out.println("Builder string => " + builder.toString());
		 * 
		 * json = new JSONObject(builder.toString()); } catch
		 * (MalformedURLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } catch (JSONException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		Log.e("asyntask", "url ???" + mSourceUrl);
		try {
			ArrayList<GoogleImageBean> imgDetails = parseHtml(mSourceUrl, 0, 0);
			return imgDetails;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<GoogleImageBean> parseHtml(String url, int reqWidth,
			int reqHeight) throws IOException {
		ArrayList<GoogleImageBean> imageDetails = new ArrayList<GoogleImageBean>();
		GoogleImageBean googleImageBean;
		Document doc = null;
		doc = Jsoup.parse(getData(url), url);
		Elements elements = doc.select("img");
		System.out.println("Img elements size : " + elements.size());
		for (Element elmnt : elements) {
			Elements elements2 = elmnt.getAllElements();
			for (int j = 0; j < elements2.size(); j++) {
				if (elements2.get(j).nodeName().equalsIgnoreCase("img")) {
					int width = 0;
					int height = 0;
					try {
						width = Integer
								.parseInt(elements2.get(j).attr("width"));
						height = Integer.parseInt(elements2.get(j).attr(
								"height"));
					} catch (NumberFormatException e) {
						System.out.println("NumberFormatException");
					}
					// if (width > reqWidth && height > reqHeight) {
					googleImageBean = new GoogleImageBean();
					googleImageBean.setWidth(width);
					googleImageBean.setHeight(height);
					googleImageBean
							.setOriginUrl(elements2.get(j).absUrl("src"));
					/*
					 * } else { imgDetails = getHeightWidth(
					 * elements2.get(j).absUrl("src"), reqWidth, reqHeight); }
					 */
					if (googleImageBean != null) {
						imageDetails.add(googleImageBean);
					}
				}
			}
		}
		return imageDetails;
	}

	@Override
	protected void onPostExecute(ArrayList<GoogleImageBean> result) {
		super.onPostExecute(result);
		DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) DataEntryActivity.mDataEntryActivityContext)
				.getSupportFragmentManager().findFragmentById(
						R.id.create_aisles_view_fragment);
		fragment.showOtherSourcesGridview(result);
	}

	private String getData(String url) {
		InputStream is = null;
		StringBuilder sb = null;

		if (url == null) {
			return null;
		}

		try {
			URL rssURL = new URL(url);
			URLConnection connection = rssURL.openConnection();
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
			is = connection.getInputStream();

			BufferedReader br = null;
			sb = new StringBuilder();
			String line;

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	public ArrayList<GoogleImageBean> getImageList(JSONArray resultArray) {
		ArrayList<GoogleImageBean> listImages = new ArrayList<GoogleImageBean>();
		GoogleImageBean bean;

		try {
			for (int i = 0; i < resultArray.length(); i++) {
				JSONObject obj;
				obj = resultArray.getJSONObject(i);
				bean = new GoogleImageBean();
				bean.setHeight(Integer.parseInt(obj.getString("height")));
				bean.setWidth(Integer.parseInt(obj.getString("width")));
				bean.setOriginUrl(obj.getString("url"));
				bean.setTitle(obj.getString("title"));
				bean.setThumbUrl(obj.getString("tbUrl"));
				Log.e("tag", "Thumb URL => " + obj);
				System.out.println("Thumb URL => " + obj.getString("tbUrl"));

				if (bean.getWidth() > 100 && bean.getHeight() > 100) {
					listImages.add(bean);
				}

			}
			return listImages;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

}
