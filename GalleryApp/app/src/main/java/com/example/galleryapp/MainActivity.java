package com.example.galleryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Uri photoUri; // MediaStore URI given to the camera

    // ── Camera result ───────────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    (ActivityResult result) -> {
                        if (result.getResultCode() == RESULT_OK && photoUri != null) {

                            // ✅ KEY FIX: Clear IS_PENDING so photo appears in gallery
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                getContentResolver().update(photoUri, values, null, null);
                            }

                            Toast.makeText(this,
                                    "Photo saved to gallery!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Cancelled — remove the empty placeholder row
                            if (photoUri != null) {
                                getContentResolver().delete(photoUri, null, null);
                                photoUri = null;
                            }
                            Toast.makeText(this,
                                    "Camera cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    });

    // ── Permission result ───────────────────────────────────────────────────
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    grants -> {
                        boolean allGranted = true;
                        for (Boolean v : grants.values()) {
                            if (!v) { allGranted = false; break; }
                        }
                        if (allGranted) {
                            openCamera();
                        } else {
                            Toast.makeText(this,
                                    "Camera permission is required.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    // ── onCreate ────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCardView cardTakePhoto   = findViewById(R.id.cardTakePhoto);
        MaterialCardView cardOpenGallery = findViewById(R.id.cardOpenGallery);

        cardTakePhoto.setOnClickListener(v   -> checkPermissionsAndOpenCamera());
        cardOpenGallery.setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
    }

    // ── Check permissions first ─────────────────────────────────────────────
    private void checkPermissionsAndOpenCamera() {
        List<String> needed = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CAMERA);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (needed.isEmpty()) {
            openCamera();
        } else {
            permissionLauncher.launch(needed.toArray(new String[0]));
        }
    }

    // ── Create MediaStore row → launch camera ───────────────────────────────
    private void openCamera() {
        String fileName = "IMG_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE,    "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Save to DCIM/Camera — visible in every gallery app
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera");
            // IS_PENDING = 1 hides the file while camera is writing it
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        // Insert row into MediaStore → returns a content:// URI
        photoUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (photoUri == null) {
            Toast.makeText(this, "Could not create image entry.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app found.", Toast.LENGTH_SHORT).show();
            getContentResolver().delete(photoUri, null, null);
            photoUri = null;
        }
    }
}