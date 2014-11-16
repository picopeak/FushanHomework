package com.fushan.homework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat", "DefaultLocale", "ShowToast", "InlinedApi" })
public class DisplayMessageActivity extends Activity implements OnRefreshListener {

	private HttpClient httpclient;
	private SwipeRefreshLayout swipeLayout;
	private ListView HomeWork, HomeWorkL, HomeWorkR;
	private String UserName = "";
	private String PassWord = "";
	private String RealName = "";
	
	// The ID of current user, can only be 1 or 2
	private int CurrentUser = 1;
	private AsyncTask<Calendar, Integer, Long> LastTask = null;

	// The date to display homework
	private Calendar c;
	private Calendar LastTodayUpdate;
	private HomeworkDatabase HWDB;
	
	private boolean login;
	// private String ViewState =
	// "dDwtMTIwMjU0ODg5NDs7bDxsb2dpbjpidG5sb2dpbjs+PirAF7I2CPvB/hrCXAPxCiCha+tS";
	private String ViewState = "";
	private DisplayMetrics dm;
	private float fontsize;
	private boolean bigfont;

	private LeftArrow_OnClickListener listener1 = new LeftArrow_OnClickListener();
	private RightArrow_OnClickListener listener2 = new RightArrow_OnClickListener();
	private CurrentDate_OnClickListener listener3 = new CurrentDate_OnClickListener();

	private ViewPager mPager;
	private List<View> AllViews;

	private static Map<String,SoftReference<Bitmap>> sImageCache; 
	private static LoaderImpl ImageLoader;
	
	// ////////////////////////////////////////////////////////
	// Utility functions
	// ////////////////////////////////////////////////////////

	private String formatTime(int t) {
		return t >= 10 ? "" + t : "0" + t;
	}
	
	private String getDate(Calendar c) {
		String date = c.get(Calendar.YEAR) + "-"
				+ formatTime(c.get(Calendar.MONTH) + 1) + "-"
				+ formatTime(c.get(Calendar.DAY_OF_MONTH));
		String w = "";
		switch (c.get(Calendar.DAY_OF_WEEK)) {
		case 1:
			w = "日";
			break;
		case 2:
			w = "一";
			break;
		case 3:
			w = "二";
			break;
		case 4:
			w = "三";
			break;
		case 5:
			w = "四";
			break;
		case 6:
			w = "五";
			break;
		case 7:
			w = "六";
			break;
		}
		date = date + "(" + w + ")";
		
		return date;
	}

	private boolean isToday(Calendar c) {
		Calendar today = Calendar.getInstance();
		if (getDate(c).equals(getDate(today)))
			return true;
		else
			return false;
	}

	private void SetCurrentDate(Calendar c) {
		String date = getDate(c);
		TextView CurrentDate = (TextView) findViewById(R.id.CurrentDate);
		CurrentDate.setText(date);
		CurrentDate.setTextColor(Color.WHITE);
		CurrentDate.setBackgroundColor(Color.parseColor("#9D61AB"));
	}

