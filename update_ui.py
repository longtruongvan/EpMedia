import re

f_xml = "testapp/src/main/res/layout/activity_edit.xml"
with open(f_xml, "r") as f:
    xml = f.read()

# 1. Remove undo/redo from top bar
undo_redo_block = """                <View
                    android:layout_width="1dp"
                    android:layout_height="24dp"
                    android:background="#1AFFFFFF"
                    android:layout_marginHorizontal="12dp" />

                <ImageView
                    android:id="@+id/bt_undo"
                    android:layout_width="30dp"
                    android:layout_height="30dp"
                    android:src="@drawable/ic_undo"
                    android:contentDescription="Undo"
                    android:layout_marginEnd="12dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_text_secondary" />

                <ImageView
                    android:id="@+id/bt_redo"
                    android:layout_width="30dp"
                    android:layout_height="30dp"
                    android:src="@drawable/ic_redo"
                    android:contentDescription="Redo"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_text_secondary" />"""
xml = xml.replace(undo_redo_block, "")

# 2. Insert seek buttons next to play/pause
play_pause_block = """                <!-- Play/Pause circle button -->
                <ImageView
                    android:id="@+id/iv_play_pause"
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@drawable/ic_play"
                    android:background="@drawable/bg_circle_play"
                    android:scaleType="centerInside"
                    android:layout_marginEnd="16dp"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_bg" />"""

seek_buttons_and_play = """                <!-- Seek backward (-0.5s) -->
                <ImageView
                    android:id="@+id/btn_seek_backward"
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@drawable/ic_undo"
                    android:layout_marginEnd="16dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_text_primary" />

                <!-- Play/Pause circle button -->
                <ImageView
                    android:id="@+id/iv_play_pause"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:src="@drawable/ic_play"
                    android:background="@drawable/bg_circle_play"
                    android:scaleType="centerInside"
                    android:layout_marginEnd="16dp"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_bg" />

                <!-- Seek forward (+0.5s) -->
                <ImageView
                    android:id="@+id/btn_seek_forward"
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@drawable/ic_redo"
                    android:layout_marginEnd="16dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:focusable="true"
                    app:tint="@color/lumina_text_primary" />"""

xml = xml.replace(play_pause_block, seek_buttons_and_play)

# 3. Redesign Export button
export_old = """            <Button
                android:id="@+id/bt_exec"
                android:layout_width="wrap_content"
                android:layout_height="36dp"
                android:backgroundTint="@color/colorAccent"
                android:textColor="@color/lumina_bg"
                android:text="@string/export"
                android:textSize="13sp"
                android:textStyle="bold"
                android:paddingHorizontal="16dp"
                app:cornerRadius="8dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true" />"""

export_new = """            <androidx.appcompat.widget.AppCompatButton
                android:id="@+id/bt_exec"
                android:layout_width="wrap_content"
                android:layout_height="36dp"
                android:background="@drawable/bg_btn_export_premium"
                android:textColor="@android:color/white"
                android:text="@string/export"
                android:textSize="13sp"
                android:textStyle="bold"
                android:paddingHorizontal="24dp"
                android:elevation="4dp"
                android:stateListAnimator="@null"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true" />"""

xml = xml.replace(export_old, export_new)

with open(f_xml, "w") as f:
    f.write(xml)

print("Updated XML")
