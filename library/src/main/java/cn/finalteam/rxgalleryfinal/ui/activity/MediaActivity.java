package cn.finalteam.rxgalleryfinal.ui.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.RxBusDisposable;
import cn.finalteam.rxgalleryfinal.rxbus.event.CloseRxMediaGridPageEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaCheckChangeEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaViewPagerChangedEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPageFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.OpenMediaPreviewFragmentEvent;
import cn.finalteam.rxgalleryfinal.rxbus.event.RequestStorageReadAccessPermissionEvent;
import cn.finalteam.rxgalleryfinal.rxjob.RxJob;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaGridFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPageFragment;
import cn.finalteam.rxgalleryfinal.ui.fragment.MediaPreviewFragment;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;
import cn.finalteam.rxgalleryfinal.view.ActivityFragmentView;
import io.reactivex.disposables.Disposable;

/**
 * Desction:
 * Author:pengjianbo  Dujinyang
 * Date:16/5/7 上午10:01
 */
public class MediaActivity extends BaseActivity implements ActivityFragmentView {

    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    public static final int REQUEST_CAMERA_ACCESS_PERMISSION = 103;

    private static final String EXTRA_CHECKED_LIST = EXTRA_PREFIX + ".CheckedList";
    private static final String EXTRA_SELECTED_INDEX = EXTRA_PREFIX + ".SelectedIndex";
    private static final String EXTRA_PAGE_MEDIA_LIST = EXTRA_PREFIX + ".PageMediaList";
    private static final String EXTRA_PAGE_POSITION = EXTRA_PREFIX + ".PagePosition";
    private static final String EXTRA_PREVIEW_POSITION = EXTRA_PREFIX + ".PreviewPosition";

    private MediaGridFragment mMediaGridFragment;
    private MediaPageFragment mMediaPageFragment;
    private MediaPreviewFragment mMediaPreviewFragment;


    private ArrayList<MediaBean> mCheckedList;
    private int mSelectedIndex = 0;
    private ArrayList<MediaBean> mPageMediaList;
    private int mPagePosition;
    private int mPreviewPosition;

    @Override
    public int getContentView() {
        return R.layout.gallery_activity_media;
    }

    @Override
    protected void onCreateOk(@Nullable Bundle savedInstanceState) {
        mMediaGridFragment = MediaGridFragment.newInstance(mConfiguration);

        mCheckedList = new ArrayList<>();
        if (mConfiguration.getSelectedList() != null) {
            mCheckedList.addAll(mConfiguration.getSelectedList());
        }

        showMediaGridFragment();
        subscribeEvent();
    }

    @Override
    public void findViews() {

    }

    @Override
    protected void setTheme() {
        int statusBarColor = ThemeUtils.resolveColor(this, R.attr.gallery_color_statusbar, R.color.gallery_default_color_statusbar);
        ThemeUtils.setStatusBarColor(statusBarColor, getWindow());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCheckedList != null) {
            outState.putParcelableArrayList(EXTRA_CHECKED_LIST, mCheckedList);
        }
        outState.putInt(EXTRA_SELECTED_INDEX, mSelectedIndex);
        if (mPageMediaList != null) {
            outState.putParcelableArrayList(EXTRA_PAGE_MEDIA_LIST, mPageMediaList);
        }
        outState.putInt(EXTRA_PAGE_POSITION, mPagePosition);
        outState.putInt(EXTRA_PREVIEW_POSITION, mPreviewPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<MediaBean> list = savedInstanceState.getParcelableArrayList(EXTRA_CHECKED_LIST);
        if (list != null && list.size() > 0) {
            mCheckedList.clear();
            mCheckedList.addAll(list);
        }
        mPageMediaList = savedInstanceState.getParcelableArrayList(EXTRA_PAGE_MEDIA_LIST);
        mPagePosition = savedInstanceState.getInt(EXTRA_PAGE_POSITION);
        mPreviewPosition = savedInstanceState.getInt(EXTRA_PREVIEW_POSITION);
        mSelectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_INDEX);
        if (!mConfiguration.isRadio()) {
            switch (mSelectedIndex) {
                case 1:
                    showMediaPageFragment(mPageMediaList, mPagePosition);
                    break;
                case 2:
                    showMediaPreviewFragment();
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backAction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMediaGridFragment() {
        setTheme();
        mMediaPreviewFragment = null;
        mMediaPageFragment = null;
        mSelectedIndex = 0;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mMediaGridFragment);
        if (mMediaPreviewFragment != null) {
            ft.hide(mMediaPreviewFragment);
        }
        if (mMediaPageFragment != null) {
            ft.hide(mMediaPageFragment);
        }
        ft.show(mMediaGridFragment)
                .commit();

    }

    @Override
    public void showMediaPageFragment(ArrayList<MediaBean> list, int position) {
        mSelectedIndex = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mMediaPageFragment = MediaPageFragment.newInstance(mConfiguration, list, position);
        ft.add(R.id.fragment_container, mMediaPageFragment);
        mMediaPreviewFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPageFragment);
        ft.commit();

        String title = getString(R.string.gallery_page_title, position + 1, list.size());
    }

