package com.lateralthoughts.vue.utils;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    public static String addImageInfo(String url,int width,int height) {
		if(url.contains("width")&& url.contains("height")) {
			return url;
		}
    	StringBuilder modifiedUrl = new StringBuilder(url);
    	modifiedUrl.append("?width="+width);
    	modifiedUrl.append("&height="+height);
    	return modifiedUrl.toString();
    	
    }
}