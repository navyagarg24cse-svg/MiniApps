package com.example.galleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private final List<ImageModel> imageList = new ArrayList<>();

    // ---------------------------------------------------------------
    // Permission Launcher — READ_MEDIA_IMAGES (API 33+) or
    //                        READ_EXTERNAL_STORAGE (API < 33)
    // ---------------------------------------------------------------
    private final ActivityResultLauncher<String> readPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            loadImages();
                        } else {
                            Toast.makeText(this,
                                    "Storage permission is required to view images.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gallery");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        // 3-column grid
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        imageAdapter = new ImageAdapter(this, imageList, imageModel -> {
            // Click → open DetailActivity
            Intent intent = new Intent(GalleryActivity.this, DetailActivity.class);
            intent.putExtra("image_path", imageModel.getPath());
            startActivity(intent);
        });
        recyclerView.setAdapter(imageAdapter);

        checkPermissionAndLoad();
    }

    // Reload gallery when returning from DetailActivity (in case an image was deleted)
    @Override
    protected void onResume() {
        super.onResume();
        if (hasReadPermission()) {
            loadImages();
        }
    }

    // ---------------------------------------------------------------
    // Check the correct read permission depending on API level
    // ---------------------------------------------------------------
    private void checkPermissionAndLoad() {
        if (hasReadPermission()) {
            loadImages();
        } else {
            // Android 13+ uses READ_MEDIA_IMAGES; below that READ_EXTERNAL_STORAGE
            String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            readPermissionLauncher.launch(permission);
        }
    }

    private boolean hasReadPermission() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ---------------------------------------------------------------
    // Query MediaStore for all images on the device
    // ---------------------------------------------------------------
    private void loadImages() {
        imageList.clear();

        // Columns we want from MediaStore
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,        // absolute file path
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
        };

        // Sort newest first
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder)) {

            if (cursor != null) {
                int nameCol  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int pathCol  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int sizeCol  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int dateCol  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                int idCol    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameCol);
                    String path = cursor.getString(pathCol);
                    long   size = cursor.getLong(sizeCol);
                    long   date = cursor.getLong(dateCol);
                    long   id   = cursor.getLong(idCol);

                    // Build a content URI from the MediaStore row id
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            String.valueOf(id));

                    imageList.add(new ImageModel(name, path, size, date, contentUri));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading images: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        imageAdapter.notifyDataSetChanged();

        if (imageList.isEmpty()) {
            Toast.makeText(this, "No images found on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}