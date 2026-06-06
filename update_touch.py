import re

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_java, "r") as f:
    java = f.read()

# Add subtitleScale to state
java = java.replace("private float subtitleYPercent = 85f;", "private float subtitleYPercent = 85f;\n\tprivate float subtitleScale = 1.0f;")
java = java.replace("float subtitleXPercent, float subtitleYPercent,", "float subtitleXPercent, float subtitleYPercent, float subtitleScale,")
java = java.replace("this.subtitleYPercent = subtitleYPercent;", "this.subtitleYPercent = subtitleYPercent;\n\t\t\tthis.subtitleScale = subtitleScale;")
java = java.replace("subtitleXPercent,\n\t\t\tsubtitleYPercent,", "subtitleXPercent,\n\t\t\tsubtitleYPercent,\n\t\t\tsubtitleScale,")
java = java.replace("subtitleYPercent = state.subtitleYPercent;", "subtitleYPercent = state.subtitleYPercent;\n\t\tsubtitleScale = state.subtitleScale;")

# Reset state
java = java.replace("subtitleYPercent = 85f;", "subtitleYPercent = 85f;\n\t\t\tsubtitleScale = 1.0f;")

# Update Intent
java = java.replace("intent.putExtra(\"SUBTITLE_Y\", subtitleYPercent);", "intent.putExtra(\"SUBTITLE_Y\", subtitleYPercent);\n\t\t\tintent.putExtra(\"SUBTITLE_SCALE\", subtitleScale);")

# updateSubtitlePreview to apply scale
update_preview_old = """			float yOffset = -((100f - subtitleYPercent) / 100f) * (containerHeight - 60f * density);
			tv_subtitle_preview.setTranslationY(yOffset);"""

update_preview_new = """			float yOffset = -((100f - subtitleYPercent) / 100f) * (containerHeight - 60f * density);
			tv_subtitle_preview.setTranslationY(yOffset);
			tv_subtitle_preview.setScaleX(subtitleScale);
			tv_subtitle_preview.setScaleY(subtitleScale);"""

java = java.replace(update_preview_old, update_preview_new)

# Setup MultiTouchListener
setup_touch = """		tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);
		tv_subtitle_preview.setOnTouchListener(new com.joe.epmediademo.Utils.MultiTouchListener(this, new com.joe.epmediademo.Utils.MultiTouchListener.OnTransformListener() {
			@Override
			public void onScale(float scaleFactor) {
				subtitleScale *= scaleFactor;
				if (subtitleScale < 0.5f) subtitleScale = 0.5f;
				if (subtitleScale > 5.0f) subtitleScale = 5.0f;
				updateSubtitlePreview();
			}

			@Override
			public void onTranslate(float dx, float dy) {
				if (video_container.getWidth() == 0) return;
				float density = getResources().getDisplayMetrics().density;
				float maxDx = video_container.getWidth() / 2f - 40f * density;
				float maxDy = video_container.getHeight() - 60f * density;
				
				float currentXOffset = ((subtitleXPercent - 50f) / 50f) * maxDx;
				float currentYOffset = -((100f - subtitleYPercent) / 100f) * maxDy;
				
				currentXOffset += dx;
				currentYOffset += dy;
				
				subtitleXPercent = (currentXOffset / maxDx) * 50f + 50f;
				subtitleYPercent = 100f - ((-currentYOffset / maxDy) * 100f);
				
				if (subtitleXPercent < 0) subtitleXPercent = 0;
				if (subtitleXPercent > 100) subtitleXPercent = 100;
				if (subtitleYPercent < 0) subtitleYPercent = 0;
				if (subtitleYPercent > 100) subtitleYPercent = 100;
				
				slider_text_x.setValue(subtitleXPercent);
				slider_text_y.setValue(subtitleYPercent);
				
				updateSubtitlePreview();
			}

			@Override
			public void onTransformEnded() {
				pushStateToUndo();
			}
		}));"""

java = java.replace("tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);", setup_touch)

with open(f_java, "w") as f:
    f.write(java)

print("Updated EditActivity.java")
