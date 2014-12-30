package com.fushan.homework;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		RelativeLayout bg = (RelativeLayout) findViewById(R.id.aboutFushan);
        bg.setBackgroundColor(Color.parseColor("#F5F5DC"));

        TextView Description = (TextView)findViewById(R.id.Description);
        Description.setMovementMethod(ScrollingMovementMethod.getInstance()); 
        Description.setMovementMethod(LinkMovementMethod.getInstance());
        String AboutString = "";
        AboutString += "<h3>福外作业使用指南:</h3>";
        AboutString += "1. 左右滑动可以切换日期。<BR>";       
        AboutString += "2. 下拉可以刷新当前日期作业内容。<BR>";       
        AboutString += "3. 可以使用菜单快速切换两个用户。<BR><BR><BR>";
        AboutString += "<h3>福外作业更新介绍:</h3>";
        AboutString += "v4.4:<BR><BR>";
        AboutString += "1. 改善成绩显示用户体验，允许离线访问成绩。<BR><BR>";
        AboutString += "v4.3:<BR><BR>";
        AboutString += "1. 增加查询成绩功能。若不能看到全部成绩数据，请旋转屏幕。另外，可以在菜单中选择成绩按照课程和年级两种不同方式排序。<BR><BR>";
        AboutString += "v4.2:<BR><BR>";
        AboutString += "1. 修复一个由福山教育网络服务器端的bug导致作业访问异常。如果在家校桥作业的同一日期上连续访问两次，第二次取到的作业是空白，因此导致连续下拉刷新<福外作业>时，显示今日没有作业!<BR>";
        AboutString += "2. 修复一个彩虹刷新条永久显示的问题!<BR><BR>";
        AboutString += "v4.1:<BR><BR>";
        AboutString += "1. 支持下拉刷新当前日期作业内容。<BR>";
        AboutString += "2. 修复一个离线模式下读取图片失败后导致软件异常退出的bug。<BR><BR>";
        AboutString += "v4.0:<BR><BR>";
        AboutString += "1. 优化网络访问，大幅减少从福山教育网络的数据下载。<BR>";
        AboutString += "2. 支持离线作业显示。<BR>";
        AboutString += "3. 提升左右滑动显示作业体验。<BR>";
        AboutString += "4. 修复一些简单的bug。<BR><BR>";
        AboutString += "v3.3:<BR><BR>";
        AboutString += "1. 修复一个不能显示bitmap导致崩溃的bug。<BR><BR>";
        AboutString += "v3.2:<BR><BR>";
        AboutString += "1. 增加显示作业中图片的功能，并且图片可以点击。<BR>";
        AboutString += "2. 增加字体放大功能，在平板上有更好的显示效果。<BR>";
        AboutString += "3. 修复登录未完成时就下载作业数据的bug。<BR><BR>";
        AboutString += "v3.1:<BR><BR>";
        AboutString += "1. 修复一个用户访问福山教育网络的严重bug，确保稳定登录。<BR><BR>";
        AboutString += "v3.0:<BR><BR>";
        AboutString += "1. 美化作业显示，不同科目之间用横线分割。<BR>";
        AboutString += "2. 修复多用户切换的bug。<BR>";
        AboutString += "3. 改进多线程网络访问安全性，提高用户体验。<BR><BR>";        
        AboutString += "v2.0:<BR><BR>";
        AboutString += "1. 支持双用户切换。<BR>";
        AboutString += "2. 支持作业内容中的超链接访问。可以直接点击下载作业中的附件文件，也可以访问其他网页。<BR>";
        AboutString += "3. 修复一些简单的错误，提高性能。<BR><BR>";
        AboutString += "v1.0:<BR><BR>";
        AboutString += "1. 初始版本。<BR><BR>";
        AboutString += "如有意见或建议，请发电子邮件至<a href=\"mailto:liujiangning@gmail.com\">liujiangning@gmail.com</a>。<BR>";
        Description.setText(Html.fromHtml(AboutString, null, null));
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

}
