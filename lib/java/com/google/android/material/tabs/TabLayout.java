/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.tabs;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.util.SeslMisc;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.reflect.widget.SeslHorizontalScrollViewReflector;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.R;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * (SESL Variant) TabLayout provides a horizontal layout to display tabs.
 *
 * <p>Population of the tabs to display is done through {@link Tab} instances. You create tabs via
 * {@link #newTab()}. From there you can change the tab's label or icon via {@link Tab#setText(int)}
 * and {@link Tab#setIcon(int)} respectively. To display the tab, you need to add it to the layout
 * via one of the {@link #addTab(Tab)} methods. For example:
 *
 * <pre>
 * TabLayout tabLayout = ...;
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
 * </pre>
 *
 * You should add a listener via {@link #addOnTabSelectedListener(OnTabSelectedListener)} to be
 * notified when any tab's selection state has been changed.
 *
 * <p>You can also add items to TabLayout in your layout through the use of {@link TabItem}. An
 * example usage is like so:
 *
 * <pre>
 * &lt;com.google.android.material.tabs.TabLayout
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_width=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.google.android.material.tabs.TabItem
 *             android:text=&quot;@string/tab_text&quot;/&gt;
 *
 *     &lt;com.google.android.material.tabs.TabItem
 *             android:icon=&quot;@drawable/ic_android&quot;/&gt;
 *
 * &lt;/com.google.android.material.tabs.TabLayout&gt;
 * </pre>
 *
 * <h3>ViewPager integration</h3>
 *
 * <p>If you're using a {@link androidx.viewpager.widget.ViewPager} together with this layout, you
 * can call {@link #setupWithViewPager(ViewPager)} to link the two together. This layout will be
 * automatically populated from the {@link PagerAdapter}'s page titles.
 *
 * <p>This view also supports being used as part of a ViewPager's decor, and can be added directly
 * to the ViewPager in a layout resource file like so:
 *
 * <pre>
 * &lt;androidx.viewpager.widget.ViewPager
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.google.android.material.tabs.TabLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_gravity=&quot;top&quot; /&gt;
 *
 * &lt;/androidx.viewpager.widget.ViewPager&gt;
 * </pre>
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Tabs.md">component
 * developer guidance</a> and <a href="https://material.io/components/tabs/overview">design
 * guidelines</a>.
 *
 * @see <a href="http://www.google.com/design/spec/components/tabs.html">Tabs</a>
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabPadding
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabPaddingStart
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabPaddingTop
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabPaddingEnd
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabPaddingBottom
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabContentStart
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabBackground
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabMinWidth
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabMaxWidth
 * @attr ref com.google.android.material.R.styleable#TabLayout_tabTextAppearance
 */
@ViewPager.DecorView
public class TabLayout extends HorizontalScrollView {
  // Sesl
  @Dimension(unit = Dimension.DP)
  private static final int SESL_DEFAULT_HEIGHT = 60;
  private static final int SESL_SUB_DEPTH_DEFAULT_HEIGHT = 56;
  private static final int DEPTH_TYPE_MAIN = 1;
  private static final int DEPTH_TYPE_SUB = 2;

  private static final int BADGE_TYPE_UNKNOWN = -1;
  private static final int BADGE_TYPE_N = 1;
  private static final int BADGE_TYPE_DOT = 2;
  private final Typeface mBoldTypeface;
  private final Typeface mNormalTypeface;
  ColorStateList mSubTabSubTextColors;
  private int mBadgeColor = Color.WHITE;
  private int mBadgeTextColor = Color.WHITE;
  private int mCurrentTouchSlop;
  private final int mDefaultTouchSlop;
  private int mDepthStyle;
  private final int mFirstTabGravity;
  private int mIconTextGap = -1;
  private int mMaxTouchSlop;
  private int mOverScreenMaxWidth = -1;
  private int mRequestedTabWidth = -1;
  private final int mSubTabIndicator2ndHeight;
  private int mSubTabIndicatorHeight;
  private int mSubTabSelectedIndicatorColor = -1;
  int mSubTabSubTextAppearance;
  int mSubTabTextSize;
  private final int mTabMinSideSpace;

  private boolean mIsOverScreen = false;
  private boolean mIsScaledTextSizeType = false;
  private int mTabSelectedIndicatorColor;
  private ContentResolver mContentResolver;
  private ColorDrawable mBackgroundColorDrawable;
  //sesl

  private static final int DEF_STYLE_RES = R.style.Widget_Design_TabLayout;

  //@Dimension(unit = Dimension.DP)
  //private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;

  @Dimension(unit = Dimension.DP)
  static final int DEFAULT_GAP_TEXT_ICON = 8;

  //@Dimension(unit = Dimension.DP)
  //private static final int DEFAULT_HEIGHT = 48;

  @Dimension(unit = Dimension.DP)
  private static final int TAB_MIN_WIDTH_MARGIN = 56;

  @Dimension(unit = Dimension.DP)
  static final int FIXED_WRAP_GUTTER_MIN = 16;

  private static final int INVALID_WIDTH = -1;

  private static final int ANIMATION_DURATION = 300;

  private static final int SELECTED_INDICATOR_HEIGHT_DEFAULT = -1;

  private static Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool<>(16);

  private static final String LOG_TAG = "TabLayout";

  /**
   * Scrollable tabs display a subset of tabs at any given moment, and can contain longer tab labels
   * and a larger number of tabs. They are best used for browsing contexts in touch interfaces when
   * users don't need to directly compare the tab labels.
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int MODE_SCROLLABLE = 0;

  /**
   * Fixed tabs display all tabs concurrently and are best used with content that benefits from
   * quick pivots between tabs. The maximum number of tabs is limited by the view's width. Fixed
   * tabs have equal width, based on the widest tab label.
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int MODE_FIXED = 1;

  /**
   * Auto-sizing tabs behave like MODE_FIXED with GRAVITY_CENTER while the tabs fit within the
   * TabLayout's content width. Fixed tabs have equal width, based on the widest tab label. Once the
   * tabs outgrow the view's width, auto-sizing tabs behave like MODE_SCROLLABLE, allowing for a
   * dynamic number of tabs without requiring additional layout logic.
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int MODE_AUTO = 2;

  /**
   * Auto-sizing tabs behave like MODE_FIXED with GRAVITY_FILL while the tabs fit within the
   * TabLayout's content width. Fixed tabs widths vary based on its content. Once the tabs outgrow the view's width, auto-sizing tabs behave like MODE_SCROLLABLE, allowing for a
   * dynamic number of tabs without requiring additional layout logic.
   * <p>
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   *
   */
  public static final int SESL_MODE_FIXED_AUTO = 11;

  /**
   * Auto-sizing tabs behave like MODE_FIXED with GRAVITY_FILL while the tabs fit within the
   * TabLayout's content width. Fixed tabs have equal width, based on the widest tab label. Once the
   * tabs outgrow the view's width, auto-sizing tabs behave like MODE_SCROLLABLE, allowing for a
   * dynamic number of tabs without requiring additional layout logic.
   * <p>
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int SESL_MODE_WEIGHT_AUTO = 12;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO,
      SESL_MODE_FIXED_AUTO, SESL_MODE_WEIGHT_AUTO})//sesl
  @Retention(RetentionPolicy.SOURCE)
  public @interface Mode {}

  /**
   * If a tab is instantiated with {@link Tab#setText(CharSequence)}, and this mode is set, the text
   * will be saved and utilized for the content description, but no visible labels will be created.
   *
   * @see Tab#setTabLabelVisibility(int)
   */
  public static final int TAB_LABEL_VISIBILITY_UNLABELED = 0;

  /**
   * This mode is set by default. If a tab is instantiated with {@link Tab#setText(CharSequence)}, a
   * visible label will be created.
   *
   * @see Tab#setTabLabelVisibility(int)
   */
  public static final int TAB_LABEL_VISIBILITY_LABELED = 1;

  /** @hide */
  @IntDef(value = {TAB_LABEL_VISIBILITY_UNLABELED, TAB_LABEL_VISIBILITY_LABELED})
  public @interface LabelVisibility {}

  /**
   * Gravity used to fill the {@link TabLayout} as much as possible. This option only takes effect
   * when used with {@link #MODE_FIXED} on non-landscape screens less than 600dp wide.
   *
   * @see #setTabGravity(int)
   * @see #getTabGravity()
   */
  public static final int GRAVITY_FILL = 0;

  /**
   * Gravity used to lay out the tabs in the center of the {@link TabLayout}.
   *
   * @see #setTabGravity(int)
   * @see #getTabGravity()
   */
  public static final int GRAVITY_CENTER = 1;

  /**
   * Gravity used to lay out the tabs aligned to the start of the {@link TabLayout}.
   *
   * @see #setTabGravity(int)
   * @see #getTabGravity()
   */
  public static final int GRAVITY_START = 1 << 1;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      flag = true,
      value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_START})
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabGravity {}

  // indicatorPosition keeps track of where the indicator is.
  int indicatorPosition = -1;

  /**
   * Indicator gravity used to align the tab selection indicator to the bottom of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown. This is the default value.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_BOTTOM = 0;

  /**
   * Indicator gravity used to align the tab selection indicator to the center of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_CENTER = 1;

  /**
   * Indicator gravity used to align the tab selection indicator to the top of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_TOP = 2;

  /**
   * Indicator gravity used to stretch the tab selection indicator across the entire height
   * of the {@link TabLayout}. This will disregard {@code tabIndicatorHeight} and the
   * indicator drawable's intrinsic height, if set.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_STRETCH = 3;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      value = {
        INDICATOR_GRAVITY_BOTTOM,
        INDICATOR_GRAVITY_CENTER,
        INDICATOR_GRAVITY_TOP,
        INDICATOR_GRAVITY_STRETCH
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabIndicatorGravity {}

  /**
   * Indicator animation mode used to translate the selected tab indicator between two tabs using a
   * linear motion.
   *
   * <p>The left and right side of the selection indicator translate in step over the duration of
   * the animation. The only exception to this is when the indicator needs to change size to fit the
   * width of its new destination tab's label.
   *
   * @see #setTabIndicatorAnimationMode(int)
   * @see #getTabIndicatorAnimationMode()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorAnimationMode
   */
  public static final int INDICATOR_ANIMATION_MODE_LINEAR = 0;

  /**
   * Indicator animation mode used to translate the selected tab indicator by growing and then
   * shrinking the indicator, making the indicator look like it is stretching while translating
   * between destinations.
   *
   * <p>The left and right side of the selection indicator translate out of step - with the right
   * decelerating and the left accelerating (when moving right). This difference in velocity between
   * the sides of the indicator, over the duration of the animation, make the indicator look like it
   * grows and then shrinks back down to fit it's new destination's width.
   *
   * @see #setTabIndicatorAnimationMode(int)
   * @see #getTabIndicatorAnimationMode()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorAnimationMode
   */
  public static final int INDICATOR_ANIMATION_MODE_ELASTIC = 1;

  /**
   * Indicator animation mode used to switch the selected tab indicator from one tab to another
   * by sequentially fading it out from the current destination and in at its new destination.
   *
   * @see #setTabIndicatorAnimationMode(int)
   * @see #getTabIndicatorAnimationMode()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorAnimationMode
   */
  public static final int INDICATOR_ANIMATION_MODE_FADE = 2;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {
      INDICATOR_ANIMATION_MODE_LINEAR,
      INDICATOR_ANIMATION_MODE_ELASTIC,
      INDICATOR_ANIMATION_MODE_FADE
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabIndicatorAnimationMode {}

  /** Callback interface invoked when a tab's selection state changes. */
  public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {}

  /**
   * Callback interface invoked when a tab's selection state changes.
   *
   * @deprecated Use {@link OnTabSelectedListener} instead.
   */
  @Deprecated
  public interface BaseOnTabSelectedListener<T extends Tab> {
    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    public void onTabSelected(T tab);

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    public void onTabUnselected(T tab);

    /**
     * Called when a tab that is already selected is chosen again by the user. Some applications may
     * use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     */
    public void onTabReselected(T tab);
  }

  private final ArrayList<Tab> tabs = new ArrayList<>();
  @Nullable private Tab selectedTab;

  @NonNull final SlidingTabIndicator slidingTabIndicator;

  int tabPaddingStart;
  int tabPaddingTop;
  int tabPaddingEnd;
  int tabPaddingBottom;

  private final int defaultTabTextAppearance;
  private final int tabTextAppearance;
  private int selectedTabTextAppearance = -1;
  ColorStateList tabTextColors;
  ColorStateList tabIconTint;
  ColorStateList tabRippleColorStateList;
  @NonNull Drawable tabSelectedIndicator;
  private int tabSelectedIndicatorColor = 0;

  android.graphics.PorterDuff.Mode tabIconTintMode;
  float tabTextSize;
  float selectedTabTextSize;
  float tabTextMultiLineSize;

  final int tabBackgroundResId;

  int tabMaxWidth = Integer.MAX_VALUE;
  private final int requestedTabMinWidth;
  private final int requestedTabMaxWidth;
  private final int scrollableTabMinWidth;
  // private final int tabSideSpace;
  private final int mOverScreenWidth;
  private final float mOverScreenWidthMaxRate;
  //private final int dotBadgeSize;

  private int contentInsetStart;

  @TabGravity int tabGravity;
  int tabIndicatorAnimationDuration;
  @TabIndicatorGravity int tabIndicatorGravity;
  @Mode int mode;
  boolean inlineLabel;
  boolean tabIndicatorFullWidth;
  int tabIndicatorHeight = SELECTED_INDICATOR_HEIGHT_DEFAULT;
  @TabIndicatorAnimationMode int tabIndicatorAnimationMode;
  boolean unboundedRipple;

  private TabIndicatorInterpolator tabIndicatorInterpolator;
  private final TimeInterpolator tabIndicatorTimeInterpolator;

  @Nullable private BaseOnTabSelectedListener selectedListener;

  private final ArrayList<BaseOnTabSelectedListener> selectedListeners = new ArrayList<>();
  @Nullable private BaseOnTabSelectedListener currentVpSelectedListener;

  private ValueAnimator scrollAnimator;

  @Nullable ViewPager viewPager;
  @Nullable private PagerAdapter pagerAdapter;
  private DataSetObserver pagerAdapterObserver;
  private TabLayoutOnPageChangeListener pageChangeListener;
  private AdapterChangeListener adapterChangeListener;
  private boolean setupViewPagerImplicitly;
  private int viewPagerScrollState;

  private static final int FONT_WEIGHT_REGULAR = 400;
  private static final int FONT_WEIGHT_SEMIBOLD = 600;

  // Pool we use as a simple RecyclerBin
  private Pools.Pool<TabView> tabViewPool = new Pools.SimplePool<>(12);

  public TabLayout(@NonNull Context context) {
    this(context, null);
  }

  public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.tabStyle);
  }

  public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    // Disable the Scroll Bar
    setHorizontalScrollBarEnabled(false);

    // Add the TabStrip
    slidingTabIndicator = new SlidingTabIndicator(context);
    super.addView(
        slidingTabIndicator,
        0,
        new HorizontalScrollView.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

    TypedArray a =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.TabLayout,
            defStyleAttr,
            SeslMisc.isLightTheme(context) ?
                R.style.Widget_Design_TabLayout_Light : R.style.Widget_Design_TabLayout);//sesl

    ColorStateList backgroundColorStateList =
        DrawableUtils.getColorStateListOrNull(getBackground());
    if (backgroundColorStateList != null) {
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(backgroundColorStateList);
      materialShapeDrawable.initializeElevationOverlay(context);
      materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
      setBackground(materialShapeDrawable);
    }

    setSelectedTabIndicator(
        MaterialResources.getDrawable(context, a, R.styleable.TabLayout_tabIndicator));
    setSelectedTabIndicatorColor(
        a.getColor(R.styleable.TabLayout_tabIndicatorColor, Color.TRANSPARENT));
    slidingTabIndicator.setSelectedIndicatorHeight(
        a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1));
    tabSelectedIndicatorColor
        = a.getColor(R.styleable.TabLayout_tabIndicatorColor, Color.TRANSPARENT);//sesl
    setSelectedTabIndicatorGravity(
        a.getInt(R.styleable.TabLayout_tabIndicatorGravity, INDICATOR_GRAVITY_BOTTOM));
    setTabIndicatorAnimationMode(
        a.getInt(R.styleable.TabLayout_tabIndicatorAnimationMode, INDICATOR_ANIMATION_MODE_LINEAR));
    setTabIndicatorFullWidth(a.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));

    tabPaddingStart =
        tabPaddingTop =
            tabPaddingEnd =
                tabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
    tabPaddingStart =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, tabPaddingStart);
    tabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, tabPaddingTop);
    tabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, tabPaddingEnd);
    tabPaddingBottom =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, tabPaddingBottom);

    if (ThemeEnforcement.isMaterial3Theme(context)) {
      defaultTabTextAppearance = R.attr.textAppearanceTitleSmall;
    } else {
      defaultTabTextAppearance = R.attr.textAppearanceButton;
    }

    tabTextAppearance =
        a.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);

    //Sesl
    TypedArray seslArray = context.obtainStyledAttributes(tabTextAppearance, R.styleable.TextAppearance);
    tabTextSize = seslArray.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
    mIsScaledTextSizeType = seslArray.getText(R.styleable.TextAppearance_android_textSize).toString().contains("sp");
    tabTextColors = MaterialResources.getColorStateList(context, seslArray, R.styleable.TextAppearance_android_textColor);

    final Resources res = getResources();

    mMaxTouchSlop = res.getDisplayMetrics().widthPixels;
    mCurrentTouchSlop
        = mDefaultTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    if (VERSION.SDK_INT >= 34) {
      Typeface family = Typeface.create("sec", 0);
      mBoldTypeface = Typeface.create(family, FONT_WEIGHT_SEMIBOLD, false);
      mNormalTypeface = Typeface.create(family, FONT_WEIGHT_REGULAR, false);
    } else {
      String familyName = res.getString(androidx.appcompat.R.string.sesl_font_family_regular);
      mBoldTypeface = Typeface.create(familyName, Typeface.BOLD);
      mNormalTypeface = Typeface.create(familyName, Typeface.NORMAL);
    }

    mSubTabIndicatorHeight = res.getDimensionPixelSize(
        R.dimen.sesl_tablayout_subtab_indicator_height);
    mSubTabIndicator2ndHeight = res.getDimensionPixelSize(
        R.dimen.sesl_tablayout_subtab_indicator_2nd_height);
    mTabMinSideSpace = res.getDimensionPixelSize(R.dimen.sesl_tab_min_side_space);
    mSubTabSubTextAppearance = a.getResourceId(
        R.styleable.TabLayout_seslTabSubTextAppearance, R.style.TextAppearance_Design_Tab_SubText);
    //sesl

    // Text colors/sizes come from the text appearance first
    TypedArray ta =
        context.obtainStyledAttributes(
            mSubTabSubTextAppearance, R.styleable.TextAppearance);
    try {
      //Sesl
      mSubTabTextSize =
          ta.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
      mSubTabSubTextColors
          = MaterialResources.getColorStateList(context, ta, R.styleable.TextAppearance_android_textColor);
      seslArray.recycle();
      ta.recycle();
      //Sesl
      if (a.hasValue(R.styleable.TabLayout_seslTabSubTextColor)) {
        mSubTabSubTextColors = MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_seslTabSubTextColor);
      }

      mDepthStyle = a.getInt(R.styleable.TabLayout_seslTabStyle, DEPTH_TYPE_MAIN);

      if (a.hasValue(R.styleable.TabLayout_seslTabSelectedSubTextColor)) {
        mSubTabSubTextColors = createColorStateList(
            mSubTabSubTextColors.getDefaultColor(),
            a.getColor(R.styleable.TabLayout_seslTabSelectedSubTextColor, 0));
      }

      if (a.hasValue(R.styleable.TabLayout_tabSelectedTextAppearance)) {
        selectedTabTextAppearance = a.getResourceId(R.styleable.TabLayout_tabSelectedTextAppearance, tabTextAppearance);
      }

      if (selectedTabTextAppearance != -1) {
        ta = context.obtainStyledAttributes(selectedTabTextAppearance, R.styleable.TextAppearance);
        try {
          selectedTabTextSize =
              ta.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) this.tabTextSize);
          ColorStateList colorStateList
              = MaterialResources.getColorStateList(context, ta, R.styleable.TextAppearance_android_textColor);
          if (colorStateList != null) {
            tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), colorStateList.getColorForState(new int[]{16842913}, colorStateList.getDefaultColor()));
          }
        } finally {
          ta.recycle();
        }
      }
      //sesl

      if (a.hasValue(R.styleable.TabLayout_tabTextColor)) {
        // If we have an explicit text color set, use it instead
        tabTextColors =
            MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabTextColor);
      }

      if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
        // We have an explicit selected text color set, so we need to make merge it with the
        // current colors. This is exposed so that developers can use theme attributes to set
        // this (theme attrs in ColorStateLists are Lollipop+)
        final int selected = a.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0);
        tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), selected);
      }

      tabIconTint =
          MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabIconTint);
      tabIconTintMode =
          ViewUtils.parseTintMode(a.getInt(R.styleable.TabLayout_tabIconTintMode, -1), null);

      tabRippleColorStateList =
          MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabRippleColor);

      tabIndicatorAnimationDuration =
          a.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);

      tabIndicatorTimeInterpolator =
          MotionUtils.resolveThemeInterpolator(
              context, R.attr.motionEasingEmphasizedInterpolator, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      requestedTabMinWidth =
          a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, -1);
      requestedTabMaxWidth =
          a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, -1);
      tabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, 0);
      contentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
      mode = a.getInt(R.styleable.TabLayout_tabMode, MODE_FIXED);
      mFirstTabGravity = tabGravity = a.getInt(R.styleable.TabLayout_tabGravity, GRAVITY_FILL);
      inlineLabel = a.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
      unboundedRipple = a.getBoolean(R.styleable.TabLayout_tabUnboundedRipple, false);
      a.recycle();

      // TODO add attr for these
      tabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.sesl_tab_text_size_2line);//sesl
      mOverScreenWidth = res.getInteger(R.integer.sesl_tablayout_over_screen_width_dp);//sesl c
      mOverScreenWidthMaxRate = ResourcesCompat.getFloat(res, R.dimen.sesl_tablayout_over_screen_max_width_rate);//sesl c

      scrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.sesl_tab_scrollable_min_width);//sesl

      // Now apply the tab mode and gravity
      applyModeAndGravity();

      Drawable background = getBackground();
      mContentResolver = context.getContentResolver();
      if (background instanceof ColorDrawable) {
        mBackgroundColorDrawable = (ColorDrawable) background;
      }

      if (mDepthStyle == DEPTH_TYPE_SUB) {
        tabTextColors = getResources().getColorStateList(
                SeslMisc.isLightTheme(getContext())
                        ? R.color.sesl_tablayout_subtab_text_color_light
                        : R.color.sesl_tablayout_subtab_text_color_dark);
      }
    } catch (Throwable th) {
      seslArray.recycle();
      throw th;
    }
  }

  /**
   * Sets the tab indicator's color for the currently selected tab.
   *
   * <p>If the tab indicator color is not {@code Color.TRANSPARENT}, the indicator will be wrapped
   * and tinted right before it is drawn by {@link SlidingTabIndicator#draw(Canvas)}. If you'd like
   * the inherent color or the tinted color of a custom drawable to be used, make sure this color is
   * set to {@code Color.TRANSPARENT} to avoid your color/tint being overridden.
   *
   * @param color color to use for the indicator
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorColor
   */
  public void setSelectedTabIndicatorColor(@ColorInt int color) {
    //Sesl
    updateTabViews(false);
    mTabSelectedIndicatorColor = color;
    Iterator<Tab> tabs = this.tabs.iterator();
    while (tabs.hasNext()) {
      SeslAbsIndicatorView indicatorView = tabs.next().view.mIndicatorView;
      if (indicatorView != null) {
        if (mDepthStyle != DEPTH_TYPE_SUB || mSubTabSelectedIndicatorColor == Color.WHITE) {
          indicatorView.setSelectedIndicatorColor(color);
        } else {
          indicatorView.setSelectedIndicatorColor(mSubTabSelectedIndicatorColor);
        }
        indicatorView.invalidate();
      }
    }
    //sesl
  }

  /**
   * Sets the tab indicator's height for the currently selected tab.
   *
   * @deprecated If possible, set the intrinsic height directly on a custom indicator drawable
   *     passed to {@link #setSelectedTabIndicator(Drawable)}.
   * @param height height to use for the indicator in pixels
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorHeight
   */
  @Deprecated
  public void setSelectedTabIndicatorHeight(int height) {
    tabIndicatorHeight = height;
    slidingTabIndicator.setSelectedIndicatorHeight(height);
  }

  /**
   * Set the scroll position of the {@link TabLayout}.
   *
   * @param position Position of the tab to scroll.
   * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
   * @param updateSelectedTabView Whether to draw the tab at the specified position + positionOffset
   *     as selected.
   *     <p>Note that calling the method with {@code updateSelectedTabView = true}
   *     <em>does not</em> select a tab at the specified position, but only <em>draws it
   *     as selected</em>. This can be useful for when the TabLayout behavior needs to be linked to
   *     another view, such as {@link androidx.viewpager.widget.ViewPager}.
   * @see #setScrollPosition(int, float, boolean, boolean)
   */
  public void setScrollPosition(int position, float positionOffset, boolean updateSelectedTabView) {
    setScrollPosition(position, positionOffset, updateSelectedTabView, true);
  }

  /**
   * Set the scroll position of the {@link TabLayout}.
   *
   * @param position Position of the tab to scroll.
   * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
   * @param updateSelectedTabView Whether to draw the tab at the specified position + positionOffset
   *     as selected.
   *     <p>Note that calling the method with {@code updateSelectedTabView = true}
   *     <em>does not</em> select a tab at the specified position, but only <em>draws it
   *     as selected</em>. This can be useful for when the TabLayout behavior needs to be linked to
   *     another view, such as {@link androidx.viewpager.widget.ViewPager}.
   * @param updateIndicatorPosition Whether to set the indicator to the specified position and
   *     offset.
   *     <p>Note that calling the method with {@code updateIndicatorPosition = true}
   *     <em>does not</em> select a tab at the specified position, but only updates the indicator
   *     position. This can be useful for when the TabLayout behavior needs to be linked to
   *     another view, such as {@link androidx.viewpager.widget.ViewPager}.
   * @see #setScrollPosition(int, float, boolean)
   */
  public void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedTabView,
      boolean updateIndicatorPosition) {
    setScrollPosition(
        position,
        positionOffset,
        updateSelectedTabView,
        updateIndicatorPosition,
        /* alwaysScroll= */ true);
  }

  void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedTabView,
      boolean updateIndicatorPosition,
      boolean alwaysScroll) {
    final int roundedPosition = Math.round(position + positionOffset);
    if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
      return;
    }

    // Set the indicator position, if enabled
    if (updateIndicatorPosition) {
      slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
    }

    // Now update the scroll position, canceling any running animation
    if (scrollAnimator != null && scrollAnimator.isRunning()) {
      scrollAnimator.cancel();
    }
    int scrollXForPosition = calculateScrollXForTab(position, positionOffset);
    int scrollX = getScrollX();
    // If the position is smaller than the selected tab position, the position is getting larger
    // to reach the selected tab position so scrollX is increasing.
    // We only want to update the scroll position if the new scroll position is greater than
    // the current scroll position.
    // Conversely if the position is greater than the selected tab position, the position is
    // getting smaller to reach the selected tab position so scrollX is decreasing.
    // We only update the scroll position if the new scroll position is less than the current
    // scroll position.
    // Lastly if the position is equal to the selected position, we want to set the scroll
    // position which also updates the selected tab view and the indicator.
    boolean toMove =
        (position < getSelectedTabPosition() && scrollXForPosition >= scrollX)
            || (position > getSelectedTabPosition() && scrollXForPosition <= scrollX)
            || (position == getSelectedTabPosition());
    // If the layout direction is RTL, the scrollXForPosition and scrollX comparisons are
    // reversed since scrollX values remain the same in RTL but tab positions go RTL.
    if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
      toMove =
          (position < getSelectedTabPosition() && scrollXForPosition <= scrollX)
              || (position > getSelectedTabPosition()
              && scrollXForPosition >= scrollX)
              || (position == getSelectedTabPosition());
    }
    // We want to scroll if alwaysScroll is true, the viewpager is being dragged, or if we should
    // scroll by the rules above.
    if (toMove || viewPagerScrollState == SCROLL_STATE_DRAGGING || alwaysScroll) {
      scrollTo(position < 0 ? 0 : scrollXForPosition, 0);
    }

    // Update the 'selected state' view as we scroll, if enabled
    if (updateSelectedTabView) {
      setSelectedTabView(roundedPosition, true/*sesl*/);
    }
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list. If this is the first
   * tab to be added it will become the selected tab.
   *
   * @param tab Tab to add
   */
  public void addTab(@NonNull Tab tab) {
    addTab(tab, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>. If this is the
   * first tab to be added it will become the selected tab.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   */
  public void addTab(@NonNull Tab tab, int position) {
    addTab(tab, position, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list.
   *
   * @param tab Tab to add
   * @param setSelected True if the added tab should become the selected tab.
   */
  public void addTab(@NonNull Tab tab, boolean setSelected) {
    addTab(tab, tabs.size(), setSelected);
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   * @param setSelected True if the added tab should become the selected tab.
   */
  public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }
    configureTab(tab, position);
    addTabView(tab);

    if (setSelected) {
      tab.select();
    }
  }

  private void addTabFromItemView(@NonNull TabItem item) {
    final Tab tab = newTab();
    if (item.text != null) {
      tab.setText(item.text);
    }
    if (item.icon != null) {
      tab.setIcon(item.icon);
    }
    if (item.customLayout != 0) {
      tab.setCustomView(item.customLayout);
    }
    if (!TextUtils.isEmpty(item.getContentDescription())) {
      tab.setContentDescription(item.getContentDescription());
    }
    if (item.mSubText != null) {
      tab.seslSetSubText(item.mSubText);//sesl
    }
    addTab(tab);
  }

  private boolean isScrollingEnabled() {
    return getTabMode() == MODE_SCROLLABLE || getTabMode() == MODE_AUTO;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    // When a touch event is intercepted and the tab mode is fixed, do not continue to process the
    // touch event. This will prevent unexpected scrolling from occurring in corner cases (i.e. a
    // layout in fixed mode that has padding should not scroll for the width of the padding).
    return isScrollingEnabled() && super.onInterceptTouchEvent(event);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_SCROLL && !isScrollingEnabled()) {
      return false;
    }
    return super.onTouchEvent(event);
  }

  /**
   * @deprecated Use {@link #addOnTabSelectedListener(OnTabSelectedListener)} and {@link
   *       #removeOnTabSelectedListener(OnTabSelectedListener)}.
   */
  @Deprecated
  public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
    setOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  /**
   * @deprecated Use {@link #addOnTabSelectedListener(OnTabSelectedListener)} and {@link
   *     #removeOnTabSelectedListener(OnTabSelectedListener)}.
   */
  @Deprecated
  public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    // The logic in this method emulates what we had before support for multiple
    // registered listeners.
    if (selectedListener != null) {
      removeOnTabSelectedListener(selectedListener);
    }
    // Update the deprecated field so that we can remove the passed listener the next
    // time we're called
    selectedListener = listener;
    if (listener != null) {
      addOnTabSelectedListener(listener);
    }
  }

  /**
   * Add a {@link TabLayout.OnTabSelectedListener} that will be invoked when tab selection changes.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    addOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  /**
   * Add a {@link TabLayout.BaseOnTabSelectedListener} that will be invoked when tab selection
   * changes.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnTabSelectedListener(BaseOnTabSelectedListener)}.
   *
   * @param listener listener to add
   * @deprecated use {@link #addOnTabSelectedListener(OnTabSelectedListener)}
   */
  @Deprecated
  public void addOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener);
    }
  }

  /**
   * Remove the given {@link TabLayout.OnTabSelectedListener} that was previously added via {@link
   * #addOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    removeOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  /**
   * Remove the given {@link TabLayout.BaseOnTabSelectedListener} that was previously added via
   * {@link #addOnTabSelectedListener(BaseOnTabSelectedListener)}.
   *
   * @param listener listener to remove
   * @deprecated use {@link #removeOnTabSelectedListener(OnTabSelectedListener)}
   */
  @Deprecated
  public void removeOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    selectedListeners.remove(listener);
  }

  /** Remove all previously added {@link TabLayout.OnTabSelectedListener}s. */
  public void clearOnTabSelectedListeners() {
    selectedListeners.clear();
  }

  /**
   * Create and return a new {@link Tab}. You need to manually add this using {@link #addTab(Tab)}
   * or a related method.
   *
   * @return A new Tab
   * @see #addTab(Tab)
   */
  @NonNull
  public Tab newTab() {
    Tab tab = createTabFromPool();
    tab.parent = this;
    tab.view = createTabView(tab);
    if (tab.id != NO_ID) {
      tab.view.setId(tab.id);
    }

    return tab;
  }

  // TODO(b/76413401): remove this method and just create the final field after the widget migration
  protected Tab createTabFromPool() {
    Tab tab = tabPool.acquire();
    if (tab == null) {
      tab = new Tab();
    }
    return tab;
  }

  // TODO(b/76413401): remove this method and just create the final field after the widget migration
  protected boolean releaseFromTabPool(Tab tab) {
    return tabPool.release(tab);
  }

  /**
   * Returns the number of tabs currently registered with the tab layout.
   *
   * @return Tab count
   */
  public int getTabCount() {
    return tabs.size();
  }

  /** Returns the tab at the specified index. */
  @Nullable
  public Tab getTabAt(int index) {
    return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
  }

  /**
   * Returns the position of the current selected tab.
   *
   * @return selected tab position, or {@code -1} if there isn't a selected tab.
   */
  public int getSelectedTabPosition() {
    return selectedTab != null ? selectedTab.getPosition() : -1;
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param tab The tab to remove
   */
  public void removeTab(@NonNull Tab tab) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    removeTabAt(tab.getPosition());
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param position Position of the tab to remove
   */
  public void removeTabAt(int position) {
    final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
    removeTabViewAt(position);

    final Tab removedTab = tabs.remove(position);
    if (removedTab != null) {
      removedTab.reset();
      releaseFromTabPool(removedTab);
    }

    final int newTabCount = tabs.size();
    int newIndicatorPosition = -1;
    for (int i = position; i < newTabCount; i++) {
      // If the current tab position is the indicator position, mark its new position as the new
      // indicator position.
      if (tabs.get(i).getPosition() == indicatorPosition) {
        newIndicatorPosition = i;
      }
      tabs.get(i).setPosition(i);
    }
    // Update the indicator position to the correct selected tab after refreshing tab positions.
    indicatorPosition = newIndicatorPosition;

    if (selectedTabPosition == position) {
      selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
    }
  }

  /** Remove all tabs from the tab layout and deselect the current tab. */
  public void removeAllTabs() {
    // Remove all the views
    for (int i = slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
      removeTabViewAt(i);
    }

    for (final Iterator<Tab> i = tabs.iterator(); i.hasNext(); ) {
      final Tab tab = i.next();
      i.remove();
      tab.reset();
      releaseFromTabPool(tab);
    }

    selectedTab = null;
  }

  /**
   * Set the behavior mode for the Tabs in this layout. The valid input options are:
   *
   * <ul>
   *   <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used with
   *       content that benefits from quick pivots between tabs.
   *   <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
   *       and can contain longer tab labels and a larger number of tabs. They are best used for
   *       browsing contexts in touch interfaces when users don't need to directly compare the tab
   *       labels. This mode is commonly used with a {@link androidx.viewpager.widget.ViewPager}.
   * </ul>
   *
   * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}.
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabMode
   */
  public void setTabMode(@Mode int mode) {
    if (mode != this.mode) {
      this.mode = mode;
      applyModeAndGravity();
      updateTabViews();
    }
  }

  /**
   * Returns the current mode used by this {@link TabLayout}.
   *
   * @see #setTabMode(int)
   */
  @Mode
  public int getTabMode() {
    return mode;
  }

  /**
   * Set the gravity to use when laying out the tabs.
   *
   * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabGravity
   */
  public void setTabGravity(@TabGravity int gravity) {
    if (tabGravity != gravity) {
      tabGravity = gravity;
      applyModeAndGravity();
    }
  }

  /**
   * The current gravity used for laying out tabs.
   *
   * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
   */
  @TabGravity
  public int getTabGravity() {
    return tabGravity;
  }

  /**
   * Set the indicator gravity used to align the tab selection indicator in the {@link TabLayout}.
   * You must set the indicator height via the custom indicator drawable's intrinsic height
   * (preferred), via the {@code tabIndicatorHeight} attribute (deprecated), or via {@link
   * #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the indicator will not be shown
   * unless gravity is set to {@link #INDICATOR_GRAVITY_STRETCH}, in which case it will ignore
   * indicator height and stretch across the entire height and width of the {@link TabLayout}. This
   * defaults to {@link #INDICATOR_GRAVITY_BOTTOM} if not set.
   *
   * @param indicatorGravity one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link
   *     #INDICATOR_GRAVITY_CENTER}, {@link #INDICATOR_GRAVITY_TOP}, or {@link
   *     #INDICATOR_GRAVITY_STRETCH}
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorGravity
   */
  public void setSelectedTabIndicatorGravity(@TabIndicatorGravity int indicatorGravity) {
    if (tabIndicatorGravity != indicatorGravity) {
      tabIndicatorGravity = indicatorGravity;
      slidingTabIndicator.postInvalidateOnAnimation();
    }
  }

  /**
   * Get the current indicator gravity used to align the tab selection indicator in the {@link
   * TabLayout}.
   *
   * @return one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link #INDICATOR_GRAVITY_CENTER}, {@link
   *     #INDICATOR_GRAVITY_TOP}, or {@link #INDICATOR_GRAVITY_STRETCH}
   */
  @TabIndicatorGravity
  public int getTabIndicatorGravity() {
    return tabIndicatorGravity;
  }

  /**
   * Set the mode by which the selection indicator should animate when moving between destinations.
   *
   * <p>Defaults to {@link #INDICATOR_ANIMATION_MODE_LINEAR}. Changing this is useful as a stylistic
   * choice.
   *
   * @param tabIndicatorAnimationMode one of {@link #INDICATOR_ANIMATION_MODE_LINEAR} or {@link
   *     #INDICATOR_ANIMATION_MODE_ELASTIC}
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorAnimationMode
   * @see #getTabIndicatorAnimationMode()
   */
  public void setTabIndicatorAnimationMode(
      @TabIndicatorAnimationMode int tabIndicatorAnimationMode) {
    this.tabIndicatorAnimationMode = tabIndicatorAnimationMode;
    switch (tabIndicatorAnimationMode) {
      case INDICATOR_ANIMATION_MODE_LINEAR:
        this.tabIndicatorInterpolator = new TabIndicatorInterpolator();
        break;
      case INDICATOR_ANIMATION_MODE_ELASTIC:
        this.tabIndicatorInterpolator = new ElasticTabIndicatorInterpolator();
        break;
      case INDICATOR_ANIMATION_MODE_FADE:
        this.tabIndicatorInterpolator = new FadeTabIndicatorInterpolator();
        break;
      default:
        throw new IllegalArgumentException(
            tabIndicatorAnimationMode + " is not a valid TabIndicatorAnimationMode");
    }
  }

  /**
   * Get the current indicator animation mode used to animate the selection indicator between
   * destinations.
   *
   * @return one of {@link #INDICATOR_ANIMATION_MODE_LINEAR} or {@link
   *     #INDICATOR_ANIMATION_MODE_ELASTIC}
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorAnimationMode
   * @see #setTabIndicatorAnimationMode(int)
   */
  @TabIndicatorAnimationMode
  public int getTabIndicatorAnimationMode() {
    return tabIndicatorAnimationMode;
  }

  /**
   * Enable or disable option to fit the tab selection indicator to the full width of the tab item
   * rather than to the tab item's content.
   *
   * <p>Defaults to true. If set to false and the tab item has a text label, the selection indicator
   * width will be set to the width of the text label. If the tab item has no text label, but does
   * have an icon, the selection indicator width will be set to the icon. If the tab item has
   * neither of these, or if the calculated width is less than a minimum width value, the selection
   * indicator width will be set to the minimum width value.
   *
   * @param tabIndicatorFullWidth Whether or not to fit selection indicator width to full width of
   *     the tab item
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorFullWidth
   * @see #isTabIndicatorFullWidth()
   */
  public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
    this.tabIndicatorFullWidth = tabIndicatorFullWidth;
    slidingTabIndicator.jumpIndicatorToSelectedPosition();
    ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
  }

  /**
   * Get whether or not selection indicator width is fit to full width of the tab item, or fit to
   * the tab item's content.
   *
   * @return whether or not selection indicator width is fit to the full width of the tab item
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabIndicatorFullWidth
   * @see #setTabIndicatorFullWidth(boolean)
   */
  public boolean isTabIndicatorFullWidth() {
    return tabIndicatorFullWidth;
  }

  /**
   * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
   * underneath tab icons.
   *
   * @see #isInlineLabel()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabInlineLabel
   */
  public void setInlineLabel(boolean inline) {
    if (inlineLabel != inline) {
      inlineLabel = inline;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateOrientation();
        }
      }
      applyModeAndGravity();
    }
  }

  /**
   * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
   * underneath tab icons.
   *
   * @param inlineResourceId Resource ID for boolean inline flag
   * @see #isInlineLabel()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabInlineLabel
   */
  public void setInlineLabelResource(@BoolRes int inlineResourceId) {
    setInlineLabel(getResources().getBoolean(inlineResourceId));
  }

  /**
   * Returns whether tab labels will be displayed inline with tab icons, or if they will be
   * displayed underneath tab icons.
   *
   * @see #setInlineLabel(boolean)
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabInlineLabel
   */
  public boolean isInlineLabel() {
    return inlineLabel;
  }

  /**
   * Set whether this {@link TabLayout} will have an unbounded ripple effect or if ripple will be
   * bound to the tab item size.
   *
   * <p>Defaults to false.
   *
   * @see #hasUnboundedRipple()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabUnboundedRipple
   */
  public void setUnboundedRipple(boolean unboundedRipple) {
    if (this.unboundedRipple != unboundedRipple) {
      this.unboundedRipple = unboundedRipple;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  /**
   * Set whether this {@link TabLayout} will have an unbounded ripple effect or if ripple will be
   * bound to the tab item size. Defaults to false.
   *
   * @param unboundedRippleResourceId Resource ID for boolean unbounded ripple value
   * @see #hasUnboundedRipple()
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabUnboundedRipple
   */
  public void setUnboundedRippleResource(@BoolRes int unboundedRippleResourceId) {
    setUnboundedRipple(getResources().getBoolean(unboundedRippleResourceId));
  }

  /**
   * Returns whether this {@link TabLayout} has an unbounded ripple effect, or if ripple is bound to
   * the tab item size.
   *
   * @see #setUnboundedRipple(boolean)
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabUnboundedRipple
   */
  public boolean hasUnboundedRipple() {
    return unboundedRipple;
  }

  /**
   * Sets the text colors for the different states (normal, selected) used for the tabs.
   *
   * @see #getTabTextColors()
   */
  public void setTabTextColors(@Nullable ColorStateList textColor) {
    if (tabTextColors != textColor) {
      tabTextColors = textColor;
      updateAllTabs();
    }
  }

  /** Gets the text colors for the different states (normal, selected) used for the tabs. */
  @Nullable
  public ColorStateList getTabTextColors() {
    return tabTextColors;
  }

  /**
   * Sets the text colors for the different states (normal, selected) used for the tabs.
   *
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabTextColor
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabSelectedTextColor
   */
  public void setTabTextColors(int normalColor, int selectedColor) {
    setTabTextColors(createColorStateList(normalColor, selectedColor));
  }

  /**
   * Sets the icon tint for the different states (normal, selected) used for the tabs.
   *
   * @see #getTabIconTint()
   */
  public void setTabIconTint(@Nullable ColorStateList iconTint) {
    if (tabIconTint != iconTint) {
      tabIconTint = iconTint;
      updateAllTabs();
    }
  }

  /**
   * Sets the icon tint resource for the different states (normal, selected) used for the tabs.
   *
   * @param iconTintResourceId A color resource to use as icon tint.
   * @see #getTabIconTint()
   */
  public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
    setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  /** Gets the icon tint for the different states (normal, selected) used for the tabs. */
  @Nullable
  public ColorStateList getTabIconTint() {
    return tabIconTint;
  }

  /**
   * Returns the ripple color for this TabLayout.
   *
   * @return the color (or ColorStateList) used for the ripple
   * @see #setTabRippleColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getTabRippleColor() {
    return tabRippleColorStateList;
  }

  /**
   * Sets the ripple color for this TabLayout.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
   * than a ripple.
   *
   * @param color color (or ColorStateList) to use for the ripple
   * @attr ref com.google.android.material.R.styleable#TabLayout_tabRippleColor
   * @see #getTabRippleColor()
   */
  public void setTabRippleColor(@Nullable ColorStateList color) {
    if (tabRippleColorStateList != color) {
      tabRippleColorStateList = color;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  /**
   * Sets the ripple color resource for this TabLayout.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
   * than a ripple.
   *
   * @param tabRippleColorResourceId A color resource to use as ripple color.
   * @see #getTabRippleColor()
   */
  public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
    setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
  }

  /**
   * Returns the selection indicator drawable for this TabLayout.
   *
   * @return The drawable used as the tab selection indicator, if set.
   * @see #setSelectedTabIndicator(Drawable)
   * @see #setSelectedTabIndicator(int)
   */
  @NonNull
  public Drawable getTabSelectedIndicator() {
    return tabSelectedIndicator;
  }

  /**
   * Sets the selection indicator for this TabLayout. By default, this is a line along the bottom of
   * the tab. If {@code tabIndicatorColor} is specified via the TabLayout's style or via {@link
   * #setSelectedTabIndicatorColor(int)} the selection indicator will be tinted that color.
   * Otherwise, it will use the colors specified in the drawable.
   *
   * <p>Setting the indicator drawable to null will cause {@link TabLayout} to use the default,
   * {@link GradientDrawable} line indicator.
   *
   * @param tabSelectedIndicator A drawable to use as the selected tab indicator.
   * @see #setSelectedTabIndicatorColor(int)
   * @see #setSelectedTabIndicator(int)
   */
  public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
    if (tabSelectedIndicator == null) {
      tabSelectedIndicator = new GradientDrawable();
    }
    this.tabSelectedIndicator = DrawableCompat.wrap(tabSelectedIndicator).mutate();
    DrawableUtils.setTint(this.tabSelectedIndicator, tabSelectedIndicatorColor);
    int indicatorHeight =
        tabIndicatorHeight == SELECTED_INDICATOR_HEIGHT_DEFAULT
            ? this.tabSelectedIndicator.getIntrinsicHeight()
            : tabIndicatorHeight;
    slidingTabIndicator.setSelectedIndicatorHeight(indicatorHeight);
  }

  /**
   * Sets the drawable resource to use as the selection indicator for this TabLayout. By default,
   * this is a line along the bottom of the tab. If {@code tabIndicatorColor} is specified via the
   * TabLayout's style or via {@link #setSelectedTabIndicatorColor(int)} the selection indicator
   * will be tinted that color. Otherwise, it will use the colors specified in the drawable.
   *
   * @param tabSelectedIndicatorResourceId A drawable resource to use as the selected tab indicator.
   * @see #setSelectedTabIndicatorColor(int)
   * @see #setSelectedTabIndicator(Drawable)
   */
  public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
    if (tabSelectedIndicatorResourceId != 0) {
      setSelectedTabIndicator(
          AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
    } else {
      setSelectedTabIndicator(null);
    }
  }

  /**
   * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewPager}.
   *
   * <p>This is the same as calling {@link #setupWithViewPager(ViewPager, boolean)} with
   * auto-refresh enabled.
   *
   * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
   */
  public void setupWithViewPager(@Nullable ViewPager viewPager) {
    setupWithViewPager(viewPager, true);
  }

  /**
   * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewPager}.
   *
   * <p>This method will link the given ViewPager and this TabLayout together so that changes in one
   * are automatically reflected in the other. This includes scroll state changes and clicks. The
   * tabs displayed in this layout will be populated from the ViewPager adapter's page titles.
   *
   * <p>If {@code autoRefresh} is {@code true}, any changes in the {@link PagerAdapter} will trigger
   * this layout to re-populate itself from the adapter's titles.
   *
   * <p>If the given ViewPager is non-null, it needs to already have a {@link PagerAdapter} set.
   *
   * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
   * @param autoRefresh whether this layout should refresh its contents if the given ViewPager's
   *     content changes
   */
  public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
    setupWithViewPager(viewPager, autoRefresh, false);
  }

  private void setupWithViewPager(
      @Nullable final ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
    if (this.viewPager != null) {
      // If we've already been setup with a ViewPager, remove us from it
      if (pageChangeListener != null) {
        this.viewPager.removeOnPageChangeListener(pageChangeListener);
      }
      if (adapterChangeListener != null) {
        this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
      }
    }

    if (currentVpSelectedListener != null) {
      // If we already have a tab selected listener for the ViewPager, remove it
      removeOnTabSelectedListener(currentVpSelectedListener);
      currentVpSelectedListener = null;
    }

    if (viewPager != null) {
      this.viewPager = viewPager;

      // Add our custom OnPageChangeListener to the ViewPager
      if (pageChangeListener == null) {
        pageChangeListener = new TabLayoutOnPageChangeListener(this);
      }
      pageChangeListener.reset();
      viewPager.addOnPageChangeListener(pageChangeListener);

      // Now we'll add a tab selected listener to set ViewPager's current item
      currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
      addOnTabSelectedListener(currentVpSelectedListener);

      final PagerAdapter adapter = viewPager.getAdapter();
      if (adapter != null) {
        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        setPagerAdapter(adapter, autoRefresh);
      }

      // Add a listener so that we're notified of any adapter changes
      if (adapterChangeListener == null) {
        adapterChangeListener = new AdapterChangeListener();
      }
      adapterChangeListener.setAutoRefresh(autoRefresh);
      viewPager.addOnAdapterChangeListener(adapterChangeListener);

      // Now update the scroll position to match the ViewPager's current item
      setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    } else {
      // We've been given a null ViewPager so we need to clear out the internal state,
      // listeners and observers
      this.viewPager = null;
      setPagerAdapter(null, false);
    }

    setupViewPagerImplicitly = implicitSetup;
  }

  /**
   * @deprecated Use {@link #setupWithViewPager(ViewPager)} to link a TabLayout with a ViewPager
   *     together. When that method is used, the TabLayout will be automatically updated when the
   *     {@link PagerAdapter} is changed.
   */
  @Deprecated
  public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
    setPagerAdapter(adapter, false);
  }

  void updateViewPagerScrollState(int scrollState) {
    this.viewPagerScrollState = scrollState;
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    // Only delay the pressed state if the tabs can scroll
    return getTabScrollRange() > 0;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    //Sesl
    TabView tabView;
    for (int i = 0; i < getTabCount(); i++) {
      Tab tab = getTabAt(i);
      if (tab != null && (tabView = tab.view) != null) {
        if (tabView.mMainTabTouchBackground != null) {
          tab.view.mMainTabTouchBackground.setAlpha(0f);
        }
        if (tab.view.mIndicatorView != null) {
          if (getSelectedTabPosition() == i) {
            tab.view.mIndicatorView.setShow();
          } else {
            tab.view.mIndicatorView.setHide();
          }
        }
      }
    }
    //sesl

    MaterialShapeUtils.setParentAbsoluteElevation(this);

    if (viewPager == null) {
      // If we don't have a ViewPager already, check if our parent is a ViewPager to
      // setup with it automatically
      final ViewParent vp = getParent();
      if (vp instanceof ViewPager) {
        // If we have a ViewPager parent and we've been added as part of its decor, let's
        // assume that we should automatically setup to display any titles
        setupWithViewPager((ViewPager) vp, true, true);
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (setupViewPagerImplicitly) {
      // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
      setupWithViewPager(null);
      setupViewPagerImplicitly = false;
    }
  }

  private int getTabScrollRange() {
    return Math.max(
        0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
  }

  void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
    if (pagerAdapter != null && pagerAdapterObserver != null) {
      // If we already have a PagerAdapter, unregister our observer
      pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
    }

    pagerAdapter = adapter;

    if (addObserver && adapter != null) {
      // Register our observer on the new adapter
      if (pagerAdapterObserver == null) {
        pagerAdapterObserver = new PagerAdapterObserver();
      }
      adapter.registerDataSetObserver(pagerAdapterObserver);
    }

    // Finally make sure we reflect the new adapter
    populateFromPagerAdapter();
  }

  void populateFromPagerAdapter() {
    removeAllTabs();

    if (pagerAdapter != null) {
      final int adapterCount = pagerAdapter.getCount();
      for (int i = 0; i < adapterCount; i++) {
        addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
      }

      // Make sure we reflect the currently set ViewPager item
      if (viewPager != null && adapterCount > 0) {
        final int curItem = viewPager.getCurrentItem();
        if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
          selectTab(getTabAt(curItem), true, true);//sesl
        }
      }
    }
  }

  private void updateAllTabs() {
    for (int i = 0, z = tabs.size(); i < z; i++) {
      tabs.get(i).updateView();
    }
  }

  @NonNull
  private TabView createTabView(@NonNull final Tab tab) {
    TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
    if (tabView == null) {
      tabView = new TabView(getContext());
    }
    if (tabView.mMainTabTouchBackground != null) {
      tabView.mMainTabTouchBackground.setAlpha(0f);//sesl
    }
    //Sesl
    if (tabView.mTabParentView != null) {
      tabView.mTabParentView.removeView(tabView.mDotBadgeView);
      tabView.mTabParentView.removeView(tabView.mNBadgeView);
      tabView.mDotBadgeView = null;
      tabView.mNBadgeView = null;
    }
    //sesl
    tabView.setTab(tab);
    tabView.setFocusable(true);
    tabView.setMinimumWidth(getTabMinWidth());
    if (TextUtils.isEmpty(tab.contentDesc)) {
      tabView.setContentDescription(tab.text);
    } else {
      tabView.setContentDescription(tab.contentDesc);
    }
    return tabView;
  }

  private void configureTab(@NonNull Tab tab, int position) {
    tab.setPosition(position);
    tabs.add(position, tab);

    final int count = this.tabs.size();
    int newIndicatorPosition = -1;
    for (int i = position + 1; i < count; i++) {
      // If the current tab position is the indicator position, mark its new position as the new
      // indicator position.
      if (tabs.get(i).getPosition() == indicatorPosition) {
        newIndicatorPosition = i;
      }
      tabs.get(i).setPosition(i);
    }
    indicatorPosition = newIndicatorPosition;
  }

  private void addTabView(@NonNull Tab tab) {
    final TabView tabView = tab.view;
    tabView.setSelected(false);
    tabView.setActivated(false);
    tabView.setStateListAnimator(
        AnimatorInflater.loadStateListAnimator(getContext(),
            androidx.appcompat.R.animator.sesl_recoil_button_selector));//sesl
    slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
  }

  @Override
  public void addView(View child) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  private void addViewInternal(final View child) {
    if (child instanceof TabItem) {
      addTabFromItemView((TabItem) child);
    } else {
      throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
    }
  }

  @NonNull
  private LinearLayout.LayoutParams createLayoutParamsForTabs() {
    final LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    updateTabViewLayoutParams(lp);
    return lp;
  }

  private void updateTabViewLayoutParams(@NonNull LinearLayout.LayoutParams lp) {
    if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
      lp.width = 0;
      lp.weight = 1;
    } else {
      lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
      lp.weight = 0;
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
            /* rowCount= */ 1,
            /* columnCount= */ getTabCount(),
            /* hierarchical= */ false,
            /* selectionMode = */ CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

//  @Override
//  protected void onDraw(@NonNull Canvas canvas) {
//    // Draw tab background layer for each tab item
//    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
//      View tabView = slidingTabIndicator.getChildAt(i);
//      if (tabView instanceof TabView) {
//        ((TabView) tabView).drawBackground(canvas);
//      }
//    }
//
//    super.onDraw(canvas);
//  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // If we have a MeasureSpec which allows us to decide our height, try and use the default
    // height
    final int idealHeight = Math.round(ViewUtils.dpToPx(getContext(), getDefaultHeight()));
    switch (MeasureSpec.getMode(heightMeasureSpec)) {
      case MeasureSpec.AT_MOST:
        if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
          getChildAt(0).setMinimumHeight(idealHeight);
        }
        break;
      case MeasureSpec.UNSPECIFIED:
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                idealHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        break;
      default:
        break;
    }

    final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
    if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
      // If we don't have an unspecified width spec, use the given size to calculate
      // the max tab width
      tabMaxWidth =
          requestedTabMaxWidth > 0
              ? requestedTabMaxWidth
              : (int) (specWidth - ViewUtils.dpToPx(getContext(), TAB_MIN_WIDTH_MARGIN));
    }

    // Now super measure itself using the (possibly) modified height spec
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getChildCount() == 1) {
      // If we're in fixed mode then we need to make sure the tab strip is the same width as us
      // so we don't scroll
      final View child = getChildAt(0);
      boolean remeasure = false;

      switch (mode) {
        case MODE_AUTO:
        case MODE_SCROLLABLE:
          // We only need to resize the child if it's smaller than us. This is similar
          // to fillViewport
          remeasure = child.getMeasuredWidth() < getMeasuredWidth();
          break;
        case MODE_FIXED:
          // Resize the child so that it doesn't scroll
          remeasure = child.getMeasuredWidth() != getMeasuredWidth();
          break;
        case SESL_MODE_FIXED_AUTO://sesl
        case SESL_MODE_WEIGHT_AUTO://sesl
          remeasure = true;
          break;
      }

      if (remeasure) {
        // Re-measure the child with a widthSpec set to be exactly our measure width
        int childHeightMeasureSpec =
            getChildMeasureSpec(
                heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(),
                child.getLayoutParams().height);

        int childWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      }

      //Sesl
      checkOverScreen();
      int childMeasuredWidth;
      int measuredWidth;
      if (mIsOverScreen
          && (childMeasuredWidth = getChildAt(0).getMeasuredWidth()) < (measuredWidth = getMeasuredWidth())) {
        setPaddingRelative((measuredWidth - childMeasuredWidth) / 2, 0, 0, 0);
      } else {
        setPaddingRelative(0, 0, 0, 0);
      }
      //sesl
    }
  }

  private void removeTabViewAt(int position) {
    final TabView view = (TabView) slidingTabIndicator.getChildAt(position);
    slidingTabIndicator.removeViewAt(position);
    if (view != null) {
      view.reset();
      tabViewPool.release(view);
    }
    requestLayout();
  }

  private void animateToTab(int newPosition) {
    if (newPosition == Tab.INVALID_POSITION) {
      return;
    }

    if (getWindowToken() == null
        || !isLaidOut()
        || slidingTabIndicator.childrenNeedLayout()) {
      // If we don't have a window token, or we haven't been laid out yet just draw the new
      // position now
      setScrollPosition(newPosition, 0f, true);
      return;
    }

    final int startScrollX = getScrollX();
    final int targetScrollX = calculateScrollXForTab(newPosition, 0);

    if (startScrollX != targetScrollX) {
      ensureScrollAnimator();

      scrollAnimator.setIntValues(startScrollX, targetScrollX);
      scrollAnimator.start();
    }

    // Now animate the indicator
    slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
  }

  private void ensureScrollAnimator() {
    if (scrollAnimator == null) {
      scrollAnimator = new ValueAnimator();
      scrollAnimator.setInterpolator(tabIndicatorTimeInterpolator);
      scrollAnimator.setDuration(tabIndicatorAnimationDuration);
      scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
          scrollTo((int) animator.getAnimatedValue(), 0);
        }
      });
    }
  }

  void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
    ensureScrollAnimator();
    scrollAnimator.addListener(listener);
  }

  /**
   * Called when a tab is selected. Unselects all other tabs in the TabLayout.
   *
   * @param position Position of the selected tab.
   */
  private void setSelectedTabView(int position, boolean skipIndicatorVI/*sesl*/) {
    final int tabCount = slidingTabIndicator.getChildCount();
    if (position < tabCount) {
      for (int i = 0; i < tabCount; i++) {
        final View child = slidingTabIndicator.getChildAt(i);
        // Update the tab view if it needs to be updated (eg. it's newly selected and it is not
        // yet selected, or it is selected and something else was selected).
        if ((i == position && !child.isSelected()) || (i != position && child.isSelected())) {
          child.setSelected(i == position);
          child.setActivated(i == position);
          if (child instanceof TabView) {
            ((TabView) child).updateTab();
          }
          continue;
        }
        child.setSelected(i == position);
        child.setActivated(i == position);
      }

      //Sesl
      tabs.get(position).view.setSelected(true);

      for (int i = 0; i < getTabCount(); i++) {
        final TabView tabView = tabs.get(i).view;

        if (i == position) {
          if (tabView.textView != null) {
            startTextColorChangeAnimation(
                tabView.textView, getSelectedTabTextColor());
            tabView.textView.setTypeface(mBoldTypeface);
            tabView.textView.setSelected(true);
          }
          if (mDepthStyle == DEPTH_TYPE_SUB && tabView.mSubTextView != null) {
            startTextColorChangeAnimation(
                tabView.mSubTextView, seslGetSelectedTabSubTextColor());
            tabView.mSubTextView.setSelected(true);
          }
          if (tabView.mIndicatorView != null) {
            if (!skipIndicatorVI) {
              tabs.get(i).view.mIndicatorView.setReleased();
            } else if (tabView.mIndicatorView.getAlpha() != 1.0f) {
              tabView.mIndicatorView.setShow();
            }
          }
        } else {
          if (tabView.mIndicatorView != null) {
            tabView.mIndicatorView.setHide();
          }
          if (tabView.textView != null) {
            tabView.textView.setTypeface(mNormalTypeface);
            startTextColorChangeAnimation(
                tabView.textView, tabTextColors.getDefaultColor());
            tabView.textView.setSelected(false);
          }
          if (mDepthStyle == DEPTH_TYPE_SUB && tabView.mSubTextView != null) {
            startTextColorChangeAnimation(
                tabView.mSubTextView, mSubTabSubTextColors.getDefaultColor());
            tabView.mSubTextView.setSelected(false);
          }
        }
      }
      //sesl
    }
  }

  /**
   * Selects the given tab.
   *
   * @param tab The tab to select, or {@code null} to select none.
   * @see #selectTab(Tab, boolean)
   */
  public void selectTab(@Nullable Tab tab) {
    selectTab(tab, true);
  }

  /**
   * Selects the given tab. Will always animate to the selected tab if the current tab is
   * reselected, regardless of the value of {@code updateIndicator}.
   *
   * @param tab The tab to select, or {@code null} to select none.
   * @param updateIndicator Whether to update the indicator.
   * @see #selectTab(Tab)
   */
  public void selectTab(@Nullable final Tab tab, boolean updateIndicator) {
    selectTab(tab, updateIndicator, true);
  }

  /**
   * Selects the given tab. Will always animate to the selected tab if the current tab is
   * reselected, regardless of the value of {@code updateIndicator}.
   *
   * @see #selectTab(Tab, boolean)
   */
  private void selectTab(@Nullable final Tab tab, boolean updateIndicator,
                         boolean skipIndicatorVI) {
    if (tab != null && !tab.view.isEnabled()) {
      if (viewPager != null) {
        viewPager.setCurrentItem(getSelectedTabPosition());
        return;
      }
    }

    final Tab currentTab = selectedTab;

    if (currentTab == tab) {
      if (currentTab != null) {
        dispatchTabReselected(tab);
        animateToTab(tab.getPosition());
      }
    } else {
      final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
      if (updateIndicator) {
        if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
            && newPosition != Tab.INVALID_POSITION) {
          // If we don't currently have a tab, just draw the indicator
          setScrollPosition(newPosition, 0f, true);
        } else {
          animateToTab(newPosition);
        }
        if (newPosition != Tab.INVALID_POSITION) {
          setSelectedTabView(newPosition, skipIndicatorVI);
        }
      }
      // Setting selectedTab before dispatching 'tab unselected' events, so that currentTab's state
      // will be interpreted as unselected
      selectedTab = tab;
      // If the current tab is still attached to the TabLayout.
      if (currentTab != null && currentTab.parent != null) {
        dispatchTabUnselected(currentTab);
      }
      if (tab != null) {
        dispatchTabSelected(tab);
      }
    }
  }

  private void dispatchTabSelected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabSelected(tab);
    }
  }

  private void dispatchTabUnselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabUnselected(tab);
    }
  }

  private void dispatchTabReselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabReselected(tab);
    }
  }

  private int calculateScrollXForTab(int position, float positionOffset) {
    if (mode == MODE_SCROLLABLE || mode == MODE_AUTO
        || mode == SESL_MODE_FIXED_AUTO || mode == SESL_MODE_WEIGHT_AUTO) {//sesl
      final View selectedChild = slidingTabIndicator.getChildAt(position);
      final View nextChild =
          position + 1 < slidingTabIndicator.getChildCount()
              ? slidingTabIndicator.getChildAt(position + 1)
              : null;
      final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
      final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

      // base scroll amount: places center of tab in center of parent
      int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
      // offset amount: fraction of the distance between centers of tabs
      int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);
      return getLayoutDirection() == LAYOUT_DIRECTION_LTR
          ? scrollBase + scrollOffset
          : scrollBase - scrollOffset;
    }
    return 0;
  }

  private void applyModeAndGravity() {
//    int paddingStart = 0;
//    if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
//      // If we're scrollable, or fixed at start, inset using padding
//      paddingStart = Math.max(0, contentInsetStart - tabPaddingStart);
//    }
    slidingTabIndicator.setPaddingRelative(0/*paddingStart*/, 0, 0, 0);

    switch (mode) {
      case MODE_AUTO:
      case MODE_FIXED:
        if (tabGravity == GRAVITY_START) {
          Log.w(
              LOG_TAG,
              "GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be"
                  + " used instead");
        }
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case MODE_SCROLLABLE:
      case SESL_MODE_FIXED_AUTO://sesl
      case SESL_MODE_WEIGHT_AUTO://sesl
        applyGravityForModeScrollable(tabGravity);
        break;
    }

    updateTabViews(true);
  }

  private void applyGravityForModeScrollable(int tabGravity) {
    switch (tabGravity) {
      case GRAVITY_CENTER:
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case GRAVITY_FILL:
        Log.w(
            LOG_TAG,
            "MODE_SCROLLABLE/SESL_MODE_FIXED_AUTO/SESL_MODE_WEIGHT_AUTO + GRAVITY_FILL is not supported, GRAVITY_START will be used"
                + " instead");
        // Fall through
      case GRAVITY_START:
        slidingTabIndicator.setGravity(GravityCompat.START);
        break;
      default:
        break;
    }
  }

  void updateTabViews(final boolean requestLayout) {
    int childCount = slidingTabIndicator.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View childAt = slidingTabIndicator.getChildAt(i);
      childAt.setMinimumWidth(getTabMinWidth());
      updateTabViewLayoutParams((LinearLayout.LayoutParams) childAt.getLayoutParams());
      if (requestLayout) {
        childAt.requestLayout();
      }
    }
    updateTabViews();
  }

  private void updateTabViews() {
    int tabsCount = tabs.size();
    for (int i = 0; i < tabsCount; i++) {
      TabView tabView = tabs.get(i).view;
      updateBadgePosition(tabView);
      setShowButtonShape(tabView);
    }
  }

  /** A tab in this layout. Instances can be created via {@link #newTab()}. */
  // TODO(b/76413401): make class final after the widget migration is finished
  public static class Tab {

    /**
     * An invalid position for a tab.
     *
     * @see #getPosition()
     */
    public static final int INVALID_POSITION = -1;

    @Nullable private Object tag;
    @Nullable private Drawable icon;
    @Nullable private CharSequence text;
    private CharSequence subText;//sesl
    // This represents the content description that has been explicitly set on the Tab or TabItem
    // in XML or through #setContentDescription. If the content description is empty, text should
    // be used as the content description instead, but contentDesc should remain empty.
    @Nullable private CharSequence contentDesc;
    private int position = INVALID_POSITION;
    @Nullable private View customView;
    private @LabelVisibility int labelVisibilityMode = TAB_LABEL_VISIBILITY_LABELED;

    // TODO(b/76413401): make package private after the widget migration is finished
    @Nullable public TabLayout parent;
    // TODO(b/76413401): make package private after the widget migration is finished
    @NonNull public TabView view;
    private int id = NO_ID;

    // TODO(b/76413401): make package private constructor after the widget migration is finished
    public Tab() {
      // Private constructor
    }

    /** @return This Tab's tag object. */
    @Nullable
    public Object getTag() {
      return tag;
    }

    /**
     * Give this Tab an arbitrary object to hold for later use.
     *
     * @param tag Object to store
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setTag(@Nullable Object tag) {
      this.tag = tag;
      return this;
    }

    /**
     * Give this tab an id, useful for testing.
     *
     * <p>Do not rely on this if using {@link TabLayout#setupWithViewPager(ViewPager)}
     *
     * @param id, unique id for this tab
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setId(int id) {
      this.id = id;
      if (view != null) {
        view.setId(id);
      }
      return this;
    }

    /** Returns the id for this tab, {@code View.NO_ID} if not set. */
    public int getId() {
      return id;
    }

    /**
     * Returns the custom view used for this tab.
     *
     * @see #setCustomView(View)
     * @see #setCustomView(int)
     */
    @Nullable
    public View getCustomView() {
      return customView;
    }

    /**
     * Set a custom view to be used for this tab.
     *
     * <p>If the provided view contains a {@link TextView} with an ID of {@link android.R.id#text1}
     * then that will be updated with the value given to {@link #setText(CharSequence)}. Similarly,
     * if this layout contains an {@link ImageView} with ID {@link android.R.id#icon} then it will
     * be updated with the value given to {@link #setIcon(Drawable)}.
     *
     * @param view Custom view to be used as a tab.
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setCustomView(@Nullable View view) {
      if (this.view.textView != null) {
        this.view.removeAllViews();//sesl
      }
      customView = view;
      updateView();
      return this;
    }

    /**
     * Set a custom view to be used for this tab.
     *
     * <p>If the inflated layout contains a {@link TextView} with an ID of {@link
     * android.R.id#text1} then that will be updated with the value given to {@link
     * #setText(CharSequence)}. Similarly, if this layout contains an {@link ImageView} with ID
     * {@link android.R.id#icon} then it will be updated with the value given to {@link
     * #setIcon(Drawable)}.
     *
     * @param resId A layout resource to inflate and use as a custom tab view
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setCustomView(@LayoutRes int resId) {
      final LayoutInflater inflater = LayoutInflater.from(view.getContext());
      return setCustomView(inflater.inflate(resId, view, false));
    }

    /**
     * Return the icon associated with this tab.
     *
     * @return The tab's icon
     */
    @Nullable
    public Drawable getIcon() {
      return icon;
    }

    /**
     * Return the current position of this tab in the tab layout.
     *
     * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in the
     *     tab layout.
     */
    public int getPosition() {
      return position;
    }

    void setPosition(int position) {
      this.position = position;
    }

    /**
     * Return the text of this tab.
     *
     * @return The tab's text
     */
    @Nullable
    public CharSequence getText() {
      return text;
    }

    /**
     * Set the icon displayed on this tab.
     *
     * @param icon The drawable to use as an icon
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setIcon(@Nullable Drawable icon) {
      this.icon = icon;
      if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
        parent.updateTabViews(true);
      }
      updateView();
      if (BadgeUtils.USE_COMPAT_PARENT
          && view.hasBadgeDrawable()
          && view.badgeDrawable.isVisible()) {
        // Invalidate the TabView if icon visibility has changed and a badge is displayed.
        view.invalidate();
      }
      return this;
    }

    /**
     * Set the icon displayed on this tab.
     *
     * @param resId A resource ID referring to the icon that should be displayed
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setIcon(@DrawableRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setIcon(AppCompatResources.getDrawable(parent.getContext(), resId));
    }

    /**
     * Set the text displayed on this tab. Text may be truncated if there is not room to display the
     * entire string.
     *
     * @param text The text to display
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setText(@Nullable CharSequence text) {
      if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
        // If no content description has been set, use the text as the content description of the
        // TabView. If the text is null, don't update the content description.
        view.setContentDescription(text);
      }

      this.text = text;
      updateView();
      return this;
    }

    //Sesl
    public Tab seslSetSubText(@Nullable CharSequence text) {
      subText = text;
      updateView();
      return this;
    }

    @Nullable
    public CharSequence seslGetSubText() {
      return subText;
    }
    //sesl

    /**
     * Set the text displayed on this tab. Text may be truncated if there is not room to display the
     * entire string.
     *
     * @param resId A resource ID referring to the text that should be displayed
     * @return The current instance for call chaining
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setText(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setText(parent.getResources().getText(resId));
    }

    /**
     * Creates an instance of {@link BadgeDrawable} if none exists. Initializes (if needed) and
     * returns the associated instance of {@link BadgeDrawable}.
     *
     * @return an instance of BadgeDrawable associated with {@code Tab}.
     */
    @NonNull
    public BadgeDrawable getOrCreateBadge() {
      return view.getOrCreateBadge();
    }

    /**
     * Removes the {@link BadgeDrawable}. Do nothing if none exists. Consider changing the
     * visibility of the {@link BadgeDrawable} if you only want to hide it temporarily.
     */
    public void removeBadge() {
      view.removeBadge();
    }

    /**
     * Returns an instance of {@link BadgeDrawable} associated with this tab, null if none was
     * initialized.
     */
    @Nullable
    public BadgeDrawable getBadge() {
      return view.getBadge();
    }

    /**
     * Sets the visibility mode for the Labels in this Tab. The valid input options are:
     *
     * <ul>
     *   <li>{@link #TAB_LABEL_VISIBILITY_UNLABELED}: Tabs will appear without labels regardless of
     *       whether text is set.
     *   <li>{@link #TAB_LABEL_VISIBILITY_LABELED}: Tabs will appear labeled if text is set.
     * </ul>
     *
     * @param mode one of {@link #TAB_LABEL_VISIBILITY_UNLABELED} or {@link
     *     #TAB_LABEL_VISIBILITY_LABELED}.
     * @return The current instance for call chaining.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setTabLabelVisibility(@LabelVisibility int mode) {
      this.labelVisibilityMode = mode;
      if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
        parent.updateTabViews(true);
      }
      this.updateView();
      if (BadgeUtils.USE_COMPAT_PARENT
          && view.hasBadgeDrawable()
          && view.badgeDrawable.isVisible()) {
        // Invalidate the TabView if label visibility has changed and a badge is displayed.
        view.invalidate();
      }
      return this;
    }

    /**
     * Gets the visibility mode for the Labels in this Tab.
     *
     * @return the label visibility mode, one of {@link #TAB_LABEL_VISIBILITY_UNLABELED} or {@link
     *     #TAB_LABEL_VISIBILITY_LABELED}.
     * @see #setTabLabelVisibility(int)
     */
    @LabelVisibility
    public int getTabLabelVisibility() {
      return this.labelVisibilityMode;
    }

    /** Select this tab. Only valid if the tab has been added to the tab layout. */
    public void select() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      parent.selectTab(this);
    }

    /** Returns true if this tab is currently selected. */
    public boolean isSelected() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      int selectedPosition = parent.getSelectedTabPosition();
      return selectedPosition != INVALID_POSITION && selectedPosition == position;
    }

    /**
     * Set a description of this tab's content for use in accessibility support. If no content
     * description is provided the title will be used.
     *
     * @param resId A resource ID referring to the description text
     * @return The current instance for call chaining
     * @see #setContentDescription(CharSequence)
     * @see #getContentDescription()
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setContentDescription(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setContentDescription(parent.getResources().getText(resId));
    }

    /**
     * Set a description of this tab's content for use in accessibility support. If no content
     * description is provided the title will be used.
     *
     * @param contentDesc Description of this tab's content
     * @return The current instance for call chaining
     * @see #setContentDescription(int)
     * @see #getContentDescription()
     */
    @NonNull
    @CanIgnoreReturnValue
    public Tab setContentDescription(@Nullable CharSequence contentDesc) {
      this.contentDesc = contentDesc;
      updateView();
      return this;
    }

    /**
     * Gets a brief description of this tab's content for use in accessibility support.
     *
     * @return Description of this tab's content
     * @see #setContentDescription(CharSequence)
     * @see #setContentDescription(int)
     */
    @Nullable
    public CharSequence getContentDescription() {
      // This returns the view's content description instead of contentDesc because if the title
      // is used as a replacement for the content description, contentDesc will be empty.
      return (view == null) ? null : view.getContentDescription();
    }

    void updateView() {
      if (view != null) {
        view.update();
      }
    }

    void reset() {
      parent = null;
      view = null;
      tag = null;
      icon = null;
      id = NO_ID;
      text = null;
      contentDesc = null;
      position = INVALID_POSITION;
      customView = null;
      subText = null;//sesl
    }

    //Sesl
    @Nullable
    public TextView seslGetTextView() {
      if (customView == null && view != null) {
        return view.textView;
      }
      return null;
    }

    @Nullable
    public TextView seslGetSubTextView() {
      if (customView == null && view != null) {
        return view.mSubTextView;
      }
      return null;
    }
    //sesl
  }

  /** A {@link LinearLayout} containing {@link Tab} instances for use with {@link TabLayout}. */
  public final class TabView extends LinearLayout {
    // Sesl
    private TextView mDotBadgeView;
    private SeslAbsIndicatorView mIndicatorView;
    private View mMainTabTouchBackground;
    private TextView mNBadgeView;
    private ConstraintLayout mTabParentView;

    private int mIconSize;

    private boolean mIsCallPerformClick;
    private CharSequence mCustomRoleDescription = null;
    private TextView mSubTextView;
    // Sesl

    private Tab tab;
    private TextView textView;
    private ImageView iconView;
    @Nullable private View badgeAnchorView;
    @Nullable private BadgeDrawable badgeDrawable;

    @Nullable private View customView;
    @Nullable private TextView customTextView;
    @Nullable private ImageView customIconView;
    @Nullable private Drawable baseBackgroundDrawable;

    private int defaultMaxLines = 2;

    View.OnKeyListener mTabViewKeyListener = (v, keyCode, event) -> false;

    public TabView(@NonNull Context context) {
      super(context);
      updateBackgroundDrawable(context);
//      ViewCompat.setPaddingRelative(
//          this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom);
      setGravity(Gravity.CENTER);
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      setClickable(true);
//      ViewCompat.setPointerIcon(
//          this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));

      //Sesl
      setOnKeyListener(mTabViewKeyListener);
      if (mDepthStyle == DEPTH_TYPE_MAIN) {
        this.setPaddingRelative(0, tabPaddingTop, 0, tabPaddingBottom);
      }
      mIconSize = getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
      //sesl
    }

    private void updateBackgroundDrawable(Context context) {
      if (tabBackgroundResId != 0 && mDepthStyle != DEPTH_TYPE_SUB/*sesl*/) {
        baseBackgroundDrawable = AppCompatResources.getDrawable(context, tabBackgroundResId);
        if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
          baseBackgroundDrawable.setState(getDrawableState());
        }
        this.setBackground(baseBackgroundDrawable);//sesl
      } else {
        baseBackgroundDrawable = null;
      }

//      if (mMainTabTouchBackground != null) {
//        ViewCompat.setBackgroundTintList(mMainTabTouchBackground,tabRippleColorStateList);
//      }
//
//      Drawable background;
//      Drawable contentDrawable = new GradientDrawable();
//      ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);
//
//      if (tabRippleColorStateList != null) {
//        GradientDrawable maskDrawable = new GradientDrawable();
//        // TODO: Find a workaround for this. Currently on certain devices/versions,
//        // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
//        // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
//        maskDrawable.setCornerRadius(0.00001F);
//        maskDrawable.setColor(Color.WHITE);
//
//        ColorStateList rippleColor =
//            RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);
//
//        // TODO: Add support to RippleUtils.compositeRippleColorStateList for different ripple color
//        // for selected items vs non-selected items
//        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//          background =
//              new RippleDrawable(
//                  rippleColor,
//                  unboundedRipple ? null : contentDrawable,
//                  unboundedRipple ? null : maskDrawable);
//        } else {
//          Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
//          DrawableCompat.setTintList(rippleDrawable, rippleColor);
//          background = new LayerDrawable(new Drawable[] {contentDrawable, rippleDrawable});
//        }
//      } else {
//        background = contentDrawable;
//      }
//      ViewCompat.setBackground(this, background);
//      TabLayout.this.invalidate();
    }

