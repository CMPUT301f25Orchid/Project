package com.example.fairdraw.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.text.InputType;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.example.fairdraw.Activities.BaseTopBottomActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.EventState;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.ServiceUtility.DeepLinkUtil;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.example.fairdraw.ServiceUtility.QrUtil;

/**
 * Activity for creating a new event.
 * <p>
 * Presents a form collecting event details and uploads a new Event object into
 * {@link com.example.fairdraw.Others.OrganizerEventsDataHolder} when the user confirms.
 */
public class CreateEventPage extends BaseTopBottomActivity {

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
    
    // Tag input fields
    private ChipGroup chipGroupTags;
    private EditText eventTagInput;
    private Button btnAddTag;



    /**
     * Activity lifecycle entry point. Sets up views, result launchers and click handlers.
     * @param savedInstanceState previously saved state or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_creation_page);
        // Initialize organizer top & bottom nav if present
        initTopNav(BarType.ORGANIZER);
        BottomNavigationView bn = findViewById(R.id.home_bottom_nav_bar);
        if (bn != null)
        {
            initBottomNav(BarType.ORGANIZER, bn);
            bn.setSelectedItemId(R.id.create_activity);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.organizer_navigation_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Get DeviceId
        final String deviceID = DevicePrefsManager.getDeviceId(this);

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
        
        // Initialize tag input fields
        chipGroupTags = inputLayout.findViewById(R.id.chip_group_tags);
        eventTagInput = inputLayout.findViewById(R.id.event_tag_input);
        btnAddTag = inputLayout.findViewById(R.id.btn_add_tag);
        
        // Setup tag add button click
        btnAddTag.setOnClickListener(v -> addTagChip(eventTagInput.getText().toString()));
        
        // Setup IME Done action to add tag
        eventTagInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTagChip(eventTagInput.getText().toString());
                return true;
            }
            return false;
        });

        // --- New: attach DatePickerDialogs to date EditTexts and disable keyboard input ---
        View.OnClickListener dateClickListener = v -> {
            final EditText et = (EditText) v;
            Calendar c = Calendar.getInstance();
            try {
                Date parsed = dateFormat.parse(et.getText().toString());
                if (parsed != null) c.setTime(parsed);
            } catch (Exception ignored) {
            }
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(CreateEventPage.this,
                    (DatePicker view, int y, int m, int d) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(y, m, d);
                        et.setText(dateFormat.format(chosen.getTime()));
                    }, year, month, day);
            dpd.show();
        };

        // Disable keyboard entry and show calendar on click/focus for all date fields
        EditText[] dateFields = new EditText[]{eventRegistrationOpenDate, eventRegistrationCloseDate, eventStartDate, eventEndDate};
        for (EditText field : dateFields) {
            field.setInputType(InputType.TYPE_NULL);
            field.setFocusable(false);
            field.setOnClickListener(dateClickListener);
            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) v.performClick();
            });
        }
        // --- End date picker setup ---

        // Initialize activity result launcher to get photo from gallery
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if (result.getResultCode() == RESULT_OK) {
                bannerPhoto = null;
                if (result.getData() != null) {
                    bannerPhoto = result.getData().getData();
                    Snackbar.make(findViewById(android.R.id.content), "Image uploaded", Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content), "No image selected", Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(findViewById(android.R.id.content), "Please fill in all fields", Snackbar.LENGTH_SHORT).show();
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
                    event.setOrganizer(deviceID);
                    if (!eventLimit.getText().toString().isEmpty()){
                        Integer limit = Integer.parseInt(eventLimit.getText().toString());
                        event.setWaitingListLimit(limit);
                    }
                    
                    // Collect tags from chips
                    List<String> tags = collectTagsFromChips();
                    event.setTags(tags);
                    
                    if (bannerPhoto == null){
                        // Use default banner image if none uploaded
                        bannerPhoto = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.default_event_banner);
                    }

                    FirebaseImageStorageService storageService = new FirebaseImageStorageService();
                    storageService.uploadEventPoster(event.getUuid(), bannerPhoto).addOnSuccessListener(uri -> {
                        Log.d("CreateEventPage", "Banner photo uploaded for event: " + event.getUuid());
                    }).addOnFailureListener(e -> {
                        Log.e("CreateEventPage", "Error uploading banner photo for event: " + event.getUuid(), e);
                    });

                    //Make QR code using DeepLinkUtil
                    Bundle extras = new Bundle();
                    extras.putString("event_id", event.getUuid());
                    Uri deepLinkUri = DeepLinkUtil.buildLink(EntrantEventDetails.class, extras);
                    Bitmap qr;
                    try {
                        qr = QrUtil.generate(deepLinkUri.toString(), 800);
                        // Save QR code to Firebase Storage
                        storageService.uploadEventQr(event.getUuid(), qr, 90, 1000000).addOnSuccessListener(uri -> {
                            Log.d("CreateEventPage", "QR code uploaded for event: " + event.getUuid());
                        }).addOnFailureListener(e -> {
                            Log.e("CreateEventPage", "Error uploading QR code for event: " + event.getUuid(), e);
                        });
                    } catch (Exception e) {
                        Log.e("CreateEventPage", "Error generating QR code for event: " + event.getUuid(), e);
                    }

                    event.setState(EventState.PUBLISHED);

                    OrganizerEventsDataHolder.addEvent(event);
                    // Save event to database
                    EventDB.addEvent(event, (ok) -> {
                        if (!ok) {
                            Log.e("CreateEventPage", "Error saving event to database: " + event.getUuid());
                        }
                    });
                    Log.d("CreateEventPage", "Event created: " + event.getUuid());
                }
                catch (Exception e) {
                    Log.e("CreateEventPage", "Error creating event", e);
                    Snackbar.make(findViewById(android.R.id.content), "Error creating event: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }

                // Return to Organizer Main Page
                Intent intent = new Intent(CreateEventPage.this, OrganizerMainPage.class);
                startActivity(intent);
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
    
    /**
     * Adds a tag chip to the ChipGroup. Prevents duplicate tags (case-insensitive).
     * @param tag The tag text to add
     */
    private void addTagChip(String tag) {
        String normalizedTag = tag.trim();
        if (normalizedTag.isEmpty()) {
            return;
        }
        
        // Check for case-insensitive duplicate (O(n) is acceptable for small tag count)
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip existingChip = (Chip) chipGroupTags.getChildAt(i);
            if (existingChip.getText().toString().equalsIgnoreCase(normalizedTag)) {
                Snackbar.make(findViewById(android.R.id.content), "Tag already exists", Snackbar.LENGTH_SHORT).show();
                eventTagInput.setText("");
                return;
            }
        }
        
        // Inflate the chip layout and add it to the ChipGroup
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.standalone_chip, chipGroupTags, false);
        chip.setText(normalizedTag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
        chipGroupTags.addView(chip);
        eventTagInput.setText("");
    }
    
    /**
     * Collects all tags from the ChipGroup into a list.
     * @return List of tag strings
     */
    private List<String> collectTagsFromChips() {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            tags.add(chip.getText().toString());
        }
        return tags;
    }
}