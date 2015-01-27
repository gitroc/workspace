package com.roc.example;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.roc.example.Receiver.Utils;

/**
 * @author roc
 *	功能描述：自定义TabHost
 */
public class Example extends FragmentActivity{

    private static String TAG = "[Example]";

    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class<?> fragmentArray[] = {
            HomePage.class,
            MessagePage.class,
            FriendsPage.class,
            SquarePage.class,
            MorePage.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {
            R.drawable.tab_home_btn,
            R.drawable.tab_message_btn,
            R.drawable.tab_selfinfo_btn,
            R.drawable.tab_square_btn,
            R.drawable.tab_more_btn
    };
    //Tab选项卡的文字
    private String mTextviewArray[];

    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_tab_layout);

        initView();

        initService();

        initBaidu();
    }

    /**
     * 初始化组件
     */
    private void initView(){
        Log.i(TAG, "initView()");
        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);

        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTextviewArray = getResources().getStringArray(R.array.text_view);

        for(int i = 0; i < fragmentArray.length; i++) {
            //Log.i(TAG, "i = " + i);
            //为每一个Tab按钮设置图标、文字和内容
            TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index){
        //Log.i(TAG, "index = " + index);
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

    private void initService() {
        Log.i(TAG, "initService()");
        Intent intent = new Intent(Example.this, CoreService.class);
        Example.this.startService(intent);
    }

    private void initBaidu() {
        Resources resource = Example.this.getResources();
        String pkgName = Example.this.getPackageName();

        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        PushManager.startWork(Example.this,
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(Example.this, "api_key"));
        // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
        // PushManager.enableLbs(getApplicationContext());

        // Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
        // 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
        // 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
//        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
//                Example.this, resource.getIdentifier(
//                "notification_custom_builder", "layout", pkgName),
//                resource.getIdentifier("notification_icon", "id", pkgName),
//                resource.getIdentifier("notification_title", "id", pkgName),
//                resource.getIdentifier("notification_text", "id", pkgName));
//        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
//        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
//                | Notification.DEFAULT_VIBRATE);
//        cBuilder.setStatusbarIcon(Example.this.getApplicationInfo().icon);
//        cBuilder.setLayoutDrawable(resource.getIdentifier(
//                "simple_notification_icon", "drawable", pkgName));
//        PushManager.setNotificationBuilder(Example.this, 1, cBuilder);
    }
}