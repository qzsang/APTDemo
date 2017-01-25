package com.qzsang.aptdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.qzsang.annotation.ViewInject;

public class MainActivity extends AppCompatActivity {

    @ViewInject(R.id.tv_test)
    TextView textView;
    int test;
    @ViewInject(R.id.tv_test1)
    TextView textView1;
    @ViewInject(R.id.tv_test2)
    TextView textView2;
    @ViewInject(R.id.tv_test3)
    TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MainActivity$$Holder().init(this);

        textView.setText("View成功注入");
        textView1.setText("View成功注入1");
        textView2.setText("View成功注入2");
        textView3.setText("View成功注入3");
    }
}