    @Override
    public void showMediaPreviewFragment() {
        mSelectedIndex = 2;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mMediaPreviewFragment = MediaPreviewFragment.newInstance(mConfiguration, mPreviewPosition);
        ft.add(R.id.fragment_container, mMediaPreviewFragment);
        mMediaPageFragment = null;
        ft.hide(mMediaGridFragment);
        ft.show(mMediaPreviewFragment);
        ft.commit();

        String title = getString(R.string.gallery_page_title, mPreviewPosition, mCheckedList.size());
    }

    private void subscribeEvent() {
        Disposable subscriptionOpenMediaPreviewEvent = RxBus.getDefault().toObservable(OpenMediaPreviewFragmentEvent.class)
                .map(mediaPreviewEvent -> mediaPreviewEvent)
                .subscribeWith(new RxBusDisposable<OpenMediaPreviewFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPreviewFragmentEvent openMediaPreviewFragmentEvent) {
                        mPreviewPosition = 0;
                        showMediaPreviewFragment();
                    }
                });

        RxBus.getDefault().add(subscriptionOpenMediaPreviewEvent);

        Disposable subscriptionMediaCheckChangeEvent = RxBus.getDefault().toObservable(MediaCheckChangeEvent.class)
                .map(mediaCheckChangeEvent -> mediaCheckChangeEvent)
                .subscribeWith(new RxBusDisposable<MediaCheckChangeEvent>() {
                    @Override
                    protected void onEvent(MediaCheckChangeEvent mediaCheckChangeEvent) {
                        MediaBean mediaBean = mediaCheckChangeEvent.getMediaBean();
                        if (mCheckedList.contains(mediaBean)) {
                            mCheckedList.remove(mediaBean);
                        } else {
                            mCheckedList.add(mediaBean);
                        }
                    }
                });
        RxBus.getDefault().add(subscriptionMediaCheckChangeEvent);

        Disposable subscriptionMediaViewPagerChangedEvent = RxBus.getDefault().toObservable(MediaViewPagerChangedEvent.class)
                .map(mediaViewPagerChangedEvent -> mediaViewPagerChangedEvent)
                .subscribeWith(new RxBusDisposable<MediaViewPagerChangedEvent>() {
                    @Override
                    protected void onEvent(MediaViewPagerChangedEvent mediaPreviewViewPagerChangedEvent) {
                        int curIndex = mediaPreviewViewPagerChangedEvent.getCurIndex();
                        int totalSize = mediaPreviewViewPagerChangedEvent.getTotalSize();
                        if (mediaPreviewViewPagerChangedEvent.isPreview()) {
                            mPreviewPosition = curIndex;
                        } else {
                            mPagePosition = curIndex;
                        }
                        String title = getString(R.string.gallery_page_title, curIndex + 1, totalSize);
                    }
                });
        RxBus.getDefault().add(subscriptionMediaViewPagerChangedEvent);

        Disposable subscriptionCloseRxMediaGridPageEvent = RxBus.getDefault().toObservable(CloseRxMediaGridPageEvent.class)
                .subscribeWith(new RxBusDisposable<CloseRxMediaGridPageEvent>() {
                    @Override
                    protected void onEvent(CloseRxMediaGridPageEvent closeRxMediaGridPageEvent) throws Exception {
                        finish();
                    }
                });
        RxBus.getDefault().add(subscriptionCloseRxMediaGridPageEvent);

        Disposable subscriptionOpenMediaPageFragmentEvent = RxBus.getDefault().toObservable(OpenMediaPageFragmentEvent.class)
                .subscribeWith(new RxBusDisposable<OpenMediaPageFragmentEvent>() {
                    @Override
                    protected void onEvent(OpenMediaPageFragmentEvent openMediaPageFragmentEvent) {
                        mPageMediaList = openMediaPageFragmentEvent.getMediaBeanList();
                        mPagePosition = openMediaPageFragmentEvent.getPosition();

                        showMediaPageFragment(mPageMediaList, mPagePosition);
                    }
                });
        RxBus.getDefault().add(subscriptionOpenMediaPageFragmentEvent);
    }

    public List<MediaBean> getCheckedList() {
        return mCheckedList;
    }

    private void backAction() {
        if (mMediaGridFragment != null && mMediaGridFragment.isShowRvBucketView()) {
            mMediaGridFragment.hideRvBucketView();
            return;
        }
        if ((mMediaPreviewFragment != null && mMediaPreviewFragment.isVisible())
                || (mMediaPageFragment != null && mMediaPageFragment.isVisible())) {
            showMediaGridFragment();
            return;
        }
        onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getDefault().removeAllStickyEvents();
        RxBus.getDefault().clear();
        RxJob.getDefault().clearJob();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.i("onRequestPermissionsResult:requestCode=" + requestCode + " permissions=" + permissions[0]);
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true, RequestStorageReadAccessPermissionEvent.TYPE_WRITE));
                } else {
                    finish();
                }
                break;
            case REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true, RequestStorageReadAccessPermissionEvent.TYPE_WRITE));
                } else {
                    finish();
                }
                break;
            case REQUEST_CAMERA_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RxBus.getDefault().post(new RequestStorageReadAccessPermissionEvent(true, RequestStorageReadAccessPermissionEvent.TYPE_CAMERA));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
