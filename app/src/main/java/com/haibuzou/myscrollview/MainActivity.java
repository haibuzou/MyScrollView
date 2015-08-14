package com.haibuzou.myscrollview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

public class MainActivity extends AppCompatActivity {

    int[] data = {R.mipmap.am,R.mipmap.fw,R.mipmap.jiansheng,R.mipmap.kaer,R.mipmap.tf};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyScrollView myScrollView = new MyScrollView(this);
        int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,400,getResources().getDisplayMetrics());
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size,size);
        layout.setOrientation(LinearLayout.VERTICAL);
        for(int i = 0 ; i< 5; i++){
            ImageView image = new ImageView(this);
            image.setImageResource(data[i]);
            layout.addView(image,lp);
        }
        myScrollView.addView(layout);
        setContentView(myScrollView);
    }

}
