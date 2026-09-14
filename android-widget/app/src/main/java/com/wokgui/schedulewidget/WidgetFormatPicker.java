package com.wokgui.schedulewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/** Native chooser reused by the launcher configuration flow without altering the classic widget. */
final class WidgetFormatPicker {
    private WidgetFormatPicker() {}

    static void show(Activity activity, int widgetId) {
        activity.setResult(Activity.RESULT_CANCELED);
        activity.getWindow().setStatusBarColor(Color.rgb(8, 124, 197));
        activity.getWindow().setNavigationBarColor(Color.WHITE);

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(246, 248, 251));

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(activity, 20), dp(activity, 24), dp(activity, 20), dp(activity, 24));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        ImageView icon = new ImageView(activity);
        icon.setImageResource(R.drawable.ic_launcher_final);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(activity, 68), dp(activity, 68));
        iconLp.bottomMargin = dp(activity, 12);
        root.addView(icon, iconLp);

        TextView title = text(activity, "Choix du format du widget", 25, true, 0xFF111827);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap(activity, 2));

        TextView subtitle = text(activity, "Sélectionner le modèle à installer", 16, false, 0xFF60708A);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrap(activity, 6));

        TextView hint = text(activity,
                "Trois formats sont disponibles. Le format actuel reste strictement inchangé.",
                13, false, 0xFF718096);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp = matchWrap(activity, 18);
        hintLp.leftMargin = dp(activity, 8);
        hintLp.rightMargin = dp(activity, 8);
        root.addView(hint, hintLp);

        RadioGroup choices = new RadioGroup(activity);
        choices.setOrientation(RadioGroup.VERTICAL);
        root.addView(choices, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        RadioButton classic = addOption(activity, choices,
                WidgetLayoutStore.FORMAT_CLASSIC,
                "Version actuelle",
                "Widget actuel, inchangé : mêmes lignes, mêmes couleurs, mêmes informations et même comportement.");
        addOption(activity, choices,
                WidgetLayoutStore.FORMAT_CONDENSED,
                "Version 3 — Journée condensée",
                "Liste chronologique plus compacte avec l’heure à gauche et un repère coloré pour suivre clairement l’enchaînement des cours.");
        addOption(activity, choices,
                WidgetLayoutStore.FORMAT_MINI,
                "Version 4 — Mini planning",
                "Frise horizontale de la journée : cours, Midi et trous visibles d’un seul coup d’œil.");

        int selected = WidgetLayoutStore.get(activity, widgetId);
        for (int i = 0; i < choices.getChildCount(); i++) {
            View child = choices.getChildAt(i);
            if (child instanceof RadioButton && child.getTag() instanceof Integer
                    && ((Integer) child.getTag()) == selected) {
                choices.check(child.getId());
                break;
            }
        }
        if (choices.getCheckedRadioButtonId() == -1) choices.check(classic.getId());

        Button install = new Button(activity);
        install.setText("Installer ce format");
        install.setTextColor(Color.WHITE);
        install.setTextSize(16);
        install.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        install.setAllCaps(false);
        install.setBackground(rounded(activity, 0xFF1677E8, 18, 0, 0));
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(activity, 54)
        );
        buttonLp.topMargin = dp(activity, 18);
        root.addView(install, buttonLp);

        TextView note = text(activity,
                "Le choix s’applique à ce widget. Pour un autre format, ajoute simplement un autre widget.",
                12, false, 0xFF7A879B);
        note.setGravity(Gravity.CENTER);
        root.addView(note, matchWrap(activity, 2));

        install.setOnClickListener(v -> {
            View selectedView = choices.findViewById(choices.getCheckedRadioButtonId());
            int format = WidgetLayoutStore.FORMAT_CLASSIC;
            if (selectedView instanceof RadioButton && selectedView.getTag() instanceof Integer) {
                format = (Integer) selectedView.getTag();
            }
            WidgetLayoutStore.set(activity, widgetId, format);
            Intent refresh = new Intent(activity, ScheduleWidgetProvider.class);
            refresh.setAction(ScheduleWidgetProvider.ACTION_REFRESH);
            activity.sendBroadcast(refresh);

            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            activity.setResult(Activity.RESULT_OK, result);
            activity.finish();
        });

        activity.setContentView(scroll);
    }

    private static RadioButton addOption(Activity activity, RadioGroup choices, int format, String label, String description) {
        RadioButton option = new RadioButton(activity);
        option.setId(View.generateViewId());
        option.setTag(format);
        option.setText(label + "\n" + description);
        option.setTextSize(15);
        option.setTextColor(0xFF17213A);
        option.setGravity(Gravity.CENTER_VERTICAL);
        option.setPadding(dp(activity, 14), dp(activity, 13), dp(activity, 14), dp(activity, 13));
        option.setButtonTintList(new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
                new int[] { 0xFF1677E8, 0xFF9AA8BA }
        ));
        option.setBackground(rounded(activity, 0xFFFFFFFF, 16, 1, 0xFFE0E6EF));
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = dp(activity, 10);
        choices.addView(option, lp);
        return option;
    }

    private static TextView text(Activity activity, String value, float sp, boolean bold, int color) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private static LinearLayout.LayoutParams matchWrap(Activity activity, int bottomMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = dp(activity, bottomMarginDp);
        return lp;
    }

    private static GradientDrawable rounded(Activity activity, int fill, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(activity, radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(activity, strokeDp), strokeColor);
        return drawable;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
