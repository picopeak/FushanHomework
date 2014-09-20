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
	private static final String DATABASE_TABLE = "HomeworkDatabase";	

	public static final String _ID_i = "_id";
	public static final String _USER = "_user";
	public static final String _DATE = "_date";
	public static final String _COURSE = "_course";
	public static final String _CONTENT = "_content";

    private static final String DATABASE_CREATE =
            "create table if not exists " + DATABASE_TABLE +"("
            		+ _ID_i + " integer primary key, "
                    + _USER + " text, "
                    + _DATE + " text, "
                    + _COURSE + " text, " 
                    + _CONTENT + " text)";

	class HomeworkDatabaseHelper extends SQLiteOpenHelper {
		public HomeworkDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DATABASE_CREATE);
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
    private SQLiteDatabase mDb;
    
    public HomeworkDatabase(Context context) {
		mCtx = context;
		mValues = new ContentValues();
	}

	private long createOneRecord(String user, String date, String course, String content){
		mValues.clear();
    	mValues.put(_USER, user);
    	mValues.put(_DATE, date);
    	mValues.put(_COURSE, course);
    	mValues.put(_CONTENT, content );
    	return mDb.insert(DATABASE_TABLE, "NA", mValues);
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
	
	// The following are interfaces for displaying homework
	
    public HomeworkDatabase open() {
    	try {
	        mDbHelper = new HomeworkDatabaseHelper(mCtx, DATABASE_NAME, null, 1);
	        mDb = mDbHelper.getWritableDatabase();
	        return this;
		} catch (Exception e) {
			Log.e("HomeworkDatabase", e.getMessage());
		}
    	
    	return null;
    }

	// Interface of DisplayHomework
	public void createRecords(String user, String date, String[] HW) {
		// Remove old records from database
		String[] Args = new String[2];
		Args[0] = user;
		Args[1] = date;
		mDb.delete(DATABASE_TABLE, _USER + "=? and " + _DATE + "=?", Args); 
		
		// Insert new records into database
		boolean HasHomework = false;
		for (int i=0; i<10; i++) {
			if (HW[i] != null) {
				createOneRecord(user, date, getCourseName(HW[i]), HW[i]);
				HasHomework = true;
			}
		}
		
		if (!HasHomework) {
			createOneRecord(user, date, "", "今日没有作业");
		}
	}
	
	// Interface of DisplayHomework
	public String[] getRecords(String user, String date) {
		String[] Args = new String[2];
		Args[0] = user;
		Args[1] = date;
		String[] HW = new String[10];
    	try {
			Cursor cursor = mDb.query(DATABASE_TABLE, 
				new String[] { _CONTENT }, _USER + "=? and " + _DATE + "=?", Args, null, null, null); 

			if(cursor != null){
				int i = 0;
				for (cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()) {
			        HW[i] = cursor.getString(cursor.getColumnIndex(_CONTENT));
			        i++;
				}
				
//			    for(int i=0; i<cursor.getCount(); i++){
//			        cursor.move(i);
//			        HW[i] = cursor.getString(cursor.getColumnIndex(_CONTENT));
//			    }
			}
		} catch (Exception e) {
			Log.e("HomeworkDatabase", e.getMessage());
		}

		return HW;
	}
	
}
