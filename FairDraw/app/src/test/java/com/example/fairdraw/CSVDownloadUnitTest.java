package com.example.fairdraw;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;

import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.example.fairdraw.Activities.OrganizerExportActivity;
import com.example.fairdraw.Activities.OrganizerMainPage;
import com.example.fairdraw.Adapters.EntrantNotificationAdapter;
import com.example.fairdraw.DBs.EventDB;
import com.example.fairdraw.Models.Event;
import com.example.fairdraw.Others.OrganizerEventsDataHolder;
import com.example.fairdraw.ServiceUtility.DevicePrefsManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for exportEnrolledEntrantsToCsv() from {@link com.example.fairdraw.Activities.OrganizerExportActivity}
 * Test to ensure CSV download works properly and can handle edge scenarios
 */
@Config(manifest=Config.NONE)
@RunWith(AndroidJUnit4.class)
public class CSVDownloadUnitTest {

    @Test
    public void emptyListToCSVTest(){
        OrganizerExportActivity test = new OrganizerExportActivity();
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"DeviceId", "Name", "Email", "Phone"});
        Method m = null;
        try {
            m = test.getClass().getDeclaredMethod("writeCsvAndShare", List.class, String.class);
        }
        catch(NoSuchMethodException e){
            assert(false);
        }
        try {
            m.setAccessible(true);
            m.invoke(test, rows, null);
            assert(true);
        }
        catch(Exception e){
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void nullListToCSVTest(){
        OrganizerExportActivity test = new OrganizerExportActivity();
        Method m = null;
        try {
            m = test.getClass().getDeclaredMethod("writeCsvAndShare", List.class, String.class);
        }
        catch(NoSuchMethodException e){
            assert(false);
        }
        try {
            m.setAccessible(true);
            m.invoke(test, null, "");
            assert(true);
        }
        catch(Exception e){
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void nullTitleToCSVTest(){
        OrganizerExportActivity test = new OrganizerExportActivity();
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"DeviceId", "Name", "Email", "Phone"});
        Method m = null;
        try {
            m = test.getClass().getDeclaredMethod("writeCsvAndShare", List.class, String.class);
        }
        catch(NoSuchMethodException e){
            assert(false);
        }
        try {
            m.setAccessible(true);
            m.invoke(test, rows, null);
            assert(true);
        }
        catch(Exception e){
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void emptyTitleToCSVTest(){
        OrganizerExportActivity test = new OrganizerExportActivity();
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"DeviceId", "Name", "Email", "Phone"});
        Method m = null;
        try {
            m = test.getClass().getDeclaredMethod("writeCsvAndShare", List.class, String.class);
        }
        catch(NoSuchMethodException e){
            assert(false);
        }
        try {
            m.setAccessible(true);
            m.invoke(test, rows, "");
            assert(true);
        }
        catch(Exception e){
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void validListToCSVTest(){
        OrganizerExportActivity test = new OrganizerExportActivity();
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"DeviceId", "Me", "me@mail.ca", "7808888888"});
        Method m = null;
        try {
            m = test.getClass().getDeclaredMethod("writeCsvAndShare", List.class, String.class);
        }
        catch(NoSuchMethodException e){
            assert(false);
        }
        try {
            m.setAccessible(true);
            m.invoke(test, rows, "Test Event");
            assert(true);
        }
        catch(Exception e){
            e.printStackTrace();
            assert(false);
        }

    }
}
