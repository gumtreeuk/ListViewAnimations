package com.haarman.listviewanimations.itemmanipulation;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ExpandableCursorListAdapter extends CursorAdapter {

    private static final int DEFAULTTITLEPARENTRESID = 10000;
    private static final int DEFAULTCONTENTPARENTRESID = 10001;

    private int mViewLayoutResId;
    private int mTitleParentResId;
    private int mContentParentResId;
    private int mActionViewResId;
    private List<Long> mVisibleIds;

    private int mLimit;
    private Map<Long, View> mExpandedViews;
    private OnExpandableButtonToggleListener listener;

    public ExpandableCursorListAdapter(Context context, Cursor c) {
        super(context, c, false);
        mTitleParentResId = DEFAULTTITLEPARENTRESID;
        mContentParentResId = DEFAULTCONTENTPARENTRESID;
        mExpandedViews = new HashMap<Long, View>();
        mVisibleIds = new ArrayList<Long>();
    }

    public ExpandableCursorListAdapter(Context context, Cursor c, int actionViewResId, int layoutResId, int titleParentResId, int contentParentResId) {
        super(context, c, false);
        mViewLayoutResId = layoutResId;
        mTitleParentResId = titleParentResId;
        mContentParentResId = contentParentResId;
        mActionViewResId = actionViewResId;

        mVisibleIds = new ArrayList<Long>();
        mExpandedViews = new HashMap<Long, View>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();
        View view = LayoutInflater.from(context).inflate(mViewLayoutResId, viewGroup, false);
        viewHolder.titleParent = (ViewGroup) view.findViewById(mTitleParentResId);
        viewHolder.contentParent = (ViewGroup) view.findViewById(mContentParentResId);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int position = cursor.getPosition();
        if (mLimit > 0) {
            if (mVisibleIds.contains(getItemId(position))) {
                mExpandedViews.put(getItemId(position), view);
            } else if (mExpandedViews.containsValue(view) && !mVisibleIds.contains(getItemId(position))) {
                mExpandedViews.remove(getItemId(position));
            }
        }

        View titleView = getTitleView(context, viewHolder.titleView, viewHolder.titleParent, cursor);
        if (titleView != viewHolder.titleView) {
            viewHolder.titleParent.removeAllViews();
            viewHolder.titleParent.addView(titleView);

            if (mActionViewResId == 0) {
                view.setOnClickListener(new TitleViewOnClickListener(viewHolder.contentParent){

                });
            } else {
                view.findViewById(mActionViewResId).setOnClickListener(new TitleViewOnClickListener(viewHolder.contentParent) {
                    @Override
                    public void onTitleViewClicked(View view, boolean isVisible) {
                        super.onTitleViewClicked(view, isVisible);
                        if (listener!=null){
                            listener.onExpandedViewToggled(view, isVisible);
                        }
                    }
                });
            }
        }
        viewHolder.titleView = titleView;

        View contentView = getContentView(viewHolder.contentView, viewHolder.contentParent, cursor);
        if (contentView != viewHolder.contentView) {
            viewHolder.contentParent.removeAllViews();
            viewHolder.contentParent.addView(contentView);
        }
        viewHolder.contentView = contentView;

        viewHolder.contentParent.setVisibility(mVisibleIds.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
        viewHolder.contentParent.setTag(getItemId(position));

        ViewGroup.LayoutParams layoutParams = viewHolder.contentParent.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewHolder.contentParent.setLayoutParams(layoutParams);
    }

    public void setLimit(int limit) {
        mLimit = limit;
        mVisibleIds.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ViewGroup titleParent;
        View titleView;
        ViewGroup contentParent;
        View contentView;
    }

    public abstract View getTitleView(Context context, View convertView, ViewGroup parent, Cursor cursor);

    public abstract View getContentView(View convertView, ViewGroup parent, Cursor cursor);

    public void setOnExpandableButtonToggleListener(OnExpandableButtonToggleListener listener){
        this.listener = listener;
    }


    public interface OnExpandableButtonToggleListener{
        void onExpandedViewToggled(View view, boolean isVisible);
    }

    private class TitleViewOnClickListener implements View.OnClickListener {

        private View mContentParent;

        private TitleViewOnClickListener(View contentParent) {
            this.mContentParent = contentParent;
        }

        @Override
        public void onClick(View view) {
            boolean isVisible = mContentParent.getVisibility() == View.VISIBLE;
            if (!isVisible && mLimit > 0 && mVisibleIds.size() >= mLimit) {
                Long firstId = mVisibleIds.get(0);
                View firstEV = mExpandedViews.get(firstId);
                if (firstEV != null) {
                    ViewHolder firstVH = ((ViewHolder) firstEV.getTag());
                    ViewGroup contentParent = firstVH.contentParent;
                    ExpandCollapseHelper.animateCollapsing(contentParent);
                    mExpandedViews.remove(mVisibleIds.get(0));
                }
                mVisibleIds.remove(mVisibleIds.get(0));
            }

            if (isVisible) {
                ExpandCollapseHelper.animateCollapsing(mContentParent);
                mVisibleIds.remove(mContentParent.getTag());
                mExpandedViews.remove(mContentParent.getTag());
            } else {
                ExpandCollapseHelper.animateExpanding(mContentParent);
                mVisibleIds.add((Long) mContentParent.getTag());

                if (mLimit > 0) {
                    View parent = (View) mContentParent.getParent();
                    mExpandedViews.put((Long) mContentParent.getTag(), parent);
                }
            }

            onTitleViewClicked(view, isVisible);
        }

        public void onTitleViewClicked(View view, boolean isVisible) {
        }
    }

    private static class ExpandCollapseHelper {

        public static void animateCollapsing(final View view) {
            int origHeight = view.getHeight();

            ValueAnimator animator = createHeightAnimator(view, origHeight, 0);
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setVisibility(View.GONE);
                }
            });
            animator.start();
        }

        public static void animateExpanding(final View view) {
            view.setVisibility(View.VISIBLE);

            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(widthSpec, heightSpec);

            ValueAnimator animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
            animator.start();
        }

        public static ValueAnimator createHeightAnimator(final View view, int start, int end) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    if (view.getMeasuredHeight()!=layoutParams.height) {
                    layoutParams.height = view.getMeasuredHeight();
                    view.setLayoutParams(layoutParams);
                    }
                }
            });
            return animator;
        }
    }
}
