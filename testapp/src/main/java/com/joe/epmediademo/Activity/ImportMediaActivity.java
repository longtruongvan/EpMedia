package com.joe.epmediademo.Activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.UriUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportMediaActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int PERMISSION_REQUEST_CODE = 100;
	private static final int SYSTEM_PICKER_REQUEST = 101;

	private ImageView btn_back;
	private Button btn_seg_recent;
	private Button btn_seg_videos;
	private Button btn_seg_photos;
	private RecyclerView recycler_media_grid;
	private LinearLayout layout_import_action;
	private Button btn_import_submit;

	private List<VideoItem> videoList = new ArrayList<>();
	private VideoAdapter adapter;
	private LruCache<String, Bitmap> thumbnailCache;
	private String templateId = null;
	private String initTool = null;

	// Representation of a video asset
	public static class VideoItem {
		public String path;
		public String name;
		public long durationMs;
		public boolean isSelected = false;

		public VideoItem(String path, String name, long durationMs) {
			this.path = path;
			this.name = name;
			this.durationMs = durationMs;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_media);

		templateId = getIntent().getStringExtra("TEMPLATE_ID");
		initTool = getIntent().getStringExtra("INIT_TOOL");

		// 4MB cache for video thumbnails
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		thumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};

		initView();
		checkPermissionsAndLoad();
	}

	private void initView() {
		btn_back = (ImageView) findViewById(R.id.btn_back);
		btn_seg_recent = (Button) findViewById(R.id.btn_seg_recent);
		btn_seg_videos = (Button) findViewById(R.id.btn_seg_videos);
		btn_seg_photos = (Button) findViewById(R.id.btn_seg_photos);
		recycler_media_grid = (RecyclerView) findViewById(R.id.recycler_media_grid);
		layout_import_action = (LinearLayout) findViewById(R.id.layout_import_action);
		btn_import_submit = (Button) findViewById(R.id.btn_import_submit);

		btn_back.setOnClickListener(this);
		btn_seg_recent.setOnClickListener(this);
		btn_seg_videos.setOnClickListener(this);
		btn_seg_photos.setOnClickListener(this);
		btn_import_submit.setOnClickListener(this);
		View btn_export_action = findViewById(R.id.btn_export_action);
		if (btn_export_action != null) {
			btn_export_action.setOnClickListener(this);
		}

		// Bottom navigation
		findViewById(R.id.nav_home).setOnClickListener(this);
		findViewById(R.id.nav_templates).setOnClickListener(this);
		findViewById(R.id.nav_projects).setOnClickListener(this);
		findViewById(R.id.nav_ai).setOnClickListener(this);
		findViewById(R.id.nav_profile).setOnClickListener(this);

		// Layout Manager
		recycler_media_grid.setLayoutManager(new GridLayoutManager(this, 3));
		adapter = new VideoAdapter();
		recycler_media_grid.setAdapter(adapter);
	}

	private void checkPermissionsAndLoad() {
		String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU 
				? Manifest.permission.READ_MEDIA_VIDEO 
				: Manifest.permission.READ_EXTERNAL_STORAGE;

		if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
		} else {
			queryLocalVideos();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				queryLocalVideos();
			} else {
				Toast.makeText(this, R.string.no_permission_toast, Toast.LENGTH_LONG).show();
				openSystemPicker();
			}
		}
	}

	private void queryLocalVideos() {
		videoList.clear();
		ContentResolver contentResolver = getContentResolver();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.DURATION
		};

		Cursor cursor = null;
		try {
			cursor = contentResolver.query(uri, projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
			if (cursor != null && cursor.moveToFirst()) {
				int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
				int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

				do {
					String path = cursor.getString(dataIndex);
					String name = cursor.getString(nameIndex);
					long duration = cursor.getLong(durationIndex);
					
					// Validate file existence
					if (path != null && new File(path).exists()) {
						videoList.add(new VideoItem(path, name, duration));
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) cursor.close();
		}

		if (videoList.isEmpty()) {
			Toast.makeText(this, R.string.no_video_toast, Toast.LENGTH_SHORT).show();
			openSystemPicker();
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	private void openSystemPicker() {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, SYSTEM_PICKER_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SYSTEM_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
			String path = UriUtils.getPath(this, data.getData());
			if (path != null && !path.isEmpty()) {
				Intent intent = new Intent(this, EditActivity.class);
				intent.putExtra("VIDEO_PATH", path);
				if (templateId != null) {
					intent.putExtra("TEMPLATE_ID", templateId);
				}
				if (initTool != null) {
					intent.putExtra("INIT_TOOL", initTool);
				}
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_back) {
			finish();
		} else if (id == R.id.btn_seg_recent) {
			setSegmentActive(btn_seg_recent);
			queryLocalVideos();
		} else if (id == R.id.btn_seg_videos) {
			setSegmentActive(btn_seg_videos);
			queryLocalVideos();
		} else if (id == R.id.btn_seg_photos) {
			setSegmentActive(btn_seg_photos);
			videoList.clear();
			adapter.notifyDataSetChanged();
			updateImportButtonState();
			Toast.makeText(this, R.string.no_photo_toast, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_import_submit) {
			ArrayList<String> selectedPaths = getSelectedVideoPaths();
			if (selectedPaths.size() == 1) {
				Intent intent = new Intent(this, EditActivity.class);
				intent.putExtra("VIDEO_PATH", selectedPaths.get(0));
				if (templateId != null) {
					intent.putExtra("TEMPLATE_ID", templateId);
				}
				if (initTool != null) {
					intent.putExtra("INIT_TOOL", initTool);
				}
				startActivity(intent);
				finish();
			} else if (selectedPaths.size() > 1) {
				openExportForSelectedVideos(selectedPaths);
			}
		} else if (id == R.id.btn_export_action) {
			ArrayList<String> selectedPaths = getSelectedVideoPaths();
			if (!selectedPaths.isEmpty()) {
				openExportForSelectedVideos(selectedPaths);
			} else {
				Toast.makeText(this, R.string.toast_select_video_first, Toast.LENGTH_SHORT).show();
			}
		} else if (id == R.id.nav_home || id == R.id.nav_templates || id == R.id.nav_projects || id == R.id.nav_ai || id == R.id.nav_profile) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("TARGET_TAB", id);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			finish();
		}
	}

	private void setSegmentActive(Button activeButton) {
		Button[] buttons = {btn_seg_recent, btn_seg_videos, btn_seg_photos};
		for (Button b : buttons) {
				if (b == activeButton) {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
					b.setTextColor(getResources().getColor(R.color.lumina_bg));
				} else {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
					b.setTextColor(getResources().getColor(R.color.lumina_text_secondary));
				}
			}
		}

	private ArrayList<String> getSelectedVideoPaths() {
		ArrayList<String> paths = new ArrayList<>();
		for (VideoItem item : videoList) {
			if (item.isSelected && item.path != null && !item.path.isEmpty()) {
				paths.add(item.path);
			}
		}
		return paths;
	}

	private void openExportForSelectedVideos(ArrayList<String> selectedPaths) {
		Intent intent = new Intent(this, ExportActivity.class);
		intent.putStringArrayListExtra("VIDEO_PATHS", selectedPaths);
		startActivity(intent);
		finish();
	}

	// RecyclerView Adapter
	private class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

		@NonNull
		@Override
		public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
			return new VideoViewHolder(view);
		}

		@Override
			public void onBindViewHolder(@NonNull final VideoViewHolder holder, int position) {
				final VideoItem item = videoList.get(position);

			// Setup duration label
			int secs = (int) (item.durationMs / 1000) % 60;
			int mins = (int) ((item.durationMs / (1000 * 60)) % 60);
			holder.tv_duration.setText(String.format(Locale.US, "%d:%02d", mins, secs));

			// Setup selected outline indicator
			if (item.isSelected) {
				holder.layout_selection_indicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
				holder.iv_check.setVisibility(View.VISIBLE);
				holder.itemView.setBackground(getResources().getDrawable(R.drawable.bg_active_clip));
			} else {
				holder.layout_selection_indicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4D000000")));
				holder.iv_check.setVisibility(View.GONE);
				holder.itemView.setBackground(null);
			}

				// Render thumbnail image dynamically from cache or disk
				holder.iv_thumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
				holder.iv_thumbnail.setBackgroundColor(android.graphics.Color.TRANSPARENT);
				Bitmap cached = thumbnailCache.get(item.path);
				if (cached != null) {
					holder.iv_thumbnail.setImageBitmap(cached);
				} else {
					// Asynchronous thumbnail generation
					new Thread(new Runnable() {
						@Override
						public void run() {
							final Bitmap bitmap = extractFrame(item.path);
							if (bitmap != null) {
								thumbnailCache.put(item.path, bitmap);
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										int adapterPosition = holder.getAdapterPosition();
										if (adapterPosition != RecyclerView.NO_POSITION
												&& adapterPosition < videoList.size()
												&& videoList.get(adapterPosition).path.equals(item.path)) {
											holder.iv_thumbnail.setImageBitmap(bitmap);
										}
									}
								});
							}
						}
					}).start();
				}

			// Item selection handler
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.isSelected = !item.isSelected;
					
					notifyDataSetChanged();
					updateImportButtonState();
				}
			});
		}

		private Bitmap extractFrame(String videoPath) {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			try {
				retriever.setDataSource(videoPath);
				return retriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			} catch (Exception e) {
				return null;
			} finally {
				try {
					retriever.release();
				} catch (Exception ignored) {}
			}
		}

		@Override
		public int getItemCount() {
			return videoList.size();
		}
	}

	private void updateImportButtonState() {
		int selectedCount = getSelectedVideoPaths().size();
		if (selectedCount > 0) {
			layout_import_action.setVisibility(View.VISIBLE);
			btn_import_submit.setText(getString(R.string.import_count_format, selectedCount));
		} else {
			layout_import_action.setVisibility(View.GONE);
		}
	}

	// View Holder
	private static class VideoViewHolder extends RecyclerView.ViewHolder {
		ImageView iv_thumbnail;
		TextView tv_duration;
		FrameLayout layout_selection_indicator;
		ImageView iv_check;

		VideoViewHolder(View itemView) {
			super(itemView);
			iv_thumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
			tv_duration = (TextView) itemView.findViewById(R.id.tv_duration);
			layout_selection_indicator = (FrameLayout) itemView.findViewById(R.id.layout_selection_indicator);
			iv_check = (ImageView) itemView.findViewById(R.id.iv_check);
		}
	}
}
