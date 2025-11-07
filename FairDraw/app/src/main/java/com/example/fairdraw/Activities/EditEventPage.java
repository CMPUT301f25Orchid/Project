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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass used to edit an event.
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

        // Create launcher to retrieve photo from library
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        bannerPhoto = null;
                        if (result.getData() != null) {
                            bannerPhoto = result.getData().getData();
                            Toast.makeText(view.getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(view.getContext(), "No image selected", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(v.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(v.getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}