import re

file_path = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(file_path, "r") as f:
    content = f.read()

# 1. Change declaration
content = content.replace("private String videoUrl;", "private java.util.ArrayList<String> videoUrls = new java.util.ArrayList<>();")

# 2. Add btn_add_video bindings
content = content.replace("private ImageView bt_file;", "private ImageView bt_file;\n\tprivate ImageView btn_add_video;")
content = content.replace("bt_file = (ImageView) findViewById(R.id.bt_file);", "bt_file = (ImageView) findViewById(R.id.bt_file);\n\t\tbtn_add_video = (ImageView) findViewById(R.id.btn_add_video);")
content = content.replace("bt_file.setOnClickListener(this);", "bt_file.setOnClickListener(this);\n\t\tif (btn_add_video != null) btn_add_video.setOnClickListener(this);")

# 3. onCreate intent handling
content = content.replace("""		videoUrl = getIntent().getStringExtra("VIDEO_PATH");
		if (videoUrl != null && !videoUrl.isEmpty()) {
			setupVideoPlayer(videoUrl);""", """		String initPath = getIntent().getStringExtra("VIDEO_PATH");
		if (initPath != null && !initPath.isEmpty()) {
			videoUrls.add(initPath);
			setupVideoPlayer(initPath);""")

# 4. click listener for btn_add_video
content = content.replace("""		} else if (id == R.id.bt_file) {
			chooseFile();""", """		} else if (id == R.id.bt_file) {
			chooseFile();
		} else if (id == R.id.btn_add_video) {
			chooseFile();""")

# 5. execVideo intent
content = content.replace("""	private void execVideo() {
		if (videoUrl != null && !videoUrl.isEmpty()) {""", """	private void execVideo() {
		if (!videoUrls.isEmpty()) {""")

content = content.replace("""			Intent intent = new Intent(EditActivity.this, ExportActivity.class);
			intent.putExtra("VIDEO_PATH", videoUrl);""", """			Intent intent = new Intent(EditActivity.this, ExportActivity.class);
			intent.putStringArrayListExtra("VIDEO_PATHS", videoUrls);""")

# 6. onActivityResult
content = content.replace("""		if (requestCode == CHOOSE_FILE && resultCode == RESULT_OK && data != null) {
			videoUrl = UriUtils.getPath(EditActivity.this, data.getData());
			setupVideoPlayer(videoUrl);
		}""", """		if (requestCode == CHOOSE_FILE && resultCode == RESULT_OK && data != null) {
			String newVideoUrl = UriUtils.getPath(EditActivity.this, data.getData());
			videoUrls.add(newVideoUrl);
			if (videoUrls.size() == 1) {
				setupVideoPlayer(newVideoUrl);
			} else {
				appendVideo(newVideoUrl);
			}
		}""")

# 7. add appendVideo method
append_logic = """
	private void appendVideo(String path) {
		if (exoPlayer != null) {
			androidx.media3.common.MediaItem mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(path));
			exoPlayer.addMediaItem(mediaItem);
			if (!exoPlayer.isPlaying()) {
				exoPlayer.prepare();
			}
			loadTimelineThumbnails(path, false);
            Toast.makeText(this, R.string.toast_video_added, Toast.LENGTH_SHORT).show();
		}
	}
"""
content = content.replace("private void chooseFile()", append_logic + "\n\tprivate void chooseFile()")

# 8. loadTimelineThumbnails should append, not clear, if append is true
content = content.replace("private void loadTimelineThumbnails(String path) {", "private void loadTimelineThumbnails(String path) {\n\t\tloadTimelineThumbnails(path, true);\n\t}\n\n\tprivate void loadTimelineThumbnails(String path, boolean clearOld) {")
content = content.replace("timeline_thumbnails.removeAllViews();", "if (clearOld) timeline_thumbnails.removeAllViews();")

# 9. In setupVideoPlayer, keep track of all media items? No, it only adds the first one. Let's fix that.
# Replace exoPlayer.setMediaItem with exoPlayer.setMediaItems if we want to reset.
# Actually, setupVideoPlayer is only called when size == 1, so setMediaItem is fine.

# Write back
with open(file_path, "w") as f:
    f.write(content)

print("Done")
