import re

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_java, "r") as f:
    java = f.read()

# Replace variable declarations
java = java.replace("private ImageView bt_undo;", "private ImageView btn_seek_backward;")
java = java.replace("private ImageView bt_redo;", "private ImageView btn_seek_forward;")
java = java.replace("bt_undo = (ImageView) findViewById(R.id.bt_undo);", "btn_seek_backward = (ImageView) findViewById(R.id.btn_seek_backward);")
java = java.replace("bt_redo = (ImageView) findViewById(R.id.bt_redo);", "btn_seek_forward = (ImageView) findViewById(R.id.btn_seek_forward);")
java = java.replace("bt_undo.setOnClickListener(this);", "if (btn_seek_backward != null) btn_seek_backward.setOnClickListener(this);")
java = java.replace("bt_redo.setOnClickListener(this);", "if (btn_seek_forward != null) btn_seek_forward.setOnClickListener(this);")

# Replace onClick bindings
undo_click = """		} else if (id == R.id.bt_undo) {
			undo();
		} else if (id == R.id.bt_redo) {
			redo();"""

seek_click = """		} else if (id == R.id.btn_seek_backward) {
			seekVideo(-500); // Back 0.5s
		} else if (id == R.id.btn_seek_forward) {
			seekVideo(500);  // Forward 0.5s"""

java = java.replace(undo_click, seek_click)

# Add seekVideo method
seek_method = """	private void seekVideo(long offsetMs) {
		if (player != null) {
			long newPosition = player.getCurrentPosition() + offsetMs;
			if (newPosition < 0) newPosition = 0;
			if (newPosition > player.getDuration()) newPosition = player.getDuration();
			player.seekTo(newPosition);
		}
	}
"""

# Insert seekVideo before updateUndoRedoButtons or undo method
java = java.replace("\tprivate void undo() {", seek_method + "\n\tprivate void undo() {")

# Remove updateUndoRedoButtons logic that tries to modify the buttons
update_ui = """		if (bt_undo != null) {
			bt_undo.setEnabled(!undoStack.isEmpty());
			bt_undo.setAlpha(undoStack.isEmpty() ? 0.4f : 1.0f);
		}
		if (bt_redo != null) {
			bt_redo.setEnabled(!redoStack.isEmpty());
			bt_redo.setAlpha(redoStack.isEmpty() ? 0.4f : 1.0f);
		}"""

java = java.replace(update_ui, "")

with open(f_java, "w") as f:
    f.write(java)

print("Updated EditActivity.java")
