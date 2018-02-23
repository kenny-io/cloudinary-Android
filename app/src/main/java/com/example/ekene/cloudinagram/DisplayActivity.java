package com.example.ekene.cloudinagram;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class DisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference DBRef;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        //database = FirebaseDatabase.getInstance();
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        DBRef = database.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Cloudinagram, CloudinagramViewHolder> FBRA = new
                FirebaseRecyclerAdapter<Cloudinagram, CloudinagramViewHolder>(
                Cloudinagram.class,
                R.layout.card_items,
                CloudinagramViewHolder.class,
                DBRef
        ) {
            @Override
            protected void populateViewHolder(CloudinagramViewHolder viewHolder, Cloudinagram model, int position) {

                viewHolder.setMessage(model.getMessage());
                viewHolder.setImageUrl(getApplicationContext(), model.getImageUrl());


            }
        };

        recyclerView.setAdapter(FBRA);

    }

    public static class CloudinagramViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CloudinagramViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message) {
            TextView post_message = mView.findViewById(R.id.post_txtview);
            post_message.setText(message);
        }

        public void setImageUrl(Context ctx, String imageUrl) {
            ImageView post_image = mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(imageUrl).into(post_image);
        }

    }
}
