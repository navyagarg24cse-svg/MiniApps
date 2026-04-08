package com.example.galleryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    // Simple click listener interface — keeps the adapter decoupled from activities
    public interface OnImageClickListener {
        void onImageClick(ImageModel imageModel);
    }

    private final Context              context;
    private final List<ImageModel>     imageList;
    private final OnImageClickListener listener;

    public ImageAdapter(Context context,
                        List<ImageModel> imageList,
                        OnImageClickListener listener) {
        this.context   = context;
        this.imageList = imageList;
        this.listener  = listener;
    }

    // ---------------------------------------------------------------
    // Standard RecyclerView methods
    // ---------------------------------------------------------------
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the single item layout (a square ImageView)
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageModel model = imageList.get(position);

        // Glide handles:
        //   • Thumbnail decoding (no OOM)
        //   • Disk & memory caching
        //   • Placeholder while loading
        Glide.with(context)
                .load(new File(model.getPath()))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // ---------------------------------------------------------------
    // ViewHolder
    // ---------------------------------------------------------------
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}