	public void onRefresh() {  
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
        		GetToDateHomeWorkTaskFromNetwork(c, true);
            }
        }, 3000);
    }

	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		AllViews = new ArrayList<View>();

		LayoutInflater mInflater = getLayoutInflater();
		View view0 = mInflater.inflate(R.layout.homework_list, null);
		HomeWorkL = (ListView) view0.findViewById(R.id.HomeWork);
		AllViews.add(view0);
		View view1 = mInflater.inflate(R.layout.homework_list, null);
        HomeWork = (ListView) view1.findViewById(R.id.HomeWork);
		AllViews.add(view1);
		
        swipeLayout = (SwipeRefreshLayout) view1.findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this);  
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright, android.R.color.holo_orange_light);  
		
		View view2 = mInflater.inflate(R.layout.homework_list, null);
		HomeWorkR = (ListView) view2.findViewById(R.id.HomeWork);
		AllViews.add(view2);

		mPager.setAdapter(new MyPagerAdapter(AllViews));

		mPager.setCurrentItem(1);
		// mPager.setOffscreenPageLimit(1);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	private class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			View view1 = mListViews.get(arg1);
			((ViewPager) arg0).addView(view1, 0);

			if (arg1 == 1) {
				// Fetch from database first
				String HW[];
				HW = HWDB.getRecords(UserName, getDate(c));
				if (HW[0] == null) {
					String TEMP_HW[] = new String[10];
					TEMP_HW[0] = "正在读取数据...";
					DisplayHomeWork(TEMP_HW, HomeWork);
				} else {
					DisplayHomeWork(HW, HomeWork);
				}

				if (UserName == "") {
					Intent intent = new Intent();
					intent.setClass(DisplayMessageActivity.this, MainActivity.class);
					intent.putExtra("CurrentUser", CurrentUser);
					startActivityForResult(intent, 0);
				} else {
					CancelLastTask();
			    	swipeLayout.setRefreshing(true);
					LastTask = new LoginTask().execute(c);
				}
			}

			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	public void DisplayHomeWorkFromCache(ListView HomeWork, String[] HW) {
		if (HW[0] == null) {
			String TEMP_HW[] = new String[10];
			TEMP_HW[0] = "没有本地作业数据!";
			DisplayHomeWork(TEMP_HW, HomeWork);
		} else {
			DisplayHomeWork(HW, HomeWork);
		}
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			String[] HW;
			if (arg0 == 0) {
				c.add(Calendar.DATE, -1);
				Calendar y = (Calendar) c.clone(), t = (Calendar) c.clone();
				y.add(Calendar.DATE, -1);
				t.add(Calendar.DATE, 1);

				// Fetch from database first
				HW = HWDB.getRecords(UserName, getDate(y));
				DisplayHomeWorkFromCache(HomeWorkL, HW);
				HW = HWDB.getRecords(UserName, getDate(t));
				DisplayHomeWorkFromCache(HomeWorkR, HW);
				HW = HWDB.getRecords(UserName, getDate(c));
				DisplayHomeWorkFromCache(HomeWork, HW);
				
				mPager.setCurrentItem(1, false);
			} else if (arg0 == 2) {
				c.add(Calendar.DATE, 1);
				Calendar y = (Calendar) c.clone(), t = (Calendar) c.clone();
				y.add(Calendar.DATE, -1);
				t.add(Calendar.DATE, 1);

				// Fetch from database first
				HW = HWDB.getRecords(UserName, getDate(y));
				DisplayHomeWorkFromCache(HomeWorkL, HW);
				HW = HWDB.getRecords(UserName, getDate(t));
				DisplayHomeWorkFromCache(HomeWorkR, HW);
				HW = HWDB.getRecords(UserName, getDate(c));
				DisplayHomeWorkFromCache(HomeWork, HW);
				
				mPager.setCurrentItem(1, false);
			} else {
				return;
			}

			SetCurrentDate(c);
	    	swipeLayout.setRefreshing(false);
			GetToDateHomeWorkTaskFromNetwork(c, (HW[0]==null));
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	class CurrentDate_OnClickListener implements OnClickListener {
		public void onClick(View v) {
		}
	}

	class LeftArrow_OnClickListener implements OnClickListener {
		public void onClick(View v) {
		}
	}

	class RightArrow_OnClickListener implements OnClickListener {
		public void onClick(View v) {
		}
	}

	// Main entry of displaying homework
	private void DisplayHomeWork(String[] HW, ListView HomeWork) {
		MyCustomAdapter adapter = new MyCustomAdapter();
		boolean findHW = false;
		for (int i = 0; i < 10; i++) {
			if (HW[i] != null) {
				findHW = true;
				// URLImageParser p = new URLImageParser(HomeWork, this);
				// adapter.addItem(Html.fromHtml(HW[i], p, null));
				adapter.addItem(HW[i], i);
			}
		}

		if (!findHW) {
			// adapter.addItem(Html.fromHtml("今日没有作业", null, null));
			adapter.addItem("今日没有作业", 0);
		}
		HomeWork.setAdapter(adapter);
	}

	// ////////////////////////////////////////////////////////
	// The followings are login facility
	// ////////////////////////////////////////////////////////

	private String getInputProperty(String input, String property) {
		Matcher m = Pattern.compile(property + "[\\s]*=[\\s]*\"[^\"]*\"")
				.matcher(input);
		if (m.find()) {
			String v = m.group();
			return v.substring(v.indexOf("\"") + 1, v.length() - 1);
		}
		return null;
	}

	private Map<String, String> getAllInputNames(String body) {
		Map<String, String> parameters = new HashMap<String, String>();
		Matcher matcher = Pattern.compile("<input[^<]*>").matcher(body);
		while (matcher.find()) {
			String input = matcher.group();
			if (input.contains("name")) {
				parameters.put(getInputProperty(input, "name"),
						getInputProperty(input, "value"));
			}
		}
		return parameters;
	}

	private String GetOldViewState(String url) {
		String vs = "";
		// Get view state
		try {
			HttpResponse httpResponse = null;
			HttpGet httpget = new HttpGet(url);
			httpget.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			httpResponse = httpclient.execute(httpget);
			int SC = httpResponse.getStatusLine().getStatusCode();
			if (SC == 200) {
				HttpEntity entity = httpResponse.getEntity();
				InputStream is = entity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "GB2312"));
				String line = "";
				StringBuilder sb = new StringBuilder();
				Map<String, String> params;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				params = getAllInputNames(sb.toString());
				vs = params.get("__VIEWSTATE");
				httpget.abort();
			}
		} catch (ClientProtocolException e) {
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		} catch (Exception e) {
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		}

		return vs;
	}

	private boolean Login() {
		// try to login 2 times;
		for (int i = 0; i < 2; i++) {
			try {
				ViewState = GetOldViewState("http://www.fushanedu.cn/jxq/jxq_User.aspx");

				HttpResponse httpResponse = null;
				HttpPost httppost = new HttpPost(
						"http://www.fushanedu.cn/jxq/jxq_User.aspx");
				httppost.addHeader("Content-Type",
						"application/x-www-form-urlencoded");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				// Only work for v2.0 and below
				// nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
				// "dDwtMTIwMjU0ODg5NDs7bDxsb2dpbjpidG5sb2dpbjs+PrJFQt1nM63efREqb/0FcyQpPFwa"));

				// This works for v3.0
				// nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
				// "dDwtMTIwMjU0ODg5NDs7bDxsb2dpbjpidG5sb2dpbjs+PirAF7I2CPvB/hrCXAPxCiCha+tS"));

				nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
						ViewState));

				nameValuePairs.add(new BasicNameValuePair("login:tbxUserName",
						UserName));
				nameValuePairs.add(new BasicNameValuePair("login:tbxPassword",
						PassWord));
				nameValuePairs.add(new BasicNameValuePair("login:btnlogin.x",
						"27"));
				nameValuePairs.add(new BasicNameValuePair("login:btnlogin.y",
						"12"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"GB2312"));
				httpResponse = httpclient.execute(httppost);
				int SC = httpResponse.getStatusLine().getStatusCode();
				if (SC == 200) {
					HttpEntity entity = httpResponse.getEntity();
					InputStream is = entity.getContent();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "GB2312"));
					String line = "";
					StringBuilder sb = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						if (line.indexOf("对不起，帐号输入错误！") != -1) {
							httppost.abort();
							return false;
						} else if (line.indexOf("您好！欢迎使用") != -1) {
							Matcher matcher = Pattern.compile("[^>]*\\(")
									.matcher(line);
							if (matcher.find()) {
								String s = matcher.group();
								int l = s.length();
								RealName = s.substring(0, l - 1);

								SharedPreferences preference = getSharedPreferences(
										"person", Context.MODE_PRIVATE);
								Editor edit = preference.edit();
								edit.putString("RealName" + CurrentUser,
										RealName);
								edit.commit();
							}
							httppost.abort();
							return true;
						}
						sb.append(line);
					}
					httppost.abort();
				}
			} catch (ClientProtocolException e) {
				// Log.e("DisplayMessageActivity", "E " + e.getMessage());
			} catch (Exception e) {
				// Log.e("DisplayMessageActivity", "E " + e.getMessage());
			}
		}

		return false;
	}

	// Login facility
	private class LoginTask extends AsyncTask<Calendar, Integer, Long> {
		private Calendar c;
		
		protected Long doInBackground(Calendar... parms) {
			try {
				c = parms[0];
				login = Login();
			} catch (Exception pce) {
				// Log.e("DisplayMessageActivity", "PCE " + pce.getMessage());
			}
			return (long) 1;
		}

		protected void onPostExecute(Long result) {
			if (!login) {
				Toast SM = Toast.makeText(DisplayMessageActivity.this, "请检查网络或账户密码...", 1);
				SM.show();
		    	swipeLayout.setRefreshing(false);

				Intent intent = new Intent();
				intent.setClass(DisplayMessageActivity.this, MainActivity.class);
				intent.putExtra("CurrentUser", CurrentUser);
				startActivityForResult(intent, 0);
			} else {
				// create calendar
				c = Calendar.getInstance();
				SetCurrentDate(c);

				// After login successfully, we update CurrentUser, UserName and
				// PassWord
				SharedPreferences preference = getSharedPreferences("person",
						Context.MODE_PRIVATE);
				Editor edit = preference.edit();
				edit.putInt("CurrentUser", CurrentUser);
				edit.commit();
				UserName = preference.getString("UserName" + CurrentUser, "");
				PassWord = preference.getString("PassWord" + CurrentUser, "");

				Calendar y = (Calendar) c.clone(), t = (Calendar) c.clone();
				y.add(Calendar.DATE, -1);
				t.add(Calendar.DATE, 1);

				String HW[];
				// Fetch from database first
				HW = HWDB.getRecords(UserName, getDate(y));
				if (HW[0] == null) {
					String TEMP_HW[] = new String[10];
					TEMP_HW[0] = "";
					DisplayHomeWork(TEMP_HW, HomeWorkL);
				} else {
					DisplayHomeWork(HW, HomeWorkL);
				}

				HW = HWDB.getRecords(UserName, getDate(t));
				if (HW[0] == null) {
					String TEMP_HW[] = new String[10];
					TEMP_HW[0] = "";
					DisplayHomeWork(TEMP_HW, HomeWorkR);
				} else {
					DisplayHomeWork(HW, HomeWorkR);
				}

				GetToDateHomeWorkTaskWithCache(c);
			}
		}
	}

	private String[] GetToDateHomeWork(Calendar c, GetHomeworkTask t, boolean once) throws ParseException {
		// Convert Date. The day after 2000/1/1, e.g. 2013/12/29 is 5111
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date beginDate = format.parse("2000-01-01");
		
		// Get the date to display homework
		Date endDate = c.getTime();
		long day = (endDate.getTime() - beginDate.getTime())
				/ (24 * 60 * 60 * 1000);

		// Get HomeWork by trying twice
		String[] HomeWork = new String[10];

		if (!Login()) {
			HomeWork[0] = "请检查网络连接...";
			return HomeWork;			
		}
		
		try {
			boolean try_workaround_once = false;
			for (int i = 0; i < 2; i++) {
				ViewState = GetOldViewState("http://www.fushanedu.cn/jxq/jxq_User_jtzyck.aspx");

				HttpResponse httpResponse = null;
				HttpPost httppost = new HttpPost(
						"http://www.fushanedu.cn/jxq/jxq_User_jtzyck.aspx");
				httppost.addHeader("Content-Type",
						"application/x-www-form-urlencoded");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET",
						"MyCalendar"));
				nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""
						+ day));

				// For v2.0 and below
				// nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
				// "dDwxOTkyMjgzMzMzO3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDE+O2k8Mz47aTw0PjtpPDU+O2k8Nj47aTw3Pjs+O2w8dDw7bDxpPDI+O2k8Mz47aTw0PjtpPDY+O2k8OD47aTw5PjtpPDExPjtpPDEzPjtpPDE1PjtpPDE3PjtpPDE5Pjs+O2w8dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxFbmFibGVkOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxFbmFibGVkOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxGb3JlQ29sb3I7VGV4dDtfIVNCOz47bDwyPDMwLCAyMDAsIDMwPjvliJjnlYUo5a626ZW/KSzmgqjlpb3vvIHmrKLov47kvb/nlKgg5a625qCh5qGlIOagj+ebruOAgjtpPDQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47Pj47dDxAMDxwPHA8bDxTRDs+O2w8bDxTeXN0ZW0uRGF0ZVRpbWUsIG1zY29ybGliLCBWZXJzaW9uPTEuMC41MDAwLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OTwyMDEzLTEyLTI5Pjs+Oz4+Oz47Ozs7Ozs7Ozs7Pjs7Pjt0PHQ8cDxwPGw8RGF0YVRleHRGaWVsZDtEYXRhVmFsdWVGaWVsZDs+O2w8U2Nob29sTmFtZTtTY2hvb2xJRDs+Pjs+O3Q8aTwxPjtAPOeRnuWNjuagoeWMujs+O0A8Nzs+PjtsPGk8MD47Pj47Oz47dDx0PHA8cDxsPERhdGFUZXh0RmllbGQ7RGF0YVZhbHVlRmllbGQ7PjtsPEdyYWRlTmFtZTtHcmFkZUlEOz4+Oz47dDxpPDE+O0A85LiA5bm057qnOz47QDwzMDQ7Pj47bDxpPDA+Oz4+Ozs+O3Q8dDxwPHA8bDxEYXRhVGV4dEZpZWxkO0RhdGFWYWx1ZUZpZWxkOz47bDxDbGFzc05hbWU7Q2xhc3NJRDs+Pjs+O3Q8aTwxPjtAPDjnj607PjtAPDE5OTA7Pj47bDxpPDA+Oz4+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85oKo5b2T5YmN5p+l55yL55qE5pivIFw8Ylw+MjAxMy0xMi0yOSAg55Ge5Y2O5qCh5Yy6ICDkuIDlubTnuqcgIDjnj61cPC9iXD4gIOeahOWutuW6reS9nOS4mjs+Pjs+Ozs+Oz4+Oz4+O2w8bG9naW46YnRuZ3VhbmxpO2xvZ2luOmJ0bmhlbHA7bG9naW46YnRubG9nb3V0Oz4+94LyYf53nRfAOHwdbhL2sO77zS4="));

				// For v3.0 and above
				// nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
				// "dDwxOTkyMjgzMzMzO3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDE+O2k8Mz47aTw0PjtpPDU+O2k8Nj47aTw3Pjs+O2w8dDw7bDxpPDI+O2k8Mz47aTw0PjtpPDY+O2k8OD47aTw5PjtpPDExPjtpPDEzPjtpPDE1PjtpPDE3PjtpPDE5Pjs+O2w8dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxFbmFibGVkOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxFbmFibGVkOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxGb3JlQ29sb3I7VGV4dDtfIVNCOz47bDwyPDMwLCAyMDAsIDMwPjvliJjnlYUo5a626ZW/KSzmgqjlpb3vvIHmrKLov47kvb/nlKgg5a625qCh5qGlIOagj+ebruOAgjtpPDQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Oz47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47Pj47dDxAMDxwPHA8bDxTRDs+O2w8bDxTeXN0ZW0uRGF0ZVRpbWUsIG1zY29ybGliLCBWZXJzaW9uPTEuMC41MDAwLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OTwyMDE0LTAyLTEzPjs+Oz4+Oz47Ozs7Ozs7Ozs7Pjs7Pjt0PHQ8cDxwPGw8RGF0YVRleHRGaWVsZDtEYXRhVmFsdWVGaWVsZDs+O2w8U2Nob29sTmFtZTtTY2hvb2xJRDs+Pjs+O3Q8aTwxPjtAPOeRnuWNjuagoeWMujs+O0A8Nzs+PjtsPGk8MD47Pj47Oz47dDx0PHA8cDxsPERhdGFUZXh0RmllbGQ7RGF0YVZhbHVlRmllbGQ7PjtsPEdyYWRlTmFtZTtHcmFkZUlEOz4+Oz47dDxpPDE+O0A85LiA5bm057qnOz47QDwzMzU7Pj47bDxpPDA+Oz4+Ozs+O3Q8dDxwPHA8bDxEYXRhVGV4dEZpZWxkO0RhdGFWYWx1ZUZpZWxkOz47bDxDbGFzc05hbWU7Q2xhc3NJRDs+Pjs+O3Q8aTwxPjtAPDjnj607PjtAPDIwNDg7Pj47bDxpPDA+Oz4+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w85oKo5b2T5YmN5p+l55yL55qE5pivIFw8Ylw+MjAxNC0yLTEzICDnkZ7ljY7moKHljLogIOS4gOW5tOe6pyAgOOePrVw8L2JcPiAg55qE5a625bqt5L2c5LiaOz4+Oz47Oz47Pj47Pj47bDxsb2dpbjpidG5ndWFubGk7bG9naW46YnRuaGVscDtsb2dpbjpidG5sb2dvdXQ7Pj42BXDTJq0rubcerSCrW9xixkDTsw=="));

				nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE",
						ViewState));

				nameValuePairs.add(new BasicNameValuePair("SchoolName", "7"));
				nameValuePairs.add(new BasicNameValuePair("GradeName", "304"));
				nameValuePairs.add(new BasicNameValuePair("ClassName", "1990"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"GB2312"));
				if (t.isCancelled()) {
					httppost.abort();					
					return HomeWork;
				}
				httpResponse = httpclient.execute(httppost);
				if (t.isCancelled()) {
					httppost.abort();					
					return HomeWork;
				}
				int SC = httpResponse.getStatusLine().getStatusCode();
				if (SC == 200) {
					HomeWork = ReadHomeWork(httpResponse);
					httppost.abort();
					
					boolean HasHomework = false;
					for (int j=0; j<10; j++) {
						if (HomeWork[j] != null) {
							HasHomework = true;
							break;
						}
					}
					
					if (HasHomework || (!HasHomework && try_workaround_once) || once) {
						HWDB.createRecords(UserName, getDate(c), HomeWork);
						return HomeWork;
					} else {
						// This is probably a workaround, because the fushan network is unstable, and some times
						// the normal read can return empty although there are some homeworks. So we will try it
						// again by reading homework yesterday.
						Calendar yesterday = (Calendar) c.clone();
						yesterday.add(Calendar.DATE, -1);
						GetToDateHomeWork(yesterday, t, true);
						try_workaround_once = true;
						continue;
					}
				} else {
					httppost.abort();
					if (!Login())
						break;
				}
			}
		} catch (ClientProtocolException e) {
			// HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		} catch (Exception e) {
			// HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		}

		HomeWork[0] = "请检查网络连接...";
		return HomeWork;
	}

	public String[] GetTodayHomeWork(Calendar c, GetHomeworkTask t) throws ParseException {
		String[] HomeWork = new String[10];

		if (!Login()) {
			HomeWork[0] = "请检查网络连接...";
			return HomeWork;			
		}

		try {
			boolean try_workaround_once = false;
			for (int i = 0; i < 2; i++) {
				HttpGet get = new HttpGet(
						"http://www.fushanedu.cn/jxq/jxq_User_jtzyck.aspx");
				HttpResponse httpResponse = null;
				if (t.isCancelled()) {
					get.abort();					
					return HomeWork;
				}
				httpResponse = httpclient.execute(get);
				if (t.isCancelled()) {
					get.abort();					
					return HomeWork;
				}
				int SC = httpResponse.getStatusLine().getStatusCode();
				if (SC == 200) {
					HomeWork = ReadHomeWork(httpResponse);
					get.abort();
					
					// Check if we really read out homework.
					boolean HasHomework = false;
					for (int j=0; j<10; j++) {
						if (HomeWork[j] != null) {
							HasHomework = true;
							break;
						}
					}
					
					if (HasHomework || (!HasHomework && try_workaround_once)) {
						HWDB.createRecords(UserName, getDate(c), HomeWork);
						
						// Update current time
						LastTodayUpdate = Calendar.getInstance(); 
						
						return HomeWork;
					} else {
						// This is probably a workaround, because the fushan network is unstable, and some times
						// the normal read can return empty although there are some homeworks. So we will try it
						// again by reading homework yesterday.
						Calendar yesterday = (Calendar) c.clone();
						yesterday.add(Calendar.DATE, -1);
						GetToDateHomeWork(yesterday, t, true);
						try_workaround_once = true;
						continue;
					}
				} 

				// If not successful on reading homework, let's try again by logging into fushan network again.
				get.abort();
				if (!Login())
					break;
			}
		} catch (ClientProtocolException e) {
			// HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		} catch (IOException e) {
			// HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		}

		HomeWork[0] = "请检查网络连接...";
		return HomeWork;
	}
	
	private class GetHomeworkTask extends AsyncTask<Calendar, Integer, Long> {
		@Override
		protected Long doInBackground(Calendar... params) {
			return null;
		}

        @Override
        protected void onCancelled() {
	    	swipeLayout.setRefreshing(false);
            super.onCancelled();
        }
	}
	
	// Get homework data facility
	private class GetTodayHomeWorkTask extends GetHomeworkTask {
		private String[] HW = new String[10];
		private Calendar c;

		protected Long doInBackground(Calendar... parms) {
			try {
				if (isCancelled())
					return (long) 0;

				c = parms[0];
				// Log.e("GetTodayHomeWorkTask", getDate(c));
		    	swipeLayout.setRefreshing(true);
				HW = GetTodayHomeWork(c, this);
		    	swipeLayout.setRefreshing(false);
				return (long) 1;
			} catch (Exception pce) {
				// Log.e("DisplayMessageActivity", "PCE " + pce.getMessage());
			}
			return (long) 0;
		}

		protected void onPostExecute(Long result) {
			if (result == 0)
				return;
			
	    	if (HW[0] == "请检查网络连接...") {
				Toast SM = Toast.makeText(DisplayMessageActivity.this, "请检查网络连接...", 1);
				SM.show();
				return;
			}
			
			if (isCancelled())
				return;

			SetCurrentDate(c);
			DisplayHomeWork(HW, HomeWork);
		}
	}

	private class GetToDateHomeWorkTask extends GetHomeworkTask {
		private String[] HW = new String[10];
		private Calendar c;

		protected Long doInBackground(Calendar... parms) {
			try {
				if (isCancelled())
					return (long) 0;

				c = parms[0];
		    	swipeLayout.setRefreshing(true);
				HW = GetToDateHomeWork(c, this, false);
		    	swipeLayout.setRefreshing(false);
				return (long) 1;
			} catch (Exception pce) {
				// Log.e("DisplayMessageActivity", "PCE " + pce.getMessage());
			}
			return (long) 0;
		}

		protected void onPostExecute(Long result) {
			if (result == 0)
				return;

			if (HW[0] == "请检查网络连接...") {
				Toast SM = Toast.makeText(DisplayMessageActivity.this, "请检查网络连接...", 1);
				SM.show();
				return;
			}
			
			if (isCancelled())
				return;

			SetCurrentDate(c);
			DisplayHomeWork(HW, HomeWork);
		}
	}

	// We won't update today in limited time
	private boolean EnoughTimePassed()
	{
		// We won't update today again in 5 minutes
		Calendar now = Calendar.getInstance();
		Date d1 = now.getTime();

		if (LastTodayUpdate != null) {
			Date d2 = LastTodayUpdate.getTime();
			long diff = d1.getTime() - d2.getTime();
			long diffSeconds = diff / 1000 % 60;
			
			if (diffSeconds < 60) {
				return false;
			}
			return true;
		}
		
		return true;
	}

	private void CancelLastTask() {
		if (LastTask != null && LastTask.getStatus()==AsyncTask.Status.RUNNING) {
			LastTask.cancel(true);
			LastTask = null;
		}		
	}
	
	private void GetToDateHomeWorkTaskFromNetwork(Calendar c, boolean HomeworkEmpty) {
		CancelLastTask();
		
		if (isToday(c)) {
			if (!EnoughTimePassed()) {
		    	swipeLayout.setRefreshing(false);
				return;
			}
			
			LastTask = new GetTodayHomeWorkTask().execute(c);
		} else {
			if (HomeworkEmpty)
				LastTask = new GetToDateHomeWorkTask().execute(c);				
		}
	}

	private void GetToDateHomeWorkTaskWithCache(Calendar c) {
		String[] HW = new String[10];

		// Fetch from database first
		HW = HWDB.getRecords(UserName, getDate(c));
		if (HW[0] != null) {
			DisplayHomeWork(HW, HomeWork);
		}

		GetToDateHomeWorkTaskFromNetwork(c, (HW[0]==null));
	}

	// This method is to parse home work html string
	private String[] GetHomeWork(Document doc) {
		String[] HomeWork = new String[10];

		Elements link = doc.select("a[href]");
		for (Element l : link) {
			String oldl = l.attr("href");
			if (!oldl.startsWith("http"))
				l.attr("href", "http://www.fushanedu.cn" + oldl);
		}

		link = doc.select("img[src]");
		for (Element l : link) {
			String oldl = l.attr("src");
			String newl;
			if (!oldl.startsWith("http")) {
				newl = "http://www.fushanedu.cn" + oldl;
				l.attr("href", newl);
			} else {
				newl = oldl;
			}
			newl = newl.replaceAll("WEBADMIN", "");
			l.wrap("<a href='" + newl + "'></a>");
		}

		Elements bold = doc.select("b");
		int i = 0;
		for (Element b : bold) {
			String bn = b.html();
			if (bn.indexOf("数学作业") != -1 || bn.indexOf("英语作业") != -1
					|| bn.indexOf("语文作业") != -1 || bn.indexOf("音乐作业") != -1
					|| bn.indexOf("体育作业") != -1 || bn.indexOf("美术作业") != -1
					|| bn.indexOf("自然作业") != -1 || bn.indexOf("信息作业") != -1
					|| bn.indexOf("劳技作业") != -1 || bn.indexOf("国际理解作业") != -1) {
				String HWDetails = b.parent().parent().nextElementSibling()
						.html();
				String AddNewLine = "";
				if (!HWDetails.endsWith("</td>"))
					AddNewLine = "<BR>";
				HomeWork[i] = b.parent().parent().html() + "<BR><BR>"
						+ HWDetails + AddNewLine;
				i = i + 1;
			}
		}

		return HomeWork;
	}

	private String[] ReadHomeWork(HttpResponse httpResponse) {
		String[] HomeWork = new String[10];

		try {
			HttpEntity entity = httpResponse.getEntity();
			InputStream is;
			is = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "GB2312"));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			String response = sb.toString();
			Document doc = Jsoup.parse(response);
			HomeWork = GetHomeWork(doc);
			return HomeWork;
		} catch (IllegalStateException e) {
			HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		} catch (IOException e) {
			HomeWork[0] = e.toString();
			// Log.e("DisplayMessageActivity", "E " + e.getMessage());
		}

		HomeWork[0] = "请检查网络连接...";
		return HomeWork;
	}

	// ////////////////////////////////////////////////////////
	// The following is to display home work
	// ////////////////////////////////////////////////////////

	public Map<String, Drawable> imageMap;

	private class MyCustomAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		private LayoutInflater mInflater;

		public MyCustomAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final String item, int pos) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("HomeWorkItem", item);
			listItem.add(map);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return listItem.size();
		}

		@Override
		public String getItem(int position) {
			return listItem.get(position).get("HomeWorkItem");
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.homework, null);
				holder = (TextView) convertView.findViewById(R.id.HomeWorkItem);
				holder.setMovementMethod(LinkMovementMethod.getInstance());
				convertView.setTag(holder);
			} else {
				holder = (TextView) convertView.getTag();
			}

			fontsize = holder.getTextSize();
			if (bigfont) {
				fontsize = 24;
				holder.setTextSize(fontsize);
			} else {
				fontsize = 18;
				holder.setTextSize(fontsize);				
			}

			URLImageParser imageGetter = new URLImageParser(HomeWork, position);

			// Have to pass HomeWork(listview) here, because Html.fromHtml will
			// be updated from time to time, so we can't keep an unique textview
			// for async image loader.
			holder.setText(Html.fromHtml(
					listItem.get(position).get("HomeWorkItem"), imageGetter, null));
			return convertView;
		}
	}

	@SuppressWarnings("deprecation")
	public class URLDrawable extends BitmapDrawable {
		// the drawable that you need to set, you could set the initial drawing
		// with the loading image if you need to
		protected Drawable drawable;

		@Override
		public void draw(Canvas canvas) {
			// override the draw to facilitate refresh function later
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}

	// This is the class to get the image embedded in homework
	private class URLImageParser implements ImageGetter {
		View container;
		int pos;

		/***
		 * Construct the URLImageParser which will execute AsyncTask and refresh
		 * the container
		 * 
		 * @param t
		 * @param c
		 */
		public URLImageParser(View t, int pos) {
			this.container = t;
			this.pos = pos;
		}

		public Drawable getDrawable(String source) {
			if (imageMap.containsKey(source)) {
				Drawable drawable = imageMap.get(source);
				if (drawable != null) {
					return drawable;
				}
			}

			URLDrawable urlDrawable = new URLDrawable();

			// get the actual source
			ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(
					urlDrawable);

			asyncTask.execute(source);

			// return reference to URLDrawable where I will change with actual
			// image from the src tag
			imageMap.put(source, urlDrawable);
			return urlDrawable;
		}

		//////////////////////////////////////////////////////////
		// The class for drawing pictures in today's homework.
		//////////////////////////////////////////////////////////
		public class ImageGetterAsyncTask extends
				AsyncTask<String, Void, Drawable> {
			URLDrawable urlDrawable;
			float scale = 1;

			public ImageGetterAsyncTask(URLDrawable d) {
				this.urlDrawable = d;
			}

			@Override
			protected Drawable doInBackground(String... params) {
				String source = params[0];
				return fetchDrawable(source);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				if (result == null) {
					return;
				}
				// set the correct bound according to the result from HTTP call
				urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(),
						0 + result.getIntrinsicHeight());

				// change the reference of the current drawable to the result
				// from the HTTP call
				urlDrawable.drawable = result;

				// redraw the image by invalidating the container
				URLImageParser.this.container.invalidate();

				// The children of the ViewGroup do not correspond 1-to-1 with
				// the items in the list, for a ListView. Instead, the
				// ViewGroup's children correspond to only those views
				// that are visible right now. So getChildAt() operates on an
				// index that's internal to the ViewGroup and doesn't
				// necessarily have anything to do with the position in the list
				// that the ListView uses.

				View holder;
				holder = HomeWork.getChildAt(pos);
				if (holder != null) {
					TextView tv = (TextView) holder
							.findViewById(R.id.HomeWorkItem);
					tv.setHeight((tv.getHeight() + (int) (result
							.getIntrinsicHeight() * scale)));
					// Pre ICS
					tv.setEllipsize(null);
				}
			}

			/***
			 * Get the Drawable from URL
			 * 
			 * @param urlString
			 * @return
			 */
			public Drawable fetchDrawable(String urlString) {
				try {
					Drawable drawable;
					if (urlString.startsWith("data:image")) {
						// Get image string
						String ImageString = "";
						Matcher m = Pattern.compile("data:image/[^;]*;base64,(.*)")
								.matcher(urlString);
						if (m.find()) {
							ImageString = m.group(1);
						}

					    byte[] bytes = Base64.decode(ImageString, Base64.DEFAULT);  
					    Bitmap bitMapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);  
						drawable = new BitmapDrawable(bitMapImage);
						drawable.setBounds(
								0,
								(int) (drawable.getIntrinsicHeight() - bitMapImage.getHeight()),
								(int) (bitMapImage.getWidth()),
								(int) (drawable.getIntrinsicHeight()));
						return drawable;
					} else if (urlString.startsWith("/")) {
						urlString = "http://www.fushanedu.cn" + urlString;
						urlString = urlString.replaceAll("/WEBADMIN", "");

						Bitmap bitMapImage;
						bitMapImage = ImageLoader.getBitmapFromMemory(urlString);
						if (bitMapImage == null) {
							bitMapImage = ImageLoader.getBitmapFromUrl(urlString, true);
						}
						drawable = new BitmapDrawable(bitMapImage);
/*						
						InputStream is;
						try {
							DefaultHttpClient httpClient = new DefaultHttpClient();
							HttpGet request = new HttpGet(urlString);
							HttpResponse response = httpClient.execute(request);
							is =  response.getEntity().getContent();
						} catch (Exception e) {
							return null;
						}
						
						drawable = Drawable.createFromStream(is, "src");
						*/
						if (urlString.contains("emotImages")) {
							scale = dm.scaledDensity;
							drawable.setBounds(
									0,
									(int) (drawable.getIntrinsicHeight() - fontsize),
									(int) fontsize,
									(int) (drawable.getIntrinsicHeight()));
						} else {
							scale = 1;
							drawable.setBounds(0, 0,
									(int) (drawable.getIntrinsicWidth() * scale),
									(int) (drawable.getIntrinsicHeight() * scale));
						}
						return drawable;
					} else {
						return null;
					}
				} catch (Exception e) {
					return null;
				}
			}
		}
	}

	// ////////////////////////////////////////////////////////
	// Main Entry of Fushan Homework
	// ////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		 * .detectDiskReads() .detectDiskWrites() .detectAll() // or
		 * .detectAll() for all detectable problemsparams.get("__VIEWSTATE")
		 * .penaltyLog() .build()); StrictMode.setVmPolicy(new
		 * StrictMode.VmPolicy.Builder() .detectLeakedSqlLiteObjects()
		 * .detectLeakedClosableObjects() .penaltyLog() .penaltyDeath()
		 * .build());
		 */
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_display_message);
		RelativeLayout bg = (RelativeLayout) findViewById(R.id.DisplayHomework);
		bg.setBackgroundColor(Color.parseColor("#F5F5DC"));

		// Create thread safe http client
		BasicHttpParams params = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory
				.getSocketFactory();
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		httpclient = new DefaultHttpClient(cm, params);

		// create calendar
		c = Calendar.getInstance();
		SetCurrentDate(c);
		TextView CurrentDate = (TextView) findViewById(R.id.CurrentDate);
		CurrentDate.setTextSize(20);
		CurrentDate.setOnClickListener(listener3);

		// set click lister of buttons
		Button LeftArrow = (Button) findViewById(R.id.LeftArrow);
		LeftArrow.setOnClickListener(listener1);
		LeftArrow.setTextColor(Color.WHITE);
		LeftArrow.setBackgroundColor(Color.parseColor("#9D61AB"));
		Button RightArrow = (Button) findViewById(R.id.RightArrow);
		RightArrow.setOnClickListener(listener2);
		RightArrow.setTextColor(Color.WHITE);
		RightArrow.setBackgroundColor(Color.parseColor("#9D61AB"));

		// Get original password
		SharedPreferences preference = getSharedPreferences("person",
				Context.MODE_PRIVATE);
		CurrentUser = preference.getInt("CurrentUser", 1);
		UserName = preference.getString("UserName" + CurrentUser, "");
		PassWord = preference.getString("PassWord" + CurrentUser, "");
		bigfont = preference.getBoolean("fontsize", false);

		imageMap = new HashMap<String, Drawable>();
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		HWDB = new HomeworkDatabase(DisplayMessageActivity.this);
		HWDB.open();

		sImageCache = new HashMap<String,SoftReference<Bitmap>>();
		ImageLoader = new LoaderImpl(sImageCache);
		String defaultDir = getCacheDir().getAbsolutePath();
		ImageLoader.setCachedDir(defaultDir);
		ImageLoader.setCache2File(true);
		
		// Display homework using page viewer
		InitViewPager();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.User1: {
			SharedPreferences preference = getSharedPreferences("person",
					Context.MODE_PRIVATE);
			int ID = preference.getInt("CurrentUser", 1);
			if (ID == 1)
				return true;

			// Get original password
			CurrentUser = 1;
			UserName = preference.getString("UserName" + 1, "");
			PassWord = preference.getString("PassWord" + 1, "");

			if (UserName == "") {
				Intent intent = new Intent();
				intent.setClass(DisplayMessageActivity.this, MainActivity.class);
				intent.putExtra("CurrentUser", CurrentUser);
				startActivityForResult(intent, 0);
				return true;
			} else {
				CancelLastTask();
		    	swipeLayout.setRefreshing(true);
				LastTask = new LoginTask().execute(c);
				return true;
			}
		}
		case R.id.User2: {
			SharedPreferences preference = getSharedPreferences("person",
					Context.MODE_PRIVATE);
			int ID = preference.getInt("CurrentUser", 1);
			if (ID == 2)
				return true;

			CurrentUser = 2;
			UserName = preference.getString("UserName" + 2, "");
			PassWord = preference.getString("PassWord" + 2, "");

			if (UserName == "") {
				Intent intent = new Intent();
				intent.setClass(DisplayMessageActivity.this, MainActivity.class);
				intent.putExtra("CurrentUser", CurrentUser);
				startActivityForResult(intent, 0);
				return true;
			} else {
				CancelLastTask();
		    	swipeLayout.setRefreshing(true);
				LastTask = new LoginTask().execute(c);
				return true;
			}
		}
		case R.id.Fontsize: {
			bigfont = !bigfont;
			SharedPreferences preference = getSharedPreferences("person",
					Context.MODE_PRIVATE);
			Editor edit = preference.edit();
			edit.putBoolean("fontsize", bigfont);
			edit.commit();
			HomeWork.invalidateViews();
			return true;
		}
		case R.id.About: {
			Intent intent = new Intent();
			intent.setClass(DisplayMessageActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		String Mark1, Mark2, UserName1, UserName2, RealName1, RealName2;

		SharedPreferences preference = getSharedPreferences("person",
				Context.MODE_PRIVATE);
		int ID = preference.getInt("CurrentUser", 1);
		UserName1 = preference.getString("UserName1", "");
		UserName2 = preference.getString("UserName2", "");
		RealName1 = preference.getString("RealName1", "用户1");
		RealName2 = preference.getString("RealName2", "用户2");

		Mark1 = (ID == 1) ? " *" : "";
		Mark2 = (ID == 2) ? " *" : "";

		menu.clear();
		menu.add(Menu.NONE, R.id.User1, Menu.NONE, UserName1 + "(" + RealName1
				+ ")" + Mark1);
		menu.add(Menu.NONE, R.id.User2, Menu.NONE, UserName2 + "(" + RealName2
				+ ")" + Mark2);
		menu.add(Menu.NONE, R.id.Fontsize, Menu.NONE, bigfont ? "小字体" : "大字体");
		menu.add(Menu.NONE, R.id.About, Menu.NONE, "关于");
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			// Get new current user passed back from login window
			Bundle b = data.getExtras();
			CurrentUser = b.getInt("CurrentUser");

			// Get original password
			SharedPreferences preference = getSharedPreferences("person",
					Context.MODE_PRIVATE);
			UserName = preference.getString("UserName" + CurrentUser, "");
			PassWord = preference.getString("PassWord" + CurrentUser, "");

			// Try new login
			CancelLastTask();
	    	swipeLayout.setRefreshing(true);
			LastTask = new LoginTask().execute(c);
			break;
		case RESULT_CANCELED:
			break;
		default:
			break;
		}
	}
}