//    /**
//     * Draw the background drawable specified by tabBackground attribute onto the canvas provided.
//     * This method will draw the background to the full bounds of this TabView. We provide a
//     * separate method for drawing this background rather than just setting this background on the
//     * TabView so that we can control when this background gets drawn. This allows us to draw the
//     * tab background underneath the TabLayout selection indicator, and then draw the TabLayout
//     * content (icons + labels) on top of the selection indicator.
//     *
//     * @param canvas canvas to draw the background on
//     */
//    private void drawBackground(@NonNull Canvas canvas) {
//      if (baseBackgroundDrawable != null) {
//        baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
//        baseBackgroundDrawable.draw(canvas);
//      }
//    }

    private void setBackgroundBounds() {
      if (baseBackgroundDrawable != null) {
        if (mMainTabTouchBackground == null) {
          baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
        }else{
          baseBackgroundDrawable.setBounds(
              getPaddingStart() + mMainTabTouchBackground.getLeft(),
              getPaddingTop() + mMainTabTouchBackground.getTop(),
              getPaddingEnd() + mMainTabTouchBackground.getRight(),
              getPaddingBottom() + mMainTabTouchBackground.getBottom());
        }
      }
    }

    @Override
    public boolean performClick() {
      //sesl
      if (mIsCallPerformClick) {
        mIsCallPerformClick = false;
        return true;
      }

      final boolean handled = super.performClick();

      if (tab != null) {
        if (!handled) {
          playSoundEffect(SoundEffectConstants.CLICK);
        }
        tab.select();
        return true;
      } else {
        return handled;
      }
    }

    @Override
    public void setSelected(final boolean selected) {
      if (!isEnabled()) return; //sesl

      //final boolean changed = isSelected() != selected;

      super.setSelected(selected);

//      if (changed && selected && Build.VERSION.SDK_INT < 16) {
//        // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
//        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
//      }

      // Always dispatch this to the child views, regardless of whether the value has
      // changed
      if (textView != null) {
        textView.setSelected(selected);
      }
      if (iconView != null) {
        iconView.setSelected(selected);
      }
      if (customView != null) {
        customView.setSelected(selected);
      }
      //Sesl
      if (mIndicatorView != null) {
        mIndicatorView.setSelected(selected);
        if (!TextUtils.isEmpty(tab != null ? tab.seslGetSubText() : null)) {
          mIndicatorView.setBackground(ContextCompat.getDrawable(getContext(),
                  SeslMisc.isLightTheme(getContext())
                          ? R.drawable.sesl_tablayout_subtab_subtext_indicator_background_light
                          : R.drawable.sesl_tablayout_subtab_subtext_indicator_background_dark));
        }
      }
      if (mSubTextView != null) {
        mSubTextView.setSelected(selected);
      }
      //sesl
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
      super.onInitializeAccessibilityNodeInfo(info);
      AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
      if (badgeDrawable != null && badgeDrawable.isVisible()) {
        //Sesl
        CharSequence contentDescription = getContentDescription();
        info.setContentDescription(
            contentDescription + "," + badgeDrawable.getContentDescription());
        //sesl
      }
      infoCompat.setCollectionItemInfo(
          CollectionItemInfoCompat.obtain(
              /* rowIndex= */ 0,
              /* rowSpan= */ 1,
              /* columnIndex= */ tab.getPosition(),
              /* columnSpan= */ 1,
              /* heading= */ false,
              /* selected= */ isSelected()));
      if (isSelected()) {
        infoCompat.setClickable(false);
        infoCompat.removeAction(AccessibilityActionCompat.ACTION_CLICK);
      }

      //sesl6 added
      if (mCustomRoleDescription == null) {
        mCustomRoleDescription = getResources().getString(R.string.item_view_role_description);
      }

      infoCompat.setRoleDescription(mCustomRoleDescription);

      //Sesl
      if (mDotBadgeView != null
          && mDotBadgeView.getVisibility() == VISIBLE
          && this.mDotBadgeView.getContentDescription() != null) {
        info.setContentDescription(getContentDescription() + "," + this.mDotBadgeView.getContentDescription());
        return;
      }

      if (mNBadgeView != null
          && mNBadgeView.getVisibility() == VISIBLE
          && this.mNBadgeView.getContentDescription() != null) {
        info.setContentDescription(getContentDescription() + "." + mNBadgeView.getContentDescription());
      }
      //sesl
    }

    @Override
    public void onMeasure(int widthMeasureSpec, final int origHeightMeasureSpec) {
      final int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
      final int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
      final int maxWidth = getTabMaxWidth();

      //Sesl
      if (mode == SESL_MODE_FIXED_AUTO || mode == SESL_MODE_WEIGHT_AUTO) {
        if (specWidthMode == MeasureSpec.UNSPECIFIED) {
          widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.UNSPECIFIED);
        } else if (specWidthMode == MeasureSpec.EXACTLY) {
          widthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidthSize, MeasureSpec.EXACTLY);
        }
      } else if (mRequestedTabWidth != INVALID_WIDTH) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mRequestedTabWidth, MeasureSpec.EXACTLY);
      } else if (maxWidth > 0 && (specWidthMode == 0 || specWidthSize > maxWidth)) {
        // If we have a max width and a given spec which is either unspecified or
        // larger than the max width, update the width spec using the same mode
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.AT_MOST);
      }
      //sesl

      // Now lets measure
      super.onMeasure(widthMeasureSpec, origHeightMeasureSpec);

      // We need to switch the text size based on whether the text is spanning 2 lines or not
      if (textView != null && customView == null/*sesl*/) {
        //Sesl
        checkMaxFontScale(textView, (int) tabTextSize);
        if (mDepthStyle == DEPTH_TYPE_SUB && mSubTextView != null) {
          checkMaxFontScale(mSubTextView, mSubTabTextSize);
        }

        float textSize = tabTextSize;
        int maxLines;
        if (iconView != null && iconView.getVisibility() == VISIBLE) {
          // If the icon view is being displayed, we limit the text to 1 line
          // and use the text size for subtext
          maxLines = 1;
          textSize = mSubTabTextSize;
        } else {
          //Otherwise we use the default max lines when icon view is not shown
          maxLines = this.defaultMaxLines;
          if (textView != null){
            if (textView.getLineCount() == 1) {
              //Use the default text size when we have only 1 line
              textSize = tabTextSize;
            }else{
              // Otherwise when we have text which wraps we reduce the text size
              textSize = tabTextMultiLineSize;
            }
          }
        }
        //sesl

        final float curTextSize = textView.getTextSize();
        final int curLineCount = textView.getLineCount();
        final int curMaxLines = TextViewCompat.getMaxLines(textView);

        if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)){
          // We've got a new text size and/or max lines...
          boolean updateTextView;

          if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
            // If we're in fixed mode, going up in text size and currently have 1 line
            // then it's very easy to get into an infinite recursion.
            // To combat that we check to see if the change in text size
            // will cause a line count change. If so, abort the size change and stick
            // to the smaller size.
            final Layout layout = textView.getLayout();
            updateTextView = !(layout == null
                || approximateLineWidth(layout, 0, textSize)
                > getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
          }else {
            updateTextView = true;
          }

          if (updateTextView) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            //Sesl
            checkMaxFontScale(textView, (int) textSize);
            if (mDepthStyle == DEPTH_TYPE_SUB && mSubTextView  != null) {
              checkMaxFontScale(mSubTextView, mSubTabTextSize);
            }
            //sesl
            textView.setMaxLines(maxLines);
            super.onMeasure(widthMeasureSpec, origHeightMeasureSpec);
          }
        }
      }

      //Sesl
      if (customTextView != null || mTabParentView == null
          || textView == null || tab == null) {
        return;
      }

      if (mode == MODE_SCROLLABLE && mDepthStyle == DEPTH_TYPE_SUB) {
        if (maxWidth > 0) {
          textView.measure(maxWidth, MeasureSpec.UNSPECIFIED);
        } else {
          textView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }

        ViewGroup.LayoutParams tabParentLP = mTabParentView.getLayoutParams();
        final int sideSpacing = (getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_side_space) * 2);
        tabParentLP.width = textView.getMeasuredWidth() + sideSpacing;

        mTabParentView.setLayoutParams(tabParentLP);
        super.onMeasure(MeasureSpec.makeMeasureSpec(tabParentLP.width, MeasureSpec.AT_MOST), origHeightMeasureSpec);
      }
      //sesl
    }

    void setTab(@Nullable final Tab tab) {
      if (tab != this.tab) {
        this.tab = tab;
        update();
      }
    }

    void reset() {
      setTab(null);
      setSelected(false);
    }

    final void updateTab() {
      final Tab tab = this.tab;
      final View custom = tab != null ? tab.getCustomView() : null;
      if (custom != null) {
        final ViewParent customParent = custom.getParent();
        if (customParent != this) {
          if (customParent != null) {
            ((ViewGroup) customParent).removeView(custom);
          }
          if (customView != null) {
            final ViewParent customViewParent = customView.getParent();
            if (customViewParent != null) {
              ((ViewGroup) customViewParent).removeView(customView);
            }
          }
          addView(custom);
        }
        customView = custom;
        if (this.textView != null) {
          this.textView.setVisibility(GONE);
        }
        if (this.iconView != null) {
          this.iconView.setVisibility(GONE);
          this.iconView.setImageDrawable(null);
        }
        //sesl
        if (mSubTextView != null) {
          mSubTextView.setVisibility(View.GONE);
        }

        customTextView = custom.findViewById(android.R.id.text1);
        if (customTextView != null) {
          defaultMaxLines = TextViewCompat.getMaxLines(customTextView);
        }
        customIconView = custom.findViewById(android.R.id.icon);
      } else {
        // We do not have a custom view. Remove one if it already exists
        if (customView != null) {
          removeView(customView);
          customView = null;
        }
        customTextView = null;
        customIconView = null;
      }
      //Sesl
      if (customView == null && tab != null) {
        if (mTabParentView == null) {
          Context context = getContext();
          if (mDepthStyle == DEPTH_TYPE_SUB) {
            mTabParentView = (ConstraintLayout) LayoutInflater.from(context)
                .inflate(R.layout.sesl_tabs_sub_tab_layout, this, false);
          } else {
            mTabParentView =  (ConstraintLayout) LayoutInflater.from(context)
                .inflate(R.layout.sesl_tabs_main_tab_layout, this, false);
            mMainTabTouchBackground = mTabParentView.findViewById(R.id.main_tab_touch_background);
            if (mMainTabTouchBackground != null && this.tab.icon == null) {
              final int touchBackgroundDrawableRes = SeslMisc.isLightTheme(context)
                  ? R.drawable.sesl_tablayout_maintab_touch_background_light
                  : R.drawable.sesl_tablayout_maintab_touch_background_dark;
              mMainTabTouchBackground.setBackground(ContextCompat
                  .getDrawable(context, touchBackgroundDrawableRes));
              mMainTabTouchBackground.setAlpha(0.0f);
            }
          }
        }

        if (mIndicatorView == null) {
          mIndicatorView = mTabParentView.findViewById(R.id.indicator);
        }

        if (mDepthStyle == DEPTH_TYPE_SUB) {
          if (mIndicatorView != null && mSubTabSelectedIndicatorColor != -1) {
            mIndicatorView.setSelectedIndicatorColor(mSubTabSelectedIndicatorColor);
          }
        } else {
          if (mIndicatorView != null) {
            mIndicatorView.setSelectedIndicatorColor(mTabSelectedIndicatorColor);
          }
        }

        if (textView == null) {
          textView = mTabParentView.findViewById(R.id.title);
        }

        defaultMaxLines = TextViewCompat.getMaxLines(textView);
        //TextViewCompat.setTextAppearance(textView, defaultTabTextAppearance);
        if (isSelected()){
          if (selectedTabTextAppearance != -1) {
            TextViewCompat.setTextAppearance(textView, selectedTabTextAppearance);
          }
          textView.setTypeface(mBoldTypeface);
        } else {
          TextViewCompat.setTextAppearance(textView, tabTextAppearance);
          textView.setTypeface(mNormalTypeface);
        }

        checkMaxFontScale(textView, (int) tabTextSize);
        textView.setTextColor(tabTextColors);
        if (mDepthStyle == DEPTH_TYPE_SUB) {
          if (mSubTextView == null) {
            mSubTextView = mTabParentView.findViewById(R.id.sub_title);
          }
          if (mSubTextView != null) {
            TextViewCompat.setTextAppearance(mSubTextView, mSubTabSubTextAppearance);
            mSubTextView.setTextColor(mSubTabSubTextColors);
            checkMaxFontScale(mSubTextView, mSubTabTextSize);
          }
        }

        if (iconView == null && mTabParentView != null) {
          iconView = mTabParentView.findViewById(R.id.icon);
        }

        seslUpdateTextAndIcon(textView, mSubTextView, iconView);

        int tabWidth;
        int tabHeight;
        boolean recreate;

        if (mDepthStyle == DEPTH_TYPE_SUB) {
          tabWidth = mode == MODE_SCROLLABLE
              ? WRAP_CONTENT
              : MATCH_PARENT;
          tabHeight = TextUtils.isEmpty(tab.seslGetSubText())
              ? mSubTabIndicatorHeight
              : mSubTabIndicator2ndHeight;
          recreate = mTabParentView != null && mTabParentView.getHeight() != tabHeight;
        } else if (tab.icon != null) {
          tabHeight = MATCH_PARENT;
          tabWidth = WRAP_CONTENT;
          recreate = false;
        } else {
          tabHeight = MATCH_PARENT;
          tabWidth = MATCH_PARENT;
          recreate = false;
        }

        if (mTabParentView != null && mTabParentView.getParent() == null) {
          addView(mTabParentView, tabWidth, tabHeight);
        } else if (recreate) {
          removeView(mTabParentView);
          addView(mTabParentView, tabWidth, tabHeight);
        }
        //sesl

        tryUpdateBadgeAnchor();
        addOnLayoutChangeListener(iconView);
        addOnLayoutChangeListener(textView);
      } else {
        // Else, we'll see if there is a TextView or ImageView present and update them
        if (customTextView != null || customIconView != null) {
          updateTextAndIcon(customTextView, customIconView, false);
        }
      }

      if (tab != null && !TextUtils.isEmpty(tab.contentDesc)) {
        // Only update the TabView's content description from Tab if the Tab's content description
        // has been explicitly set.
        setContentDescription(tab.contentDesc);
      }
    }

    final void update() {
      updateTab();
      // Finally update our selected state
      setSelected(this.tab != null && this.tab.isSelected());
    }

    private void inflateAndAddDefaultIconView() {
      ViewGroup iconViewParent = this;
      if (BadgeUtils.USE_COMPAT_PARENT) {
        iconViewParent = createPreApi18BadgeAnchorRoot();
        addView(iconViewParent, 0);
      }
      this.iconView =
          (ImageView)
              LayoutInflater.from(getContext())
                  .inflate(R.layout.sesl_layout_tab_icon, iconViewParent, false);
      iconViewParent.addView(iconView, 0);
    }

    private void inflateAndAddDefaultTextView() {
      ViewGroup textViewParent = this;
      if (BadgeUtils.USE_COMPAT_PARENT) {
        textViewParent = createPreApi18BadgeAnchorRoot();
        addView(textViewParent, 0);
      }
      this.textView =
          (TextView)
              LayoutInflater.from(getContext())
                  .inflate(R.layout.sesl_layout_tab_text, textViewParent, false);
      textViewParent.addView(textView);
    }

    @NonNull
    private FrameLayout createPreApi18BadgeAnchorRoot() {
      FrameLayout frameLayout = new FrameLayout(getContext());
      FrameLayout.LayoutParams layoutparams =
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      frameLayout.setLayoutParams(layoutparams);
      return frameLayout;
    }

    /**
     * Creates an instance of {@link BadgeDrawable} if none exists. Initializes (if needed) and
     * returns the associated instance of {@link BadgeDrawable}.
     *
     * @return an instance of BadgeDrawable associated with {@code Tab}.
     */
    @NonNull
    private BadgeDrawable getOrCreateBadge() {
      // Creates a new instance if one is not already initialized for this TabView.
      if (badgeDrawable == null) {
        badgeDrawable = BadgeDrawable.create(getContext());
      }
      tryUpdateBadgeAnchor();
      if (badgeDrawable == null) {
        throw new IllegalStateException("Unable to create badge");
      }
      return badgeDrawable;
    }

    @Nullable
    private BadgeDrawable getBadge() {
      return badgeDrawable;
    }

    private void removeBadge() {
      if (badgeAnchorView != null) {
        tryRemoveBadgeFromAnchor();
      }
      badgeDrawable = null;
    }

    private void addOnLayoutChangeListener(@Nullable final View view) {
      if (view == null) {
        return;
      }
      view.addOnLayoutChangeListener(
          new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
              if (view.getVisibility() == VISIBLE) {
                tryUpdateBadgeDrawableBounds(view);
              }
            }
          });
    }

    private void tryUpdateBadgeAnchor() {
      if (!hasBadgeDrawable()) {
        return;
      }
      if (customView != null) {
        // TODO(b/123406505): Support badging on custom tab views.
        tryRemoveBadgeFromAnchor();
      } else {
        if (iconView != null && tab != null && tab.getIcon() != null) {
          if (badgeAnchorView != iconView) {
            tryRemoveBadgeFromAnchor();
            // Anchor badge to icon.
            tryAttachBadgeToAnchor(iconView);
          } else {
            tryUpdateBadgeDrawableBounds(iconView);
          }
        } else if (textView != null
            && tab != null
            && tab.getTabLabelVisibility() == TAB_LABEL_VISIBILITY_LABELED) {
          if (badgeAnchorView != textView) {
            tryRemoveBadgeFromAnchor();
            // Anchor badge to label.
            tryAttachBadgeToAnchor(textView);
          } else {
            tryUpdateBadgeDrawableBounds(textView);
          }
        } else {
          tryRemoveBadgeFromAnchor();
        }
      }
    }

    private void tryAttachBadgeToAnchor(@Nullable View anchorView) {
      if (!hasBadgeDrawable()) {
        return;
      }
      if (anchorView != null) {
        clipViewToPaddingForBadge(false);
        BadgeUtils.attachBadgeDrawable(
            badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
        badgeAnchorView = anchorView;
      }
    }

    private void tryRemoveBadgeFromAnchor() {
      if (!hasBadgeDrawable()) {
        return;
      }
      clipViewToPaddingForBadge(true);
      if (badgeAnchorView != null) {
        BadgeUtils.detachBadgeDrawable(badgeDrawable, badgeAnchorView);
        badgeAnchorView = null;
      }
    }

    private void clipViewToPaddingForBadge(boolean flag) {
      // Avoid clipping a badge if it's displayed.
      // Clip children / view to padding when no badge is displayed.
      setClipChildren(flag);
      setClipToPadding(flag);
      ViewGroup parent = (ViewGroup) getParent();
      if (parent != null) {
        parent.setClipChildren(flag);
        parent.setClipToPadding(flag);
      }
    }

    final void updateOrientation() {
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      if (customTextView != null || customIconView != null) {
        updateTextAndIcon(customTextView, customIconView, false);
      } else {
        updateTextAndIcon(textView, iconView, true);
      }
    }

    //Sesl
    private void seslUpdateTextAndIcon(
        @Nullable final TextView textView,
        @Nullable final TextView subTextView,
        @Nullable final ImageView iconView) {
      updateTextAndIcon(textView, iconView, true);

      if (subTextView != null) {
        CharSequence subText = tab != null ? tab.seslGetSubText() : null;
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
        boolean hasSubText = !TextUtils.isEmpty(subText);
        lp.topToTop = hasSubText ? -1 : 0;
        lp.bottomToBottom = hasSubText ? -1 : 0;
        lp.bottomToTop = hasSubText ? R.id.center_anchor : -1;
        subTextView.setText(hasSubText ? subText : null);
        if (!hasSubText) {
          subTextView.setVisibility(GONE);
          return;
        }
        if (tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED) {
          subTextView.setVisibility(VISIBLE);
        } else {
          subTextView.setVisibility(GONE);
        }
        setVisibility(VISIBLE);
      }
    }
    //sesl

    private void updateTextAndIcon(
        @Nullable final TextView textView,
        @Nullable final ImageView iconView,
        final boolean addDefaultMargins) {
      final Drawable icon =
          (tab != null && tab.getIcon() != null)
              ? DrawableCompat.wrap(tab.getIcon()).mutate()
              : null;
      if (icon != null) {
        //Sesl
        if (tabIconTint == null) {
          DrawableCompat.setTintList(icon, tabTextColors);
        } else {
          DrawableCompat.setTintList(icon, tabIconTint);
        }
        //sesl
        if (tabIconTintMode != null) {
          DrawableCompat.setTintMode(icon, tabIconTintMode);
        }
      }

      final CharSequence text = tab != null ? tab.getText() : null;

      if (iconView != null) {
        if (icon != null) {
          iconView.setImageDrawable(icon);
          iconView.setVisibility(VISIBLE);
          setVisibility(VISIBLE);
        } else {
          iconView.setVisibility(GONE);
          iconView.setImageDrawable(null);
        }
      }

      final boolean hasText = !TextUtils.isEmpty(text);
      final boolean showingText;
      if (textView != null) {
        showingText = hasText && tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED;
        textView.setText(hasText ? text : null);
        textView.setVisibility(showingText ? VISIBLE : GONE);

        if (hasText){
          setVisibility(VISIBLE);
        }
      } else {
        showingText = false;
      }

      if (addDefaultMargins && iconView != null) {
        if (showingText && iconView.getVisibility() == VISIBLE) {
          MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
          int iconMargin;
          // If we're showing both text and icon, add some margin to the icon
          if (mIconTextGap != -1) {
            iconMargin = mIconTextGap;
          }else{
            iconMargin = (int) ViewUtils.dpToPx(getContext(), DEFAULT_GAP_TEXT_ICON);
          }
          if (inlineLabel) {
            if (iconMargin != lp.getMarginEnd()) {
              lp.setMarginEnd(iconMargin);
              lp.bottomMargin = 0;
              // Calls resolveLayoutParams(), necessary for layout direction
              iconView.setLayoutParams(lp);
              iconView.requestLayout();
            }
          } /*else {Note: No icon to text gap applied in sesl
            if (iconMargin != lp.bottomMargin) {
              lp.bottomMargin = iconMargin;
              lp.setMarginEnd(0);
              // Calls resolveLayoutParams(), necessary for layout direction
              iconView.setLayoutParams(lp);
              iconView.requestLayout();
            }
          }*/
        }
      }

      final CharSequence contentDesc = tab != null ? tab.contentDesc : null;
      // Avoid calling tooltip for L and M devices because long pressing twice may freeze devices.
      if (VERSION.SDK_INT > VERSION_CODES.M) {
        TooltipCompat.setTooltipText(this, hasText ? null : tab != null ? contentDesc : null);//sesl
      }
    }

    private void tryUpdateBadgeDrawableBounds(@NonNull View anchor) {
      // Check that this view is the badge's current anchor view.
      if (hasBadgeDrawable() && anchor == badgeAnchorView) {
        BadgeUtils.setBadgeDrawableBounds(badgeDrawable, anchor, getCustomParentForBadge(anchor));
      }
    }

    private boolean hasBadgeDrawable() {
      return badgeDrawable != null;
    }

    @Nullable
    private FrameLayout getCustomParentForBadge(@NonNull View anchor) {
      if (anchor != iconView && anchor != textView) {
        return null;
      }
      return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout) anchor.getParent()) : null;
    }

    /**
     * Calculates the width of the TabView's content.
     *
     * @return Width of the tab label, if present, or the width of the tab icon, if present. If tabs
     *     is in inline mode, returns the sum of both the icon and tab label widths.
     */
    int getContentWidth() {
      boolean initialized = false;
      int left = 0;
      int right = 0;

      for (View view : new View[] {textView, iconView, customView}) {
        if (view != null && view.getVisibility() == VISIBLE) {
          left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
          right = initialized ? Math.max(right, view.getRight()) : view.getRight();
          initialized = true;
        }
      }

      return right - left;
    }

    /**
     * Calculates the height of the TabView's content.
     *
     * @return Height of the tab label, if present, or the height of the tab icon, if present. If
     *     the tab contains both a label and icon, the combined will be returned.
     */
    int getContentHeight() {
      boolean initialized = false;
      int top = 0;
      int bottom = 0;

      for (View view : new View[] {textView, iconView, customView}) {
        if (view != null && view.getVisibility() == VISIBLE) {
          top = initialized ? Math.min(top, view.getTop()) : view.getTop();
          bottom = initialized ? Math.max(bottom, view.getBottom()) : view.getBottom();
          initialized = true;
        }
      }

      return bottom - top;
    }

    @Nullable
    public Tab getTab() {
      return tab;
    }

    /** Approximates a given lines width with the new provided text size. */
    private float approximateLineWidth(@NonNull Layout layout, int line, float textSize) {
      return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
    }

    //Sesl
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      mIconSize = getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
    }

    @Override
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      if (mMainTabTouchBackground != null) {
        mMainTabTouchBackground.setVisibility(enabled
            ? VISIBLE : GONE);
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);

      if (mMainTabTouchBackground != null) {
        mMainTabTouchBackground.setLeft(0);
        mMainTabTouchBackground.setRight(mTabParentView != null
            ? mTabParentView.getWidth() : r - l);

        Animation tabBgAnimation = mMainTabTouchBackground.getAnimation();
        if (tabBgAnimation != null && tabBgAnimation.hasEnded()) {
          mMainTabTouchBackground.setAlpha(0.0f);
        }
      }

      if (iconView != null && tab.icon != null) {
        if (textView != null && mIndicatorView != null && mTabParentView != null) {
          int measuredWidth = mIconSize + textView.getMeasuredWidth();
          if (mIconTextGap != -1) {
            measuredWidth += mIconTextGap;
          }

          int offset = Math.abs((getWidth() - measuredWidth) / 2);
          if (ViewUtils.isLayoutRtl(this)) {
            if (iconView.getRight() == mTabParentView.getRight()) {
              textView.offsetLeftAndRight(-offset);
              iconView.offsetLeftAndRight(-offset);
              mIndicatorView.offsetLeftAndRight(-offset);
            }
          } else if (iconView.getLeft() == this.mTabParentView.getLeft()) {
            textView.offsetLeftAndRight(offset);
            iconView.offsetLeftAndRight(offset);
            mIndicatorView.offsetLeftAndRight(offset);
          }
        }
      }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (!isEnabled() || isScrollingEnabled()/*sesl*/) {
        return super.onTouchEvent(event);
      }
      if (tab.getCustomView() != null) {
        return super.onTouchEvent(event);
      }
      return startTabTouchAnimation(event, null);
    }

    private boolean startTabTouchAnimation(MotionEvent motionEvent, KeyEvent keyEvent) {
      if (motionEvent == null || tab.getCustomView() != null
          || textView == null || keyEvent != null) {//sesl
        return false;
      }

      final int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;

      switch (action) {
        case MotionEvent.ACTION_DOWN:
          mIsCallPerformClick = false;
          if (tab.position != getSelectedTabPosition() && textView != null) {
            textView.setTypeface(mBoldTypeface);
            startTextColorChangeAnimation(textView,
                getSelectedTabTextColor());

            if (mIndicatorView != null) {
              mIndicatorView.setPressed();
            }

            final Tab tab = getTabAt(getSelectedTabPosition());
            if (tab != null) {
              TextView tabViewText = tab.view.textView;
              if (tabViewText != null) {
                tabViewText.setTypeface(mNormalTypeface);
                startTextColorChangeAnimation(
                    tabViewText, tabTextColors.getDefaultColor());
              }
              SeslAbsIndicatorView tabViewIndicator = tab.view.mIndicatorView;
              if (tabViewIndicator != null) {
                tabViewIndicator.setHide();
              }
            }
          } else if (tab.position == getSelectedTabPosition() && mIndicatorView != null) {
            mIndicatorView.setPressed();
          }
          //showMainTabTouchBackground(MotionEvent.ACTION_DOWN);
          break;
        case MotionEvent.ACTION_UP:
          //showMainTabTouchBackground(MotionEvent.ACTION_UP);
          if (mIndicatorView != null) {
            mIndicatorView.setReleased();
            mIndicatorView.onTouchEvent(motionEvent);
          }
          performClick();
          mIsCallPerformClick = true;
          break;
        case MotionEvent.ACTION_CANCEL:
          textView.setTypeface(mNormalTypeface);
          startTextColorChangeAnimation(
              textView, tabTextColors.getDefaultColor());

          if (mIndicatorView != null && !mIndicatorView.isSelected()) {
            mIndicatorView.setHide();
          }

          final Tab tab = getTabAt(getSelectedTabPosition());
          if (tab != null) {
            TextView tabViewText = tab.view.textView;
            if (tabViewText != null) {
              tabViewText.setTypeface(TabLayout.this.mBoldTypeface);
              startTextColorChangeAnimation(
                  tabViewText, getSelectedTabTextColor());
            }
            SeslAbsIndicatorView tabViewIndicator = tab.view.mIndicatorView;
            if (tabViewIndicator != null) {
              tabViewIndicator.setShow();
            }
          }
          if (mDepthStyle != DEPTH_TYPE_MAIN) {
            if (mIndicatorView != null && mIndicatorView.isSelected()) {
              mIndicatorView.setReleased();
            }
          }
          break;
      }

      return super.onTouchEvent(motionEvent);
    }

    @Override
    public void drawableStateChanged() {
      super.drawableStateChanged();
      //     boolean changed = false;
      int[] state = getDrawableState();
      if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
        /*changed |= */baseBackgroundDrawable.setState(state);
      }

