package com.youth.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youth.banner.listener.OnBannerClickListener;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoaderInterface;
import com.youth.banner.view.BannerViewPager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.support.v4.view.ViewPager.PageTransformer;

public class Banner extends FrameLayout implements OnPageChangeListener {
    public String tag;
    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int indicatorSize;
    private int bannerStyle;
    private int delayTime;
    private int scrollTime;
    private boolean isAutoPlay;
    private boolean isScroll;
    private int mIndicatorSelectedResId;
    private int mIndicatorUnselectedResId;
    private int mLayoutResId;
    private int titleHeight;
    private int titleBackground;
    private int titleTextColor;
    private int titleTextSize;
    private int count;
    private int currentItem;
    private int gravity;
    private int lastPosition;
    private int scaleType;
    private List<String> titles;
    private List imageUrls;
    private List<View> imageViews;
    private List<ImageView> indicatorImages;
    private BannerViewPager viewPager;
    private TextView bannerTitle;
    private TextView numIndicatorInside;
    private TextView numIndicator;
    private LinearLayout indicator;
    private LinearLayout indicatorInside;
    private LinearLayout titleView;
    private ImageLoaderInterface imageLoader;
    private Banner.BannerPagerAdapter adapter;
    private OnPageChangeListener mOnPageChangeListener;
    private BannerScroller mScroller;
    private OnBannerClickListener bannerListener;
    private OnBannerListener listener;
    private DisplayMetrics dm;
    private Context context;
    private WeakHandler handler;
    private int circle_paddingbuttom;
    private final Runnable task;

