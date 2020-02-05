package com.example.android.mygarden;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

// https://github.com/udacity/AdvancedAndroid_MyGarden/compare/TWID.05-Exercise-GridView...TWID.05-Solution-GridView
public class PlantWidgetProvider extends AppWidgetProvider {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {
        // Get current width to decide on single plant vs garden grid view
        // TODO (5): Use getAppWidgetOptions to get widget width and use the appropriate RemoteView method
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        // we can do this for any other widget option or extras we have added
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        // how to create RemoteViews for each widget width
        RemoteViews rv;
        if (width < 300) {
            // display as single plan widget
            rv = getSinglePlantRemoteView(context, imgRes, plantId, showWater);
        } else {
            // show GridView of plants (just as in MainActivity)
            rv = getGardenGridRemoteView(context);
        }
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    /**
     * Updates ALL widget instances given the widget Ids and display information
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param imgRes           The image resource for single plant mode
     * @param plantId          The database ID for that plant
     * @param showWater        Boolean to show/hide water drop button
     * @param appWidgetIds     Array of widget Ids to be updated
     */
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }


    // TODO (4): separate the updateAppWidget logic into getGardenGridRemoteView and getSinglePlantRemoteView
    /**
     * Creates and returns the RemoteViews to be displayed in the single plant mode widget
     *
     * @param context   The context
     * @param imgRes    The image resource of the plant image to be displayed
     * @param plantId   The database plant Id for watering button functionality
     * @param showWater Boolean to either show/hide the water drop
     * @return The RemoteViews for the single plant mode widget
     */
    private static RemoteViews getSinglePlantRemoteView(Context context, int imgRes, long plantId, boolean showWater) {
        // Set the click handler to open the DetailActivity for plant ID,
        // or the MainActivity if plant ID is invalid
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID) {
            intent = new Intent(context, MainActivity.class);
        } else { // Set on click to open the corresponding detail activity
            Log.d(PlantWidgetProvider.class.getSimpleName(), "plantId=" + plantId);
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);
        // Update image and text
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        // Show/Hide the water drop button
        if (showWater) views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
        else views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        // Add the wateringservice click handler
        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        // Add the plant ID as extra to water only that plant when clicked
        wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, wateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
        return views;
    }

    // TODO (6): Set the PendingIntent template in getGardenGridRemoteView to launch PlantDetailActivity
    /**
     * Creates and returns the RemoteViews to be displayed in the GridView mode widget
     *
     * @param context The context
     * @return The RemoteViews for the GridView mode widget
     */
    private static RemoteViews getGardenGridRemoteView(Context context) {
       return  null;
    }

    // TODO To trigger update after widget size has changed (any widget option has changed)
    //  @TargetApi: just for lint tool purposes and has no outcome in runtime
    //  If you use API 23 methods & don't declare the TargetApi, you will just get some warnings indicating you're using API's not available in your minimum SDK version.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }



    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }

}
