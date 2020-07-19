package com.example.vmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyartistHolder> {
    private Context mContext;
    private ArrayList<MusicFiles> artistFiles;
    View view;
    public ArtistAdapter(Context mContext, ArrayList<MusicFiles> artistFiles) {
        this.mContext = mContext;
        this.artistFiles = artistFiles;
    }

    @NonNull
    @Override
    public MyartistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(mContext).inflate(R.layout.artists_itme,parent,false);
        return new MyartistHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyartistHolder holder, final int position) {
        holder.artist_name.setText(artistFiles.get(position).getArtists());
        final byte[] image = getAlbumArt(artistFiles.get(position).getPath());
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.artist_image);
        } else {
            Glide.with(mContext).load(R.drawable.music).into(holder.artist_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,ArtistDetails.class);
                intent.putExtra("artistName",artistFiles.get(position).getArtists());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artistFiles.size();
    }

    public class MyartistHolder extends RecyclerView.ViewHolder {
        ImageView artist_image;
        TextView artist_name;
        public MyartistHolder(@NonNull View itemView) {
            super(itemView);
            artist_image=itemView.findViewById(R.id.artist_img);
            artist_name=itemView.findViewById(R.id.artist_file_name);

        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
