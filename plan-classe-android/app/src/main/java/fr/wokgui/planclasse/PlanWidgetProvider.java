package fr.wokgui.planclasse;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlanWidgetProvider extends AppWidgetProvider {
    private static final int GREEN = Color.rgb(15,154,135);
    private static final int GREEN_DARK = Color.rgb(8,117,102);
    private static final int INK = Color.rgb(23,33,43);
    private static final int MUTED = Color.rgb(155,164,168);
    private static final int BG = Color.rgb(248,251,250);
    private static final int BORDER = Color.rgb(218,229,226);
    private static final int DESK = Color.rgb(241,234,216);
    private static final int DESK_BORDER = Color.rgb(198,181,142);
    private static final int SEAT_BORDER = Color.rgb(190,201,205);

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int appWidgetId, Bundle newOptions) {
        updateWidget(context, manager, appWidgetId);
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, PlanWidgetProvider.class));
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int id) {
        Bundle o = manager.getAppWidgetOptions(id);
        int wDp = Math.max(250, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 320));
        int hDp = Math.max(190, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 260));
        Bitmap bitmap = render(context, Math.min(1200, wDp * 2), Math.min(1200, hDp * 2));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_plan);
        views.setImageViewBitmap(R.id.widgetImage, bitmap);
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 11000 + id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, pi);
        views.setOnClickPendingIntent(R.id.widgetImage, pi);
        manager.updateAppWidget(id, views);
    }

    private static Bitmap render(Context context, int width, int height) {
        width = Math.max(500, width);
        height = Math.max(380, height);
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float sx = width / 900f;
        float sy = height / 650f;
        float s = Math.min(sx, sy);

        p.setColor(BG);
        c.drawRoundRect(new RectF(3*s,3*s,width-3*s,height-3*s), 30*s,30*s,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3*s); p.setColor(BORDER);
        c.drawRoundRect(new RectF(4*s,4*s,width-4*s,height-4*s),30*s,30*s,p);
        p.setStyle(Paint.Style.FILL);

        JSONObject cls = currentClass(context);
        String className = cls == null ? "3G34 ALL" : cls.optString("name", "Classe");
        float top = 14*s;
        float pillW = Math.min(width*.46f, 340*s), pillH = 45*s;
        RectF pill = new RectF(width/2f-pillW/2, top, width/2f+pillW/2, top+pillH);
        p.setColor(Color.rgb(232,246,242)); c.drawRoundRect(pill,17*s,17*s,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f*s); p.setColor(Color.rgb(207,233,225)); c.drawRoundRect(pill,17*s,17*s,p); p.setStyle(Paint.Style.FILL);
        drawCentered(c,p,className,width/2f,top+29*s,24*s,GREEN_DARK,true);

        float y = top + pillH + 12*s;
        if (cls == null) cls = defaultClass();
        JSONObject landmarks = cls.optJSONObject("landmarks");
        boolean boardVisible = landmarks == null || landmarks.optJSONObject("tableau") == null || landmarks.optJSONObject("tableau").optBoolean("visible", true);
        boolean deskVisible = landmarks == null || landmarks.optJSONObject("bureau") == null || landmarks.optJSONObject("bureau").optBoolean("visible", true);
        if (boardVisible) {
            RectF board = new RectF(width*.34f,y,width*.66f,y+31*s); p.setColor(GREEN_DARK); c.drawRoundRect(board,7*s,7*s,p); drawCentered(c,p,"Tableau",width/2f,y+21*s,15*s,Color.WHITE,true); y += 38*s;
        }
        if (deskVisible) {
            RectF prof = new RectF(width*.43f,y,width*.57f,y+28*s); p.setColor(DESK); c.drawRoundRect(prof,7*s,7*s,p); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f*s);p.setColor(DESK_BORDER);c.drawRoundRect(prof,7*s,7*s,p);p.setStyle(Paint.Style.FILL);drawCentered(c,p,"Bureau",width/2f,y+19*s,14*s,INK,true); y += 39*s;
        }
        y += 5*s;

        JSONArray rows = cls.optJSONArray("rows");
        JSONArray seats = cls.optJSONArray("seats");
        JSONArray students = cls.optJSONArray("students");
        Map<String, JSONObject> studentMap = new HashMap<>();
        if (students != null) for (int i=0;i<students.length();i++) { JSONObject st=students.optJSONObject(i); if(st!=null) studentMap.put(st.optString("id"),st); }
        int rowCount = rows == null ? 7 : rows.length();
        float bottomPad = 18*s;
        float rowH = Math.max(42*s, (height-y-bottomPad)/Math.max(1,rowCount));
        int seatIndex=0;
        for (int r=0;r<rowCount;r++) {
            JSONArray groups = rows == null ? null : rows.optJSONArray(r);
            if (groups == null) groups = defaultRows().optJSONArray(Math.min(r,6));
            int totalTables=0; for(int g=0;g<groups.length();g++) totalTables+=Math.max(1,groups.optInt(g,1));
            float left=25*s,right=width-25*s, gap=18*s;
            float tableW=(right-left-gap*Math.max(0,groups.length()-1))/Math.max(1,totalTables);
            float x=left, deskH=Math.min(47*s,rowH*.72f);
            float deskY=y + r*rowH + (rowH-deskH)/2;
            for(int gi=0;gi<groups.length();gi++) {
                int count=Math.max(1,groups.optInt(gi,1));
                for(int t=0;t<count;t++) {
                    float tw=tableW-4*s;
                    RectF outer=new RectF(x+2*s,deskY,x+tw,deskY+deskH);p.setColor(DESK);c.drawRoundRect(outer,7*s,7*s,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f*s);p.setColor(DESK_BORDER);c.drawRoundRect(outer,7*s,7*s,p);p.setStyle(Paint.Style.FILL);
                    float sw=(tw-4*s)/2f;
                    for(int j=0;j<2;j++) {
                        float seatX=x+4*s+j*sw;
                        JSONObject st=null;
                        if(seats!=null && seatIndex<seats.length() && !seats.isNull(seatIndex)) st=studentMap.get(seats.optString(seatIndex));
                        int fill=Color.WHITE;
                        if(st!=null) {
                            String gender=st.optString("gender","");
                            if("boy".equals(gender)) fill=Color.rgb(234,244,255);
                            else if("girl".equals(gender)) fill=Color.rgb(255,240,246);
                        }
                        RectF seatR=new RectF(seatX,deskY+3*s,seatX+sw-2*s,deskY+deskH-3*s);p.setColor(fill);c.drawRoundRect(seatR,6*s,6*s,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.1f*s);p.setColor(SEAT_BORDER);c.drawRoundRect(seatR,6*s,6*s,p);p.setStyle(Paint.Style.FILL);
                        String name=st==null?"Libre":st.optString("name","Élève");
                        float font=name.length()>10?8.5f*s:name.length()>8?9.5f*s:11*s;
                        drawCentered(c,p,name,(seatR.left+seatR.right)/2,deskY+deskH*.56f,font,st==null?MUTED:INK,st!=null);
                        if(st!=null) drawTags(c,p,st.optJSONArray("tags"),(seatR.left+seatR.right)/2,deskY+deskH*.79f,s);
                        seatIndex++;
                    }
                    x += tableW;
                }
                if(gi<groups.length()-1) x += gap;
            }
        }
        return b;
    }

    private static void drawTags(Canvas c, Paint p, JSONArray tags, float cx, float cy, float s) {
        if(tags==null||tags.length()==0) return;
        int n=Math.min(5,tags.length()); float rad=2.2f*s, gap=5.8f*s, start=cx-(n-1)*gap/2;
        for(int i=0;i<n;i++) { String tag=tags.optString(i); int col=Color.rgb(96,125,108); if("perturbateur".equals(tag)) col=Color.rgb(209,122,0); else if("bavard".equals(tag)) col=Color.rgb(62,110,216); else if("problematique".equals(tag)) col=Color.rgb(204,51,51); else if("handicap".equals(tag)) col=Color.rgb(106,85,181); p.setColor(col); c.drawCircle(start+i*gap,cy,rad,p); }
    }

    private static void drawCentered(Canvas c, Paint p, String text, float cx, float baseline, float size, int color, boolean bold) {
        p.setColor(color); p.setTextSize(size); p.setTypeface(bold ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT); p.setTextAlign(Paint.Align.CENTER); c.drawText(text,cx,baseline,p);
    }

    private static JSONObject currentClass(Context context) {
        try {
            String raw=context.getSharedPreferences("plan_widget",Context.MODE_PRIVATE).getString("state",null);
            if(raw==null) return null;
            JSONObject root=new JSONObject(raw); String cur=root.optString("cur",""); JSONArray classes=root.optJSONArray("classes"); if(classes==null||classes.length()==0) return null;
            for(int i=0;i<classes.length();i++){JSONObject cl=classes.optJSONObject(i);if(cl!=null&&cur.equals(cl.optString("id"))) return cl;}
            return classes.optJSONObject(0);
        } catch(Exception e) { return null; }
    }

    private static JSONArray defaultRows() {
        try { return new JSONArray("[[4],[2],[2],[2],[4],[4],[4]]"); } catch(Exception e){ return new JSONArray(); }
    }

    private static JSONObject defaultClass() {
        try {
            String[] names={"Azra","Redouane","Arthur","Aurélian","Abigail","Evan","Hidaya","Ermin","Nina","Rosie","Lamine","Théo","Roufaida","Elynna","Zoé","Mila","Lina","Maëlys","Manuela","Naim","Janna","Victor","Timéo","Léon","Anaïs","Louay","Matias","Adam","Mathilde","Tristan","Victoire","Seynabou"};
            JSONObject cl=new JSONObject(); cl.put("name","3G34 ALL"); cl.put("rows",defaultRows()); JSONArray students=new JSONArray(),seats=new JSONArray();
            for(int i=0;i<names.length;i++){JSONObject st=new JSONObject();String id="s"+i;st.put("id",id);st.put("name",names[i]);st.put("gender","");st.put("tags",new JSONArray());students.put(st);seats.put(id);} while(seats.length()<48)seats.put(JSONObject.NULL); cl.put("students",students);cl.put("seats",seats); JSONObject lm=new JSONObject(); JSONObject tb=new JSONObject();tb.put("visible",true);JSONObject bu=new JSONObject();bu.put("visible",true);lm.put("tableau",tb);lm.put("bureau",bu);cl.put("landmarks",lm);return cl;
        } catch(Exception e){return new JSONObject();}
    }
}
