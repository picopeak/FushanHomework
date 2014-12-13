package com.fushan.homework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fushan.homework.DisplayMessageActivity.GetHomeworkTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreMark extends Activity implements OnRefreshListener {

	private ListView listview;
	private String ViewState = "";
	private String[][] Score = new String[72][];
	private int NumOfScore = 0;
	private SwipeRefreshLayout swipeLayout;
    private int score_sort = 1;

	private void GetScoreMark(Document doc) {
		String[] ScoreMark = new String[11];
		boolean FindScore = false;
		String term = "";
		
		Elements tbl = doc.select("span[id]");
		int i = 0;

		for (Element t : tbl) {
			String s = t.text();
			if (s.indexOf("欢迎使用") != -1) {
				// Make sure score data is obtained, and return immediately otherwise.
				FindScore = true;
				continue;
			}
			
			if (!FindScore)
				continue;
			
			// Skip the case like "2013学年第二学期", and keep the case like "2014学年第一学期二年级数学期中练习"
			if (s.indexOf("学年第") != -1 && s.indexOf("年级") == -1) {
				if (s.indexOf("第一") != -1)
					term="上";
				else if (s.indexOf("第二") != -1)
					term="下";
				continue;
			}

			ScoreMark[i] = s;
			i++;
			
			if (i>10) {
				// Reset back to next score item
				i = 0;

				Score[NumOfScore] = new String[11];

				String m = ScoreMark[0];
				if (m.indexOf("第一") != -1)
					term="上";
				else if (m.indexOf("第二") != -1)
					term="下";

				if (m.indexOf("数学") != -1) 
					Score[NumOfScore][0] = "数学";
				else if (m.indexOf("语文") != -1) 
					Score[NumOfScore][0] = "语文";
				else if (m.indexOf("英语") != -1) 
					Score[NumOfScore][0] = "英语";

				if (m.indexOf("一年级") != -1) 
					Score[NumOfScore][1] = "一年";
				else if (m.indexOf("二年级") != -1) 
					Score[NumOfScore][1] = "二年";
				else if (m.indexOf("三年级") != -1) 
					Score[NumOfScore][1] = "三年";
				else if (m.indexOf("四年级") != -1) 
					Score[NumOfScore][1] = "四年";
				else if (m.indexOf("五年级") != -1) 
					Score[NumOfScore][1] = "五年";
				else if (m.indexOf("六年级") != -1) 
					Score[NumOfScore][1] = "六年";
				Score[NumOfScore][1] = Score[NumOfScore][1] + term;
				
				Score[NumOfScore][2] = ScoreMark[1].substring(0, 2);
				Score[NumOfScore][3] = ScoreMark[2];
				Score[NumOfScore][4] = ScoreMark[8];
				Score[NumOfScore][5] = ScoreMark[9];
				Score[NumOfScore][6] = ScoreMark[10];
				
				// Next Item
				NumOfScore++;
			}
		}
	}
	
	private void ReadScore(HttpResponse httpResponse) {
		try {
			HttpEntity entity = httpResponse.getEntity();
			InputStream is;
			is = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GB2312"));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			String response = sb.toString();
			Document doc = Jsoup.parse(response);
			GetScoreMark(doc);
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
	}

	private void GetScoreItem(String item) {
		try {
/*
			HttpResponse httpResponse = null;
			HttpGet httpget = new HttpGet("http://www.fushanedu.cn/jxq/jxq_User_xscjcx_Sh.aspx?SubjectID=1");
			httpget.addHeader("Content-Type", "application/x-www-form-urlencoded");
			httpResponse = DisplayMessageActivity.httpclient.execute(httpget);
*/			
			String url = "http://www.fushanedu.cn/jxq/jxq_User_xscjcx_Sh.aspx?SubjectID="+item;
			ViewState = DisplayMessageActivity.GetOldViewState(url);
			HttpResponse httpResponse = null;
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("Content-Type","application/x-www-form-urlencoded");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", ViewState));
			nameValuePairs.add(new BasicNameValuePair("ShowOtherTermScores", "%CF%D4%CA%BE%CB%F9%D3%D0%B2%E2%CA%D4%B3%C9%BC%A8%BC%C7%C2%BC%A3%A8%B0%FC%C0%A8%C6%E4%CB%FB%D1%A7%C6%DA%A3%A9"));
			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "GB2312"));
			httpResponse = DisplayMessageActivity.httpclient.execute(httppost);
			int SC = httpResponse.getStatusLine().getStatusCode();
			if (SC == 200) {
				ReadScore(httpResponse);
			}
			httppost.abort();			
		} catch (ClientProtocolException e) {
		} catch (Exception e) {
		}
	}
	
	private void GetScore() throws ParseException {
		GetScoreItem("1");
		GetScoreItem("2");
		GetScoreItem("3");
	}	

	private void ShowScoreMark(Context c) {
		List<HashMap<String,Object>> data;
		HashMap<String,Object> map;
		SimpleAdapter adapter;

		data = new ArrayList<HashMap<String,Object>>();  
        map = new HashMap<String,Object>();  
        map.put("CourseName", "课程"); 
        map.put("CourseGrade", "年级");  
        map.put("CourseTerm", "学期"); 
        map.put("CourseScore", "成绩");
        map.put("CourseScoreTop", "最高");
        map.put("CourseScoreAverage", "平均");
        map.put("CourseScoreVariance", "方差");
        data.add(map);

        String courses[] = {"语文", "数学", "英语"};
        String grades[] = {"六年下", "六年上", "五年下", "五年上", "四年下", "四年上", "三年下", "三年上", "二年下", "二年上", "一年下", "一年上"};
        String terms[] = {"期末", "期中"};
        
        if (Score != null) {
        	if (score_sort == 1) {
				for (int i=0; i<NumOfScore; i++) {
			        map = new HashMap<String,Object>();  
			        map.put("CourseName", Score[i][0]); 
			        map.put("CourseGrade", Score[i][1]);  
			        map.put("CourseTerm", Score[i][2]);  
			        map.put("CourseScore", Score[i][3]);  
			        map.put("CourseScoreTop", Score[i][4]);  
			        map.put("CourseScoreAverage", Score[i][5]);  
			        map.put("CourseScoreVariance", Score[i][6]);  
			        data.add(map);
				}        		
        	} else {
        		for (int j=0; j<grades.length; j++) {
        			String g = grades[j];
            		for (int k=0; k<terms.length; k++) {
            			String t = terms[k];
						for (int i=0; i<NumOfScore; i++) {
							String gr = Score[i][1]; 
							String te = Score[i][2]; 
							if (!g.equals(gr) || !t.equals(te))
								continue;
					        map = new HashMap<String,Object>();  
					        map.put("CourseName", Score[i][0]); 
					        map.put("CourseGrade", Score[i][1]);  
					        map.put("CourseTerm", Score[i][2]);  
					        map.put("CourseScore", Score[i][3]);  
					        map.put("CourseScoreTop", Score[i][4]);  
					        map.put("CourseScoreAverage", Score[i][5]);  
					        map.put("CourseScoreVariance", Score[i][6]);  
					        data.add(map);
						}
            		}
        		}
        	}
		}

		adapter = new SimpleAdapter(c, data, R.layout.scoremark,
        		new String[] {"CourseName","CourseGrade","CourseTerm","CourseScore","CourseScoreTop","CourseScoreAverage","CourseScoreVariance"},
        		new int[] {R.id.CourseName, R.id.CourseGrade, R.id.CourseTerm, R.id.CourseScore,R.id.CourseScoreTop,R.id.CourseScoreAverage,R.id.CourseScoreVariance});
        
        listview.setAdapter(adapter);  
    	swipeLayout.setRefreshing(false);		
	}
	private class GetScoreTask extends AsyncTask<Context, Integer, Long> {
		private Context c = null;
		
		@Override
		protected Long doInBackground(Context... params) {
			try {
				c = params[0];
				GetScore();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return (long) 1;
		}

		protected void onPostExecute(Long result) {
			ShowScoreMark(c);
		}

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scoremark_list);	

		LinearLayout bg = (LinearLayout) findViewById(R.id.ScoreMarkList);
		bg.setBackgroundColor(Color.parseColor("#F5F5DC"));

        listview = (ListView)this.findViewById(R.id.ScoreMark);
		listview.setBackgroundColor(Color.parseColor("#F5F5DC"));

        swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.score_swipe_refresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright, android.R.color.holo_orange_light);  
    	swipeLayout.setRefreshing(true);
		
    	AsyncTask<Context, Integer, Long> task = new GetScoreTask().execute(this);
	}

	@Override
	public void onRefresh() {
    	swipeLayout.setRefreshing(false);
	}

	public boolean onPrepareOptionsMenu(Menu menu)
	{
        String mark1, mark2;

        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
        score_sort = preference.getInt("score_sort", 1);
        mark1 = (score_sort == 1) ? "*" : "";
        mark2 = (score_sort == 2) ? "*" : "";
        	
        menu.clear();
        menu.add(Menu.NONE, R.id.course, Menu.NONE, "按课程排序 "+mark1);
		menu.add(Menu.NONE, R.id.grade, Menu.NONE, "按年级排序 "+mark2);
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
		case R.id.course: {
	        if (score_sort == 1)
	        	return true;

	        score_sort = 1;
			SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
			Editor edit = preference.edit();
			edit.putInt("score_sort", score_sort);
			edit.commit();
			
			ShowScoreMark(this);
            return true;
		}
		case R.id.grade: {
	        if (score_sort == 2)
	        	return true;
	        
	        score_sort = 2;
			SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
			Editor edit = preference.edit();
			edit.putInt("score_sort", score_sort);
			edit.commit();

			ShowScoreMark(this);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}	
}
