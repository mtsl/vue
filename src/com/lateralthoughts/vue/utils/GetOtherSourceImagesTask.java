package com.lateralthoughts.vue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.lateralthoughts.vue.DataEntryFragment;
import com.lateralthoughts.vue.R;
import com.lateralthoughts.vue.VueLandingPageActivity;

public class GetOtherSourceImagesTask extends
		AsyncTask<String, String, ArrayList<OtherSourceImageDetails>> {

	private String mSourceUrl = null;
	private static final int WIDTH_LIMIT = 150;
	private static final int HEIGHT_LIMIT = 150;
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
					if (width > reqWidth && height > reqHeight) {
						OtherSourceImageDetails = new OtherSourceImageDetails();
						OtherSourceImageDetails.setWidth(width);
						OtherSourceImageDetails.setHeight(height);
						OtherSourceImageDetails.setOriginUrl(elements2.get(j)
								.absUrl("src"));
						OtherSourceImageDetails
								.setWidthHeightMultipliedValue(width * height);
					} else {
						OtherSourceImageDetails = getHeightWidth(
								elements2.get(j).absUrl("src"), reqWidth,
								reqHeight);
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
			fragment.showOtherSourcesGridview(result);
		} else {
			VueLandingPageActivity vueLandingPageActivity = (VueLandingPageActivity) mContext;
			vueLandingPageActivity.showOtherSourcesGridview(result);
		}
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

	private OtherSourceImageDetails getHeightWidth(String absUrl, int reqWidth,
			int reqHeight) {
		OtherSourceImageDetails otherSourceImageDetails = new OtherSourceImageDetails();
		Bitmap bmp = getImage(absUrl);
		if (bmp == null) {
			return null;
		} else if (bmp.getWidth() >= reqWidth && bmp.getHeight() >= reqHeight) {
			otherSourceImageDetails.setHeight(bmp.getHeight());
			otherSourceImageDetails.setWidth(bmp.getWidth());
			otherSourceImageDetails.setOriginUrl(absUrl);
			otherSourceImageDetails.setWidthHeightMultipliedValue(bmp
					.getHeight() * bmp.getWidth());
			return otherSourceImageDetails;
		} else {
			return null;
		}
	}

	private Bitmap getImage(String url) {
		InputStream is = getInputScream(url);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		return bmp;
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
