package com.lateralthoughts.vue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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

public class GetOtherSourceImagesTask extends
		AsyncTask<String, String, ArrayList<OtherSourceImageDetails>> {

	private String mSourceUrl = null;

	public GetOtherSourceImagesTask(String sourceUrl) {
		mSourceUrl = sourceUrl;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected ArrayList<OtherSourceImageDetails> doInBackground(String... arg0) {
		Log.e("asyntask", "url ???" + mSourceUrl);
		try {
			ArrayList<OtherSourceImageDetails> imgDetails = parseHtml(mSourceUrl, 0, 0);
			return imgDetails;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<OtherSourceImageDetails> parseHtml(String url, int reqWidth,
			int reqHeight) throws IOException {
		ArrayList<OtherSourceImageDetails> imageDetails = new ArrayList<OtherSourceImageDetails>();
		OtherSourceImageDetails googleImageBean;
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
					googleImageBean = new OtherSourceImageDetails();
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
	protected void onPostExecute(ArrayList<OtherSourceImageDetails> result) {
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
}
