import re

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_java, "r") as f:
    java = f.read()

seek_method_old = """	private void seekVideo(long offsetMs) {
		if (player != null) {
			long newPosition = player.getCurrentPosition() + offsetMs;
			if (newPosition < 0) newPosition = 0;
			if (newPosition > player.getDuration()) newPosition = player.getDuration();
			player.seekTo(newPosition);
		}
	}"""

seek_method_new = """	private void seekVideo(long offsetMs) {
		if (exoPlayer != null) {
			long newPosition = exoPlayer.getCurrentPosition() + offsetMs;
			if (newPosition < 0) newPosition = 0;
			if (newPosition > exoPlayer.getDuration()) newPosition = exoPlayer.getDuration();
			exoPlayer.seekTo(newPosition);
		}
	}"""

java = java.replace(seek_method_old, seek_method_new)

with open(f_java, "w") as f:
    f.write(java)

print("Fixed player name")
