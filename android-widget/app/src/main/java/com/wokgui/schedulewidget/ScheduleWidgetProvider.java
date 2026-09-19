package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

public class ScheduleWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.wokgui.schedulewidget.REFRESH";
    static final String ACTION_BOUNDARY = "com.wokgui.schedulewidget.BOUNDARY";
    static final String ACTION_TOGGLE_MODE = "com.wokgui.schedulewidget.TOGGLE_MODE";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) { ScheduleStore.ensureInitialized(context); for (int id : appWidgetIds) updateWidget(context, manager, id); scheduleNextBoundary(context); ReminderScheduler.reschedule(context); }
    @Override public void onReceive(Context context, Intent intent) { super.onReceive(context, intent); String action=intent==null?null:intent.getAction(); if(ACTION_REFRESH.equals(action)||ACTION_BOUNDARY.equals(action)||ACTION_TOGGLE_MODE.equals(action)||Intent.ACTION_BOOT_COMPLETED.equals(action)||Intent.ACTION_TIME_CHANGED.equals(action)||Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_DATE_CHANGED.equals(action)){updateAll(context);scheduleNextBoundary(context);ReminderScheduler.reschedule(context);} }
    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int appWidgetId, android.os.Bundle newOptions) { manager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.upcomingList);updateWidget(context,manager,appWidgetId); }
    @Override public void onDeleted(Context context,int[] appWidgetIds){for(int id:appWidgetIds)WidgetModeStore.clear(context,id);}
    @Override public void onEnabled(Context context){ScheduleStore.ensureInitialized(context);updateAll(context);scheduleNextBoundary(context);ReminderScheduler.reschedule(context);}
    @Override public void onDisabled(Context context){cancelBoundary(context);}
    static void refreshAll(Context context){updateAll(context);}

    private static void updateAll(Context context){AppWidgetManager manager=AppWidgetManager.getInstance(context);Class<?>[] providers={ScheduleWidgetProvider.class,ScheduleWidgetCondensedProvider.class,ScheduleWidgetMiniProvider.class};for(Class<?> provider:providers){int[] ids=manager.getAppWidgetIds(new ComponentName(context,provider));if(ids==null||ids.length==0)continue;manager.notifyAppWidgetViewDataChanged(ids,R.id.upcomingList);for(int id:ids)updateWidget(context,manager,id);}}

    static void updateWidget(Context context,AppWidgetManager manager,int widgetId){
        ScheduleStore.ensureInitialized(context);
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_schedule);
        views.setViewVisibility(R.id.widgetHeader,View.GONE);views.setViewVisibility(R.id.currentCard,View.GONE);views.setViewVisibility(R.id.btnWidgetMode,View.GONE);
        int format=WidgetLayoutStore.get(context,widgetId);
        // Every format owns an opaque surface. The previous transparent classic surface made the launcher wallpaper bleed through.
        int surface=0xFFF7F9FC;
        views.setInt(R.id.widgetRoot,"setBackgroundColor",surface);views.setInt(R.id.widgetBody,"setBackgroundColor",surface);views.setInt(R.id.emptyUpcoming,"setBackgroundColor",surface);
        views.setTextColor(R.id.emptyUpcoming,0xFF64748B);
        views.setTextViewText(R.id.emptyUpcoming,UiSettingsStore.t(context,"noCourse"));
        int dayProgressValue=dayProgress(context);
        configureEdgeBar(views,R.id.widgetTopBar,R.id.dayProgressTop,R.id.dayColorTop,
                AdvancedSettingsStore.widgetTopBarMode(context),AdvancedSettingsStore.widgetTopBarColor(context),dayProgressValue);
        configureEdgeBar(views,R.id.widgetBottomBar,R.id.dayProgress,R.id.dayColorBottom,
                AdvancedSettingsStore.widgetBottomBarMode(context),AdvancedSettingsStore.widgetBottomBarColor(context),dayProgressValue);
        boolean adaptiveRows = AdvancedSettingsStore.widgetAutoDensity(context)
                && format != WidgetLayoutStore.FORMAT_MINI;
        views.removeAllViews(R.id.adaptiveDayRows);
        views.setViewVisibility(R.id.upcomingList, adaptiveRows ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.adaptiveDayRows, adaptiveRows ? View.VISIBLE : View.GONE);

        if (adaptiveRows) {
            List<RemoteViews> rows = format == WidgetLayoutStore.FORMAT_CONDENSED
                    ? CondensedCoursesService.buildAdaptiveRows(context, widgetId)
                    : UpcomingCoursesService.buildAdaptiveRows(context, widgetId);
            boolean empty = rows.isEmpty();
            views.setViewVisibility(R.id.adaptiveDayRows, empty ? View.GONE : View.VISIBLE);
            views.setViewVisibility(R.id.emptyUpcoming, empty ? View.VISIBLE : View.GONE);
            for (RemoteViews row : rows) views.addView(R.id.adaptiveDayRows, row);
        } else {
            Intent listIntent=new Intent(context,UpcomingCoursesService.class);listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);listIntent.setData(Uri.parse("edt://widget/"+widgetId+"/courses"));views.setRemoteAdapter(R.id.upcomingList,listIntent);views.setEmptyView(R.id.upcomingList,R.id.emptyUpcoming);
            views.setViewVisibility(R.id.emptyUpcoming, View.VISIBLE);
        }

        Intent openIntent=new Intent(context,MainActivity.class);openIntent.putExtra("open_mode","week");PendingIntent openPending=PendingIntent.getActivity(context,100+widgetId,openIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);views.setOnClickPendingIntent(R.id.widgetRoot,openPending);views.setOnClickPendingIntent(R.id.emptyUpcoming,openPending);views.setOnClickPendingIntent(R.id.adaptiveDayRows,openPending);
        if (!adaptiveRows) {
            Intent rowIntent=new Intent(context,MainActivity.class);rowIntent.putExtra("open_mode","week");PendingIntent rowPending=PendingIntent.getActivity(context,4000+widgetId,rowIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);views.setPendingIntentTemplate(R.id.upcomingList,rowPending);
        }
        manager.updateAppWidget(widgetId,views);
        if (!adaptiveRows) manager.notifyAppWidgetViewDataChanged(widgetId,R.id.upcomingList);
    }

    private static void configureEdgeBar(RemoteViews views,int containerId,int progressId,int colorId,
                                         String mode,int color,int progress){
        boolean hidden="none".equals(mode),solid="color".equals(mode);
        views.setViewVisibility(containerId,hidden?View.GONE:View.VISIBLE);
        views.setViewVisibility(progressId,!hidden&&!solid?View.VISIBLE:View.GONE);
        views.setViewVisibility(colorId,!hidden&&solid?View.VISIBLE:View.GONE);
        if(!hidden&&!solid)views.setProgressBar(progressId,1000,progress,false);
        if(!hidden&&solid)views.setInt(colorId,"setBackgroundColor",color);
    }
    private static int dayProgress(Context context){Calendar now=Calendar.getInstance();int n=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE),s=ScheduleData.toMinutes(ScheduleStore.getSlotStart(context,1)),e=ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context,9));if(e<=s||n<=s)return 0;if(n>=e)return 1000;return Math.max(0,Math.min(1000,Math.round((n-s)*1000f/(e-s))));}
    private static void scheduleNextBoundary(Context context){AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(alarm==null)return;long nowMs=System.currentTimeMillis(),next=nowMs+15L*60L*1000L;Calendar now=Calendar.getInstance(),mid=(Calendar)now.clone();mid.add(Calendar.DAY_OF_YEAR,1);mid.set(Calendar.HOUR_OF_DAY,0);mid.set(Calendar.MINUTE,0);mid.set(Calendar.SECOND,2);mid.set(Calendar.MILLISECOND,0);next=Math.min(next,mid.getTimeInMillis());Calendar cursor=(Calendar)now.clone();for(int day=0;day<2;day++){List<ScheduleData.Course> courses=ScheduleStore.getCourses(context,cursor);if(courses!=null)for(ScheduleData.Course c:courses){long s=boundaryMillis(cursor,c.start),e=boundaryMillis(cursor,c.end);if(s>nowMs+1000L)next=Math.min(next,s+1000L);if(e>nowMs+1000L)next=Math.min(next,e+1000L);}cursor.add(Calendar.DAY_OF_YEAR,1);}PendingIntent p=boundaryPendingIntent(context);try{if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S&&!alarm.canScheduleExactAlarms())alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,p);else alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,p);}catch(Exception ignored){try{alarm.set(AlarmManager.RTC_WAKEUP,next,p);}catch(Exception ignoredAgain){}}}
    private static long boundaryMillis(Calendar date,String hhmm){int m=ScheduleData.toMinutes(hhmm);Calendar c=(Calendar)date.clone();c.set(Calendar.HOUR_OF_DAY,m/60);c.set(Calendar.MINUTE,m%60);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}
    private static PendingIntent boundaryPendingIntent(Context context){Intent i=new Intent(context,ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);return PendingIntent.getBroadcast(context,9107,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private static void cancelBoundary(Context context){AlarmManager a=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(a!=null)a.cancel(boundaryPendingIntent(context));}
}
