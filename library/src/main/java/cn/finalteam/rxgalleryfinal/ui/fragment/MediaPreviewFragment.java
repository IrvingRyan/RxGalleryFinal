package cn.finalteam.rxgalleryfinal.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.event.CloseMediaViewPageFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaViewPagerChangedEvent;
import cn.finalteam.rxgalleryfinal.ui.activity.MediaActivity;
import cn.finalteam.rxgalleryfinal.ui.adapter.MediaPreviewAdapter;
import cn.finalteam.rxgalleryfinal.ui.widget.ClickableViewPager;
import cn.finalteam.rxgalleryfinal.utils.DeviceUtils;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;

/**
 * Desction:图片预览
 * Author:pengjianbo  Dujinyang
 * Date:16/6/9 上午1:35
 */
public class MediaPreviewFragment extends BaseFragment implements ViewPager.OnPageChangeListener,
        View.OnClickListener, ClickableViewPager.OnItemClickListener {

    private static final String EXTRA_PAGE_INDEX = EXTRA_PREFIX + ".PageIndex";

    DisplayMetrics mScreenSize;

    private AppCompatCheckBox mCbCheck;
    private ClickableViewPager mViewPager;
    private List<MediaBean> mMediaBeanList;
    private RelativeLayout mRlRootView;

    private MediaActivity mMediaActivity;
    private int mPagerPosition;
    private RelativeLayout mRlTopBar;
    private RelativeLayout mRlBottomBar;
    private TextView mSelectDone;
    private ArrayList<MediaBean> selectList;

    public static MediaPreviewFragment newInstance(Configuration configuration, int position) {
        MediaPreviewFragment fragment = new MediaPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CONFIGURATION, configuration);
        bundle.putInt(EXTRA_PAGE_INDEX, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MediaActivity) {
            mMediaActivity = (MediaActivity) context;
        }
    }

    @Override
    public int getContentView() {
        return R.layout.gallery_fragment_media_preview;
    }


    @Override
    public void onViewCreatedOk(View view, @Nullable Bundle savedInstanceState) {
        selectList = new ArrayList<>();
        mCbCheck = (AppCompatCheckBox) view.findViewById(R.id.cb_check);
        mViewPager = (ClickableViewPager) view.findViewById(R.id.view_pager);
        mRlRootView = (RelativeLayout) view.findViewById(R.id.rl_root_view);
        mRlTopBar = (RelativeLayout) view.findViewById(R.id.rl_top_bar);
        mRlBottomBar = (RelativeLayout) view.findViewById(R.id.rl_bottom_bar);
        mSelectDone = (TextView) view.findViewById(R.id.select_done);
        view.findViewById(R.id.iv_back).setOnClickListener(v -> mMediaActivity.showMediaGridFragment());
        mScreenSize = DeviceUtils.getScreenSize(getContext());
        mMediaBeanList = new ArrayList<>();
        if (mMediaActivity.getCheckedList() != null) {
            mMediaBeanList.addAll(mMediaActivity.getCheckedList());
        }
        selectList.addAll(mMediaBeanList);
        MediaPreviewAdapter mMediaPreviewAdapter = new MediaPreviewAdapter(mMediaBeanList,
                mScreenSize.widthPixels, mScreenSize.heightPixels, mConfiguration,
                ThemeUtils.resolveColor(getActivity(), R.attr.gallery_page_bg, R.color.gallery_default_page_bg),
                ContextCompat.getDrawable(getActivity(), ThemeUtils.resolveDrawableRes(getActivity(), R.attr.gallery_default_image, R.drawable.gallery_default_image)));
        mViewPager.setAdapter(mMediaPreviewAdapter);
        mViewPager.setOnItemClickListener(this);
        mCbCheck.setOnClickListener(this);
        mSelectDone.setOnClickListener(v -> {
                    mMediaActivity.getCheckedList().clear();
                    mMediaActivity.getCheckedList().addAll(selectList);
                    mMediaActivity.showMediaGridFragment();
                }
        );
        if (savedInstanceState != null) {
            mPagerPosition = savedInstanceState.getInt(EXTRA_PAGE_INDEX);
        }
        if (mMediaActivity.getCheckedList() == null || mMediaActivity.getCheckedList().size() == 0) {
            mSelectDone.setText(getString(R.string.gallery_over_button_text));
        } else {
            mSelectDone.setText(getString(R.string.gallery_over_button_text_checked, mMediaActivity.getCheckedList().size()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewPager.setCurrentItem(mPagerPosition, false);
        mViewPager.addOnPageChangeListener(this);
        //#ADD UI预览数量的BUG
        RxBus.getDefault().post(new MediaViewPagerChangedEvent(mPagerPosition, mMediaBeanList.size(), true));
    }

    @Override
    public void setTheme() {
        super.setTheme();
        int pageColor = ThemeUtils.resolveColor(getContext(), R.attr.gallery_page_bg, R.color.gallery_default_page_bg);
        ThemeUtils.setStatusBarColor(pageColor, getActivity().getWindow());
    }

    @Override
    protected void onFirstTimeLaunched() {

    }

    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPagerPosition = savedInstanceState.getInt(EXTRA_PAGE_INDEX);
        }
    }

    @Override
    protected void onSaveState(Bundle outState) {
        if (outState != null) {
            outState.putInt(EXTRA_PAGE_INDEX, mPagerPosition);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mPagerPosition = position;
        MediaBean mediaBean = mMediaBeanList.get(position);
        //判断是否选择
        if (selectList != null && selectList != null) {
            mCbCheck.setChecked(selectList.contains(mediaBean));
        }

        RxBus.getDefault().post(new MediaViewPagerChangedEvent(position, mMediaBeanList.size(), true));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * 改变选择
     */
    @Override
    public void onClick(View view) {
        int position = mViewPager.getCurrentItem();
        MediaBean mediaBean = mMediaBeanList.get(position);
        if (selectList.contains(mediaBean)) {
            selectList.remove(mediaBean);
        } else {
            selectList.add(mediaBean);
        }
        if (selectList == null || selectList.size() == 0) {
            mSelectDone.setText(getString(R.string.gallery_over_button_text));
        } else {
            mSelectDone.setText(getString(R.string.gallery_over_button_text_checked, selectList.size()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPagerPosition = 0;
        RxBus.getDefault().post(new CloseMediaViewPageFragmentEvent());
    }

    @Override
    public void onItemClick(int position) {
        if (mRlBottomBar.isShown()) {
            mRlBottomBar.setVisibility(View.GONE);
            mRlTopBar.setVisibility(View.GONE);
        } else {
            mRlBottomBar.setVisibility(View.VISIBLE);
            mRlTopBar.setVisibility(View.VISIBLE);
        }
    }
}
