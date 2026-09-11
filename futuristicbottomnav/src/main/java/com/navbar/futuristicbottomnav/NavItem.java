package com.navbar.futuristicbottomnav;

import android.graphics.drawable.Drawable;

/**
 * A single tab in the FuturisticBottomNav.
 * Consumers of the library create these and pass a list to setItems().
 */
public class NavItem {
    private final int id;
    private final Drawable icon;
    private final String label;

    public NavItem(Drawable icon, String label) {
        this(-1, icon, label);
    }

    public NavItem(int id, Drawable icon, String label) {
        this.id = id;
        this.icon = icon;
        this.label = label;
    }

    public int getId() {
        return id;
    }
    public Drawable getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }
}