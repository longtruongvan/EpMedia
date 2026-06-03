package com.joe.epmediademo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.joe.epmediademo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private androidx.cardview.widget.CardView btn_new_project;
	private ImageView btn_search;
	private TextView btn_see_all;
	private LinearLayout btn_mock_project_1;
	private LinearLayout btn_mock_project_2;
	private LinearLayout btn_ai_remove_bg;
	private LinearLayout btn_ai_captions;
	private LinearLayout btn_draft_1;
	private LinearLayout btn_draft_2;

	// Tab items
	private LinearLayout nav_home;
	private ImageView iv_nav_home;
	private TextView tv_nav_home;

	private LinearLayout nav_templates;
	private ImageView iv_nav_templates;
	private TextView tv_nav_templates;

	private LinearLayout nav_ai;
	private ImageView iv_nav_ai;
	private TextView tv_nav_ai;

	private LinearLayout nav_projects;
	private ImageView iv_nav_projects;
	private TextView tv_nav_projects;

	private LinearLayout nav_profile;
	private ImageView iv_nav_profile;
	private TextView tv_nav_profile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		// Main actions
		btn_new_project = (androidx.cardview.widget.CardView) findViewById(R.id.btn_new_project);
		btn_search = (ImageView) findViewById(R.id.btn_search);
		btn_see_all = (TextView) findViewById(R.id.btn_see_all);
		btn_mock_project_1 = (LinearLayout) findViewById(R.id.btn_mock_project_1);
		btn_mock_project_2 = (LinearLayout) findViewById(R.id.btn_mock_project_2);
		btn_ai_remove_bg = (LinearLayout) findViewById(R.id.btn_ai_remove_bg);
		btn_ai_captions = (LinearLayout) findViewById(R.id.btn_ai_captions);
		btn_draft_1 = (LinearLayout) findViewById(R.id.btn_draft_1);
		btn_draft_2 = (LinearLayout) findViewById(R.id.btn_draft_2);

		// Nav tabs
		nav_home = (LinearLayout) findViewById(R.id.nav_home);
		iv_nav_home = (ImageView) findViewById(R.id.iv_nav_home);
		tv_nav_home = (TextView) findViewById(R.id.tv_nav_home);

		nav_templates = (LinearLayout) findViewById(R.id.nav_templates);
		iv_nav_templates = (ImageView) findViewById(R.id.iv_nav_templates);
		tv_nav_templates = (TextView) findViewById(R.id.tv_nav_templates);

		nav_ai = (LinearLayout) findViewById(R.id.nav_ai);
		iv_nav_ai = (ImageView) findViewById(R.id.iv_nav_ai);
		tv_nav_ai = (TextView) findViewById(R.id.tv_nav_ai);

		nav_projects = (LinearLayout) findViewById(R.id.nav_projects);
		iv_nav_projects = (ImageView) findViewById(R.id.iv_nav_projects);
		tv_nav_projects = (TextView) findViewById(R.id.tv_nav_projects);

		nav_profile = (LinearLayout) findViewById(R.id.nav_profile);
		iv_nav_profile = (ImageView) findViewById(R.id.iv_nav_profile);
		tv_nav_profile = (TextView) findViewById(R.id.tv_nav_profile);

		// Click listeners
		btn_new_project.setOnClickListener(this);
		btn_search.setOnClickListener(this);
		btn_see_all.setOnClickListener(this);
		btn_mock_project_1.setOnClickListener(this);
		btn_mock_project_2.setOnClickListener(this);
		btn_ai_remove_bg.setOnClickListener(this);
		btn_ai_captions.setOnClickListener(this);
		btn_draft_1.setOnClickListener(this);
		btn_draft_2.setOnClickListener(this);

		nav_home.setOnClickListener(this);
		nav_templates.setOnClickListener(this);
		nav_ai.setOnClickListener(this);
		nav_projects.setOnClickListener(this);
		nav_profile.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_new_project) {
			// Open the custom media picker activity
			Intent intent = new Intent(MainActivity.this, ImportMediaActivity.class);
			startActivity(intent);
		} else if (id == R.id.btn_search) {
			Toast.makeText(this, R.string.toast_search, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_see_all) {
			Toast.makeText(this, R.string.toast_see_all, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_mock_project_1) {
			Toast.makeText(this, getString(R.string.toast_loading_project, getString(R.string.project_mountain)), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_mock_project_2) {
			Toast.makeText(this, getString(R.string.toast_loading_project, getString(R.string.project_cybercity)), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_ai_remove_bg) {
			Toast.makeText(this, R.string.toast_ai_remove_bg, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_ai_captions) {
			Toast.makeText(this, R.string.toast_ai_captions, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_draft_1) {
			Toast.makeText(this, getString(R.string.toast_loading_draft, getString(R.string.draft_tiktok)), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_draft_2) {
			Toast.makeText(this, getString(R.string.toast_loading_draft, getString(R.string.draft_nature)), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_home) {
			setActiveTab(nav_home, iv_nav_home, tv_nav_home);
		} else if (id == R.id.nav_templates) {
			setActiveTab(nav_templates, iv_nav_templates, tv_nav_templates);
			Toast.makeText(this, R.string.toast_templates_loading, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_ai) {
			setActiveTab(nav_ai, iv_nav_ai, tv_nav_ai);
			Toast.makeText(this, R.string.toast_ai_loading, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_projects) {
			setActiveTab(nav_projects, iv_nav_projects, tv_nav_projects);
			Toast.makeText(this, R.string.toast_projects_list, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_profile) {
			setActiveTab(nav_profile, iv_nav_profile, tv_nav_profile);
			Toast.makeText(this, R.string.toast_profile, Toast.LENGTH_SHORT).show();
		}
	}

	private void setActiveTab(LinearLayout activeTab, ImageView activeIcon, TextView activeText) {
		int normalColor = getResources().getColor(R.color.lumina_text_secondary);
		int activeColor = getResources().getColor(R.color.colorAccent);

		ImageView[] icons = {iv_nav_home, iv_nav_templates, iv_nav_ai, iv_nav_projects, iv_nav_profile};
		TextView[] texts = {tv_nav_home, tv_nav_templates, tv_nav_ai, tv_nav_projects, tv_nav_profile};

		for (int i = 0; i < icons.length; i++) {
			if (icons[i] != null) {
				icons[i].setImageTintList(android.content.res.ColorStateList.valueOf(normalColor));
			}
			if (texts[i] != null) {
				texts[i].setTextColor(normalColor);
			}
		}

		if (activeIcon != null) {
			activeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(activeColor));
		}
		if (activeText != null) {
			activeText.setTextColor(activeColor);
		}
	}
}
