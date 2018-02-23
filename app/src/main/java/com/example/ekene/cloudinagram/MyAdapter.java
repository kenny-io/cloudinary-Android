package com.example.ekene.cloudinagram;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<Cloudinagram>cloudinagrams;
    private Context context;

    public MyAdapter(List<Cloudinagram> cloudinagrams, Context context) {
        this.cloudinagrams = cloudinagrams;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cloudinagram cloudinagram = cloudinagrams.get(position);
        holder.postTitle.setText(cloudinagram.getMessage());


    }

    @Override
    public int getItemCount() {
        return cloudinagrams.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView postTitle;
        public ImageView postImage;

        public ViewHolder(View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.post_txtview);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }

}
