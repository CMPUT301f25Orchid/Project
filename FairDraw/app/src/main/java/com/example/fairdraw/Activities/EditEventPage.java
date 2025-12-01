package com.example.fairdraw.Activities;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment used to edit an existing {@link com.example.fairdraw.Models.Event}.
 * <p>
 * The fragment expects an integer argument named "position" which refers to the index of the
 * event in {@link com.example.fairdraw.Others.OrganizerEventsDataHolder#getDataList()}.
 * The fragment pre-fills the form with the event data and writes updates back via
 * {@link com.example.fairdraw.Others.OrganizerEventsDataHolder#updateEvent(Event, int)}.
 */
public class EditEventPage extends Fragment {

    private ActivityResultLauncher<Intent> launcher;
    Uri bannerPhoto = null;
    Button confirmEventEdit = null;
    Button cancelEventEdit = null;
    Button uploadPosterImage = null;
    View inputLayout = null;
    EditText eventTitle = null;
    EditText eventDescription = null;
    EditText eventCapacity = null;
    EditText eventRegistrationOpenDate = null;
    EditText eventRegistrationCloseDate = null;
    EditText eventStartDate = null;
    EditText eventEndDate = null;
    EditText eventLocation = null;
    EditText eventPrice = null;
    EditText eventLimit = null;
    RadioGroup eventGeolocation = null;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    Event event = null;
    Integer index;
    
    // Tag input fields
    private ChipGroup chipGroupTags;
    private EditText eventTagInput;
    private Button btnAddTag;

    /**
     * Required empty public constructor which attaches the fragment layout.
     */
    public EditEventPage() {
        // Required empty public constructor
        super(R.layout.fragment_edit_event_page);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_event_page, container, false);

    }

    /**
     * Called after the fragment view has been created. Binds views, pre-fills fields with
     * event data and attaches click listeners for uploading images and saving changes.
     *
     * @param view the root view for the fragment
     * @param savedInstanceState saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // Get selected event to edit
        index = requireArguments().getInt("position");
        event = OrganizerEventsDataHolder.getDataList().get(index);
        // Assign views to variables
        inputLayout = view.findViewById(R.id.edit_event_input_layout);
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
        confirmEventEdit = view.findViewById(R.id.confirm_event_edit);
        cancelEventEdit = view.findViewById(R.id.cancel_event_update);
        
        // Initialize tag input fields
        chipGroupTags = inputLayout.findViewById(R.id.chip_group_tags);
        eventTagInput = inputLayout.findViewById(R.id.event_tag_input);
        btnAddTag = inputLayout.findViewById(R.id.btn_add_tag);
        
        // Setup tag add button click
        btnAddTag.setOnClickListener(v -> addTagChip(eventTagInput.getText().toString()));
        
        // Setup IME Done action to add tag
        eventTagInput.setOnEditorActionListener((v, actionId, evt) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTagChip(eventTagInput.getText().toString());
                return true;
            }
            return false;
        });

        // Create launcher to retrieve photo from library
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                 result -> {
                     if (result.getResultCode() == RESULT_OK) {
                         bannerPhoto = null;
                         if (result.getData() != null) {
                             bannerPhoto = result.getData().getData();
                            Snackbar.make(view, "Image uploaded", Snackbar.LENGTH_SHORT).show();
                         }
                         else {
                            Snackbar.make(view, "No image selected", Snackbar.LENGTH_SHORT).show();
                         }
                     }
                 });

        // Set button listener to upload poster image
        uploadPosterImage.setOnClickListener(v -> {
            // Handle upload poster image button click
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcher.launch(intent);
        });

        // Return to Organizer Main Page
        cancelEventEdit.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        // Prefilling fields with event data
        if (event != null) {
            String openDate = dateFormat.format(event.getEventOpenRegDate());
            String closeDate = dateFormat.format(event.getEventCloseRegDate());
            String startDate = dateFormat.format(event.getStartDate());
            String endDate = dateFormat.format(event.getEndDate());
            eventTitle.setText(event.getTitle());
            eventDescription.setText(event.getDescription());
            eventCapacity.setText(event.getCapacity().toString());
            eventRegistrationOpenDate.setText(openDate);
            eventRegistrationCloseDate.setText(closeDate);
            eventStartDate.setText(startDate);
            eventEndDate.setText(endDate);
            eventLocation.setText(event.getLocation());
            eventPrice.setText(event.getPrice().toString());
            eventLimit.setText(event.getWaitingListLimit().toString());
            eventGeolocation.check(event.getGeolocation() ? R.id.geo_yes : R.id.geo_no);
            
            // Pre-fill existing tags as chips
            List<String> existingTags = event.getTags();
            if (existingTags != null) {
                for (String tag : existingTags) {
                    addTagChipWithoutValidation(tag);
                }
            }
        }

        // Updating event data
        confirmEventEdit.setOnClickListener(v->{
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
                    Snackbar.make(v, "Please fill in all fields", Snackbar.LENGTH_SHORT).show();
            } else {
                try {
                    Date openDate = dateFormat.parse(eventRegistrationOpenDate.getText().toString());
                    Date closeDate = dateFormat.parse(eventRegistrationCloseDate.getText().toString());
                    Date startDate = dateFormat.parse(eventStartDate.getText().toString());
                    Date endDate = dateFormat.parse(eventEndDate.getText().toString());
                    // Remember to change to update Event object that is passed
                    event.setTitle(eventTitle.getText().toString());
                    event.setDescription(eventDescription.getText().toString());
                    event.setCapacity(Integer.parseInt(eventCapacity.getText().toString()));
                    event.setEventOpenRegDate(openDate);
                    event.setEventCloseRegDate(closeDate);
                    event.setStartDate(startDate);
                    event.setEndDate(endDate);
                    event.setLocation(eventLocation.getText().toString());
                    event.setPrice(Float.parseFloat(eventPrice.getText().toString()));
                    event.setGeolocation(eventGeolocation.getCheckedRadioButtonId() == R.id.geo_yes);
                    event.setQrSlug(null);
                    event.setPosterPath(null);
                    event.setWaitingListLimit(null);
                    
                    // Collect tags from chips
                    List<String> tags = collectTagsFromChips();
                    event.setTags(tags);

                    if (!eventLimit.getText().toString().isEmpty()){
                        Integer limit = Integer.parseInt(eventLimit.getText().toString());
                        System.out.println(limit);
                        event.setWaitingListLimit(limit);
                    }
                    if (bannerPhoto != null){
                        event.setPosterPath(bannerPhoto.toString());
                    }
                    OrganizerEventsDataHolder.updateEvent(event, index);
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                catch (Exception e) {
                    Snackbar.make(v, "Invalid date format", Snackbar.LENGTH_SHORT).show();
                }
            }
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
                Snackbar.make(requireView(), "Tag already exists", Snackbar.LENGTH_SHORT).show();
                eventTagInput.setText("");
                return;
            }
        }
        
        // Inflate the chip layout and add it to the ChipGroup
        Chip chip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.standalone_chip, chipGroupTags, false);
        chip.setText(normalizedTag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
        chipGroupTags.addView(chip);
        eventTagInput.setText("");
    }
    
    /**
     * Adds a tag chip without duplicate validation (used for pre-filling existing tags).
     * @param tag The tag text to add
     */
    private void addTagChipWithoutValidation(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        Chip chip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.standalone_chip, chipGroupTags, false);
        chip.setText(tag.trim());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupTags.removeView(chip));
        chipGroupTags.addView(chip);
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