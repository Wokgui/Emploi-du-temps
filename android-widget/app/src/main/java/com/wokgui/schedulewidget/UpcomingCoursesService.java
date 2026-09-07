package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpcomingCoursesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent == null ? AppWidgetManager.INVALID_APPWIDGET_ID
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new Factory(getApplicationContext(), widgetId);
    }

    private static final class Item {
        static final int COURSE = 0;
        static final int LUNCH = 1;
        static final int GAP = 2;
        final String label, time, room, relative;
        final int type, order;
        final boolean uncertain;
        Item(String label, String time, String room, int type, int order, String relative, boolean uncertain) {
            this.label=label;this.time=time;this.room=room;this.type=type;this.order=order;this.relative=relative;this.uncertain=uncertain;
        }
    }

    private static final class GapInfo {
        final int start, end;
        GapInfo(int start, int end) { this.start=start;this.end=end; }
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private final int widgetId;
        private final List<Item> items = new ArrayList<>();
        private boolean compactHeight;

        Factory(Context context, int widgetId) {
            this.context=context;
            this.widgetId=widgetId;
        }

        @Override public void onCreate(){reload();}
        @Override public void onDataSetChanged(){reload();}
        @Override public void onDestroy(){items.clear();}
        @Override public int getCount(){return items.size();}

        private boolean isCompactHeight(){
            if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID)return false;
            Bundle options=AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            int h=options==null?180:options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,180);
            return h>0&&h<=210;
        }

        private void reload() {
            items.clear();
            ScheduleStore.ensureInitialized(context);
            compactHeight=isCompactHeight();
            Calendar now=Calendar.getInstance();
            int nowMin=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);
            int lunchStart=ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context,4));
            int lunchEnd=ScheduleData.toMinutes(ScheduleStore.getSlotStart(context,5));
            boolean dayMode=widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID && WidgetModeStore.isDayMode(context,widgetId) && !compactHeight;

            if(dayMode){
                Calendar target=resolveDayTarget(now,nowMin);
                loadWholeDay(target,now,lunchStart,lunchEnd);
                return;
            }

            List<ScheduleData.Course> todayCourses=ScheduleStore.getCourses(context,now);
            ScheduleData.Course current=null;
            for(ScheduleData.Course c:todayCourses){
                int s=ScheduleData.toMinutes(c.start),e=ScheduleData.toMinutes(c.end);
                if(nowMin>=s&&nowMin<e){current=c;break;}
            }

            boolean lunchValid=lunchEnd>lunchStart;
            boolean inLunch=current==null&&lunchValid&&hasLunchGap(todayCourses,lunchStart,lunchEnd)&&nowMin>=lunchStart&&nowMin<lunchEnd;
            GapInfo currentGap=current==null&&!inLunch?findCurrentGap(todayCourses,nowMin,lunchStart,lunchEnd):null;
            Calendar targetDate;
            int threshold,previousEnd;

            if(current!=null){
                targetDate=(Calendar)now.clone();threshold=ScheduleData.toMinutes(current.start)+1;previousEnd=ScheduleData.toMinutes(current.end);
            }else if(inLunch){
                targetDate=(Calendar)now.clone();threshold=lunchEnd;previousEnd=lunchEnd;
            }else if(currentGap!=null){
                targetDate=(Calendar)now.clone();threshold=currentGap.end;previousEnd=currentGap.end;
            }else{
                ScheduleData.Course topNext=null;targetDate=null;Calendar cursor=(Calendar)now.clone();
                for(int add=0;add<21&&topNext==null;add++){
                    List<ScheduleData.Course> courses=ScheduleStore.getCourses(context,cursor);
                    for(ScheduleData.Course c:courses){
                        if(add==0&&ScheduleData.toMinutes(c.start)<=nowMin)continue;
                        topNext=c;targetDate=(Calendar)cursor.clone();break;
                    }
                    cursor.add(Calendar.DAY_OF_YEAR,1);
                }
                if(topNext==null||targetDate==null)return;
                threshold=ScheduleData.toMinutes(topNext.start)+1;previousEnd=ScheduleData.toMinutes(topNext.end);
            }

            List<ScheduleData.Course> sameDay=ScheduleStore.getCourses(context,targetDate);
            for(int i=0;i<sameDay.size();i++){
                ScheduleData.Course c=sameDay.get(i);
                int start=ScheduleData.toMinutes(c.start);
                if(start<threshold)continue;
                if(!compactHeight)appendBreaks(previousEnd,start,lunchStart,lunchEnd);
                int order=c.slot>0?c.slot:i+1;
                items.add(new Item(c.label,c.start+" - "+c.end,c.room,Item.COURSE,order,relativeLabel(now,targetDate,c),c.uncertain));
                previousEnd=ScheduleData.toMinutes(c.end);
                if(compactHeight)break;
            }
            ensureOneFollowingCourse(now,targetDate);
            trimForPreference();
        }

        private Calendar resolveDayTarget(Calendar now,int nowMin){
            List<ScheduleData.Course> today=ScheduleStore.getCourses(context,now);
            for(ScheduleData.Course c:today){
                if(ScheduleData.toMinutes(c.end)>nowMin)return (Calendar)now.clone();
            }
            Calendar cursor=(Calendar)now.clone();
            cursor.add(Calendar.DAY_OF_YEAR,1);
            for(int add=0;add<21;add++){
                List<ScheduleData.Course> list=ScheduleStore.getCourses(context,cursor);
                if(list!=null&&!list.isEmpty())return (Calendar)cursor.clone();
                cursor.add(Calendar.DAY_OF_YEAR,1);
            }
            return (Calendar)now.clone();
        }

        private void loadWholeDay(Calendar targetDate,Calendar now,int lunchStart,int lunchEnd){
            List<ScheduleData.Course> courses=ScheduleStore.getCourses(context,targetDate);
            if(courses==null||courses.isEmpty())return;
            boolean sameDay=targetDate.get(Calendar.YEAR)==now.get(Calendar.YEAR)
                    && targetDate.get(Calendar.DAY_OF_YEAR)==now.get(Calendar.DAY_OF_YEAR);
            int previousEnd=-1;
            for(int i=0;i<courses.size();i++){
                ScheduleData.Course c=courses.get(i);
                int start=ScheduleData.toMinutes(c.start);
                if(previousEnd>=0)appendBreaks(previousEnd,start,lunchStart,lunchEnd);
                int order=c.slot>0?c.slot:i+1;
                String relative=sameDay?relativeLabel(now,targetDate,c):"";
                items.add(new Item(c.label,c.start+" - "+c.end,c.room,Item.COURSE,order,relative,c.uncertain));
                previousEnd=ScheduleData.toMinutes(c.end);
            }
        }

        private void ensureOneFollowingCourse(Calendar now,Calendar afterDate){
            for(Item item:items)if(item.type==Item.COURSE)return;
            Calendar cursor=(Calendar)afterDate.clone();
            cursor.add(Calendar.DAY_OF_YEAR,1);
            for(int add=0;add<21;add++){
                List<ScheduleData.Course> list=ScheduleStore.getCourses(context,cursor);
                if(list!=null&&!list.isEmpty()){
                    ScheduleData.Course c=list.get(0);
                    int order=c.slot>0?c.slot:1;
                    items.add(new Item(c.label,c.start+" - "+c.end,c.room,Item.COURSE,order,relativeLabel(now,cursor,c),c.uncertain));
                    return;
                }
                cursor.add(Calendar.DAY_OF_YEAR,1);
            }
        }

        private boolean hasLunchGap(List<ScheduleData.Course> courses,int lunchStart,int lunchEnd){
            if(courses==null||courses.isEmpty()||lunchEnd<=lunchStart)return false;
            boolean before=false,after=false;
            for(ScheduleData.Course c:courses){
                int start=ScheduleData.toMinutes(c.start),end=ScheduleData.toMinutes(c.end);
                if(end<=lunchStart)before=true;
                if(start>=lunchEnd)after=true;
                if(start<lunchEnd&&end>lunchStart)return false;
            }
            return before&&after;
        }

        private void trimForPreference() {
            int requested=AdvancedSettingsStore.upcomingCount(context);
            int maxCourses=compactHeight?1:("compact".equals(AdvancedSettingsStore.widgetFormat(context))?1:requested);
            if(maxCourses<=0)return;
            maxCourses=Math.max(1,maxCourses);
            int courses=0,keep=items.size();
            for(int i=0;i<items.size();i++){
                if(items.get(i).type==Item.COURSE){
                    courses++;
                    if(courses>=maxCourses){keep=i+1;break;}
                }
            }
            while(items.size()>keep)items.remove(items.size()-1);
        }

        private GapInfo findCurrentGap(List<ScheduleData.Course> courses,int minute,int lunchStart,int lunchEnd){
            if(courses==null||courses.size()<2)return null;
            int previousEnd=-1,nextStart=Integer.MAX_VALUE;
            for(ScheduleData.Course c:courses){
                int start=ScheduleData.toMinutes(c.start),end=ScheduleData.toMinutes(c.end);
                if(end<=minute&&end>previousEnd)previousEnd=end;
                if(start>minute&&start<nextStart)nextStart=start;
            }
            if(previousEnd<0||nextStart==Integer.MAX_VALUE||nextStart<=previousEnd)return null;
            boolean lunchValid=lunchEnd>lunchStart;
            if(lunchValid&&minute>=lunchStart&&minute<lunchEnd)return null;
            int start=previousEnd,end=nextStart;
            if(lunchValid){
                if(minute<lunchStart&&end>lunchStart)end=lunchStart;
                else if(minute>=lunchEnd&&start<lunchEnd)start=lunchEnd;
            }
            if(end<=start||minute<start||minute>=end)return null;
            return new GapInfo(start,end);
        }

        private void appendBreaks(int from,int to,int lunchStart,int lunchEnd){
            if(to<=from)return;
            boolean lunchValid=lunchEnd>lunchStart;
            if(!lunchValid||to<=lunchStart||from>=lunchEnd){addGap(from,to);return;}
            if(from<lunchStart)addGap(from,Math.min(to,lunchStart));
            if(from<=lunchStart&&to>=lunchEnd&&AdvancedSettingsStore.showLunch(context))
                items.add(new Item(localizedBreakLabel(true),minuteLabel(lunchStart)+" - "+minuteLabel(lunchEnd),"",Item.LUNCH,0,"",false));
            if(to>lunchEnd)addGap(Math.max(from,lunchEnd),to);
        }

        private void addGap(int start,int end){
            if(!AdvancedSettingsStore.showBreaks(context))return;
            int duration=end-start;
            if(duration<=0)return;
            items.add(new Item(localizedBreakLabel(false),minuteLabel(start)+" - "+minuteLabel(end),"",Item.GAP,0,"",false));
        }

        private String localizedBreakLabel(boolean lunch){
            String custom=lunch?ScheduleStore.getLunchLabel(context):ScheduleStore.getGapLabel(context);
            if(lunch&&"Pause de midi".equalsIgnoreCase(custom))return "Midi";
            if(!lunch&&"Trou".equalsIgnoreCase(custom))return UiSettingsStore.t(context,"gap");
            return custom;
        }

        private String relativeLabel(Calendar now,Calendar targetDate,ScheduleData.Course course){
            Calendar start=(Calendar)targetDate.clone();
            int startMin=ScheduleData.toMinutes(course.start),endMin=ScheduleData.toMinutes(course.end);
            start.set(Calendar.HOUR_OF_DAY,startMin/60);start.set(Calendar.MINUTE,startMin%60);start.set(Calendar.SECOND,0);start.set(Calendar.MILLISECOND,0);
            Calendar end=(Calendar)targetDate.clone();
            end.set(Calendar.HOUR_OF_DAY,endMin/60);end.set(Calendar.MINUTE,endMin%60);end.set(Calendar.SECOND,0);end.set(Calendar.MILLISECOND,0);
            long nowMs=now.getTimeInMillis();
            if(nowMs>=start.getTimeInMillis()&&nowMs<end.getTimeInMillis()){
                long rem=Math.max(1L,(end.getTimeInMillis()-nowMs+59999L)/60000L);
                return rem+" min";
            }
            long diff=Math.max(0L,(start.getTimeInMillis()-nowMs)/60000L);
            if(diff<=0)return "";
            if(diff<60)return UiSettingsStore.t(context,"in")+" "+diff+" min";
            boolean tomorrow=start.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&start.get(Calendar.DAY_OF_YEAR)==now.get(Calendar.DAY_OF_YEAR)+1;
            if(tomorrow&&diff>=12*60)return UiSettingsStore.t(context,"tomorrow");
            long hours=Math.max(1L,Math.round(diff/60.0));
            return UiSettingsStore.t(context,"in")+" "+hours+" h";
        }

        private String minuteLabel(int minute){return String.format(Locale.FRANCE,"%02d:%02d",minute/60,minute%60);}

        private String courseMeta(Item item){
            boolean times=AdvancedSettingsStore.showTimes(context),room=AdvancedSettingsStore.showRoom(context);
            if(times&&room)return item.time+" · "+UiSettingsStore.t(context,"room")+" "+(item.room.isEmpty()?"—":item.room);
            if(times)return item.time;
            if(room)return UiSettingsStore.t(context,"room")+" "+(item.room.isEmpty()?"—":item.room);
            return "";
        }

        @Override public RemoteViews getViewAt(int position){
            if(position<0||position>=items.size())return null;
            Item item=items.get(position);
            String density=AdvancedSettingsStore.density(context);
            int layout=compactHeight?R.layout.widget_course_row_compact:("compact".equals(density)?R.layout.widget_course_row_compact:("comfortable".equals(density)?R.layout.widget_course_row_comfortable:R.layout.widget_course_row));
            RemoteViews v=new RemoteViews(context.getPackageName(),layout);
            float scale=UiSettingsStore.widgetFontScale(context);
            float densityScale=compactHeight?.93f:("compact".equals(density)?.93f:("comfortable".equals(density)?1.08f:1f));

            v.setTextViewTextSize(R.id.rowTitle,TypedValue.COMPLEX_UNIT_SP,10.5f*scale*densityScale);
            v.setTextViewTextSize(R.id.rowMeta,TypedValue.COMPLEX_UNIT_SP,8.5f*scale*densityScale);
            v.setTextViewTextSize(R.id.rowRelative,TypedValue.COMPLEX_UNIT_SP,8.5f*scale*densityScale);

            v.setViewVisibility(R.id.rowIndex,View.GONE);
            v.setViewVisibility(R.id.rowDot,View.GONE);
            v.setViewVisibility(R.id.rowLineTop,View.GONE);
            v.setViewVisibility(R.id.rowLineBottom,View.GONE);
            v.setTextViewText(R.id.rowTitle,(item.uncertain?"⚠ ":"")+item.label);
            v.setTextViewText(R.id.rowRelative,item.relative);
            v.setViewVisibility(R.id.rowRelative,item.relative.isEmpty()?View.GONE:View.VISIBLE);

            if(item.type==Item.LUNCH){
                int bg=WidgetPaletteStore.lunchBackground(context),ink=WidgetPaletteStore.lunchText(context);
                v.setInt(R.id.rowContent,"setBackgroundColor",bg);
                v.setInt(R.id.rowTitle,"setGravity",Gravity.START|Gravity.CENTER_VERTICAL);
                v.setTextViewText(R.id.rowTitle,item.label);
                String meta=AdvancedSettingsStore.showTimes(context)?item.time:"";
                v.setTextViewText(R.id.rowMeta,meta);
                v.setViewVisibility(R.id.rowMeta,meta.isEmpty()?View.GONE:View.VISIBLE);
                v.setTextColor(R.id.rowTitle,ink);
                v.setTextColor(R.id.rowMeta,ink);
                v.setViewVisibility(R.id.rowRelative,View.GONE);
            }else if(item.type==Item.GAP){
                int bg=WidgetPaletteStore.gapBackground(context),ink=WidgetPaletteStore.gapText(context);
                v.setInt(R.id.rowContent,"setBackgroundColor",bg);
                v.setInt(R.id.rowTitle,"setGravity",Gravity.START|Gravity.CENTER_VERTICAL);
                String meta=AdvancedSettingsStore.showTimes(context)?item.time:"";
                v.setTextViewText(R.id.rowMeta,meta);
                v.setViewVisibility(R.id.rowMeta,meta.isEmpty()?View.GONE:View.VISIBLE);
                v.setTextColor(R.id.rowTitle,ink);
                v.setTextColor(R.id.rowMeta,ink);
                v.setViewVisibility(R.id.rowRelative,View.GONE);
            }else{
                int bg=WidgetPaletteStore.courseColor(context,item.order,item.label);
                boolean dark=WidgetPaletteStore.useDarkText(context,item.order,item.label);
                int ink=dark?0xFF17213A:0xFFFFFFFF;
                int muted=dark?0xFF35435A:0xFFF7FBFF;
                v.setInt(R.id.rowContent,"setBackgroundColor",bg);
                String meta=courseMeta(item);
                v.setTextViewText(R.id.rowMeta,meta);
                v.setViewVisibility(R.id.rowMeta,meta.isEmpty()?View.GONE:View.VISIBLE);
                v.setTextColor(R.id.rowTitle,ink);
                v.setTextColor(R.id.rowMeta,muted);
                v.setTextColor(R.id.rowRelative,dark?0xFF72570B:0xFF9A2342);
            }

            Intent fill=new Intent();
            fill.putExtra("open_mode","week");
            v.setOnClickFillInIntent(R.id.rowRoot,fill);
            v.setOnClickFillInIntent(R.id.rowTitle,fill);
            v.setOnClickFillInIntent(R.id.rowMeta,fill);
            v.setOnClickFillInIntent(R.id.rowRelative,fill);
            return v;
        }

        @Override public RemoteViews getLoadingView(){return null;}
        @Override public int getViewTypeCount(){return 3;}
        @Override public long getItemId(int position){return position;}
        @Override public boolean hasStableIds(){return true;}
    }
}
