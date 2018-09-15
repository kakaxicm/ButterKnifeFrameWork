package com.qicode.kakaxicm.annotationprocessdr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qicode.kakaxicm.annotations.BindColor;
import com.qicode.kakaxicm.annotations.BindString;
import com.qicode.kakaxicm.annotations.BindView;
import com.qicode.kakaxicm.annotations.JPHello;
import com.qicode.kakaxicm.annotations.OnClick;
import com.qicode.kakaxicm.annotations.TestAnnotation;
import com.qicode.kakaxicm.butterknife.ButterKnife;

//@TestAnnotation("hello processor!")
@JPHello
public class MainActivity extends AppCompatActivity {
//    @BindView(R.id.tv_test)
    TextView textView;
    @BindString(R.string.app_name)
    String testStr;
    @BindString(R.string.app_name)
    String testStr1;
    @BindColor(R.color.colorPrimary)
    int color;
    @BindView(R.id.tv_test)
    TextView testView;
    //一个id只能绑定一个View
//    @BindView(R.id.tv_test)
//    TextView testView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.e("TAG", testStr);
        TextView tv = findViewById(R.id.tv_test);
        tv.setTextColor(color);
        Log.e("TAG", testView.getText().toString());
    }

    @OnClick(R.id.tv_test)
    public void onTestClick(View v){
        Toast.makeText(this,R.string.app_name, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tv_test1)
    public void onTestClick1(View v){
        Toast.makeText(this,R.string.app_name, Toast.LENGTH_SHORT).show();
    }
}
