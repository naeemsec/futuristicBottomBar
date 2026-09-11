package com.navbar.futuristicbottomnav;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * FuturisticBottomNav
 * --------------------
 * A self-drawing (Canvas-based) bottom navigation bar with:
 *  - a convex "bump" that slides between tabs with a bouncy animation
 *  - a soft neon glow behind the active tab
 *  - fully customizable colors (via XML attrs or setColors())
 *  - any number of items (via setItems())
 *
 * Usage (XML):
 *   <com.navbar.futuristicbottomnav.FuturisticBottomNav
 *       android:id="@+id/bottomNav"
 *       android:layout_width="match_parent"
 *       android:layout_height="wrap_content"
 *       app:navBackgroundColor="#12162E"
 *       app:navActiveColor="#5EEAD4"
 *       app:navInactiveColor="#8B93B8"
 *       app:navGlowColor="#5EEAD4" />
 *
 * Usage (Java):
 *   bottomNav.setItems(Arrays.asList(
 *       new NavItem(homeIcon, "Home"),
 *       new NavItem(newIcon, "New"),
 *       new NavItem(discoverIcon, "Discover"),
 *       new NavItem(exploreIcon, "Explore")
 *   ));
 *   bottomNav.setOnItemSelectedListener((index, item) -> { ... });
 */
public class FuturisticBottomNav extends View {

    public interface OnItemSelectedListener {
        void onItemSelected(int index, NavItem item);
    }

    public static final int THEME_DARK = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_AUTO = 2;

    private int themeMode = THEME_DARK;
    private final List<NavItem> items = new ArrayList<>();
    private int selectedIndex = 0;

    private int backgroundColor = Color.parseColor("#12162E");
    private int activeColor = Color.parseColor("#5EEAD4");
    private int inactiveColor = Color.parseColor("#8B93B8");
    private int glowColor = Color.parseColor("#5EEAD4");
    private boolean glowEnabled = true;
    private float glowIntensity = 0.7f; // 0f = off/subtle, 1f = maximum spread & brightness
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bumpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float indicatorX = 0f;   // current animated center-x of the bump
    private float bumpScale = 1f;    // gives the bump a little "pop" on selection

    private ValueAnimator positionAnimator;
    private ValueAnimator popAnimator;

    private OnItemSelectedListener listener;

    private static final float BAR_HEIGHT_DP = 100f;
    private static final float BUMP_RADIUS_DP = 24f;
    private static final float ICON_SIZE_DP = 22f;
    private static final float BAR_TOP_INSET_DP = 44f; // leaves room for the bump + glow to float above the bar

    public FuturisticBottomNav(Context context) {
        this(context, null);
    }

    public FuturisticBottomNav(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FuturisticBottomNav(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int menuRes = 0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FuturisticBottomNav);

            int requestedTheme = a.getInt(R.styleable.FuturisticBottomNav_navTheme, THEME_DARK);
            themeMode = resolveTheme(context, requestedTheme);
            applyThemeDefaults(); // theme palette pehle set hoga, XML colors iske upar override karenge

            backgroundColor = a.getColor(R.styleable.FuturisticBottomNav_navBackgroundColor, backgroundColor);
            activeColor = a.getColor(R.styleable.FuturisticBottomNav_navActiveColor, activeColor);
            inactiveColor = a.getColor(R.styleable.FuturisticBottomNav_navInactiveColor, inactiveColor);
            glowColor = a.getColor(R.styleable.FuturisticBottomNav_navGlowColor, glowColor);
            glowIntensity = a.getFloat(R.styleable.FuturisticBottomNav_navGlowIntensity, glowIntensity);
            glowEnabled = a.getBoolean(R.styleable.FuturisticBottomNav_navGlowEnabled, glowEnabled);
            menuRes = a.getResourceId(R.styleable.FuturisticBottomNav_navMenu, 0);

            a.recycle();
        } else {
            applyThemeDefaults();
        }

        setWillNotDraw(false);
        setClickable(true);

        barPaint.setStyle(Paint.Style.FILL);

