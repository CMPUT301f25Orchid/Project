package com.example.fairdraw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class CreateEventPage extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    View bottomNavInclude;
    BottomNavigationView bottomNav;
    Uri bannerPhoto;
    Button confirmCreateEvent;
    Button uploadPosterImage;
    View inputLayout;
    EditText eventTitle;
    EditText eventDescription;
    EditText eventCapacity;
    EditText eventRegistrationOpenDate;
    EditText eventRegistrationCloseDate;
    EditText eventStartDate;
    EditText eventEndDate;
    EditText eventLocation;
    EditText eventPrice;
    EditText eventLimit;
    RadioGroup eventGeolocation;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_creation_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.organizer_navigation_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize view variables
        confirmCreateEvent = findViewById(R.id.confirm_create_event);
        inputLayout = findViewById(R.id.event_creation_input_layout);
        uploadPosterImage = inputLayout.findViewById(R.id.poster_upload);
        eventTitle = inputLayout.findViewById(R.id.event_title);
        eventDescription = inputLayout.findViewById(R.id.event_description);
        eventCapacity = inputLayout.findViewById(R.id.event_capacity);
        eventRegistrationOpenDate = inputLayout.findViewById(R.id.event_registration_start_date);
        eventRegistrationCloseDate = inputLayout.findViewById(R.id.event_registration_end_date);
        eventStartDate = inputLayout.findViewById(R.id.event_start_date);
        eventEndDate = inputLayout.findViewById(R.id.event_end_date);
        eventLocation = inputLayout.findViewById(R.id.event_location);
        eventPrice = inputLayout.findViewById(R.id.event_price);
        eventLimit = inputLayout.findViewById(R.id.event_limit);
        eventGeolocation = inputLayout.findViewById(R.id.event_geolocation);
        bottomNavInclude = findViewById(R.id.create_bottom_nav_bar);

        // Initialize activity result launcher to get photo from gallery
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if (result.getResultCode() == RESULT_OK) {
                bannerPhoto = null;
                if (result.getData() != null) {
                    bannerPhoto = result.getData().getData();
                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle confirm create event button click
        confirmCreateEvent.setOnClickListener(v -> {
            if (eventTitle.getText().toString().isEmpty() ||
                    eventDescription.getText().toString().isEmpty() ||
                    eventCapacity.getText().toString().isEmpty() ||
                    eventRegistrationOpenDate.getText().toString().isEmpty() ||
                    eventRegistrationCloseDate.getText().toString().isEmpty() ||
                    eventStartDate.getText().toString().isEmpty() ||
                    eventEndDate.getText().toString().isEmpty() ||
                    eventLocation.getText().toString().isEmpty() ||
                    eventPrice.getText().toString().isEmpty() ||
                    eventGeolocation.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Date openDate = dateFormat.parse(eventRegistrationOpenDate.getText().toString());
                    Date closeDate = dateFormat.parse(eventRegistrationCloseDate.getText().toString());
                    Date startDate = dateFormat.parse(eventStartDate.getText().toString());
                    Date endDate = dateFormat.parse(eventEndDate.getText().toString());
                    Event event = new Event(
                            eventTitle.getText().toString(),
                            eventDescription.getText().toString(),
                            Integer.parseInt(eventCapacity.getText().toString()),
                            null,
                            null,
                            openDate,
                            closeDate,
                            null,
                            startDate,
                            endDate,
                            eventLocation.getText().toString(),
                            null,
                            Float.parseFloat(eventPrice.getText().toString()),
                            eventGeolocation.getCheckedRadioButtonId() == R.id.geo_yes,
                            null,
                            null
                    );
                    if (!eventLimit.getText().toString().isEmpty()){
                        Integer limit = Integer.parseInt(eventLimit.getText().toString());
                        System.out.println(limit);
                        event.setWaitingListLimit(limit);
                    }
                    if (bannerPhoto != null){
                        event.setPosterPath(bannerPhoto.toString());
                    }
                    String qrText = event.getTitle().concat(" - ").concat(event.getDescription());
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(qrText, BarcodeFormat.QR_CODE, 400, 400);
                    event.setQrSlug(bitmap.toString());

                    // Upload event to database
                    EventDB.addEvent(event, success -> {
                        if (success) {
                            Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (Exception e) {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Handle upload poster image button click
        uploadPosterImage.setOnClickListener(v -> {
            // Handle upload poster image button click
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcher.launch(intent);
        });
        // Return to Organizer Main Page
        bottomNav = bottomNavInclude.findViewById(R.id.home_bottom_nav_bar);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home_activity) {

                Intent intent = new Intent(CreateEventPage.this, OrganizerMainPage.class);
                startActivity(intent);
            }
            return true;
        });

    }
}