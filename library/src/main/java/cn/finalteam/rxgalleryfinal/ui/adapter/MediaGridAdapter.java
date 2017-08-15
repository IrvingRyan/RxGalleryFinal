package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.imageloader.FrescoImageLoader;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaCheckChangeEvent;
import cn.finalteam.rxgalleryfinal.rxjob.Job;
import cn.finalteam.rxgalleryfinal.rxjob.RxJob;
import cn.finalteam.rxgalleryfinal.rxjob.job.ImageThmbnailJobCreate;
import cn.finalteam.rxgalleryfinal.ui.activity.MediaActivity;
import cn.finalteam.rxgalleryfinal.ui.base.IMultiImageCheckedListener;
import cn.finalteam.rxgalleryfinal.ui.widget.FixImageView;
import cn.finalteam.rxgalleryfinal.ui.widget.SquareRelativeLayout;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.OsCompat;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;

/**
 * Desction:
 * Author:pengjianbo  Dujinyang
 * Date:16/5/18 下午7:48
 */
public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.GridViewHolder> {

    private static IMultiImageCheckedListener iMultiImageCheckedListener;
    private final MediaActivity mMediaActivity;
    private final List<MediaBean> mMediaBeanList;
    private final int mImageSize;
    private final Configuration mConfiguration;
    private final Drawable mDefaultImage;
    private final Drawable mImageViewBg;
    private int imageLoaderType = 0;

    public MediaGridAdapter(
            MediaActivity mediaActivity,
            List<MediaBean> list,
            int screenWidth,
            Configuration configuration) {
        this.mMediaActivity = mediaActivity;
        this.mMediaBeanList = list;
        this.mImageSize = screenWidth / 3;
        int defaultResId = ThemeUtils.resolveDrawableRes(mediaActivity, R.attr.gallery_default_image, R.drawable.gallery_default_image);
        this.mDefaultImage = ContextCompat.getDrawable(mediaActivity, defaultResId);
        this.mConfiguration = configuration;
        this.imageLoaderType = configuration.getImageLoaderType();
        this.mImageViewBg = ThemeUtils.resolveDrawable(mMediaActivity, R.attr.gallery_imageview_bg, R.drawable.gallery_default_image);
    }

    public static void setCheckedListener(IMultiImageCheckedListener checkedListener) {
        iMultiImageCheckedListener = checkedListener;
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (imageLoaderType != 3) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_media_grid, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_media_grid_fresco, parent, false);
        }
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GridViewHolder holder, int position) {
        MediaBean mediaBean = mMediaBeanList.get(position);
        if (mediaBean.getId() == Integer.MIN_VALUE) {
            holder.mCbCheck.setVisibility(View.GONE);
            holder.mIvMediaImage.setVisibility(View.GONE);
            holder.mLlCamera.setVisibility(View.VISIBLE);
        } else {
            if (mConfiguration.isRadio()) {
                holder.mCbCheck.setVisibility(View.GONE);
            } else {
                holder.mCbCheck.setVisibility(View.VISIBLE);
                holder.mCbCheck.setOnCheckedChangeListener(new OnCheckBoxCheckListener(mediaBean));
                holder.mCbCheck.setOnClickListener(new OnCheckBoxClickListener(mediaBean));
                holder.relativeLayout.setOnClickListener(new OnCheckBoxClickListener(mediaBean));
            }
            holder.mIvMediaImage.setVisibility(View.VISIBLE);
            holder.mLlCamera.setVisibility(View.GONE);
            holder.mCbCheck.setChecked(mMediaActivity.getCheckedList() != null && mMediaActivity.getCheckedList().contains(mediaBean));
            String bitPath = mediaBean.getThumbnailSmallPath();
            String smallPath = mediaBean.getThumbnailSmallPath();

            if (!new File(bitPath).exists() || !new File(smallPath).exists()) {
                Job job = new ImageThmbnailJobCreate(mMediaActivity, mediaBean).create();
                RxJob.getDefault().addJob(job);
            }
            String path;
            if (mConfiguration.isPlayGif() && (imageLoaderType == 3 || imageLoaderType == 2)) {
                path = mediaBean.getOriginalPath();
            } else {
                path = mediaBean.getThumbnailSmallPath();
                if (TextUtils.isEmpty(path)) {
                    path = mediaBean.getThumbnailBigPath();
                }
                if (TextUtils.isEmpty(path)) {
                    path = mediaBean.getOriginalPath();
                }
            }
            Logger.w("提示path：" + path);
            if (imageLoaderType != 3) {
                OsCompat.setBackgroundDrawableCompat(holder.mIvMediaImage, mImageViewBg);
                mConfiguration.getImageLoader()
                        .displayImage(mMediaActivity, path, (FixImageView) holder.mIvMediaImage, mDefaultImage, mConfiguration.getImageConfig(),
                                true, mConfiguration.isPlayGif(), mImageSize, mImageSize, mediaBean.getOrientation());
            } else {
                OsCompat.setBackgroundDrawableCompat(holder.mIvMediaImage, mImageViewBg);
                FrescoImageLoader.setImageSmall("file://" + path, (SimpleDraweeView) holder.mIvMediaImage,
                        mImageSize, mImageSize, holder.relativeLayout, mConfiguration.isPlayGif());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMediaBeanList.size();
    }


    static class GridViewHolder extends RecyclerView.ViewHolder {

        final AppCompatCheckBox mCbCheck;
        final LinearLayout mLlCamera;
        View mIvMediaImage;
        SquareRelativeLayout relativeLayout;


        GridViewHolder(View itemView) {
            super(itemView);
            mIvMediaImage = itemView.findViewById(R.id.iv_media_image);
            mCbCheck = (AppCompatCheckBox) itemView.findViewById(R.id.cb_check);
            relativeLayout = (SquareRelativeLayout) itemView.findViewById(R.id.rootView);
            mLlCamera = (LinearLayout) itemView.findViewById(R.id.ll_camera);

        }
    }

    private class OnCheckBoxClickListener implements View.OnClickListener {

        private final MediaBean mediaBean;

        OnCheckBoxClickListener(MediaBean bean) {
            this.mediaBean = bean;
        }

        @Override
        public void onClick(View view) {
            if (view instanceof AppCompatCheckBox) {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) view;
                if (mConfiguration.getMaxSize() == mMediaActivity.getCheckedList().size() &&
                        !mMediaActivity.getCheckedList().contains(mediaBean)) {
                    Logger.i("=>" + mMediaActivity.getResources().getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()));
                } else {
                    RxBus.getDefault().post(new MediaCheckChangeEvent(mediaBean));
                }
            } else {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) view.findViewById(R.id.cb_check);
                if (checkBox == null) {
                    return;
                }
                if (mConfiguration.getMaxSize() == mMediaActivity.getCheckedList().size() &&
                        !mMediaActivity.getCheckedList().contains(mediaBean)) {
                    checkBox.setChecked(false);
                    Logger.i("=>" + mMediaActivity.getResources().getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()));
                } else {
                    RxBus.getDefault().post(new MediaCheckChangeEvent(mediaBean));
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                    } else {
                        checkBox.setChecked(true);
                    }
                }
            }
        }
    }

    /**
     * @author KARL-dujinyang
     */
    private class OnCheckBoxCheckListener implements CompoundButton.OnCheckedChangeListener {
        private final MediaBean mediaBean;

        OnCheckBoxCheckListener(MediaBean bean) {
            this.mediaBean = bean;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mConfiguration.getMaxSize() == mMediaActivity.getCheckedList().size() &&
                    !mMediaActivity.getCheckedList().contains(mediaBean)) {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) buttonView;
                checkBox.setChecked(false);
                Logger.i("选中：" + mMediaActivity.getResources().getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()));
                if (iMultiImageCheckedListener != null) {
                    iMultiImageCheckedListener.selectedImgMax(buttonView, isChecked, mConfiguration.getMaxSize());
                }
            } else {
                if (iMultiImageCheckedListener != null)
                    iMultiImageCheckedListener.selectedImg(buttonView, isChecked);
            }

        }
    }
}
