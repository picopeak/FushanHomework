package com.fushan.homework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	private int CurrentUser = 1;
    private Login_OnClickListener listener1 = new Login_OnClickListener();  

    class Login_OnClickListener implements OnClickListener  
    {  
        public void onClick(View v)  
        {  
            // Get user name and password from display
        	EditText usrText = (EditText) findViewById(R.id.username);
        	String UserName = usrText.getText().toString();
        	EditText pwdText = (EditText) findViewById(R.id.password);
        	String PassWord = pwdText.getText().toString();

        	// Set user name and password to preference
            SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
            Editor edit = preference.edit();
            edit.putString("UserName"+CurrentUser, UserName);
            edit.putString("PassWord"+CurrentUser, PassWord);
            edit.commit();
            
            // Call the DisplayMessageActivity
        	Intent intent = new Intent();
        	intent.setClass(MainActivity.this, DisplayMessageActivity.class);
        	// Pass new current user back to display activity
            intent.putExtra("CurrentUser", CurrentUser);  
            setResult(RESULT_OK, intent);
            finish();
        }  
    }  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        RelativeLayout bg = (RelativeLayout) findViewById(R.id.LoginWindow);
        bg.setBackgroundColor(Color.parseColor("#F5F5DC"));        
        // bg.setBackgroundColor(Color.parseColor("#9D61AB"));

        Intent mIntent = getIntent();  
        Bundle b = mIntent.getExtras(); 
        CurrentUser = b.getInt("CurrentUser");
                
        // Get original password from preference
        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
        String UserName = preference.getString("UserName"+CurrentUser, "");
        String PassWord = preference.getString("PassWord"+CurrentUser, "");

        // Display original password
        EditText usrText = (EditText) findViewById(R.id.username);
        EditText pwdText = (EditText) findViewById(R.id.password);
        usrText.setText(UserName);
        pwdText.setText(PassWord);

        // Set button listener
        Button Login = (Button)findViewById(R.id.Login);  
        Login.setOnClickListener(listener1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
    	getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	public boolean onPrepareOptionsMenu(Menu menu)
	{
        String Mark1, Mark2, UserName1, UserName2, RealName1, RealName2;

        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
        UserName1 = preference.getString("UserName1", "");
        UserName2 = preference.getString("UserName2", "");
        RealName1 = preference.getString("RealName1", "用户1");
        RealName2 = preference.getString("RealName2", "用户2");
        
        Mark1 = (CurrentUser == 1) ? " *" : "";
        Mark2 = (CurrentUser == 2) ? " *" : "";        
        
        menu.clear();
        menu.add(Menu.NONE, R.id.User1, Menu.NONE, UserName1+"("+RealName1+")"+Mark1);
		menu.add(Menu.NONE, R.id.User2, Menu.NONE, UserName2+"("+RealName2+")"+Mark2);
		menu.add(Menu.NONE, R.id.About, Menu.NONE, "关于");
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
	        if (CurrentUser == 1)
	        	return true;

	        CurrentUser = 1;
            // Get original password from preference
	        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
            String UserName = preference.getString("UserName1", "");
            String PassWord = preference.getString("PassWord1", "");

            // Display original password
            EditText usrText = (EditText) findViewById(R.id.username);
            EditText pwdText = (EditText) findViewById(R.id.password);
            usrText.setText(UserName);
            pwdText.setText(PassWord);
            
            return true;
		}
		case R.id.User2: {
	        if (CurrentUser == 2)
	        	return true;
	        
	        CurrentUser = 2;
            // Get original password from preference
	        SharedPreferences preference = getSharedPreferences("person", Context.MODE_PRIVATE);
            String UserName = preference.getString("UserName2", "");
            String PassWord = preference.getString("PassWord2", "");

            // Display original password
            EditText usrText = (EditText) findViewById(R.id.username);
            EditText pwdText = (EditText) findViewById(R.id.password);
            usrText.setText(UserName);
            pwdText.setText(PassWord);

            return true;
		}
		case R.id.About: {
            Intent intent = new Intent();
        	intent.setClass(MainActivity.this, AboutActivity.class);
        	startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