//      if (changed) {
//        invalidate();
//        TabLayout.this.invalidate(); // Invalidate TabLayout, which draws mBaseBackgroundDrawable
//      }
    }

    private void inflateAndAddDefaultSubTextView() {
      ViewGroup iconViewParent = this;
      if (BadgeUtils.USE_COMPAT_PARENT) {
        iconViewParent = createPreApi18BadgeAnchorRoot();
        addView(iconViewParent, 0);
      }
      this.textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sesl_layout_tab_sub_text, iconViewParent, false);
      iconViewParent.addView(textView);
    }


    @Override
    public void onDraw(Canvas canvas) {
      setBackgroundBounds();
      super.onDraw(canvas);
    }

    public void seslSetRoleDescription(@Nullable CharSequence charSequence) {
      this.mCustomRoleDescription = charSequence;
    }

    public void setShowButtonShape(int i, ColorStateList colorStateList) {
      Drawable drawable = getResources().getDrawable(R.drawable.sesl_bottom_nav_show_button_shapes_background);
      TextView textView = this.textView;
      if (textView != null) {
        textView.setTextColor(i);
        this.textView.setBackground(drawable);
        this.textView.setBackgroundTintList(colorStateList);
      }
      TextView textView2 = this.mSubTextView;
      if (textView2 != null) {
        textView2.setTextColor(i);
        this.mSubTextView.setBackground(drawable);
        this.mSubTextView.setBackgroundTintList(colorStateList);
      }
    }
  }

  class SlidingTabIndicator extends LinearLayout {
    ValueAnimator indicatorAnimator;

    private int layoutDirection = -1;

    SlidingTabIndicator(Context context) {
      super(context);
      setWillNotDraw(false);
    }

    void setSelectedIndicatorHeight(int height) {
      Rect bounds = tabSelectedIndicator.getBounds();
      tabSelectedIndicator.setBounds(bounds.left, 0, bounds.right, height);
      this.requestLayout();
    }

    boolean childrenNeedLayout() {
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (child.getWidth() <= 0) {
          return true;
        }
      }
      return false;
    }

    /**
     * Set the indicator position based on an offset between two adjacent tabs.
     *
     * @param position Position index of the first tab (with less index) currently being displayed.
     *     Tab position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset Value from [0, 1) indicating the offset from the tab at position.
     */
    void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
      // Since we are tweening the indicator in between the position and position+positionOffset,
      // we set the indicator position to whichever is closer.
      indicatorPosition = Math.round(position + positionOffset);
      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        indicatorAnimator.cancel();
      }
      tweenIndicatorPosition(getChildAt(position), getChildAt(position + 1), positionOffset);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
      super.onRtlPropertiesChanged(layoutDirection);

