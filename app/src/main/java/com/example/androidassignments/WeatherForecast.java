package com.example.androidassignments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;import android.content.Context;import java.io.File;

public class WeatherForecast extends AppCompatActivity {

    private ImageView imageViewWeather;
    private TextView textViewCurrentTemp;
    private TextView textViewMinTemp;
    private TextView textViewMaxTemp;
    private ProgressBar progressBarLoading;
    private Bitmap iconBitmap;

    private String minTemperature;
    private String maxTemperature;
    private String currentTemperature;
    private String iconName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        imageViewWeather = findViewById(R.id.imageViewWeather);
        textViewCurrentTemp = findViewById(R.id.textViewCurrentTemp);
        textViewMinTemp = findViewById(R.id.textViewMinTemp);
        textViewMaxTemp = findViewById(R.id.textViewMaxTemp);
        progressBarLoading = findViewById(R.id.progressBarLoading);

        progressBarLoading.setVisibility(View.VISIBLE);
        new ForecastQuery().execute();
    }

    private class ForecastQuery extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... args) {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=c500c8b82b16591f008817c996983855&mode=xml&units=metric";

            try {
                URL url = new URL(urlString);
                InputStream inputStream = downloadXml(url);
                if (inputStream != null) {
                    readFeed(inputStream);
                    inputStream.close();
                }

                if (iconName != null && !iconName.isEmpty()) {
                    String imageName = iconName + ".png";
                    if (fileExistance(imageName)) {
                        Log.i("WeatherForecast", "Icon found locally: " + imageName);
                        FileInputStream fis = openFileInput(imageName);
                        Bitmap iconBitmap = BitmapFactory.decodeStream(fis);
                        fis.close();
                        publishProgress(100);
                    } else {
                        Log.i("WeatherForecast", "Downloading icon: " + imageName);
                        Bitmap image = HTTPUtils.getImage("http://openweathermap.org/img/w/" + iconName + ".png");

                        FileOutputStream outputStream = openFileOutput(imageName, Context.MODE_PRIVATE);
                        image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        publishProgress(100);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private InputStream downloadXml(URL url) {
            try {
                Log.d("WeatherForecast", "Connecting to URL: " + url.toString()); // Log URL

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d("WeatherForecast", "Response Code: " + responseCode); // Log response code

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return connection.getInputStream();
                } else {
                    Log.e("WeatherForecast", "Connection failed with response code: " + responseCode); // Log error if response is not OK
                }
            } catch (Exception e) {
                Log.e("WeatherForecast", "Error in downloadXml", e); // Log exception details
                e.printStackTrace();
            }
            return null;
        }


        private void readFeed(InputStream inputStream) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }

                    String tagName = parser.getName();

                    if (tagName.equals("temperature")) {
                        currentTemperature = parser.getAttributeValue(null, "value") + "°C";
                        publishProgress(25);
                        minTemperature = parser.getAttributeValue(null, "min") + "°C";
                        publishProgress(50);
                        maxTemperature = parser.getAttributeValue(null, "max") + "°C";
                        publishProgress(75);


                        Log.d("WeatherForecast", "Current Temp: " + currentTemperature);
                        Log.d("WeatherForecast", "Min Temp: " + minTemperature);
                        Log.d("WeatherForecast", "Max Temp: " + maxTemperature);
                    }

                    if (tagName.equals("weather")) {
                        iconName = parser.getAttributeValue(null, "icon");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean fileExistance(String fname) {
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }

        @Override
        protected void onPostExecute(Void result) {

            textViewMinTemp.setText("Min: " + minTemperature);
            textViewMaxTemp.setText("Max: " + maxTemperature);
            textViewCurrentTemp.setText("Current: " + currentTemperature);


            if (iconBitmap != null) {
                imageViewWeather.setImageBitmap(iconBitmap);
            }


            progressBarLoading.setVisibility(View.INVISIBLE);
        }
    }
}

class HTTPUtils {
    public static Bitmap getImage(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return BitmapFactory.decodeStream(connection.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
