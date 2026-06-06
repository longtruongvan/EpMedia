import re

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/ExportActivity.java"
with open(f_java, "r") as f:
    java = f.read()

# Add subtitleScale
java = java.replace("private float subtitleYPercent;", "private float subtitleYPercent;\n\tprivate float subtitleScale;")
java = java.replace("subtitleYPercent = intent.getFloatExtra(\"SUBTITLE_Y\", 85f);", "subtitleYPercent = intent.getFloatExtra(\"SUBTITLE_Y\", 85f);\n\t\tsubtitleScale = intent.getFloatExtra(\"SUBTITLE_SCALE\", 1.0f);")

# Update addText
old_add_text = """					int targetX = (int) (videoWidth * (subtitleXPercent / 100f));
					int targetY = (int) (videoHeight * (subtitleYPercent / 100f));
					epVideo.addText(targetX, targetY, 36, "white", MyApplication.getSavePath() + "msyh.ttf", subtitleText);"""

new_add_text = """					int targetX = (int) (videoWidth * (subtitleXPercent / 100f));
					int targetY = (int) (videoHeight * (subtitleYPercent / 100f));
					int fontSize = (int) (36 * subtitleScale);
					if (fontSize < 10) fontSize = 10;
					if (fontSize > 200) fontSize = 200;
					epVideo.addText(targetX, targetY, fontSize, "white", MyApplication.getSavePath() + "msyh.ttf", subtitleText);"""

java = java.replace(old_add_text, new_add_text)

with open(f_java, "w") as f:
    f.write(java)

print("Updated ExportActivity.java")