//      // Workaround for a bug before Android M where LinearLayout did not re-layout itself when
//      // layout direction changed
//      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//        if (this.layoutDirection != layoutDirection) {
//          requestLayout();
//          this.layoutDirection = layoutDirection;
//        }
//      }
    }

    @Override
    protected void onMeasure(/*final*/int widthMeasureSpec, final int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
        // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
        // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
        return;
      }
      //Sesl
      if (mode == SESL_MODE_FIXED_AUTO || mode == SESL_MODE_WEIGHT_AUTO) {
        checkOverScreen();
        widthMeasureSpec = mIsOverScreen
            ? mOverScreenMaxWidth
            : MeasureSpec.getSize(widthMeasureSpec);

        int childCount = getChildCount();
        int[] childWidthsArr = new int[childCount];

        int totalChildWidth = 0;
        for (int i = 0; i < childCount; i++) {
          View child = getChildAt(i);
          if (child.getVisibility() == VISIBLE) {
            child.measure(MeasureSpec.makeMeasureSpec(tabMaxWidth, UNSPECIFIED),
                heightMeasureSpec);
            int childWidth = (mTabMinSideSpace * 2) + child.getMeasuredWidth();
            childWidthsArr[i] = childWidth;
            totalChildWidth += childWidth;
          }
        }

        int maxChildWidth = widthMeasureSpec / childCount;
        if (totalChildWidth > widthMeasureSpec) {
          for (int i = 0; i < childCount; i++) {
            getChildAt(i).getLayoutParams().width = childWidthsArr[i];
          }
        } else {

          boolean remeasure = false;
          if (mode == SESL_MODE_FIXED_AUTO) {
            for (int i = 0; i < childCount; i++) {
              if (childWidthsArr[i] > maxChildWidth) {
                remeasure = true;
                break;
              }
            }
          }

          if (remeasure) {
            int padding = (widthMeasureSpec - totalChildWidth) / childCount;
            for (int i = 0; i < childCount; i++) {
              getChildAt(i).getLayoutParams().width = childWidthsArr[i] + padding;
            }
          } else {
            for (int i = 0; i < childCount; i++) {
              getChildAt(i).getLayoutParams().width = maxChildWidth;
            }
          }
        }

        if (totalChildWidth > widthMeasureSpec) {
          widthMeasureSpec = totalChildWidth;
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(
            widthMeasureSpec, MeasureSpec.EXACTLY), heightMeasureSpec);

      } else {
        // GRAVITY_CENTER will make all tabs the same width as the largest tab, and center them in the
        // SlidingTabIndicator's width (with a "gutter" of padding on either side). If the Tabs do not
        // fit in the SlidingTabIndicator, then fall back to GRAVITY_FILL behavior.
        if (tabGravity == GRAVITY_CENTER || mode == MODE_AUTO || mFirstTabGravity == GRAVITY_CENTER /*sesl*/) {
          final int count = this.getChildCount();

          //Sesl
          if (tabGravity == GRAVITY_FILL && mFirstTabGravity == GRAVITY_CENTER) {
            for (int i = 0; i < count; i++) {
              View childTab = getChildAt(i);
              LayoutParams childTabLp = (LayoutParams) childTab.getLayoutParams();
              childTabLp.width = WRAP_CONTENT;
              childTabLp.weight = 0.0f;
              childTab.measure(
                  MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED,
                      MeasureSpec.UNSPECIFIED),
                  heightMeasureSpec);
            }
          }
          //sesl

          // We'll find the widest tab
          int largestTabWidth = 0;
          for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
              largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
            }
          }

          if (largestTabWidth <= 0) {
            // If we don't have a largest child yet, skip until the next measure pass
            return;
          }

          final int gutter = (int) ViewUtils.dpToPx(getContext(), FIXED_WRAP_GUTTER_MIN);
          boolean remeasure = false;

          if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
            // If the tabs fit within our width minus gutters, we will set all tabs to have
            // the same width
            for (int i = 0; i < count; i++) {
              final LinearLayout.LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
              if (lp.width != largestTabWidth || lp.weight != 0) {
                lp.width = largestTabWidth;
                lp.weight = 0;
                remeasure = true;
              }
            }

            //Sesl
            if (tabGravity == GRAVITY_FILL && mFirstTabGravity == GRAVITY_CENTER) {
              tabGravity = GRAVITY_CENTER;
            }
            //sesl
          } else {
            // If the tabs will wrap to be larger than the width minus gutters, we need
            // to switch to GRAVITY_FILL.
            // TODO (b/129799806): This overrides the user TabGravity setting.
            tabGravity = GRAVITY_FILL;
            updateTabViews(false);
            remeasure = true;
          }

          if (remeasure) {
            // Now re-measure after our changes
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
          }
        }
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);

      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        // It's possible that the tabs' layout is modified while the indicator is animating (ex. a
        // new tab is added, or a tab is removed in onTabSelected). This would change the target end
        // position of the indicator, since the tab widths are different. We need to modify the
        // animation's updateListener to pick up the new target positions.
        updateOrRecreateIndicatorAnimation(
            /* recreateAnimation= */ false, getSelectedTabPosition(), /* duration= */ -1);
      } else {
        // If we've been laid out, update the indicator position
        jumpIndicatorToSelectedPosition();//sesl
      }
    }

    /**
     * Immediately update the indicator position to the specified position, unless we are mid-scroll
     * in a viewpager.
     */
    private void jumpIndicatorToPosition(int position) {
      // Don't update the indicator position if the scroll state is not idle, and the indicator
      // is drawn.
      if (viewPagerScrollState != SCROLL_STATE_IDLE
          && !(getTabSelectedIndicator().getBounds().left == -1
          && getTabSelectedIndicator().getBounds().right == -1)) {
        return;
      }
      final View currentView = getChildAt(position);
      tabIndicatorInterpolator.setIndicatorBoundsForTab(
          TabLayout.this, currentView, tabSelectedIndicator);
      indicatorPosition = position;
    }

    /** Immediately update the indicator position to the currently selected position. */
    private void jumpIndicatorToSelectedPosition() {
//      // If indicator position has not yet been set, set indicator to the selected tab position.
//      if (indicatorPosition == -1) {
//        indicatorPosition = getSelectedTabPosition();
//      }
//      jumpIndicatorToPosition(indicatorPosition);
    }

    /**
     * Update the position of the indicator by tweening between the currently selected tab and the
     * destination tab.
     *
     * <p>This method is called for each frame when either animating the indicator between
     * destinations or driving an animation through gesture, such as with a viewpager.
     *
     * @param startTitle The tab which should be selected (as marked by the indicator), when
     *     fraction is 0.0.
     * @param endTitle The tab which should be selected (as marked by the indicator), when fraction
     *     is 1.0.
     * @param fraction A value between 0.0 and 1.0 that indicates how far between currentTitle and
     *     endTitle the indicator should be drawn. e.g. If a viewpager attached to this TabLayout is
     *     currently half way slid between page 0 and page 1, fraction will be 0.5.
     */
    private void tweenIndicatorPosition(View startTitle, View endTitle, float fraction) {
      boolean hasVisibleTitle = startTitle != null && startTitle.getWidth() > 0;
      if (hasVisibleTitle) {
        tabIndicatorInterpolator.updateIndicatorForOffset(
            TabLayout.this, startTitle, endTitle, fraction, tabSelectedIndicator);
      } else {
        // Hide the indicator by setting the drawable's width to 0 and off screen.
        tabSelectedIndicator.setBounds(
            -1, tabSelectedIndicator.getBounds().top, -1, tabSelectedIndicator.getBounds().bottom);
      }

      this.postInvalidateOnAnimation();
    }

    /**
     * Animate the position of the indicator from its current position to a new position.
     *
     * <p>This is typically used when a tab destination is tapped. If the indicator should be moved
     * as a result of a gesture, see {@link #setIndicatorPositionFromTabPosition(int, float)}.
     *
     * @param position The new position to animate the indicator to.
     * @param duration The duration over which the animation should take place.
     */
    void animateIndicatorToPosition(final int position, int duration) {
      /*No op - SESL has no sliding indicator*/
//      if (indicatorAnimator != null
//          && indicatorAnimator.isRunning()
//          && indicatorPosition != position) {
//        indicatorAnimator.cancel();
//      }
//
//      updateOrRecreateIndicatorAnimation(/* recreateAnimation= */ true, position, duration);
    }

    /**
     * Animate the position of the indicator from its current position to a new position.
     *
     * @param recreateAnimation Whether a currently running animator should be re-targeted to move
     *     the indicator to it's new position.
     * @param position The new position to animate the indicator to.
     * @param duration The duration over which the animation should take place.
     */
    private void updateOrRecreateIndicatorAnimation(
        boolean recreateAnimation, final int position, int duration) {
      /*No op - SESL has no sliding indicator*/
//      // If the indicator position is already the target position, we don't need to update the
//      // indicator animation because nothing has changed.
//      if (indicatorPosition == position) {
//        return;
//      }
//      final View currentView = getChildAt(getSelectedTabPosition());
//      final View targetView = getChildAt(position);
//      if (targetView == null) {
//        // If we don't have a view, just update the position now and return
//        jumpIndicatorToSelectedPosition();
//        return;
//      }
//      indicatorPosition = position;
//
//      // Create the update listener with the new target indicator positions. If we're not recreating
//      // then animationStartLeft/Right will be the same as when the previous animator was created.
//      ValueAnimator.AnimatorUpdateListener updateListener =
//          new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
//              tweenIndicatorPosition(currentView, targetView, valueAnimator.getAnimatedFraction());
//            }
//          };
//
//      if (recreateAnimation) {
//        // Create & start a new indicatorAnimator.
//        ValueAnimator animator = indicatorAnimator = new ValueAnimator();
//        animator.setInterpolator(tabIndicatorTimeInterpolator);
//        animator.setDuration(duration);
//        animator.setFloatValues(0F, 1F);
//        animator.addUpdateListener(updateListener);
//        animator.start();
//      } else {
//        // Reuse the existing animator. Updating the listener only modifies the target positions.
//        indicatorAnimator.removeAllUpdateListeners();
//        indicatorAnimator.addUpdateListener(updateListener);
//      }
//
//      indicatorPosition = position;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
//      int indicatorHeight = tabSelectedIndicator.getBounds().height();
//      if (indicatorHeight < 0) {
//        indicatorHeight = tabSelectedIndicator.getIntrinsicHeight();
//      }
//
//      int indicatorTop = 0;
//      int indicatorBottom = 0;
//
//      switch (tabIndicatorGravity) {
//        case INDICATOR_GRAVITY_BOTTOM:
//          indicatorTop = getHeight() - indicatorHeight;
//          indicatorBottom = getHeight();
//          break;
//        case INDICATOR_GRAVITY_CENTER:
//          indicatorTop = (getHeight() - indicatorHeight) / 2;
//          indicatorBottom = (getHeight() + indicatorHeight) / 2;
//          break;
//        case INDICATOR_GRAVITY_TOP:
//          indicatorTop = 0;
//          indicatorBottom = indicatorHeight;
//          break;
//        case INDICATOR_GRAVITY_STRETCH:
//          indicatorTop = 0;
//          indicatorBottom = getHeight();
//          break;
//        default:
//          break;
//      }
//
//      // Ensure the drawable actually has a width and is worth drawing
//      if (tabSelectedIndicator.getBounds().width() > 0) {
//        // Use the left and right bounds of the drawable, as set by the indicator interpolator.
//        // Update the top and bottom to respect the indicator gravity property.
//        Rect indicatorBounds = tabSelectedIndicator.getBounds();
//        tabSelectedIndicator.setBounds(
//            indicatorBounds.left, indicatorTop, indicatorBounds.right, indicatorBottom);
//        tabSelectedIndicator.draw(canvas);
//      }
//
//      // Draw the tab item contents (icon and label) on top of the background + indicator layers
      super.draw(canvas);
    }

    private void jumpIndicatorToIndicatorPosition() {
    }
  }

  private void seslCheckMaxFontScale(TextView textView, int i) {
    float f = getResources().getConfiguration().fontScale;
    if (f > 1.2f) {
      textView.setTextSize(0, (i / f) * 1.2f);
    }
  }


  private void setShowButtonShape(@NonNull TabView tabView) {
    int color;
    ColorStateList tabTextColors = getTabTextColors();
    if (mDepthStyle == DEPTH_TYPE_MAIN && isShowButtonShapesEnabled()) {
      if (Build.VERSION.SDK_INT <= 26) {
        tabView.setShowButtonShape(0, tabTextColors);
        return;
      }

      ColorDrawable colorDrawable = this.mBackgroundColorDrawable;
      if (colorDrawable != null) {
        color = colorDrawable.getColor();
      } else {
        color = ResourcesCompat.getColor(getResources(),
                SeslMisc.isLightTheme(getContext())
                        ? R.color.sesl_bottom_navigation_background_light
                        : R.color.sesl_bottom_navigation_background_dark, null);
      }
      tabView.setShowButtonShape(color, tabTextColors);
    }
  }

  private boolean isShowButtonShapesEnabled() {
    return Settings.System.getInt(mContentResolver, "show_button_background", 0) == 1;
  }

  @NonNull
  private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
    final int[][] states = new int[2][];
    final int[] colors = new int[2];
    int i = 0;

    states[i] = SELECTED_STATE_SET;
    colors[i] = selectedColor;
    i++;

    // Default enabled state
    states[i] = EMPTY_STATE_SET;
    colors[i] = defaultColor;
    i++;

    return new ColorStateList(states, colors);
  }

  @Dimension(unit = Dimension.DP)
  private int getDefaultHeight() {
//    boolean hasIconAndText = false;
//    for (int i = 0, count = tabs.size(); i < count; i++) {
//      Tab tab = tabs.get(i);
//      if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
//        hasIconAndText = true;
//        break;
//      }
//    }
//    return (hasIconAndText && !inlineLabel) ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
    return mDepthStyle == DEPTH_TYPE_SUB
        ? SESL_SUB_DEPTH_DEFAULT_HEIGHT
        : SESL_DEFAULT_HEIGHT;//sesl
  }

  private int getTabMinWidth() {
    if (requestedTabMinWidth != INVALID_WIDTH) {
      // If we have been given a min width, use it
      return requestedTabMinWidth;
    }
    return 0;//sesl
//   // Else, we'll use the default value
//   return (mode == MODE_SCROLLABLE || mode == MODE_AUTO) ? scrollableTabMinWidth : 0;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    // We don't care about the layout params of any views added to us, since we don't actually
    // add them. The only view we add is the SlidingTabStrip, which is done manually.
    // We return the default layout params so that we don't blow up if we're given a TabItem
    // without android:layout_* values.
    return generateDefaultLayoutParams();
  }

  int getTabMaxWidth() {
    return tabMaxWidth;
  }

  /**
   * A {@link ViewPager.OnPageChangeListener} class which contains the necessary calls back to the
   * provided {@link TabLayout} so that the tab position is kept in sync.
   *
   * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
   * ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
   * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and not cause a
   * leak.
   */
  public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
    @NonNull private final WeakReference<TabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
      previousScrollState = scrollState;
      scrollState = state;
      TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        tabLayout.updateViewPagerScrollState(scrollState);
      }
    }

    @Override
    public void onPageScrolled(
        final int position, final float positionOffset, final int positionOffsetPixels) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        // Only update the tab view selection if we're not settling, or we are settling after
        // being dragged
        final boolean updateSelectedTabView =
            scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
        // Update the indicator if we're not settling after being idle. This is caused
        // from a setCurrentItem() call and will be handled by an animation from
        // onPageSelected() instead.
        final boolean updateIndicator =
            !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(
            position, positionOffset, updateSelectedTabView, updateIndicator /*, false*//*sesl*/);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        final boolean updateIndicator =
            scrollState == SCROLL_STATE_IDLE
                || (scrollState == SCROLL_STATE_SETTLING
                && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
  }

  /**
   * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
   * provided {@link ViewPager} so that the tab position is kept in sync.
   */
  public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private final ViewPager viewPager;

    public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
      this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
      viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
      // No-op
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
      // No-op
    }
  }

  private class PagerAdapterObserver extends DataSetObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateFromPagerAdapter();
    }

    @Override
    public void onInvalidated() {
      populateFromPagerAdapter();
    }
  }

  private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
    private boolean autoRefresh;

    AdapterChangeListener() {}

    @Override
    public void onAdapterChanged(
        @NonNull ViewPager viewPager,
        @Nullable PagerAdapter oldAdapter,
        @Nullable PagerAdapter newAdapter) {
      if (TabLayout.this.viewPager == viewPager) {
        setPagerAdapter(newAdapter, autoRefresh);
      }
    }

    void setAutoRefresh(boolean autoRefresh) {
      this.autoRefresh = autoRefresh;
    }
  }


  public void seslSetSubTabStyle() {
    if (mDepthStyle == DEPTH_TYPE_MAIN) {
      mDepthStyle = DEPTH_TYPE_SUB;

      tabTextColors = getResources().getColorStateList(
          SeslMisc.isLightTheme(getContext())
              ? R.color.sesl_tablayout_subtab_text_color_light
              : R.color.sesl_tablayout_subtab_text_color_dark);

      int tabSize = tabs.size();

      if (tabSize > 0) {
        int selectedTabPosition = getSelectedTabPosition();
        ArrayList<Tab> savedTabs = new ArrayList<>(tabs.size());

        for (int i = 0; i < tabSize; i++) {
          Tab tabCopy = newTab();
          tabCopy.text = tabs.get(i).text;
          tabCopy.icon = tabs.get(i).icon;
          tabCopy.customView = tabs.get(i).customView;
          tabCopy.subText = tabs.get(i).subText;
          if (i == selectedTabPosition) {
            tabCopy.select();
          }
          tabCopy.view.update();
          savedTabs.add(tabCopy);
        }

        removeAllTabs();

        tabViewPool = new Pools.SimplePool<>(12);
        tabPool = new Pools.SynchronizedPool<>(16);

        int savedTabsSize = savedTabs.size();
        for (int i = 0; i < savedTabsSize; i++) {
          addTab(savedTabs.get(i), i == selectedTabPosition);
          Tab addedTab = tabs.get(i);
          if (addedTab != null) {
            addedTab.view.update();
          }
        }

        savedTabs.clear();
      }
    }
  }


  private void updateBadgePosition(@NonNull TabView tabView) {
    TextView badgeView;
    int badgeType;
    View tabTextView = tabView.textView;
    View tabIconView = tabView.iconView;
    if (tabView.getWidth() > 0) {
      int startOffset;
      if (tabView.mNBadgeView != null && tabView.mNBadgeView.getVisibility() == VISIBLE) {
        badgeView = tabView.mNBadgeView;
        startOffset = getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
        badgeType = BADGE_TYPE_N;
      } else if (tabView.mDotBadgeView != null && tabView.mDotBadgeView.getVisibility() == VISIBLE) {
        badgeView = tabView.mDotBadgeView;
        startOffset = getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_dot_badge_offset_x);
        badgeType = BADGE_TYPE_DOT;
      } else {
        badgeView = null;
        startOffset = getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
        badgeType = BADGE_TYPE_UNKNOWN;
      }
      if (badgeView == null || badgeView.getVisibility() != VISIBLE) {
        return;
      }
      badgeView.measure(0, 0);
      int measuredWidth = badgeType == BADGE_TYPE_N
          ? badgeView.getMeasuredWidth()
          : getResources().getDimensionPixelSize(R.dimen.sesl_tab_badge_dot_size);
      if (tabIconView != null && tabIconView.getVisibility() == VISIBLE) {
        tabTextView = tabIconView;
      }
      if (tabTextView == null) {
        return;
      }
      int width = tabView.getWidth();
      if (tabTextView.getRight() + startOffset + measuredWidth > width) {
        startOffset = -((tabTextView.getRight() + measuredWidth) - width);
      }
      ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) badgeView.getLayoutParams();
      int i = layoutParams.width;
      if (layoutParams.getMarginStart() == startOffset && i == measuredWidth) {
        return;
      }
      layoutParams.setMarginStart(startOffset);
      layoutParams.width = measuredWidth;
      badgeView.setLayoutParams(layoutParams);
    }
  }

  public void seslSetSubTabSelectedIndicatorColor(int color) {
    mSubTabSelectedIndicatorColor = color;
    setSelectedTabIndicatorColor(color);
  }

  @Deprecated
  public void seslSetTabTextColor(ColorStateList textColor, boolean updateTabView) {
    if (tabTextColors != textColor) {
      tabTextColors = textColor;
      if (updateTabView) {
        updateAllTabs();
      } else if (tabs != null) {
        for (int i = 0; i < tabs.size(); i++) {
          TabView tabView = tabs.get(i).view;
          if (tabView.textView != null) {
            tabView.textView.setTextColor(tabTextColors);
          }
        }
      }
    }
  }

  public void seslSetBadgeColor(int color) {
    mBadgeColor = color;
  }

  public void seslSetBadgeTextColor(int color) {
    mBadgeTextColor = color;
  }

  public void seslSetTabWidth(int width) {
    mRequestedTabWidth = width;
  }

  private int getSelectedTabTextColor() {
    if (tabTextColors != null) {
      return tabTextColors.getColorForState(
          new int[]{
              android.R.attr.state_selected,
              android.R.attr.state_enabled
          }, tabTextColors.getDefaultColor());
    }
    return Color.WHITE;
  }

  private void startTextColorChangeAnimation(
      TextView textView, int color) {
    if (textView != null) {
      textView.setTextColor(color);
    }
  }

  private void checkMaxFontScale(TextView textview, int baseSize) {
    final float currentFontScale
        = getResources().getConfiguration().fontScale;
    if (textview != null
        && mIsScaledTextSizeType && currentFontScale > 1.3f) {
      textview.setTextSize(
          TypedValue.COMPLEX_UNIT_PX,
          (baseSize / currentFontScale) * 1.3f);
    }
  }

  private void createAddBadge(int badgeType, TabView tabView) {
    if (tabView != null && tabView.mTabParentView != null) {
      TextView badgeView = new TextView(getContext());
      Resources resources = getResources();
      switch (badgeType) {
        case BADGE_TYPE_DOT:
          if (tabView.mDotBadgeView == null) {
            badgeView.setVisibility(GONE);
            badgeView.setBackground(resources.getDrawable(R.drawable.sesl_dot_badge));
            badgeView.setId(R.id.sesl_badge_dot);

            int dotBadgeSize = resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_dot_size);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(dotBadgeSize, dotBadgeSize);
            if (tabView.iconView == null || tabView.iconView.getVisibility() != VISIBLE) {
              layoutParams.topToTop = R.id.title;
              layoutParams.startToEnd = R.id.title;
            } else {
              layoutParams.topToTop = R.id.icon;
              layoutParams.startToEnd = R.id.icon;
            }
            int startOffset = resources.getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_dot_badge_offset_x);
            layoutParams.setMargins(startOffset, 0, 0, 0);
            tabView.mTabParentView.addView(badgeView, layoutParams);
            tabView.mDotBadgeView = badgeView;
          }
          break;

        case BADGE_TYPE_N:
          if (tabView.mNBadgeView == null) {
            badgeView.setVisibility(GONE);
            badgeView.setMinWidth(resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_number_min_width));
            int nBadgeSize = resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_number_text_size);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, nBadgeSize);
            seslCheckMaxFontScale(badgeView, nBadgeSize);
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setTextColor(resources.getColor(R.color.sesl_badge_text_color));
            badgeView.setBackground(resources.getDrawable(R.drawable.sesl_tab_n_badge));
            badgeView.setId(R.id.sesl_badge_n);
            badgeView.setMaxLines(1);
            ConstraintLayout.LayoutParams badgeLP = new ConstraintLayout.LayoutParams(WRAP_CONTENT,
                    resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_number_height));
            if (tabView.iconView == null || tabView.iconView.getVisibility() != 0) {
              badgeLP.topToTop = R.id.title;
              badgeLP.startToEnd = R.id.title;
            } else {
              badgeLP.topToTop = R.id.icon;
              badgeLP.startToEnd = R.id.icon;
            }
            badgeLP.setMargins(
                    resources.getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset),
                    resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_offset_y), 0, 0);
            tabView.mTabParentView.addView(badgeView, badgeLP);
            tabView.mNBadgeView = badgeView;
          }
      }
    }
  }

  public void seslShowDotBadge(int index, boolean show) {
    seslShowDotBadge(index, show, null);
  }

  public void seslShowDotBadge(int index, boolean show, String badgeDescription) {
    Tab tab = tabs.get(index);
    if (tab != null) {
      TabView tabView = tab.view;

      if (tabView.mDotBadgeView == null) {
        if (show) {
          createAddBadge(BADGE_TYPE_DOT, tabView);
        } else return;
      }

      TextView dotBadgeView = tabView.mDotBadgeView;
      if (show) {
        dotBadgeView.setVisibility(VISIBLE);
        if (mBadgeColor != Color.WHITE) {
          DrawableCompat.setTint(dotBadgeView.getBackground(), this.mBadgeColor);
        }
        if (badgeDescription != null) {
          dotBadgeView.setContentDescription(badgeDescription);
        }
        updateTabViews();
        return;
      }else {
        dotBadgeView.setVisibility(GONE);
      }
    }

  }

  public void seslShowBadge(int index, boolean show, String content) {
    seslShowBadge(index, show, content, null);
  }

  public void seslShowBadge(int index, boolean show, String content,
                            String contentDescription) {
    if (mDepthStyle == DEPTH_TYPE_SUB) {
      Log.e(LOG_TAG, "Subtab style doesn't support text badge. Use `seslShowDotBadge` instead.");
      return;
    }

    if (tabs.get(index) != null) {
      TabView tabView = tabs.get(index).view;

      if (tabView.mNBadgeView == null) {
        if (show) {
          createAddBadge(BADGE_TYPE_N, tabView);
        } else return;
      }

      TextView nBadgeView = tabView.mNBadgeView;

      if (show) {
        nBadgeView.setText(content);
        nBadgeView.setVisibility(VISIBLE);

        if (mBadgeColor != Color.WHITE) {
          DrawableCompat.setTint(nBadgeView.getBackground(), mBadgeColor);
        }
        if (mBadgeTextColor != Color.WHITE) {
          nBadgeView.setTextColor(mBadgeTextColor);
        }
        if (contentDescription != null) {
          nBadgeView.setContentDescription(contentDescription);
        }
        updateTabViews();
        nBadgeView.requestLayout();
      } else {
        nBadgeView.setVisibility(GONE);
      }
    }
  }

  @Override
  protected void onLayout(boolean changed,
                          int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    updateTabViews();

    if (changed) {
      mMaxTouchSlop = Math.max(mMaxTouchSlop, right - left);
    }

    final int touchSlop;
    if (mode == MODE_FIXED
        || (!canScrollHorizontally(1) && !canScrollHorizontally(-1))) {
      touchSlop = mMaxTouchSlop;
    } else {
      touchSlop = mDefaultTouchSlop;
    }

    if (mCurrentTouchSlop != touchSlop) {
      SeslHorizontalScrollViewReflector.setTouchSlop(this, touchSlop);
      mCurrentTouchSlop = touchSlop;
    }
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);

    if (tabs == null) return;

    int tabCount = getTabCount();
    for (int i = 0; i < tabCount; i++) {
      Tab tab = getTabAt(i);
      if (tab != null && tab.view.mMainTabTouchBackground != null) {
        tab.view.mMainTabTouchBackground.setAlpha(0f);
      }
    }
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    for (int i = 0; i < getTabCount(); i++) {
      Tab tab = getTabAt(i);
      if (tab != null && tab.view.mMainTabTouchBackground != null) {
        tab.view.mMainTabTouchBackground.setAlpha(0.0f);
      }
    }
    updateTabViews();
  }

  public void seslSetSubTabIndicatorHeight(int heightPixel) {
    mSubTabIndicatorHeight = heightPixel;
  }

  public void seslSetIconTextGap(int gap) {
    mIconTextGap = gap;
    updateAllTabs();
  }

  public void seslSetTabSubTextColors(@Nullable ColorStateList color) {
    if (mSubTabSubTextColors != color) {
      mSubTabSubTextColors = color;
      updateAllTabs();
    }
  }

  public ColorStateList seslGetTabSubTextColors() {
    return mSubTabSubTextColors;
  }

  public void seslSetTabSubTextColors(int defaultColor,
                                      int selectedColor) {
    seslSetTabSubTextColors(
        createColorStateList(defaultColor, selectedColor));
  }

  private int seslGetSelectedTabSubTextColor() {
    if (mSubTabSubTextColors != null) {
      return mSubTabSubTextColors
          .getColorForState(new int[]{
              android.R.attr.state_selected,
              android.R.attr.state_enabled
          }, mSubTabSubTextColors.getDefaultColor());
    }
    return Color.WHITE;
  }

  private void checkOverScreen() {
    final int measuredWidth = getMeasuredWidth();
    final float overScreenWidth = TypedValue.applyDimension(COMPLEX_UNIT_DIP, mOverScreenWidth, getResources().getDisplayMetrics());
    if (measuredWidth > overScreenWidth) {
      mIsOverScreen = true;
      mOverScreenMaxWidth = (int) (mOverScreenWidthMaxRate * measuredWidth);
    } else {
      mIsOverScreen = false;
    }
  }
  //sesl
}
