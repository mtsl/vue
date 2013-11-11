package com.lateralthoughts.vue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueLandingPageActivity;
import com.lateralthoughts.vue.logging.Logger;

public class GetOtherSourceImagesTask extends
		AsyncTask<String, String, ArrayList<OtherSourceImageDetails>> {

	private String mSourceUrl = null;
	private static final int WIDTH_LIMIT = 145;
	private static final int HEIGHT_LIMIT = 145;
	private Context mContext = null;
	private boolean mFromLandingScreenFlag;

	public GetOtherSourceImagesTask(String sourceUrl, Context context,
			boolean fromLandingScreenFlag) {
		mSourceUrl = sourceUrl;
		mContext = context;
		mFromLandingScreenFlag = fromLandingScreenFlag;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected ArrayList<OtherSourceImageDetails> doInBackground(String... arg0) {
		Log.e("asyntask", "url ???" + mSourceUrl);
		try {
			if (mSourceUrl != null) {
				ArrayList<OtherSourceImageDetails> imgDetails = parseHtml(
						mSourceUrl, WIDTH_LIMIT, HEIGHT_LIMIT);
				return imgDetails;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<OtherSourceImageDetails> parseHtml(String url,
			int reqWidth, int reqHeight) throws Exception {
		ArrayList<OtherSourceImageDetails> imageDetails = new ArrayList<OtherSourceImageDetails>();
		OtherSourceImageDetails OtherSourceImageDetails = null;
		Document doc = null;
		doc = Jsoup.parse(getData(url), url);
		System.out.println("Img elements  doc size : " + doc.childNodeSize());
		Elements elements = doc.select("img");
		System.out.println("Img elements size : " + elements.size());
		if (elements.size() == 0) {
			for (int i = 0; i < 3; i++) {
				System.out.println("Img elements retry count : " + i);
				doc = Jsoup.parse(getData(url), url);
				elements = doc.select("img");
				if (elements.size() > 0) {
					break;
				}
			}
		}
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
					if (width > reqWidth && height > reqHeight) {
						OtherSourceImageDetails = new OtherSourceImageDetails();
						OtherSourceImageDetails.setWidth(width);
						OtherSourceImageDetails.setHeight(height);
						String tempImgUrl = elements2.get(j).absUrl("rel");
						if (tempImgUrl != null && tempImgUrl.length() > 1) {
							OtherSourceImageDetails.setOriginUrl(elements2.get(
									j).absUrl("rel"));
						} else {
							OtherSourceImageDetails.setOriginUrl(elements2.get(
									j).absUrl("src"));
						}
						OtherSourceImageDetails
								.setWidthHeightMultipliedValue(width * height);
					} else {
						String tempImgUrl = elements2.get(j).absUrl("rel");
						if (tempImgUrl != null && tempImgUrl.length() > 1) {
							OtherSourceImageDetails = getHeightWidth(elements2
									.get(j).absUrl("rel"), reqWidth, reqHeight);
						} else {
							OtherSourceImageDetails = getHeightWidth(elements2
									.get(j).absUrl("src"), reqWidth, reqHeight);
						}
					}
					if (OtherSourceImageDetails != null) {
						imageDetails.add(OtherSourceImageDetails);
					}
				}
			}
		}
		if (imageDetails != null) {
			Collections.sort(imageDetails, new SortBasedOnImageWidthHeight());
			Collections.reverse(imageDetails);
		}
		return imageDetails;
	}

	@Override
	protected void onPostExecute(ArrayList<OtherSourceImageDetails> result) {
		super.onPostExecute(result);
		if (!mFromLandingScreenFlag) {
			DataEntryFragment fragment = (DataEntryFragment) ((FragmentActivity) mContext)
					.getSupportFragmentManager().findFragmentById(
							R.id.create_aisles_view_fragment);
			fragment.showOtherSourcesGridview(result, mSourceUrl);
		} else {
			VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mContext;
			vueLandingPageActivity.showOtherSourcesGridview(result, mSourceUrl);
		}
	}

	private String getData(String url) {
		// InputStream is = null;
		// StringBuilder sb = null;
		StringBuffer html = null;

		if (url == null) {
			return null;
		}

		try {
			URL rssURL = new URL(url);

			HttpURLConnection conn = (HttpURLConnection) rssURL
					.openConnection();
			conn.setInstanceFollowRedirects(true); // you still need to handle
													// redirect manully.
			HttpURLConnection.setFollowRedirects(true);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
			conn.setReadTimeout(5000);
			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.addRequestProperty("User-Agent", "Mozilla");
			conn.addRequestProperty("Referer", "google.com");
			boolean redirect = false;
			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}

			Log.e("GetOtherSourceImagesTask", "Response Code ... " + status);

			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");

				// get the cookie if need, for login
				String cookies = conn.getHeaderField("Set-Cookie");

				// open the new connnection again
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				conn.setRequestProperty("Cookie", cookies);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Mozilla");
				conn.addRequestProperty("Referer", "google.com");

				System.out.println("Redirect to URL : " + newUrl);

			}
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			html = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				html.append(inputLine);
			}
			in.close();

			/*
			 * URLConnection connection = rssURL.openConnection(); connection
			 * .setRequestProperty( "User-Agent",
			 * "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19"
			 * ); is = connection.getInputStream();
			 * 
			 * BufferedReader br = null; sb = new StringBuilder(); String line;
			 * 
			 * br = new BufferedReader(new InputStreamReader(is)); while ((line
			 * = br.readLine()) != null) { sb.append(line); }
			 */
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
		String st = html.toString();
		Logger.log("error", "GetOtherSourceImages", st);
		Log.e("GetOtherSourceImagesTask", "PARSED HTML PAGE: " + st);
		return st;/* sb.toString(); */
	}

	private OtherSourceImageDetails getHeightWidth(String absUrl, int reqWidth,
			int reqHeight) {
		try {
			OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails();
			InputStream is = getInputScream(absUrl);
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, o);
			is.close();
			if (o.outWidth >= reqWidth && o.outHeight >= reqHeight) {
				otherSourceImageDetails.setHeight(o.outHeight);
				otherSourceImageDetails.setWidth(o.outWidth);
				otherSourceImageDetails.setOriginUrl(absUrl);
				otherSourceImageDetails
						.setWidthHeightMultipliedValue(o.outHeight * o.outWidth);
				return otherSourceImageDetails;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private InputStream getInputScream(String imgUrl) {
		InputStream is = null;
		try {
			URL url = new URL(imgUrl);
			URLConnection con = url.openConnection();
			is = con.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

}
