package com.example.galleryapp;

import android.net.Uri;

/**
 * Plain data class holding metadata for a single image.
 * Passed between activities and used by ImageAdapter.
 */
public class ImageModel {

    private final String name;    // display filename, e.g. "IMG_20240101.jpg"
    private final String path;    // absolute file path,  e.g. "/sdcard/DCIM/..."
    private final long   size;    // file size in bytes
    private final long   date;    // DATE_ADDED from MediaStore (seconds since epoch)
    private final Uri    uri;     // content:// URI from MediaStore

    public ImageModel(String name, String path, long size, long date, Uri uri) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.date = date;
        this.uri  = uri;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public long   getSize() { return size; }
    public long   getDate() { return date; }
    public Uri    getUri()  { return uri;  }
}