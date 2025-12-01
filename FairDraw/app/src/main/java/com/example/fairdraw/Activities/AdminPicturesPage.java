package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;

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

    }
}