        bumpPaint.setStyle(Paint.Style.FILL);
        bumpPaint.setColor(activeColor);

        glowPaint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(11));

        if (menuRes != 0) {
            inflateMenu(menuRes);
        }
    }

    private int resolveTheme(Context context, int requestedTheme) {
        if (requestedTheme == THEME_AUTO) {
            int nightFlags = context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return (nightFlags == Configuration.UI_MODE_NIGHT_YES) ? THEME_DARK : THEME_LIGHT;
        }
        return requestedTheme;
    }

    private void applyThemeDefaults() {
        if (themeMode == THEME_LIGHT) {
            backgroundColor = Color.parseColor("#F7F8FC");
            activeColor = Color.parseColor("#0D9488");
            inactiveColor = Color.parseColor("#6B7280");
            glowColor = Color.parseColor("#0D9488");
        } else {
            backgroundColor = Color.parseColor("#12162E");
            activeColor = Color.parseColor("#5EEAD4");
            inactiveColor = Color.parseColor("#8B93B8");
            glowColor = Color.parseColor("#5EEAD4");
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    // ---------------------------------------------------------
    // Public API — this is what other developers will call
    // ---------------------------------------------------------

    public void setItems(List<NavItem> newItems) {
        items.clear();
        items.addAll(newItems);
        selectedIndex = 0;
        requestLayout();
        post(() -> {
            indicatorX = slotCenterX(selectedIndex);
            invalidate();
        });
    }

    public void setColors(int background, int active, int inactive, int glow) {
        this.backgroundColor = background;
        this.activeColor = active;
        this.inactiveColor = inactive;
        this.glowColor = glow;
        bumpPaint.setColor(activeColor);
        invalidate();
    }

    /** Runtime pe theme change karne ke liye — THEME_DARK / THEME_LIGHT / THEME_AUTO */
    public void setTheme(int mode) {
        this.themeMode = resolveTheme(getContext(), mode);
        applyThemeDefaults();
        bumpPaint.setColor(activeColor);
        invalidate();
    }

    /**
     * XML menu resource se items load karo (jese app:navMenu="@menu/bottom_nav_menu").
     * Har <item> ka android:icon aur android:title use hoga.
     */
    public void inflateMenu(int menuRes) {
        if (menuRes == 0) return;
        PopupMenu popupMenu = new PopupMenu(getContext(), null);
        popupMenu.inflate(menuRes);
        Menu menu = popupMenu.getMenu();

        List<NavItem> menuItems = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItems.add(new NavItem(
                    menuItem.getItemId(),
                    menuItem.getIcon(),
                    menuItem.getTitle() != null ? menuItem.getTitle().toString() : ""
            ));
        }
        setItems(menuItems);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    /** Turn the glow effect fully on/off. */
    public void setGlowEnabled(boolean enabled) {
        this.glowEnabled = enabled;
        invalidate();
    }

    /**
     * Controls how big and how bright the glow is.
     * @param intensity 0f (barely visible) to 1f (maximum spread & brightness).
     *                  Values are clamped to that range.
     */
    public void setGlowIntensity(float intensity) {
        this.glowIntensity = Math.max(0f, Math.min(1f, intensity));
        invalidate();
    }

    /** Programmatically select a tab (e.g. to sync with a ViewPager). */
    public void setSelectedIndex(int index) {
        selectItem(index);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    // ---------------------------------------------------------
    // Measurement
    // ---------------------------------------------------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dp(BAR_HEIGHT_DP);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        indicatorX = slotCenterX(selectedIndex);
    }

    private float slotCenterX(int index) {
        if (items.isEmpty() || getWidth() == 0) return 0f;
        float slotWidth = getWidth() / (float) items.size();
        return slotWidth * index + slotWidth / 2f;
    }

    // ---------------------------------------------------------
    // Touch handling
    // ---------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (items.isEmpty()) return true;
            float slotWidth = getWidth() / (float) items.size();
            int index = (int) (event.getX() / slotWidth);
            index = Math.max(0, Math.min(items.size() - 1, index));
            selectItem(index);
        }
        return true;
    }

    private void selectItem(int index) {
        selectedIndex = index;
        float targetX = slotCenterX(index);

        if (positionAnimator != null) positionAnimator.cancel();
        positionAnimator = ValueAnimator.ofFloat(indicatorX, targetX);
        positionAnimator.setDuration(380);
        positionAnimator.setInterpolator(new OvershootInterpolator(1.6f));
        positionAnimator.addUpdateListener(anim -> {
            indicatorX = (float) anim.getAnimatedValue();
            invalidate();
        });
        positionAnimator.start();

        if (popAnimator != null) popAnimator.cancel();
        popAnimator = ValueAnimator.ofFloat(0.55f, 1f);
        popAnimator.setDuration(320);
        popAnimator.setInterpolator(new OvershootInterpolator(3f));
        popAnimator.addUpdateListener(anim -> {
            bumpScale = (float) anim.getAnimatedValue();
            invalidate();
        });
        popAnimator.start();

        if (listener != null) {
            listener.onItemSelected(index, items.get(index));
        }
    }

    // ---------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) return;

        drawBarBackground(canvas);
        drawGlow(canvas);
        drawBump(canvas);
        drawItems(canvas);
    }

    private void drawBarBackground(Canvas canvas) {
        float radius = dp(28);
        float top = dp(BAR_TOP_INSET_DP);
        RectF rect = new RectF(0, top, getWidth(), getHeight());

        LinearGradient gradient = new LinearGradient(
                0, top, 0, getHeight(),
                lighten(backgroundColor, 0.08f), backgroundColor,
                Shader.TileMode.CLAMP
        );
        barPaint.setShader(gradient);
        canvas.drawRoundRect(rect, radius, radius, barPaint);
    }

    private void drawGlow(Canvas canvas) {
        if (!glowEnabled || glowIntensity <= 0f) return;

        float bumpRadius = dp(BUMP_RADIUS_DP);
        float cy = dp(BAR_TOP_INSET_DP);
        float glowRadius = bumpRadius * (1f + glowIntensity * 1.6f); // scales from 1x up to 2.6x
        int alpha = (int) (glowIntensity * 160);

        RadialGradient glow = new RadialGradient(
                indicatorX, cy, glowRadius,
                withAlpha(glowColor, alpha), withAlpha(glowColor, 0),
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(glow);
        canvas.drawCircle(indicatorX, cy, glowRadius, glowPaint);
    }

    private void drawBump(Canvas canvas) {
        float bumpRadius = dp(BUMP_RADIUS_DP) * bumpScale;
        float cy = dp(BAR_TOP_INSET_DP);
        canvas.drawCircle(indicatorX, cy, bumpRadius, bumpPaint);
    }

    private void drawItems(Canvas canvas) {
        float slotWidth = getWidth() / (float) items.size();
        float barTop = dp(BAR_TOP_INSET_DP);
        float barCenterY = barTop + (getHeight() - barTop) / 2f;

        for (int i = 0; i < items.size(); i++) {
            NavItem item = items.get(i);
            float cx = slotWidth * i + slotWidth / 2f;
            boolean active = (i == selectedIndex);

            float iconSize = dp(ICON_SIZE_DP) * (active ? 1.15f : 1f);
            float iconCy = active ? barTop : barCenterY - dp(4);

            Drawable icon = item.getIcon();
            if (icon != null) {
                icon.setTint(active ? Color.WHITE : inactiveColor);
                int half = (int) (iconSize / 2);
                icon.setBounds((int) (cx - half), (int) (iconCy - half),
                        (int) (cx + half), (int) (iconCy + half));
                icon.draw(canvas);
            }

            if (!active) {
                textPaint.setColor(inactiveColor);
                canvas.drawText(item.getLabel(), cx, getHeight() - dp(10), textPaint);
            }
        }
    }

    private int lighten(int color, float factor) {
        int r = (int) Math.min(255, Color.red(color) + 255 * factor);
        int g = (int) Math.min(255, Color.green(color) + 255 * factor);
        int b = (int) Math.min(255, Color.blue(color) + 255 * factor);
        return Color.rgb(r, g, b);
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}