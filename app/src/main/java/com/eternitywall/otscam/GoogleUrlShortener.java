package com.eternitywall.otscam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class GoogleUrlShortener {

    public static String shorten(final String longUrl) {
        if (longUrl == null)
            return longUrl;

        final String urlStr = longUrl;

        try {
            final URL url = new URL("http://goo.gl/api/url");
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "toolbar");

            final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("url=" + URLEncoder.encode(urlStr, "UTF-8"));
            writer.close();

            final BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while (( line = rd.readLine()) != null)
                sb.append(line + '\n');

            final String json = sb.toString();
            //It extracts easily...
            return json.substring(json.indexOf("http"), json.indexOf("\"", json.indexOf("http")));
        } catch (final MalformedURLException e) {
            return longUrl;
        } catch (final IOException e) {
            return longUrl;
        }
    }

}