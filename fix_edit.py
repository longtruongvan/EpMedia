import re

f_edit = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_edit, "r") as f:
    content = f.read()

content = content.replace("private Button bt_file;", "private Button bt_file;\n\tprivate android.widget.ImageView btn_add_video;")
content = content.replace("bt_file = (Button) findViewById(R.id.bt_file);", "bt_file = (Button) findViewById(R.id.bt_file);\n\t\tbtn_add_video = (android.widget.ImageView) findViewById(R.id.btn_add_video);")

# We already added the click listener earlier via the other script which failed, wait... did it?
# Let's just add it near bt_file.setOnClickListener(this);
content = content.replace("bt_file.setOnClickListener(this);", "bt_file.setOnClickListener(this);\n\t\tif (btn_add_video != null) btn_add_video.setOnClickListener(this);")

# Fix loadTimelineThumbnails
content = content.replace("private void loadTimelineThumbnails(final String path) {", "private void loadTimelineThumbnails(final String path) {\n\t\tloadTimelineThumbnails(path, true);\n\t}\n\n\tprivate void loadTimelineThumbnails(final String path, final boolean clearOld) {")
content = content.replace("timeline_thumbnails.removeAllViews();", "if (clearOld) timeline_thumbnails.removeAllViews();")

with open(f_edit, "w") as f:
    f.write(content)

print("Fixed")
