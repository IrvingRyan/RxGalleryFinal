package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.BucketBean;
import cn.finalteam.rxgalleryfinal.ui.widget.SquareImageView;

/**
 * Desction:
 * Author:pengjianbo  Dujinyang
 * Date:16/7/4 下午5:40
 */
public class BucketAdapter extends RecyclerView.Adapter<BucketAdapter.BucketViewHolder> {

    private final List<BucketBean> mBucketList;
    private final Drawable mDefaultImage;
    private final Configuration mConfiguration;
    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;

    public BucketAdapter(
            List<BucketBean> bucketList,
            Configuration configuration,
            @ColorInt int color) {
        this.mBucketList = bucketList;
        this.mConfiguration = configuration;
        this.mDefaultImage = new ColorDrawable(color);
    }

    @Override
    public BucketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_adapter_bucket_item, parent, false);
        return new BucketViewHolder(parent, view);
    }

    @Override
    public void onBindViewHolder(BucketViewHolder holder, int position) {
        BucketBean bucketBean = mBucketList.get(position);
        String bucketName = bucketBean.getBucketName();
        int count = bucketBean.getImageCount();
        holder.mTvBucketName.setText(bucketName);
        holder.mTvCount.setText(count + "");

        String path = bucketBean.getCover();
        mConfiguration.getImageLoader()
                .displayImage(holder.itemView.getContext(), path, holder.mIvBucketCover, mDefaultImage, mConfiguration.getImageConfig(),
                        true, mConfiguration.isPlayGif(), 100, 100, bucketBean.getOrientation());
    }


    @Override
    public int getItemCount() {
        return mBucketList.size();
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnRecyclerViewItemClickListener = listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    class BucketViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView mTvBucketName;
        final SquareImageView mIvBucketCover;

        private final ViewGroup mParentView;
        private final TextView mTvCount;

        BucketViewHolder(ViewGroup parent, View itemView) {
            super(itemView);
            this.mParentView = parent;
            mTvBucketName = (TextView) itemView.findViewById(R.id.tv_bucket_name);
            mTvCount = (TextView) itemView.findViewById(R.id.tv_count);
            mIvBucketCover = (SquareImageView) itemView.findViewById(R.id.iv_bucket_cover);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mOnRecyclerViewItemClickListener != null) {
                mOnRecyclerViewItemClickListener.onItemClick(v, getLayoutPosition());
            }

        }

    }
}
