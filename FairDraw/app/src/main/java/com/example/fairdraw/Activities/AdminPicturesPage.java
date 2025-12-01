package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AdminPicturesPage extends BaseTopBottomActivity {


    private LinearLayout pictureListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pictures_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initTopNav(BarType.ADMIN);
        initBottomNav(BarType.ADMIN, findViewById(R.id.bottom_nav));

        pictureListContainer = findViewById(R.id.picture_list_container);

        // Load and display pictures in pictureListContainer
        FirebaseImageStorageService imageService = new FirebaseImageStorageService();

        imageService.listAllEventPosters()
                .addOnSuccessListener(eventPosters -> {
                    // eventPosters is List<FirebaseImageStorageService.EventPosterInfo>
                    // Bind to a RecyclerView / GridView:
                    // - eventId -> label / subtitle
                    // - downloadUri -> load with Glide/Picasso/etc

                    displayPictures(eventPosters);
                })
                .addOnFailureListener(e -> {
                    // Show a snack bar / error UI

                    Snackbar.make(findViewById(R.id.main), "Failed to load pictures", Snackbar.LENGTH_LONG).show();
                });


    }

    public void displayPictures(List<FirebaseImageStorageService.EventPosterInfo> eventPosters) {
        pictureListContainer.removeAllViews(); // Clear previous views to prevent duplicates
        LayoutInflater inflater = LayoutInflater.from(this);
        for (FirebaseImageStorageService.EventPosterInfo posterInfo : eventPosters) {
            CardView cardView = (CardView) inflater.inflate(R.layout.picture_card, pictureListContainer, false);
            ImageView posterImage = cardView.findViewById(R.id.event_picture);
            TextView eventTitleView = cardView.findViewById(R.id.event_picture_title);
            TextView organizerView = cardView.findViewById(R.id.event_picture_author);

            // Set values
            EventDB.getEvent(posterInfo.eventId, event -> {
                if (event != null) {
                    eventTitleView.setText(event.getTitle());
                    organizerView.setText("By: " + event.getOrganizer());
                } else {
                    eventTitleView.setText("Unknown Event");
                    organizerView.setText("");
                }
            });

            // Load image using Glide
            Glide.with(this)
                    .load(posterInfo.downloadUri)
                    .placeholder(R.drawable.default_event_banner) // Optional placeholder
                    .error(R.drawable.default_event_banner) // Optional error image
                    .into(posterImage);

            pictureListContainer.addView(cardView);

        }
    }
}

