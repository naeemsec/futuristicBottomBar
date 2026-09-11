# Futuristic Bottom Nav
[![JitPack](https://jitpack.io/v/naeemsec/futuristicBottomBar.svg)](https://jitpack.io/#naeemsec/futuristicBottomBar)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A self-drawing, animated Bottom Navigation Bar for native Android
(Java + XML) — with a convex glowing "bump" that slides between tabs.
No external dependencies. Fully customizable colors. Any number of items.

<!-- Add a screen-recording GIF here once your demo app is running.
     This matters a lot for adoption — put it right at the top. -->

## Installation

**Step 1.** Add JitPack to your project's `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency to your app module's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.naeemsec.futuristicBottomBar:futuristicbottomnav:v1.0.0'
}
```

## Usage

**In your layout XML:**

```xml
<com.naeem.futuristicbottomnav.FuturisticBottomNav
    android:id="@+id/bottomNav"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:navBackgroundColor="#12162E"
    app:navActiveColor="#5EEAD4"
    app:navInactiveColor="#8B93B8"
    app:navGlowColor="#5EEAD4" />
```

**In your Activity/Fragment:**

```.java
FuturisticBottomNav bottomNav = findViewById(R.id.bottomNav);

bottomNav.setItems(Arrays.asList(
    new NavItem(homeIcon, "Home"),
    new NavItem(newIcon, "New"),
    new NavItem(discoverIcon, "Discover"),
    new NavItem(exploreIcon, "Explore")
));

bottomNav.setOnItemSelectedListener((index, item) -> {
    // handle tab change
});
```

You can add as many `NavItem`s as you want — the bar automatically
splits the available width evenly between them.

## Theming (Light / Dark / Auto)

```.xml
app:navTheme="auto"   <!-- follows system dark mode -->
```

Runtime pe switch karne ke liye:
```.java
bottomNav.setTheme(FuturisticBottomNav.THEME_LIGHT);
// THEME_DARK, THEME_LIGHT, THEME_AUTO
```

Agar `navBackgroundColor` / `navActiveColor` / etc. explicitly XML mein diye hain, to woh theme ke defaults ko override kar dete hain.

## Items via Menu Resource (no hardcoding)

Java code likhne ke bajaye, standard Android `<menu>` resource se bhi items diye ja sakte hain — `BottomNavigationView` ki tarah:

`res/menu/bottom_nav_menu.xml`:
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_home" android:icon="@drawable/ic_home" android:title="Home" />
    <item android:id="@+id/nav_new" android:icon="@drawable/ic_new" android:title="New" />
    <item android:id="@+id/nav_discover" android:icon="@drawable/ic_discover" android:title="Discover" />
</menu>
```

```.xml
app:navMenu="@menu/bottom_nav_menu"
```

**Note:** `app:navMenu` aur manual `setItems()` ek sath mat use karo — jo baad mein call hoga wo dusre ko overwrite kar dega.

## Customization

| Attribute            | Description                          |
|-----------------------|--------------------------------------|
| `navBackgroundColor`  | Background color of the bar          |
| `navActiveColor`      | Color of the sliding bump (selected) |
| `navInactiveColor`    | Icon/text color when not selected    |
| `navGlowColor`        | Color of the neon glow behind the bump |
| `navGlowIntensity`    | 0.0–1.0, glow ka spread/brightness   |
| `navGlowEnabled`      | true/false, glow on/off              |
| `navTheme`            | `dark` / `light` / `auto` — color palette |
| `navMenu`             | `@menu/xxx` — XML menu resource se items load karo |

Colors can also be set at runtime:

```.java
bottomNav.setColors(backgroundColor, activeColor, inactiveColor, glowColor);
```

## How it works

- The bar background, glow, and bump are all drawn manually with
  `Canvas` + `Paint` (gradients and shadow-style glow) — no images needed.
- The bump position animates with `ValueAnimator` + `OvershootInterpolator`,
  giving it a small satisfying bounce when you switch tabs.
- Icons are tinted programmatically, so a single vector drawable set
  works for both active and inactive states.

## Publishing your own version (for the repo owner)

1. Push this project to a public GitHub repo.
2. Confirm the `futuristicbottomnav` module's `build.gradle` uses
   `id 'com.android.library'` (already set up).
3. Create a GitHub Release with a version tag, e.g. `v1.0.0`.
4. Go to [jitpack.io](https://jitpack.io), paste your repo URL, click
   "Get it" — JitPack builds the library from your tag automatically.
5. Anyone can now add it with the two steps under **Installation** above.

## License

Apache-2.0 — free to use, modify, and distribute.