package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;
import static com.example.android.mygarden.provider.PlantContract.INVALID_PLANT_ID;

// https://github.com/udacity/AdvancedAndroid_MyGarden/compare/TWID.04-Exercise-Extras...TWID.04-Solution-Extras
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PlantWateringService extends IntentService {

    // TODO (1): Change all ACTION_WATER_PLANTS to a more specific one ACTION_WATER_PLANT
//    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_WATER_PLANT = "com.example.android.mygarden.action.water_plant";
    // use EXTRA_PLANT_ID to pass the plant ID to the service and update the query to use SINGLE_PLANT_URI
    public static final String EXTRA_PLANT_ID = "com.example.android.mygarden.extra.PLANT_ID";;
    public static final String ACTION_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";


    public PlantWateringService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform WaterPlant action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * To be able to water a specific plant you will need to launch the same IntentService from the widget as for all plants but will also need to pass the Plant ID with it.
     * @see IntentService
     */
    public static void startActionWaterPlant(Context context, long plantId) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(EXTRA_PLANT_ID, plantId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform UpdatePlantWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WATER_PLANT.equals(action)) {
                // TODO Call getLongExtra to extract the plnatId in the service
                final long plantId = intent.getLongExtra(EXTRA_PLANT_ID, PlantContract.INVALID_PLANT_ID);
                handleActionWaterPlant(plantId);
            } else if (ACTION_UPDATE_PLANT_WIDGETS.equals(action)) {
                handleActionUpdatePlantWidgets();
            }
        }
    }

    /**
     * Handle action WaterPlant in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlant(long plantId) {
        // TODO Change the update query to use a SINGLE_PLANT_URI
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), plantId);
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only if that plant is still alive
        getContentResolver().update(
                SINGLE_PLANT_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});

        // TODO Always update widgets after watering plants
        startActionUpdatePlantWidgets(this);
    }


    /**
     * Handle action UpdatePlantWidgets in the provided background thread
     */
    private void handleActionUpdatePlantWidgets() {
        //Query to get the plant that's most in need for water (last watered)
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
        );
        // Extract the plant details
        int imgRes = R.drawable.grass; // Default image in case our garden is empty
        boolean canWater = false; // Default to hide the water drop button
        long plantId = INVALID_PLANT_ID;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
            plantId = cursor.getLong(idIndex);
            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);
            cursor.close();
            // TODO
            canWater = (timeNow - wateredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER && (timeNow - wateredAt) < PlantUtils.MAX_AGE_WITHOUT_WATER;
            imgRes = PlantUtils.getPlantImageRes(this, timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        //Now update all widgets
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes,plantId ,canWater,appWidgetIds);
    }
}
