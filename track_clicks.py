import re

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_java, "r") as f:
    java = f.read()

# Add track variables
java = java.replace("private LinearLayout timeline_container;", "private LinearLayout timeline_container;\n\tprivate android.widget.FrameLayout track_effects;\n\tprivate android.widget.FrameLayout track_text;")

# Bind tracks
bind_str = """		timeline_container = (LinearLayout) findViewById(R.id.timeline_container);"""
new_bind = """		timeline_container = (LinearLayout) findViewById(R.id.timeline_container);
		track_effects = (android.widget.FrameLayout) findViewById(R.id.track_effects);
		track_text = (android.widget.FrameLayout) findViewById(R.id.track_text);
		if (track_effects != null) track_effects.setOnClickListener(this);
		if (track_text != null) track_text.setOnClickListener(this);"""
java = java.replace(bind_str, new_bind)

# Handle clicks
click_str = """		} else if (id == R.id.btn_close_filters) {"""
new_click = """		} else if (id == R.id.track_effects) {
			showPanel(panel_filters);
			setActiveTab(tab_filters, iv_tab_filters, tv_tab_filters);
		} else if (id == R.id.track_text) {
			showPanel(panel_text_input);
			setActiveTab(tab_text, iv_tab_text, tv_tab_text);
		} else if (id == R.id.btn_close_filters) {"""
java = java.replace(click_str, new_click)

with open(f_java, "w") as f:
    f.write(java)

print("Added track clicks")
