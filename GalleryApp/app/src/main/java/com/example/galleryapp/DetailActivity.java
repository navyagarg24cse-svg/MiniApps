package com.example.galleryapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Image Detail");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the file path passed from GalleryActivity
        imagePath = getIntent().getStringExtra("image_path");

        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "Image not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView imageView    = findViewById(R.id.imageViewDetail);
        TextView  tvName       = findViewById(R.id.tvName);
        TextView  tvPath       = findViewById(R.id.tvPath);
        TextView  tvSize       = findViewById(R.id.tvSize);
        TextView  tvDate       = findViewById(R.id.tvDate);
        Button    btnDelete    = findViewById(R.id.btnDelete);

        File imageFile = new File(imagePath);

        // Load image safely using Glide (handles large images without OOM crash)
        Glide.with(this)
                .load(imageFile)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .into(imageView);

        // Populate metadata
        tvName.setText("Name: " + imageFile.getName());
        tvPath.setText("Path: " + imagePath);
        tvSize.setText("Size: " + formatSize(imageFile.length()));
        tvDate.setText("Date: " + formatDate(imageFile.lastModified()));

        btnDelete.setOnClickListener(v -> confirmDelete(imageFile));
    }

    // ---------------------------------------------------------------
    // Show a confirmation dialog before deleting
    // ---------------------------------------------------------------
    private void confirmDelete(File imageFile) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete \"" + imageFile.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(imageFile))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------------
    // Delete from file system AND remove from MediaStore
    // ---------------------------------------------------------------
    private void deleteImage(File imageFile) {
        boolean deleted = false;

        // 1. Delete the actual file from storage
        if (imageFile.exists()) {
            deleted = imageFile.delete();
        }

        // 2. Remove the stale entry from MediaStore so it no longer shows
        //    in the system gallery or our app's query results.
        if (deleted) {
            try {
                ContentResolver resolver = getContentResolver();
                resolver.delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?",
                        new String[]{imagePath}
                );
            } catch (Exception e) {
                e.printStackTrace();
                // Non-fatal: the file is deleted even if MediaStore update fails
            }

            Toast.makeText(this, "Image deleted.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);  // signal GalleryActivity to refresh
            finish();
        } else {
            Toast.makeText(this, "Could not delete the image. It may already be gone.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private String formatSize(long bytes) {
        if (bytes < 1024)       return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024));
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                .format(new Date(millis));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}