    public Banner(Context context) {
        this(context, (AttributeSet)null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.tag = "banner";
        this.mIndicatorMargin = 5;
        this.bannerStyle = 1;
        this.delayTime = 2000;
        this.scrollTime = 800;
        this.isAutoPlay = true;
        this.isScroll = true;
        this.mIndicatorSelectedResId = drawable.gray_radius;
        this.mIndicatorUnselectedResId = drawable.white_radius;
        this.mLayoutResId = layout.banner;
        this.count = 0;
        this.gravity = -1;
        this.lastPosition = 1;
        this.scaleType = 1;
        this.handler = new WeakHandler();
        this.task = new Runnable() {
            public void run() {
                if(Banner.this.count > 1 && Banner.this.isAutoPlay) {
                    Banner.this.currentItem = Banner.this.currentItem % (Banner.this.count + 1) + 1;
                    if(Banner.this.currentItem == 1) {
                        Banner.this.viewPager.setCurrentItem(Banner.this.currentItem, false);
                        Banner.this.handler.post(Banner.this.task);
                    } else {
                        Banner.this.viewPager.setCurrentItem(Banner.this.currentItem);
                        Banner.this.handler.postDelayed(Banner.this.task, (long)Banner.this.delayTime);
                    }
                }

            }
        };
        this.context = context;
        this.titles = new ArrayList();
        this.imageUrls = new ArrayList();
        this.imageViews = new ArrayList();
        this.indicatorImages = new ArrayList();
        this.dm = context.getResources().getDisplayMetrics();
        this.indicatorSize = this.dm.widthPixels / 80;
        this.initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        this.imageViews.clear();
        this.handleTypedArray(context, attrs);
        View view = LayoutInflater.from(context).inflate(this.mLayoutResId, this, true);
        this.viewPager = (BannerViewPager)view.findViewById(id.bannerViewPager);
        this.titleView = (LinearLayout)view.findViewById(id.titleView);
        this.indicator = (LinearLayout)view.findViewById(id.circleIndicator);
        this.indicator.setPadding(0, 0, 0, this.circle_paddingbuttom);
        this.indicatorInside = (LinearLayout)view.findViewById(id.indicatorInside);
        this.bannerTitle = (TextView)view.findViewById(id.bannerTitle);
        this.numIndicator = (TextView)view.findViewById(id.numIndicator);
        this.numIndicatorInside = (TextView)view.findViewById(id.numIndicatorInside);
        this.initViewPagerScroll();
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, styleable.Banner);
            this.mIndicatorWidth = typedArray.getDimensionPixelSize(styleable.Banner_indicator_width, this.indicatorSize);
            this.circle_paddingbuttom = typedArray.getDimensionPixelSize(styleable.Banner_circle_paddingbuttom, 0);
            this.mIndicatorHeight = typedArray.getDimensionPixelSize(styleable.Banner_indicator_height, this.indicatorSize);
            this.mIndicatorMargin = typedArray.getDimensionPixelSize(styleable.Banner_indicator_margin, 5);
            this.mIndicatorSelectedResId = typedArray.getResourceId(styleable.Banner_indicator_drawable_selected, drawable.gray_radius);
            this.mIndicatorUnselectedResId = typedArray.getResourceId(styleable.Banner_indicator_drawable_unselected, drawable.white_radius);
            this.scaleType = typedArray.getInt(styleable.Banner_image_scale_type, this.scaleType);
            this.delayTime = typedArray.getInt(styleable.Banner_delay_time, 2000);
            this.scrollTime = typedArray.getInt(styleable.Banner_scroll_time, 800);
            this.isAutoPlay = typedArray.getBoolean(styleable.Banner_is_auto_play, true);
            this.titleBackground = typedArray.getColor(styleable.Banner_title_background, -1);
            this.titleHeight = typedArray.getDimensionPixelSize(styleable.Banner_title_height, -1);
            this.titleTextColor = typedArray.getColor(styleable.Banner_title_textcolor, -1);
            this.titleTextSize = typedArray.getDimensionPixelSize(styleable.Banner_title_textsize, -1);
            this.mLayoutResId = typedArray.getResourceId(styleable.Banner_layout_id, this.mLayoutResId);
            typedArray.recycle();
        }
    }

    private void initViewPagerScroll() {
        try {
            Field e = ViewPager.class.getDeclaredField("mScroller");
            e.setAccessible(true);
            this.mScroller = new BannerScroller(this.viewPager.getContext());
            this.mScroller.setDuration(this.scrollTime);
            e.set(this.viewPager, this.mScroller);
        } catch (Exception var2) {
            Log.e(this.tag, var2.getMessage());
        }

    }

    public Banner isAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        return this;
    }

    public Banner setImageLoader(ImageLoaderInterface imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public Banner setDelayTime(int delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    public Banner setIndicatorGravity(int type) {
        switch(type) {
        case 5:
            this.gravity = 19;
            break;
        case 6:
            this.gravity = 17;
            break;
        case 7:
            this.gravity = 21;
        }

        return this;
    }

    public Banner setBannerAnimation(Class<? extends PageTransformer> transformer) {
        try {
            this.setPageTransformer(true, (PageTransformer)transformer.newInstance());
        } catch (Exception var3) {
            Log.e(this.tag, "Please set the PageTransformer class");
        }

        return this;
    }

    public Banner setOffscreenPageLimit(int limit) {
        if(this.viewPager != null) {
            this.viewPager.setOffscreenPageLimit(limit);
        }

        return this;
    }

    public Banner setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        this.viewPager.setPageTransformer(reverseDrawingOrder, transformer);
        return this;
    }

    public Banner setBannerTitles(List<String> titles) {
        this.titles = titles;
        return this;
    }

    public Banner setBannerStyle(int bannerStyle) {
        this.bannerStyle = bannerStyle;
        return this;
    }

    public Banner setViewPagerIsScroll(boolean isScroll) {
        this.isScroll = isScroll;
        return this;
    }

    public Banner setImages(List<?> imageUrls) {
        this.imageUrls = imageUrls;
        this.count = imageUrls.size();
        return this;
    }

    public void update(List<?> imageUrls, List<String> titles) {
        this.imageUrls.clear();
        this.titles.clear();
        this.imageUrls.addAll(imageUrls);
        this.titles.addAll(titles);
        this.count = this.imageUrls.size();
        this.start();
    }

    public void update(List<?> imageUrls) {
        this.imageUrls.clear();
        this.imageUrls.addAll(imageUrls);
        this.count = this.imageUrls.size();
        this.start();
    }

    public void updateBannerStyle(int bannerStyle) {
        this.indicator.setVisibility(8);
        this.numIndicator.setVisibility(8);
        this.numIndicatorInside.setVisibility(8);
        this.indicatorInside.setVisibility(8);
        this.bannerTitle.setVisibility(8);
        this.titleView.setVisibility(8);
        this.bannerStyle = bannerStyle;
        this.start();
    }

    public Banner start() {
        this.setBannerStyleUI();
        this.setImageList(this.imageUrls);
        this.setData();
        return this;
    }

    private void setTitleStyleUI() {
        if(this.titles.size() != this.imageUrls.size()) {
            throw new RuntimeException("[Banner] --> The number of titles and images is different");
        } else {
            if(this.titleBackground != -1) {
                this.titleView.setBackgroundColor(this.titleBackground);
            }

            if(this.titleHeight != -1) {
                this.titleView.setLayoutParams(new LayoutParams(-1, this.titleHeight));
            }

            if(this.titleTextColor != -1) {
                this.bannerTitle.setTextColor(this.titleTextColor);
            }

            if(this.titleTextSize != -1) {
                this.bannerTitle.setTextSize(0, (float)this.titleTextSize);
            }

            if(this.titles != null && this.titles.size() > 0) {
                this.bannerTitle.setText((CharSequence)this.titles.get(0));
                this.bannerTitle.setVisibility(0);
                this.titleView.setVisibility(0);
            }

        }
    }

    private void setBannerStyleUI() {
        byte visibility;
        if(this.count > 1) {
            visibility = 0;
        } else {
            visibility = 8;
        }

        switch(this.bannerStyle) {
        case 1:
            this.indicator.setVisibility(visibility);
            break;
        case 2:
            this.numIndicator.setVisibility(visibility);
            break;
        case 3:
            this.numIndicatorInside.setVisibility(visibility);
            this.setTitleStyleUI();
            break;
        case 4:
            this.indicator.setVisibility(visibility);
            this.setTitleStyleUI();
            break;
        case 5:
            this.indicatorInside.setVisibility(visibility);
            this.setTitleStyleUI();
        }

    }

    private void initImages() {
        this.imageViews.clear();
        if(this.bannerStyle != 1 && this.bannerStyle != 4 && this.bannerStyle != 5) {
            if(this.bannerStyle == 3) {
                this.numIndicatorInside.setText("1/" + this.count);
            } else if(this.bannerStyle == 2) {
                this.numIndicator.setText("1/" + this.count);
            }
        } else {
            this.createIndicator();
        }

    }

    private void setImageList(List<?> imagesUrl) {
        if(imagesUrl != null && imagesUrl.size() > 0) {
            this.initImages();

            for(int i = 0; i <= this.count + 1; ++i) {
                Object imageView = null;
                if(this.imageLoader != null) {
                    imageView = this.imageLoader.createImageView(this.context);
                }

                if(imageView == null) {
                    imageView = new ImageView(this.context);
                }

                this.setScaleType((View)imageView);
                Object url = null;
                if(i == 0) {
                    url = imagesUrl.get(this.count - 1);
                } else if(i == this.count + 1) {
                    url = imagesUrl.get(0);
                } else {
                    url = imagesUrl.get(i - 1);
                }

                this.imageViews.add(imageView);
                if(this.imageLoader != null) {
                    this.imageLoader.displayImage(this.context, url, (View)imageView);
                } else {
                    Log.e(this.tag, "Please set images loader.");
                }
            }

        } else {
            Log.e(this.tag, "Please set the images data.");
        }
    }

    private void setScaleType(View imageView) {
        if(imageView instanceof ImageView) {
            ImageView view = (ImageView)imageView;
            switch(this.scaleType) {
            case 0:
                view.setScaleType(ScaleType.CENTER);
                break;
            case 1:
                view.setScaleType(ScaleType.CENTER_CROP);
                break;
            case 2:
                view.setScaleType(ScaleType.CENTER_INSIDE);
                break;
            case 3:
                view.setScaleType(ScaleType.FIT_CENTER);
                break;
            case 4:
                view.setScaleType(ScaleType.FIT_END);
                break;
            case 5:
                view.setScaleType(ScaleType.FIT_START);
                break;
            case 6:
                view.setScaleType(ScaleType.FIT_XY);
                break;
            case 7:
                view.setScaleType(ScaleType.MATRIX);
            }
        }

    }

    private void createIndicator() {
        this.indicatorImages.clear();
        this.indicator.removeAllViews();
        this.indicatorInside.removeAllViews();

        for(int i = 0; i < this.count; ++i) {
            ImageView imageView = new ImageView(this.context);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(this.mIndicatorWidth, this.mIndicatorHeight);
            params.leftMargin = this.mIndicatorMargin;
            params.rightMargin = this.mIndicatorMargin;
            if(i == 0) {
                imageView.setImageResource(this.mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(this.mIndicatorUnselectedResId);
            }

            this.indicatorImages.add(imageView);
            if(this.bannerStyle != 1 && this.bannerStyle != 4) {
                if(this.bannerStyle == 5) {
                    this.indicatorInside.addView(imageView, params);
                }
            } else {
                this.indicator.addView(imageView, params);
            }
        }

    }

    private void setData() {
        this.currentItem = 1;
        if(this.adapter == null) {
            this.adapter = new Banner.BannerPagerAdapter();
            this.viewPager.addOnPageChangeListener(this);
        }

        this.viewPager.setAdapter(this.adapter);
        this.viewPager.setFocusable(true);
        this.viewPager.setCurrentItem(1);
        if(this.gravity != -1) {
            this.indicator.setGravity(this.gravity);
        }

        if(this.isScroll && this.count > 1) {
            this.viewPager.setScrollable(true);
        } else {
            this.viewPager.setScrollable(false);
        }

        if(this.isAutoPlay) {
            this.startAutoPlay();
        }

    }

    public void startAutoPlay() {
        this.handler.removeCallbacks(this.task);
        this.handler.postDelayed(this.task, (long)this.delayTime);
    }

    public void stopAutoPlay() {
        this.handler.removeCallbacks(this.task);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(this.isAutoPlay) {
            int action = ev.getAction();
            if(action != 1 && action != 3 && action != 4) {
                if(action == 0) {
                    this.stopAutoPlay();
                }
            } else {
                this.startAutoPlay();
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public int toRealPosition(int position) {
        int realPosition = (position - 1) % this.count;
        if(realPosition < 0) {
            realPosition += this.count;
        }

        return realPosition;
    }

    public void onPageScrollStateChanged(int state) {
        if(this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrollStateChanged(state);
        }

        this.currentItem = this.viewPager.getCurrentItem();
        switch(state) {
        case 0:
            if(this.currentItem == 0) {
                this.viewPager.setCurrentItem(this.count, false);
            } else if(this.currentItem == this.count + 1) {
                this.viewPager.setCurrentItem(1, false);
            }
            break;
        case 1:
            if(this.currentItem == this.count + 1) {
                this.viewPager.setCurrentItem(1, false);
            } else if(this.currentItem == 0) {
                this.viewPager.setCurrentItem(this.count, false);
            }
        case 2:
        }

    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

    }

    public void onPageSelected(int position) {
        if(this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageSelected(position);
        }

        if(this.bannerStyle == 1 || this.bannerStyle == 4 || this.bannerStyle == 5) {
            ((ImageView)this.indicatorImages.get((this.lastPosition - 1 + this.count) % this.count)).setImageResource(this.mIndicatorUnselectedResId);
            ((ImageView)this.indicatorImages.get((position - 1 + this.count) % this.count)).setImageResource(this.mIndicatorSelectedResId);
            this.lastPosition = position;
        }

        if(position == 0) {
            position = this.count;
        }

        if(position > this.count) {
            position = 1;
        }

        switch(this.bannerStyle) {
        case 1:
        default:
            break;
        case 2:
            this.numIndicator.setText(position + "/" + this.count);
            break;
        case 3:
            this.numIndicatorInside.setText(position + "/" + this.count);
            this.bannerTitle.setText((CharSequence)this.titles.get(position - 1));
            break;
        case 4:
            this.bannerTitle.setText((CharSequence)this.titles.get(position - 1));
            break;
        case 5:
            this.bannerTitle.setText((CharSequence)this.titles.get(position - 1));
        }

    }

    /** @deprecated */
    @Deprecated
    public Banner setOnBannerClickListener(OnBannerClickListener listener) {
        this.bannerListener = listener;
        return this;
    }

    public Banner setOnBannerListener(OnBannerListener listener) {
        this.listener = listener;
        return this;
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    public void releaseBanner() {
        this.handler.removeCallbacksAndMessages((Object)null);
    }

    class BannerPagerAdapter extends PagerAdapter {
        BannerPagerAdapter() {
        }

        public int getCount() {
            return Banner.this.imageViews.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView((View)Banner.this.imageViews.get(position));
            View view = (View)Banner.this.imageViews.get(position);
            if(Banner.this.bannerListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Log.e(Banner.this.tag, "你正在使用旧版点击事件接口，下标是从1开始，为了体验请更换为setOnBannerListener，下标从0开始计算");
                        Banner.this.bannerListener.OnBannerClick(position);
                    }
                });
            }

            if(Banner.this.listener != null) {
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Banner.this.listener.OnBannerClick(Banner.this.toRealPosition(position));
                    }
                });
            }

            return view;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }
}

