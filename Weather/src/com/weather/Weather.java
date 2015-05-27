package com.weather;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Weather extends Activity {
	TextView txt_weather_date, txt_weather_city;
	TextView txt_weather_temp1, txt_weather_detail1;
	TextView txt_weather_temp2, txt_weather_detail2;
	TextView txt_weather_temp3, txt_weather_detail3;
	TextView txt_weather_temp4, txt_weather_detail4;
	TextView txt_weather_temp5, txt_weather_detail5;
	TextView txt_weather_temp6, txt_weather_detail6;
	public final String cityGetUrl = "http://whois.pconline.com.cn/ipJson.jsp";
	private static String TAG = "[Weather]";
	
	private static final int  GET_CITY_INFROMATION = 1;
	private static final int  GET_WEATHER_DATA = 2;
	private static final int  SHOW_WEATHER_VIEW = 3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		
		txt_weather_date = (TextView) findViewById(R.id.txt_weather_date);
		txt_weather_city = (TextView) findViewById(R.id.txt_weather_city);
		txt_weather_temp1 = (TextView) findViewById(R.id.txt_weather_temp1);
		txt_weather_detail1 = (TextView) findViewById(R.id.txt_weather_detail1);
		txt_weather_temp2 = (TextView) findViewById(R.id.txt_weather_temp2);
		txt_weather_detail2 = (TextView) findViewById(R.id.txt_weather_detail2);
		txt_weather_temp3 = (TextView) findViewById(R.id.txt_weather_temp3);
		txt_weather_detail3 = (TextView) findViewById(R.id.txt_weather_detail3);
		txt_weather_temp4 = (TextView) findViewById(R.id.txt_weather_temp4);
		txt_weather_detail4 = (TextView) findViewById(R.id.txt_weather_detail4);
		txt_weather_temp5 = (TextView) findViewById(R.id.txt_weather_temp5);
		txt_weather_detail5 = (TextView) findViewById(R.id.txt_weather_detail5);
		txt_weather_temp6 = (TextView) findViewById(R.id.txt_weather_temp6);
		txt_weather_detail6 = (TextView) findViewById(R.id.txt_weather_detail6);
		
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
	private void init() {
		Message message = new Message();
		message.what = GET_CITY_INFROMATION;
		handler.sendMessage(message);
	}
	
	public void getCityInfromation() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				getCityObject(getJsonCityInfromation(queryStringForGet(cityGetUrl)));	
			}
		}).start();
    }
	
	private String getJsonCityInfromation(String response) {
		String jsonCityInformation = null;
		if (response != null) {
			Log.i(TAG, "getJsonCityInfromation: response = " + response);
			String start = "if(window.IPCallBack) {IPCallBack(";
			String end = ");}";
			int startIndex = response.indexOf(start) + start.length();
			int endIndex = response.indexOf(end);
			Log.i(TAG, "startIndex = " + startIndex + "   endIndex = " + endIndex);
			if (startIndex != -1 && endIndex != -1) {
				jsonCityInformation = response.substring(startIndex, endIndex);
				Log.i(TAG, "getJsonCityInfromation: jsonCityInformation = " + jsonCityInformation);
			}
		}

		return jsonCityInformation;
	}
	
	private void getCityObject(String json) {
		Log.i(TAG, "getCityObject:" + json);
		if (json != null) {
			try {
				JSONObject jsonObject = new JSONObject(json);
				
				Message message = new Message();
				message.obj = jsonObject;
				message.what = GET_WEATHER_DATA;
				handler.sendMessage(message);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getCityName(final JSONObject jsonObject) {
		String cityName = null;
		try {
			cityName = jsonObject.getString("city");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.i(TAG, "getCityName cityName = " + cityName);
		
		return cityName.replace("市", "");
	}
	
	private int getCityCode(final JSONObject jsonObject) {
		String cityCode = null;
		String sql = "select * from city_table where CITY =" + "'" + getCityName(jsonObject) + "'" + ";";
		DBHelper helper = new DBHelper(this);
		DBManager manager = new DBManager(this);
		manager.copyDatabase();
		Cursor cursor = helper.getReadableDatabase().rawQuery(sql, null);
		if (cursor != null) {
			cursor.moveToFirst();
			cityCode = cursor.getString(cursor.getColumnIndex("WEATHER_ID"));
		}
		cursor.close();
		helper.close();
		Log.i(TAG, "getCityCode: cityCode = " + cityCode);
		
		return Integer.valueOf(cityCode);
	}

	public void getWeatherData(final JSONObject jsonObject) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode=101020100&weatherType=0
				String weatherUrl = "http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode="	+ getCityCode(jsonObject) + "&weatherType=0";
				Log.i(TAG, "getWeatherData: weatherUrl = " + weatherUrl);
				String weatherJson = queryStringForGet(weatherUrl);
				Log.i(TAG, "getWeatherData: weatherJson = " + weatherJson);
				try {
					JSONObject jsonObject = new JSONObject(weatherJson);
					JSONObject weatherObject = jsonObject.getJSONObject("weatherinfo");
					Message message = new Message();
					message.obj = weatherObject;
					message.what = SHOW_WEATHER_VIEW;
					handler.sendMessage(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what) {
			case GET_CITY_INFROMATION:
				getCityInfromation();
				break;
			case GET_WEATHER_DATA:
				getWeatherData((JSONObject)msg.obj);
				break;
			case SHOW_WEATHER_VIEW:
				showView(msg);
				break;
			default:
				break;
			}
		}

		private void showView(Message msg) {
			JSONObject object = (JSONObject) msg.obj;
			Gson gson = new Gson();
			WeatherInformation weatherInformation = gson.fromJson(object.toString(), WeatherInformation.class);
			try {
				txt_weather_date.setText(weatherInformation.getDate_y());
				txt_weather_city.setText(weatherInformation.getCity());
				txt_weather_temp1.setText(weatherInformation.getTemp1());
				txt_weather_detail1.setText(weatherInformation.getWeather1());
				
				txt_weather_temp2.setText(weatherInformation.getTemp2());
				txt_weather_detail2.setText(weatherInformation.getWeather2());
				
				txt_weather_temp3.setText(weatherInformation.getTemp3());
				txt_weather_detail3.setText(weatherInformation.getWeather3());
				
				txt_weather_temp4.setText(weatherInformation.getTemp4());
				txt_weather_detail4.setText(weatherInformation.getWeather4());
				
				txt_weather_temp5.setText(weatherInformation.getTemp5());
				txt_weather_detail5.setText(weatherInformation.getWeather5());
				
				txt_weather_temp6.setText(weatherInformation.getTemp6());
				txt_weather_detail6.setText(weatherInformation.getWeather6());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	/**
	 * 
	 * @param url
	 * @return
	 */
	private String queryStringForGet(String url) {
		HttpGet request = new HttpGet(url);

		String result = null;

		try {
			HttpResponse response = new DefaultHttpClient().execute(request);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
				return result;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}
