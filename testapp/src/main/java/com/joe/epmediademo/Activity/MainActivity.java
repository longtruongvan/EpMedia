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

	// Content layout containers
	private androidx.core.widget.NestedScrollView layout_home_content;
	private androidx.core.widget.NestedScrollView layout_templates_content;
	private androidx.core.widget.NestedScrollView layout_ai_content;
	private androidx.core.widget.NestedScrollView layout_projects_content;
	private androidx.core.widget.NestedScrollView layout_profile_content;

	// Profile controls
	private LinearLayout btn_switch_language;
	private TextView tv_current_lang;
	private LinearLayout btn_help_support;

	// AI Tab controls
	private LinearLayout btn_ai_remove_bg_tab;
	private LinearLayout btn_ai_captions_tab;
	private LinearLayout btn_ai_cutout_tab;
	private LinearLayout btn_ai_voice_tab;

	// Projects Tab controls
	private LinearLayout btn_proj_1_tab;
	private LinearLayout btn_proj_2_tab;

	// Home screen templates
	private androidx.cardview.widget.CardView btn_home_template_1;
	private androidx.cardview.widget.CardView btn_home_template_2;
	private androidx.cardview.widget.CardView btn_home_template_3;
	private TextView btn_explore_templates;

	// Templates tab templates
	private androidx.cardview.widget.CardView btn_template_1;
	private androidx.cardview.widget.CardView btn_template_2;
	private androidx.cardview.widget.CardView btn_template_3;

	private TextView tv_header_export;

	private int activeTabId = R.id.nav_home;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		
		if (savedInstanceState != null) {
			activeTabId = savedInstanceState.getInt("activeTabId", R.id.nav_home);
			restoreActiveTab(activeTabId);
		} else {
			if (getIntent() != null && getIntent().hasExtra("TARGET_TAB")) {
				handleIntent(getIntent());
			} else {
				restoreActiveTab(R.id.nav_home);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.hasExtra("TARGET_TAB")) {
			int targetTabId = intent.getIntExtra("TARGET_TAB", R.id.nav_home);
			restoreActiveTab(targetTabId);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("activeTabId", activeTabId);
	}

	private void initView() {
		// Main actions
		btn_new_project = (androidx.cardview.widget.CardView) findViewById(R.id.btn_new_project);
		btn_search = (ImageView) findViewById(R.id.btn_search);
		btn_see_all = (TextView) findViewById(R.id.btn_see_all);
		btn_mock_project_1 = (LinearLayout) findViewById(R.id.btn_mock_project_1);
		tv_header_export = (TextView) findViewById(R.id.tv_header_export);
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

		// Content layout containers
		layout_home_content = (androidx.core.widget.NestedScrollView) findViewById(R.id.layout_home_content);
		layout_templates_content = (androidx.core.widget.NestedScrollView) findViewById(R.id.layout_templates_content);
		layout_ai_content = (androidx.core.widget.NestedScrollView) findViewById(R.id.layout_ai_content);
		layout_projects_content = (androidx.core.widget.NestedScrollView) findViewById(R.id.layout_projects_content);
		layout_profile_content = (androidx.core.widget.NestedScrollView) findViewById(R.id.layout_profile_content);

		// Profile controls
		btn_switch_language = (LinearLayout) findViewById(R.id.btn_switch_language);
		tv_current_lang = (TextView) findViewById(R.id.tv_current_lang);
		btn_help_support = (LinearLayout) findViewById(R.id.btn_help_support);

		// AI Tab controls
		btn_ai_remove_bg_tab = (LinearLayout) findViewById(R.id.btn_ai_remove_bg_tab);
		btn_ai_captions_tab = (LinearLayout) findViewById(R.id.btn_ai_captions_tab);
		btn_ai_cutout_tab = (LinearLayout) findViewById(R.id.btn_ai_cutout_tab);
		btn_ai_voice_tab = (LinearLayout) findViewById(R.id.btn_ai_voice_tab);

		// Projects Tab controls
		btn_proj_1_tab = (LinearLayout) findViewById(R.id.btn_proj_1_tab);
		btn_proj_2_tab = (LinearLayout) findViewById(R.id.btn_proj_2_tab);

		// Home screen templates
		btn_home_template_1 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_home_template_1);
		btn_home_template_2 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_home_template_2);
		btn_home_template_3 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_home_template_3);
		btn_explore_templates = (TextView) findViewById(R.id.btn_explore_templates);

		// Templates tab templates
		btn_template_1 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_template_1);
		btn_template_2 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_template_2);
		btn_template_3 = (androidx.cardview.widget.CardView) findViewById(R.id.btn_template_3);

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

		if (btn_switch_language != null) btn_switch_language.setOnClickListener(this);
		if (btn_help_support != null) btn_help_support.setOnClickListener(this);
		if (btn_ai_remove_bg_tab != null) btn_ai_remove_bg_tab.setOnClickListener(this);
		if (btn_ai_captions_tab != null) btn_ai_captions_tab.setOnClickListener(this);
		if (btn_ai_cutout_tab != null) btn_ai_cutout_tab.setOnClickListener(this);
		if (btn_ai_voice_tab != null) btn_ai_voice_tab.setOnClickListener(this);
		if (btn_proj_1_tab != null) btn_proj_1_tab.setOnClickListener(this);
		if (btn_proj_2_tab != null) btn_proj_2_tab.setOnClickListener(this);

		if (btn_home_template_1 != null) btn_home_template_1.setOnClickListener(this);
		if (btn_home_template_2 != null) btn_home_template_2.setOnClickListener(this);
		if (btn_home_template_3 != null) btn_home_template_3.setOnClickListener(this);
		if (btn_explore_templates != null) btn_explore_templates.setOnClickListener(this);
		if (tv_header_export != null) tv_header_export.setOnClickListener(this);

		if (btn_template_1 != null) btn_template_1.setOnClickListener(this);
		if (btn_template_2 != null) btn_template_2.setOnClickListener(this);
		if (btn_template_3 != null) btn_template_3.setOnClickListener(this);

		updateLanguageText();
	}

	private void restoreActiveTab(int id) {
		View tabView = findViewById(id);
		if (tabView != null) {
			onClick(tabView);
		}
	}

	private void updateLanguageText() {
		java.util.Locale currentLocale = getResources().getConfiguration().locale;
		String lang = currentLocale.getLanguage();
		if (tv_current_lang != null) {
			if ("vi".equals(lang)) {
				tv_current_lang.setText("Tiếng Việt (VI)");
			} else {
				tv_current_lang.setText("English (EN)");
			}
		}
	}

	private void toggleLanguage() {
		java.util.Locale currentLocale = getResources().getConfiguration().locale;
		String newLang = "vi".equals(currentLocale.getLanguage()) ? "en" : "vi";
		
		java.util.Locale locale = new java.util.Locale(newLang);
		java.util.Locale.setDefault(locale);
		
		android.content.res.Resources resources = getResources();
		android.content.res.Configuration config = resources.getConfiguration();
		android.util.DisplayMetrics dm = resources.getDisplayMetrics();
		
		config.setLocale(locale);
		resources.updateConfiguration(config, dm);
		
		if (getApplicationContext() != null) {
			android.content.res.Resources appRes = getApplicationContext().getResources();
			android.content.res.Configuration appConfig = appRes.getConfiguration();
			appConfig.setLocale(locale);
			appRes.updateConfiguration(appConfig, appRes.getDisplayMetrics());
		}

		recreate();
	}

	private void switchContentLayouts(View activeLayout) {
		if (layout_home_content != null) layout_home_content.setVisibility(View.GONE);
		if (layout_templates_content != null) layout_templates_content.setVisibility(View.GONE);
		if (layout_ai_content != null) layout_ai_content.setVisibility(View.GONE);
		if (layout_projects_content != null) layout_projects_content.setVisibility(View.GONE);
		if (layout_profile_content != null) layout_profile_content.setVisibility(View.GONE);

		if (activeLayout != null) {
			activeLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_new_project) {
			// Open the custom media picker activity
			Intent intent = new Intent(MainActivity.this, ImportMediaActivity.class);
			startActivity(intent);
		} else if (id == R.id.btn_search) {
			final android.widget.EditText input = new android.widget.EditText(this);
			input.setHint(R.string.search_media_hint);
			input.setTextColor(android.graphics.Color.WHITE);
			input.setHintTextColor(android.graphics.Color.GRAY);
			
			android.widget.FrameLayout container = new android.widget.FrameLayout(this);
			android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT
			);
			params.leftMargin = (int) (16 * getResources().getDisplayMetrics().density);
			params.rightMargin = (int) (16 * getResources().getDisplayMetrics().density);
			input.setLayoutParams(params);
			container.addView(input);

			new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
				.setTitle(R.string.toast_search)
				.setView(container)
				.setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(android.content.DialogInterface dialog, int which) {
						String query = input.getText().toString().trim();
						if (!query.isEmpty()) {
							Toast.makeText(MainActivity.this, getString(R.string.toast_loading_project, query), Toast.LENGTH_SHORT).show();
							Intent intent = new Intent(MainActivity.this, EditActivity.class);
							intent.putExtra("VIDEO_PATH", "mock_cybercity.mp4");
							intent.putExtra("SEARCH_QUERY", query);
							startActivity(intent);
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
		} else if (id == R.id.tv_header_export) {
			Toast.makeText(this, getString(R.string.toast_select_video_first), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_see_all) {
			activeTabId = R.id.nav_projects;
			setActiveTab(nav_projects, iv_nav_projects, tv_nav_projects);
			switchContentLayouts(layout_projects_content);
		} else if (id == R.id.btn_mock_project_1 || id == R.id.btn_proj_1_tab) {
			Toast.makeText(this, getString(R.string.toast_loading_project, getString(R.string.project_mountain)), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_mountain.mp4");
			startActivity(intent);
		} else if (id == R.id.btn_mock_project_2 || id == R.id.btn_proj_2_tab) {
			Toast.makeText(this, getString(R.string.toast_loading_project, getString(R.string.project_cybercity)), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_cybercity.mp4");
			startActivity(intent);
		} else if (id == R.id.btn_ai_remove_bg || id == R.id.btn_ai_remove_bg_tab) {
			Toast.makeText(this, R.string.toast_ai_remove_bg, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_mountain.mp4");
			intent.putExtra("INIT_TOOL", "remove_bg");
			startActivity(intent);
		} else if (id == R.id.btn_ai_captions || id == R.id.btn_ai_captions_tab) {
			Toast.makeText(this, R.string.toast_ai_captions, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_cybercity.mp4");
			intent.putExtra("INIT_TOOL", "auto_captions");
			startActivity(intent);
		} else if (id == R.id.btn_ai_cutout_tab) {
			Toast.makeText(this, getString(R.string.ai_smart_cutout), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_forest.mp4");
			intent.putExtra("INIT_TOOL", "cutout");
			startActivity(intent);
		} else if (id == R.id.btn_ai_voice_tab) {
			Toast.makeText(this, getString(R.string.ai_voice_changer), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_mountain.mp4");
			intent.putExtra("INIT_TOOL", "voice_changer");
			startActivity(intent);
		} else if (id == R.id.btn_draft_1) {
			Toast.makeText(this, getString(R.string.toast_loading_draft, getString(R.string.draft_tiktok)), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_cybercity.mp4");
			startActivity(intent);
		} else if (id == R.id.btn_draft_2) {
			Toast.makeText(this, getString(R.string.toast_loading_draft, getString(R.string.draft_nature)), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, EditActivity.class);
			intent.putExtra("VIDEO_PATH", "mock_forest.mp4");
			startActivity(intent);
		} else if (id == R.id.btn_switch_language) {
			toggleLanguage();
		} else if (id == R.id.btn_help_support) {
			Toast.makeText(this, R.string.profile_help, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_home_template_1 || id == R.id.btn_template_1) {
			Toast.makeText(this, getString(R.string.toast_applying_template, getString(R.string.template_neon)) + "\n" + getString(R.string.toast_select_video_first), Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, ImportMediaActivity.class);
			intent.putExtra("TEMPLATE_ID", "template_neon");
			startActivity(intent);
		} else if (id == R.id.btn_home_template_2 || id == R.id.btn_template_2) {
			Toast.makeText(this, getString(R.string.toast_applying_template, getString(R.string.template_retro)) + "\n" + getString(R.string.toast_select_video_first), Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, ImportMediaActivity.class);
			intent.putExtra("TEMPLATE_ID", "template_retro");
			startActivity(intent);
		} else if (id == R.id.btn_home_template_3 || id == R.id.btn_template_3) {
			Toast.makeText(this, getString(R.string.toast_applying_template, getString(R.string.template_soft)) + "\n" + getString(R.string.toast_select_video_first), Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, ImportMediaActivity.class);
			intent.putExtra("TEMPLATE_ID", "template_soft");
			startActivity(intent);
		} else if (id == R.id.btn_explore_templates) {
			activeTabId = R.id.nav_templates;
			setActiveTab(nav_templates, iv_nav_templates, tv_nav_templates);
			switchContentLayouts(layout_templates_content);
		} else if (id == R.id.nav_home) {
			activeTabId = id;
			setActiveTab(nav_home, iv_nav_home, tv_nav_home);
			switchContentLayouts(layout_home_content);
		} else if (id == R.id.nav_templates) {
			activeTabId = id;
			setActiveTab(nav_templates, iv_nav_templates, tv_nav_templates);
			switchContentLayouts(layout_templates_content);
		} else if (id == R.id.nav_ai) {
			activeTabId = id;
			setActiveTab(nav_ai, iv_nav_ai, tv_nav_ai);
			switchContentLayouts(layout_ai_content);
		} else if (id == R.id.nav_projects) {
			activeTabId = id;
			setActiveTab(nav_projects, iv_nav_projects, tv_nav_projects);
			switchContentLayouts(layout_projects_content);
		} else if (id == R.id.nav_profile) {
			activeTabId = id;
			setActiveTab(nav_profile, iv_nav_profile, tv_nav_profile);
			switchContentLayouts(layout_profile_content);
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
