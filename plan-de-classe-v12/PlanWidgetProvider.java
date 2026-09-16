package fr.wokgui.planclasse;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
    private static final int GREEN = Color.rgb(8,113,99);
    private static final int GREEN2 = Color.rgb(11,137,119);
    private static final int INK = Color.rgb(26,35,43);
    private static final int MUTED = Color.rgb(150,159,164);
    private static final int LINE = Color.rgb(205,194,164);
    private static final int TABLE = Color.rgb(248,245,236);
    private static final int BOY = Color.rgb(234,244,255);
    private static final int GIRL = Color.rgb(255,240,246);

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) { updateAll(context, manager, ids); }
    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int id, Bundle opts) { updateAll(context, manager, new int[]{id}); }

    public static void updateAll(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateOne(context, manager, id);
    }

    private static void updateOne(Context context, AppWidgetManager manager, int id) {
        Bundle o = manager.getAppWidgetOptions(id);
        int wdp = Math.max(180, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300));
        int hdp = Math.max(90, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 150));
        float density = context.getResources().getDisplayMetrics().density;
        int w = clamp(Math.round(wdp * density), 520, 1200);
        int h = clamp(Math.round(hdp * density), 260, 760);
        String state = context.getSharedPreferences("plan_widget", Context.MODE_PRIVATE).getString("state", "");
        Bitmap bitmap = draw(context, state, w, h);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_plan);
        rv.setImageViewBitmap(R.id.widget_image, bitmap);
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_image, pi);
        manager.updateAppWidget(id, rv);
    }

    private static Bitmap draw(Context context, String json, int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float d = context.getResources().getDisplayMetrics().density;
        float rad = Math.max(18f, 7f*d);
        p.setColor(Color.WHITE); c.drawRoundRect(new RectF(0,0,w,h), rad, rad, p);

        JSONObject cls = null;
        try {
            JSONObject root = new JSONObject(json);
            String cur = root.optString("cur", "");
            JSONArray classes = root.optJSONArray("classes");
            if (classes != null) for (int i=0;i<classes.length();i++) {
                JSONObject q=classes.optJSONObject(i); if(q!=null && (cur.isEmpty() || cur.equals(q.optString("id")))) { cls=q; break; }
            }
            if (cls==null && classes!=null && classes.length()>0) cls=classes.optJSONObject(0);
        } catch(Exception ignored) {}

        String name = cls != null ? cls.optString("name", "Plan de classe") : "Plan de classe";
        float header = Math.max(54f, h*0.17f);
        p.setColor(GREEN2); c.drawRoundRect(new RectF(0,0,w,header+rad), rad,rad,p); c.drawRect(0,header-rad,w,header+rad,p);
        p.setColor(Color.WHITE); p.setTextAlign(Paint.Align.CENTER); p.setFakeBoldText(true); p.setTextSize(clampF(header*0.42f, 24f, 40f));
        c.drawText(ellipsize(p,name,w-50),w/2f,header*0.66f,p); p.setFakeBoldText(false);

        if (cls == null) { p.setColor(INK); p.setTextSize(24f); c.drawText("Ouvrez l’application une fois",w/2f,h*0.62f,p); return bm; }

        JSONArray rows=cls.optJSONArray("rows"), seats=cls.optJSONArray("seats"), students=cls.optJSONArray("students"), modes=cls.optJSONArray("rowModes");
        Map<String,JSONObject> smap=new HashMap<>();
        if(students!=null) for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i);if(s!=null)smap.put(s.optString("id"),s);}
        int rowCount = rows==null?0:rows.length();
        if(rowCount==0) return bm;

        float pad=Math.max(8f,3f*d), top=header+pad;
        boolean roomy=h>=390;
        float objectH = roomy ? Math.min(52f,h*0.105f) : 0f;
        if(roomy){
            p.setTextAlign(Paint.Align.CENTER);p.setFakeBoldText(true);p.setTextSize(clampF(objectH*.38f,14f,23f));
            p.setColor(GREEN);RectF board=new RectF(w*.28f,top,w*.72f,top+objectH*.72f);c.drawRoundRect(board,8,8,p);p.setColor(Color.WHITE);c.drawText("Tableau",w*.5f,top+objectH*.50f,p);
            p.setColor(Color.rgb(237,228,207));RectF desk=new RectF(w*.75f,top,w*.96f,top+objectH*.72f);c.drawRoundRect(desk,8,8,p);p.setColor(Color.rgb(70,58,42));c.drawText("Bureau",w*.855f,top+objectH*.50f,p);p.setFakeBoldText(false);
            top += objectH;
        }
        float bottom= h-pad, rowGap=Math.max(4f,1.5f*d), rowH=(bottom-top-rowGap*(rowCount-1))/rowCount;
        int seatIndex=0;
        for(int r=0;r<rowCount;r++){
            JSONArray groups=rows.optJSONArray(r); if(groups==null) continue;
            int gcount=groups.length(); int totalTables=0; for(int gi=0;gi<gcount;gi++) totalTables+=Math.max(1,groups.optInt(gi,1));
            float y=top+r*(rowH+rowGap); float availW=w-2*pad; float groupGap=Math.max(7f,2.2f*d); float usable=availW-groupGap*Math.max(0,gcount-1); float x=pad;
            for(int gi=0;gi<gcount;gi++){
                int tables=Math.max(1,groups.optInt(gi,1)); float gw=usable*tables/Math.max(1,totalTables);
                boolean isolated=false; if(modes!=null){JSONArray mr=modes.optJSONArray(r);if(mr!=null)isolated="isolated".equals(mr.optString(gi,"joined"));}
                float tableGap=isolated?Math.max(7f,2f*d):1f; float tw=(gw-tableGap*Math.max(0,tables-1))/tables;
                for(int t=0;t<tables;t++){
                    float tx=x+t*(tw+tableGap); RectF table=new RectF(tx,y,tx+tw,y+rowH);
                    p.setColor(TABLE); c.drawRoundRect(table,6,6,p); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1.4f,d*.55f));p.setColor(LINE);c.drawRoundRect(table,6,6,p);p.setStyle(Paint.Style.FILL);
                    float half=tw/2f;
                    for(int si=0;si<2;si++){
                        int idx=seatIndex++; String sid=seats!=null?seats.optString(idx,""):""; JSONObject st=smap.get(sid); RectF sr=new RectF(tx+si*half+1,y+1,tx+(si+1)*half-1,y+rowH-1);
                        int bg=Color.WHITE; if(st!=null){String g=st.optString("gender","");if("boy".equals(g))bg=BOY;else if("girl".equals(g))bg=GIRL;}
                        p.setColor(bg);c.drawRoundRect(sr,4,4,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,d*.42f));p.setColor(Color.rgb(190,197,199));c.drawRoundRect(sr,4,4,p);p.setStyle(Paint.Style.FILL);
                        String label=st!=null?st.optString("name",""):"Libre"; p.setColor(st!=null?INK:MUTED);p.setTextAlign(Paint.Align.CENTER);p.setFakeBoldText(st!=null);
                        float fs=clampF(Math.min(rowH*.34f,half*.20f),11f,25f);p.setTextSize(fs);while(p.measureText(label)>half-8 && fs>9){fs-=1;p.setTextSize(fs);} c.drawText(ellipsize(p,label,half-8),tx+si*half+half/2f,y+rowH*.60f,p);p.setFakeBoldText(false);
                    }
                }
                x += gw+groupGap;
            }
        }
        return bm;
    }

    private static String ellipsize(Paint p,String s,float max){if(s==null)return "";if(p.measureText(s)<=max)return s;String e="…";int n=s.length();while(n>1&&p.measureText(s.substring(0,n)+e)>max)n--;return s.substring(0,Math.max(1,n))+e;}
    private static int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}
    private static float clampF(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}
