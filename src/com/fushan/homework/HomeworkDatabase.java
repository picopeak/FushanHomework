package com.fushan.homework;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HomeworkDatabase {
	private static final String DATABASE_NAME = "MyStorage";
	private static final String DATABASE_TABLE_HW = "HomeworkDatabase";	
	private static final String DATABASE_TABLE_SM = "ScoremarkDatabase";	

	public static final String _ID_i = "_id";
	public static final String _USER = "_user";
	public static final String _DATE = "_date";
	public static final String _COURSE = "_course";
	public static final String _CONTENT = "_content";

	public static final String _GRADE = "_grade";
	public static final String _TERM = "_term";
	public static final String _SCORE_MARK = "_score_mark";
	public static final String _SCORE_TOP = "_score_top";
	public static final String _SCORE_AVG = "_score_avg";
	public static final String _SCORE_VARIANCE = "_score_variance";
	
    private static final String DATABASE_CREATE_HW =
            "create table if not exists " + DATABASE_TABLE_HW +"("
            		+ _ID_i + " integer primary key, "
                    + _USER + " text, "
                    + _DATE + " text, "
                    + _COURSE + " text, " 
                    + _CONTENT + " text)";

    private static final String DATABASE_CREATE_SM =
            "create table if not exists " + DATABASE_TABLE_SM +"("
            		+ _ID_i + " integer primary key, "
                    + _USER + " text, "
                    + _COURSE + " text, " 
                    + _GRADE + " text, " 
                    + _TERM + " text, " 
                    + _SCORE_MARK + " text, " 
                    + _SCORE_TOP + " text, " 
                    + _SCORE_AVG + " text, " 
                    + _SCORE_VARIANCE + " text)";

    class HomeworkDatabaseHelper extends SQLiteOpenHelper {
		public HomeworkDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DATABASE_CREATE_HW);
			db.execSQL(DATABASE_CREATE_SM);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			//            db.execSQL("DROP TABLE IF EXISTS " + "'" + DATABASE_NAME + "'");
			//            onCreate(db);
		}
	}

    ContentValues mValues = null;
    private Context mCtx;
    private HomeworkDatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    
    public HomeworkDatabase(Context context) {
		mCtx = context;
		mValues = new ContentValues();
	}

	private String getCourseName(String c) {
		if (c.indexOf("数学作业") != -1)
			return "数学作业";
		if (c.indexOf("英语作业") != -1)
			return "英语作业";
		if (c.indexOf("语文作业") != -1)
			return "语文作业";
		if (c.indexOf("音乐作业") != -1)
			return "音乐作业";
		if (c.indexOf("体育作业") != -1)
			return "体育作业";
		if (c.indexOf("美术作业") != -1)
			return "美术作业";
		if (c.indexOf("自然作业") != -1)
			return "自然作业";
		if (c.indexOf("信息作业") != -1)
			return "信息作业";
		if (c.indexOf("劳技作业") != -1)
			return "劳技作业";
		if (c.indexOf("国际理解作业") != -1)
			return "国际理解作业";
		return "";
	}
	
    public HomeworkDatabase open() {
    	try {
	        mDbHelper = new HomeworkDatabaseHelper(mCtx, DATABASE_NAME, null, 1);
	        mDb = mDbHelper.getWritableDatabase();
	        return this;
		} catch (Exception e) {
			// Log.e("HomeworkDatabase", e.getMessage());
		}
    	
    	return null;
    }

	private long createOneHWRecord(String user, String date, String course, String content){
		mValues.clear();
    	mValues.put(_USER, user);
    	mValues.put(_DATE, date);
    	mValues.put(_COURSE, course);
    	mValues.put(_CONTENT, content );
    	return mDb.insert(DATABASE_TABLE_HW, "NA", mValues);
    }
	
	public void createHWRecords(String user, String date, String[] HW) {
		// Remove old records from database
		String[] Args = new String[2];
		Args[0] = user;
		Args[1] = date;
		mDb.delete(DATABASE_TABLE_HW, _USER + "=? and " + _DATE + "=?", Args); 
		
		// Insert new records into database
		boolean HasHomework = false;
		for (int i=0; i<10; i++) {
			if (HW[i] != null) {
				createOneHWRecord(user, date, getCourseName(HW[i]), HW[i]);
				HasHomework = true;
			}
		}

		if (!HasHomework) {
			createOneHWRecord(user, date, "", "今日没有作业");
			// Log.e("createRecords", date);
		}
	}
	
	public String[] getHWRecords(String user, String date) {
		String[] Args = new String[2];
		Args[0] = user;
		Args[1] = date;
		String[] HW = new String[10];
    	try {
			Cursor cursor = mDb.query(DATABASE_TABLE_HW, 
				new String[] { _CONTENT }, _USER + "=? and " + _DATE + "=?", Args, null, null, null); 

			if(cursor != null){
				int i = 0;
				for (cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()) {
			        HW[i] = cursor.getString(cursor.getColumnIndex(_CONTENT));
			        i++;
				}
			}
		} catch (Exception e) {
			// Log.e("HomeworkDatabase", e.getMessage());
		}

		return HW;
	}

	private long createOneSMRecord(String user, String[] scores){
		mValues.clear();
    	mValues.put(_USER, user);
    	mValues.put(_COURSE, scores[0]);
    	mValues.put(_GRADE, scores[1]);
    	mValues.put(_TERM, scores[2]);
    	mValues.put(_SCORE_MARK, scores[3]);
    	mValues.put(_SCORE_TOP, scores[4]);
    	mValues.put(_SCORE_AVG, scores[5]);
    	mValues.put(_SCORE_VARIANCE, scores[6]);
    	return mDb.insert(DATABASE_TABLE_SM, "NA", mValues);
    }
	
	public void createSMRecords(String user, String[][] all_scores) {
		int NumOfScore = 0;
		for (int i=0; i<all_scores.length; i++) {
			if (all_scores[i] == null)
				break;
			NumOfScore++;
		}
		
		if (NumOfScore == 0)
			return;

		// Remove old records from database
		String[] Args = new String[1];
		Args[0] = user;
		mDb.delete(DATABASE_TABLE_SM, _USER + "=?", Args); 
		
		for (int i=0; i<all_scores.length; i++) {
			if (all_scores[i] != null) {
				createOneSMRecord(user, all_scores[i]);
			}
		}
	}
	
	public static String[][] getSMRecords(String user) {
		String[] Args = new String[1];
		Args[0] = user;

		String[][] Score = new String[72][];
    	try {
			Cursor cursor = mDb.query(DATABASE_TABLE_SM, 
				new String[] { _COURSE, _GRADE, _TERM, _SCORE_MARK, _SCORE_TOP, _SCORE_AVG, _SCORE_VARIANCE }, _USER + "=?", Args, null, null, null); 

			if(cursor != null){
				int i = 0;
				for (cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()) {
					Score[i] = new String[7];
			        Score[i][0] = cursor.getString(cursor.getColumnIndex(_COURSE));
			        Score[i][1] = cursor.getString(cursor.getColumnIndex(_GRADE));
			        Score[i][2] = cursor.getString(cursor.getColumnIndex(_TERM));
			        Score[i][3] = cursor.getString(cursor.getColumnIndex(_SCORE_MARK));
			        Score[i][4] = cursor.getString(cursor.getColumnIndex(_SCORE_TOP));
			        Score[i][5] = cursor.getString(cursor.getColumnIndex(_SCORE_AVG));
			        Score[i][6] = cursor.getString(cursor.getColumnIndex(_SCORE_VARIANCE));
			        i++;
				}
			}
		} catch (Exception e) {
			Log.e("S从remarkDatabase", e.getMessage());
		}

		return Score;
	}
}
