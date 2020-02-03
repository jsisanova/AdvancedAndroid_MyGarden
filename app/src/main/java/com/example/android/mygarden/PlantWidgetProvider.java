package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.mygarden.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
// TODO (3): Create a PlantWidgetProvider class that extends AppWidgetProvider and set updateAppWidget to handle clicks and launch MainActivity
// We communicate with app via broadcast messages, this makes widgets a broadcast receiver (so we need to register widget provider in manifest using a receiver tag)
// AppWidgetProvider extends BroadcastReceiver. So our widget class is a receiver class.
public class PlantWidgetProvider extends AppWidgetProvider {


    // set updateAppWidget to handle clicks and launch MainActivity
    // AppWidgetManager gives info about all existing widgets on home screen; it's also access to forcing an update on all these widgets
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget_provider);

        // we don't need this as we have image in widget and no text
//        CharSequence widgetText = context.getString(R.string.appwidget_text);
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // RemoteViews can be linked to PendingIntent that will launch once that view is clicked
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    // This is called to update the App Widget at intervals defined by the updatePeriodMillis attribute
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        // user can have multiple instances of the same widgets on home screen
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // This is called every time an App Widget is deleted from the App Widget host.
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    // This is called when an instance the App Widget is created for the first time.
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    // This is called when the last instance of your App Widget is deleted from the App Widget host.
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

