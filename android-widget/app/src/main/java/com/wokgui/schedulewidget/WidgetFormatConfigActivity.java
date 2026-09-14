package com.wokgui.schedulewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ColorStateList;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/** Configuration shown by the launcher when a new timetable widget is placed. */
public final class WidgetFormatConfigActivity extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private RadioGroup choices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        widgetId = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        );
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        getWindow().setStatusBarColor(Color.rgb(8, 124, 197));
        getWindow().setNavigationBarColor(Color.WHITE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(246, 248, 251));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_launcher_final);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(68), dp(68));
        iconLp.bottomMargin = dp(12);
        root.addView(icon, iconLp);

        TextView title = text("Choix du format du widget", 25, true, 0xFF111827);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap(dp(2)));

        TextView subtitle = text("Sélectionner le modèle à installer", 16, false, 0xFF60708A);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrap(dp(6)));

        TextView hint = text(
                "Trois formats sont disponibles. Le format actuel reste strictement inchangé.",
                13,
                false,
                0xFF718096
        );
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = matchWrap(dp(18));
        hintLp.leftMargin = dp(8);
        hintLp.rightMargin = dp(8);
        root.addView(hint, hintLp);

        choices = new RadioGroup(this);
        choices.setOrientation(RadioGroup.VERTICAL);
        root.addView(choices, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        RadioButton classic = addOption(
                WidgetLayoutStore.FORMAT_CLASSIC,
                "Version actuelle",
                "Widget actuel, inchangé : mêmes lignes, mêmes couleurs, mêmes informations et même comportement."
        );
        addOption(
                WidgetLayoutStore.FORMAT_CONDENSED,
                "Version 3 — Journée condensée",
                "Liste chronologique plus compacte avec l’heure à gauche et un repère coloré pour suivre clairement l’enchaînement des cours."
        );
        addOption(
                WidgetLayoutStore.FORMAT_MINI,
                "Version 4 — Mini planning",
                "Frise horizontale de la journée : cours, Midi et trous visibles d’un seul coup d’œil."
        );

        int selected = WidgetLayoutStore.get(this, widgetId);
        for (int i = 0; i < choices.getChildCount(); i++) {
            View child = choices.getChildAt(i);
            if (child instanceof RadioButton && ((Integer) child.getTag()) == selected) {
                choices.check(child.getId());
                break;
            }
        }
        if (choices.getCheckedRadioButtonId() == -1) choices.check(classic.getId());

        Button install = new Button(this);
        install.setText("Installer ce format");
        install.setTextColor(Color.WHITE);
        install.setTextSize(16);
        install.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        install.setAllCaps(false);
        install.setBackground(rounded(0xFF1677E8, 18, 0, 0));
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
        );
        buttonLp.topMargin = dp(18);
        root.addView(install, buttonLp);

        TextView note = text(
                "Le format pourra être modifié ensuite depuis les options du widget proposées par le lanceur.",
                12,
                false,
                0xFF7A879B
        );
        note.setGravity(Gravity.CENTER);
        root.addView(note, matchWrap(dp(2)));

        install.setOnClickListener(v -> confirmSelection());
        setContentView(scroll);
    }

    private RadioButton addOption(int format, String label, String description) {
        RadioButton option = new RadioButton(this);
        option.setId(View.generateViewId());
        option.setTag(format);
        option.setText(label + "\n" + description);
        option.setTextSize(15);
        option.setTextColor(0xFF17213A);
        option.setGravity(Gravity.CENTER_VERTICAL);
        option.setPadding(dp(14), dp(13), dp(14), dp(13));
        option.setButtonTintList(new ColorStateList(
                new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] {}
                },
                new int[] { 0xFF1677E8, 0xFF9AA8BA }
        ));
        option.setBackground(rounded(0xFFFFFFFF, 16, 1, 0xFFE0E6EF));
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = dp(10);
        choices.addView(option, lp);
        return option;
    }

    private void confirmSelection() {
        int checked = choices.getCheckedRadioButtonId();
        View selectedView = choices.findViewById(checked);
        int format = WidgetLayoutStore.FORMAT_CLASSIC;
        if (selectedView instanceof RadioButton && selectedView.getTag() instanceof Integer) {
            format = (Integer) selectedView.getTag();
        }

        WidgetLayoutStore.set(this, widgetId, format);
        ScheduleWidgetProvider.refreshWidget(this, widgetId);

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private TextView text(String value, float sp, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap(int bottomMargin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = bottomMargin;
        return lp;
    }

    private GradientDrawable rounded(int fill, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
