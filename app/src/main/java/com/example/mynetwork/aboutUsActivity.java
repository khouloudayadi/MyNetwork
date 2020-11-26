package com.example.mynetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class aboutUsActivity extends AppCompatActivity {

    private Boolean img_condition_tag = false;
    private Boolean img_features_tag = false;
    private Boolean img_important_tag = false;
    private Boolean img_improvement_tag = false;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.layout_condition) LinearLayout layout_condition;
    @BindView(R.id.img_drop_down_condition) ImageView img_drop_down_condition;
    @BindView(R.id.img_drop_down_features) ImageView img_drop_down_features;
    @BindView(R.id.img_drop_down_important) ImageView img_drop_down_important;
    @BindView(R.id.img_drop_down_improvement) ImageView img_drop_down_improvement;


    @OnClick(R.id.img_drop_down_condition)
    void drop_down_condition() {
        if (!img_condition_tag) {
            img_condition_tag = true;
            layout_condition.setVisibility(View.VISIBLE);
            img_drop_down_condition.setImageResource(R.drawable.ic_chevron_left_24);
        } else {
            img_condition_tag = false;
            layout_condition.setVisibility(View.GONE);
            img_drop_down_condition.setImageResource(R.drawable.ic_details_right_24);
        }
    }

    @BindView(R.id.layout_features) LinearLayout layout_features;
    @OnClick(R.id.img_drop_down_features)
    void drop_down_features() {
        if (!img_features_tag) {
            img_features_tag = true;
            layout_features.setVisibility(View.VISIBLE);
            img_drop_down_features.setImageResource(R.drawable.ic_chevron_left_24);
        } else {
            img_features_tag = false;
            layout_features.setVisibility(View.GONE);
            img_drop_down_features.setImageResource(R.drawable.ic_details_right_24);
        }
    }


    @BindView(R.id.layout_important) LinearLayout layout_important;
    @OnClick(R.id.img_drop_down_important)
    void drop_down_important() {
        if (!img_important_tag) {
            img_important_tag = true;
            img_drop_down_important.setImageResource(R.drawable.ic_chevron_left_24);
            layout_important.setVisibility(View.VISIBLE);
        } else {
            img_important_tag = false;
            layout_important.setVisibility(View.GONE);
            img_drop_down_important.setImageResource(R.drawable.ic_details_right_24);
        }
    }

    @BindView(R.id.layout_improvement) LinearLayout layout_improvement;
    @OnClick(R.id.img_drop_down_improvement)
    void drop_down_improvement() {
        if (!img_improvement_tag) {
            img_improvement_tag = true;
            layout_improvement.setVisibility(View.VISIBLE);
            img_drop_down_improvement.setImageResource(R.drawable.ic_chevron_left_24);
        } else {
            img_improvement_tag = false;
            layout_improvement.setVisibility(View.GONE);
            img_drop_down_improvement.setImageResource(R.drawable.ic_details_right_24);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        initView();
    }

    private void initView() {
        //init View
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.apropos);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}