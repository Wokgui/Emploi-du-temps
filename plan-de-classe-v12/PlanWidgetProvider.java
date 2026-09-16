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
    private static final int LINE = Color.rgb(199,211,208);
    private static final int BOY_DEF = Color.rgb(234,244,255);
    private static final int GIRL_DEF = Color.rgb(255,240,246);
    private static final int BOY_BORDER = Color.rgb(118,174,232);
    private static final int GIRL_BORDER = Color.rgb(223,141,177);

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) { updateAll(context, manager, ids); }
    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int id, Bundle opts) { updateAll(context, manager, new int[]{id}); }

    public static void updateAll(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateOne(context, manager, id);
    }

    private static void updateOne(Context context, AppWidgetManager manager, int id) {
        Bundle o = manager.getAppWidgetOptions(id);
        int wdp = Math.max(180, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250));
        int hdp = Math.max(90, o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 120));
        float density = context.getResources().getDisplayMetrics().density;
        int w = clamp(Math.round(wdp * density), 360, 1500);
        int h = clamp(Math.round(hdp * density), 180, 1000);
        String state = context.getSharedPreferences("plan_widget", Context.MODE_PRIVATE).getString("state", "");
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_plan);
        rv.setImageViewBitmap(R.id.widget_image, draw(state, w, h));
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_root, pi);
        manager.updateAppWidget(id, rv);
    }

    private static Bitmap draw(String json, int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float rad = Math.max(14f, Math.min(w,h)*0.035f);
        p.setColor(Color.WHITE);
        c.drawRoundRect(new RectF(0,0,w,h), rad,rad,p);

        JSONObject root = null, cls = null;
        try {
            root = new JSONObject(json);
            String cur = root.optString("cur", "");
            JSONArray classes = root.optJSONArray("classes");
            if (classes != null) for (int i=0;i<classes.length();i++) {
                JSONObject q=classes.optJSONObject(i);
                if(q!=null && (cur.isEmpty() || cur.equals(q.optString("id")))) { cls=q; break; }
            }
            if(cls==null && classes!=null && classes.length()>0) cls=classes.optJSONObject(0);
        } catch(Exception ignored) {}

        float header = clampF(h*0.16f, 44f, 96f);
        p.setColor(GREEN2);
        c.drawRoundRect(new RectF(0,0,w,header+rad),rad,rad,p);
        c.drawRect(0,header-rad,w,header+rad,p);
        p.setColor(Color.WHITE); p.setTextAlign(Paint.Align.CENTER); p.setFakeBoldText(true);
        p.setTextSize(clampF(header*0.40f,18f,42f));
        String name = cls!=null ? cls.optString("name","Plan de classe") : "Plan de classe";
        c.drawText(ellipsize(p,name,w-32f),w/2f,header*0.66f,p); p.setFakeBoldText(false);

        if(cls==null){
            p.setColor(INK);p.setTextSize(clampF(h*0.07f,14f,28f));
            c.drawText("Ouvrez l’application",w/2f,h*0.60f,p);
            return bm;
        }

        int boy=BOY_DEF,girl=GIRL_DEF;
        if(root!=null){
            JSONObject pal=root.optJSONObject("palette");
            if(pal!=null){boy=parseColor(pal.optString("boy",null),BOY_DEF);girl=parseColor(pal.optString("girl",null),GIRL_DEF);}
        }

        JSONArray rows=cls.optJSONArray("rows"), seats=cls.optJSONArray("seats"), students=cls.optJSONArray("students"), modes=cls.optJSONArray("rowModes");
        Map<String,JSONObject> smap=new HashMap<>();
        if(students!=null)for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i);if(s!=null)smap.put(s.optString("id"),s);}
        int rowCount=rows==null?0:rows.length(); if(rowCount==0)return bm;

        int[] offsets=new int[rowCount];int acc=0;
        for(int r=0;r<rowCount;r++){offsets[r]=acc;JSONArray g=rows.optJSONArray(r);if(g!=null)for(int i=0;i<g.length();i++)acc+=Math.max(1,g.optInt(i,1));}

        float pad=clampF(Math.min(w,h)*0.018f,5f,18f);
        float objectH=clampF(h*0.10f,28f,62f);
        float top=header+pad, bottom=h-pad-objectH;
        float rowGap=clampF(h*0.012f,2f,9f);
        float rowH=Math.max(12f,(bottom-top-rowGap*Math.max(0,rowCount-1))/rowCount);

        for(int visual=0;visual<rowCount;visual++){
            int r=rowCount-1-visual;JSONArray groups=rows.optJSONArray(r);if(groups==null)continue;
            int total=0;for(int gi=0;gi<groups.length();gi++)total+=Math.max(1,groups.optInt(gi,1));
            float y=top+visual*(rowH+rowGap),groupGap=clampF(w*0.012f,4f,16f),usable=w-2*pad-groupGap*Math.max(0,groups.length()-1),x=pad;
            int seatIndex=offsets[r];
            for(int gi=0;gi<groups.length();gi++){
                int tables=Math.max(1,groups.optInt(gi,1));float gw=usable*tables/Math.max(1,total);
                boolean isolated=false;if(modes!=null){JSONArray mr=modes.optJSONArray(r);if(mr!=null)isolated="isolated".equals(mr.optString(gi,"joined"));}
                float tableGap=isolated?clampF(w*0.014f,6f,18f):clampF(w*0.004f,2f,6f);
                float tw=(gw-tableGap*Math.max(0,tables-1))/tables;
                for(int t=0;t<tables;t++){
                    float tx=x+t*(tw+tableGap);RectF sr=new RectF(tx,y,tx+tw,y+rowH);
                    String sid=seats!=null?seats.optString(seatIndex++,""):"";JSONObject st=smap.get(sid);
                    int fill=Color.WHITE,border=LINE;
                    if(st!=null){String g=st.optString("gender","");if("boy".equals(g)){fill=boy;border=BOY_BORDER;}else if("girl".equals(g)){fill=girl;border=GIRL_BORDER;}}
                    p.setColor(fill);c.drawRoundRect(sr,10f,10f,p);
                    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(clampF(Math.min(w,h)*0.0026f,1f,3f));p.setColor(border);c.drawRoundRect(sr,10f,10f,p);p.setStyle(Paint.Style.FILL);
                    String label=st!=null?st.optString("name",""):"Libre";p.setColor(st!=null?INK:MUTED);p.setFakeBoldText(st!=null);p.setTextAlign(Paint.Align.CENTER);
                    float fs=clampF(Math.min(rowH*0.35f,tw*0.18f),8f,24f);p.setTextSize(fs);while(p.measureText(label)>tw-8f&&fs>7f){fs-=0.7f;p.setTextSize(fs);}c.drawText(ellipsize(p,label,tw-8f),tx+tw/2f,y+rowH*0.61f,p);p.setFakeBoldText(false);
                }
                x+=gw+groupGap;
            }
        }

        float oy=h-pad-objectH*0.72f;p.setTextAlign(Paint.Align.CENTER);p.setFakeBoldText(true);p.setTextSize(clampF(objectH*0.30f,10f,21f));
        RectF board=new RectF(w*0.25f,oy,w*0.69f,oy+objectH*0.58f);p.setColor(GREEN);c.drawRoundRect(board,8f,8f,p);p.setColor(Color.WHITE);c.drawText("Tableau",(board.left+board.right)/2f,oy+objectH*0.39f,p);
        RectF desk=new RectF(w*0.73f,oy,w*0.95f,oy+objectH*0.58f);p.setColor(Color.rgb(245,242,234));c.drawRoundRect(desk,8f,8f,p);p.setStyle(Paint.Style.STROKE);p.setColor(Color.rgb(196,185,160));p.setStrokeWidth(1.5f);c.drawRoundRect(desk,8f,8f,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(73,67,55));c.drawText("Bureau",(desk.left+desk.right)/2f,oy+objectH*0.39f,p);p.setFakeBoldText(false);
        return bm;
    }

    private static String ellipsize(Paint p,String s,float max){if(s==null)return "";if(p.measureText(s)<=max)return s;String e="…";int n=s.length();while(n>1&&p.measureText(s.substring(0,n)+e)>max)n--;return s.substring(0,Math.max(1,n))+e;}
    private static int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}
    private static float clampF(float v,float a,float b){return Math.max(a,Math.min(b,v));}
    private static int parseColor(String s,int def){try{return s!=null&&!s.isEmpty()?Color.parseColor(s):def;}catch(Exception e){return def;}}
}
