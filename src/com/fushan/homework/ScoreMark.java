package com.fushan.homework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private AsyncTask<Context, Integer, Long> task = null;
    private String courses[] = {"语文", "数学", "英语"};
    private String grades[] = {"六年下", "六年上", "五年下", "五年上", "四年下", "四年上", "三年下", "三年上", "二年下", "二年上", "一年下", "一年上"};
    private String terms[] = {"期末", "期中"};
	private String CurrentUser;
	private boolean FirstScoreUpdate;
	private boolean online;

    private class ChangeBGAdapater extends SimpleAdapter {
		public ChangeBGAdapater(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		     convertView = super.getView(position, convertView, parent);
		     
		     TextView v = null;
		     int c;
		     if (score_sort == 1) {
		    	 v = (TextView) convertView.findViewById(R.id.CourseName);
		    	 String t = (String) v.getText();
		    	 int i = 0;
		    	 for (i=0; i<courses.length; i++) {
		    		 if (t.equals(courses[i])) {
		    			 break;
		    		 }
		    	 }
		    	 int color = i % 2;
		    	 c = (color == 0) ? Color.TRANSPARENT : Color.LTGRAY;
			     v.setBackgroundColor(c);

			     v = (TextView) convertView.findViewById(R.id.CourseGrade);
		    	 v.setBackgroundColor(c);
		     } else {
		    	 v = (TextView) convertView.findViewById(R.id.CourseGrade);
		    	 String t = (String) v.getText();
		    	 int i = 0;
		    	 for (i=0; i<grades.length; i++) {
		    		 if (t.equals(grades[i])) {
		    			 break;
		    		 }
		    	 }
		    	 int color = i % 2;
		    	 c = (color == 0) ? Color.TRANSPARENT : Color.LTGRAY;
			     v.setBackgroundColor(c);

			     v = (TextView) convertView.findViewById(R.id.CourseName);
		    	 v.setBackgroundColor(c);
		     }
	    	 v = (TextView) convertView.findViewById(R.id.CourseTerm);
	    	 v.setBackgroundColor(c);
	    	 v = (TextView) convertView.findViewById(R.id.CourseScore);
	    	 v.setBackgroundColor(c);
	    	 v = (TextView) convertView.findViewById(R.id.CourseScoreTop);
	    	 v.setBackgroundColor(c);
	    	 v = (TextView) convertView.findViewById(R.id.CourseScoreAverage);
	    	 v.setBackgroundColor(c);
	    	 v = (TextView) convertView.findViewById(R.id.CourseScoreVariance);
	    	 v.setBackgroundColor(c);
		     
		     return convertView;
		}
    }
    
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

	private boolean GetScoreItem(String item) {
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
				if (!FirstScoreUpdate) {
			        // Clean scores
			        for (int i=0; i<Score.length; i++) {
			        	Score[i] = null;
			        }
					NumOfScore = 0;
				}
				ReadScore(httpResponse);
				FirstScoreUpdate = true;
				httppost.abort();
				return true;
			}
			httppost.abort();
		} catch (ClientProtocolException e) {
		} catch (Exception e) {
		}
		
		return false;
	}
	
	private void GetScore(Context c) throws ParseException {
		boolean Got1, Got2, Got3;
		FirstScoreUpdate = false;
		online = Got1 = Got2 = Got3 = false;
		
		if (!DisplayMessageActivity.login) {
			return;
		}
		
		Got1 = GetScoreItem("1");
		Got2 = GetScoreItem("2");
		Got3 = GetScoreItem("3");
		
		online = (Got1 && Got2 && Got3);
	}	

	private void ShowScoreMark(Context c) {
		List<HashMap<String,Object>> data;
		HashMap<String,Object> map;
		SimpleAdapter adapter;

		// Rest the number of scores
		NumOfScore = 0;
		for (int i=0; i<Score.length; i++) {
			if (Score[i] != null)
				NumOfScore++;
		}

		data = new ArrayList<HashMap<String,Object>>();  
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

		adapter = new ChangeBGAdapater(c, data, R.layout.scoremark,
        		new String[] {"CourseName","CourseGrade","CourseTerm","CourseScore","CourseScoreTop","CourseScoreAverage","CourseScoreVariance"},
        		new int[] {R.id.CourseName, R.id.CourseGrade, R.id.CourseTerm, R.id.CourseScore,R.id.CourseScoreTop,R.id.CourseScoreAverage,R.id.CourseScoreVariance});
        
        listview.setAdapter(adapter);  
	}

	private class GetScoreTask extends AsyncTask<Context, Integer, Long> {
		private Context c = null;
		
		@Override
		protected Long doInBackground(Context... params) {
			try {
				c = params[0];
				GetScore(c);
				if (online)
					DisplayMessageActivity.HWDB.createSMRecords(CurrentUser, Score);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return (long) 1;
		}

		protected void onPostExecute(Long result) {
	    	swipeLayout.setRefreshing(false);		
			if (online)
				ShowScoreMark(c);
		}

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scoremark_list);	

		TextView v;
		v = (TextView) findViewById(R.id.CourseNameTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseGradeTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseTermTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseScoreTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseScoreTopTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseScoreAverageTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));
		v = (TextView) findViewById(R.id.CourseScoreVarianceTitile);
		v.setTextColor(Color.WHITE);
		v.setBackgroundColor(Color.parseColor("#9D61AB"));

		LinearLayout bg = (LinearLayout) findViewById(R.id.ScoreMarkList);
		bg.setBackgroundColor(Color.parseColor("#F5F5DC"));

        listview = (ListView)this.findViewById(R.id.ScoreMark);
		listview.setBackgroundColor(Color.parseColor("#F5F5DC"));

        swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.score_swipe_refresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright, android.R.color.holo_orange_light);  
    	swipeLayout.setRefreshing(true);

        Intent mIntent = getIntent();  
        Bundle b = mIntent.getExtras(); 
        CurrentUser = b.getString("CurrentUser");
        
        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
        score_sort = preference.getInt("score_sort", 1);

        // show offline data
        Score = HomeworkDatabase.getSMRecords(CurrentUser);
        ShowScoreMark(this);

    	task = new GetScoreTask().execute(this);
	}

	@Override
	public void onRefresh() {
		if (task != null && task.getStatus()==AsyncTask.Status.RUNNING) {
			return;
		}

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
