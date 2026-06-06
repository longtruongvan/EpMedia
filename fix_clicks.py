import re

f_xml = "testapp/src/main/res/layout/activity_edit.xml"
with open(f_xml, "r") as f:
    xml = f.read()

clip_effects_old = """                            <!-- Clip: Teal & Orange -->
                            <LinearLayout
                                android:layout_width="180dp"
                                android:layout_height="match_parent"
                                android:layout_marginStart="160dp"
                                android:background="@drawable/bg_track_clip"
                                android:backgroundTint="#33FF998C"
                                android:orientation="horizontal"
                                android:gravity="center_vertical"
                                android:paddingHorizontal="8dp">"""

clip_effects_new = """                            <!-- Clip: Teal & Orange -->
                            <LinearLayout
                                android:id="@+id/clip_effects"
                                android:layout_width="180dp"
                                android:layout_height="match_parent"
                                android:layout_marginStart="160dp"
                                android:background="@drawable/bg_track_clip"
                                android:backgroundTint="#33FF998C"
                                android:orientation="horizontal"
                                android:gravity="center_vertical"
                                android:paddingHorizontal="8dp"
                                android:clickable="true"
                                android:focusable="true">"""

xml = xml.replace(clip_effects_old, clip_effects_new)

clip_text_old = """                            <!-- Clip: Summer Vibes -->
                            <LinearLayout
                                android:layout_width="140dp"
                                android:layout_height="match_parent"
                                android:layout_marginStart="240dp"
                                android:background="@drawable/bg_track_clip"
                                android:backgroundTint="#33D1BCFF"
                                android:orientation="horizontal"
                                android:gravity="center_vertical"
                                android:paddingHorizontal="8dp">"""

clip_text_new = """                            <!-- Clip: Summer Vibes -->
                            <LinearLayout
                                android:id="@+id/clip_text"
                                android:layout_width="140dp"
                                android:layout_height="match_parent"
                                android:layout_marginStart="240dp"
                                android:background="@drawable/bg_track_clip"
                                android:backgroundTint="#33D1BCFF"
                                android:orientation="horizontal"
                                android:gravity="center_vertical"
                                android:paddingHorizontal="8dp"
                                android:clickable="true"
                                android:focusable="true">"""

xml = xml.replace(clip_text_old, clip_text_new)

with open(f_xml, "w") as f:
    f.write(xml)

f_java = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_java, "r") as f:
    java = f.read()

# Replace track_effects with clip_effects
java = java.replace("track_effects", "clip_effects")
java = java.replace("track_text", "clip_text")
java = java.replace("android.widget.FrameLayout clip_effects", "android.widget.LinearLayout clip_effects")
java = java.replace("android.widget.FrameLayout clip_text", "android.widget.LinearLayout clip_text")

with open(f_java, "w") as f:
    f.write(java)

print("Fixed clip clicks")
