/*
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haarman.listviewanimations.swinginadapters;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.haarman.listviewanimations.ArrayAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * An ArrayAdapter class which applies multiple Animators at once to views when
 * they are first shown. The Animators applied are to be specified in
 * getAnimators(ViewGroup, View), plus an alpha transition.
 */
public abstract class AnimationAdapter<T> extends ArrayAdapter<T> {

	private static final long INITIALDELAYMILLIS = 150;

	private Context mContext;

	private ListView mListView;

	private SparseArray<Animator> mAnimators;
	private long mAnimationStartMillis;
	private int mLastAnimatedPosition;

	public AnimationAdapter(Context context) {
		this(context, null);
	}

	public AnimationAdapter(Context context, ArrayList<T> items) {
		super(items);
		mContext = context;
		mAnimators = new SparseArray<Animator>();

		mAnimationStartMillis = -1;
		mLastAnimatedPosition = -1;
	}

	public void setListView(ListView listView) {
		mListView = listView;
		if (mListView.getDivider() != null) {
			int dividerHeight = mListView.getDividerHeight();
			mListView.setDivider(null);
			mListView.setDividerHeight(dividerHeight);
		}
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		Assert.assertNotNull("Call setListView() on this AnimationAdapter before setAdapter()!", mListView);

		if (convertView != null) {
			int previousPosition = (Integer) convertView.getTag();
			Animator animator = mAnimators.get(previousPosition);
			if (animator != null) {
				animator.end();
			}
			mAnimators.remove(previousPosition);
		}

		View itemView = getItemView(position, convertView, parent);
		itemView.setTag(position);
		animateViewIfNecessary(position, itemView, parent);
		return itemView;
	}

	private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
		if (position > mLastAnimatedPosition) {
			animateView(parent, view);
			mLastAnimatedPosition = position;
		}
	}

	private void animateView(ViewGroup parent, View view) {
		if (mAnimationStartMillis == -1) {
			mAnimationStartMillis = System.currentTimeMillis();
		}

		hideView(view);

		Animator[] animators = getAnimators(parent, view);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(withAlphaAnimator(animators, view));
		set.setStartDelay(calculateAnimationDelay());
		set.start();

		mAnimators.put((Integer) view.getTag(), set);
	}

	private void hideView(View view) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0);
		AnimatorSet set = new AnimatorSet();
		set.play(animator);
		set.setDuration(0);
		set.start();
	}

	private Animator[] withAlphaAnimator(Animator[] animators, View view) {
		Animator[] allAnimators = new Animator[animators.length + 1];
		for (int i = 0; i < animators.length; ++i) {
			allAnimators[i] = animators[i];
		}

		allAnimators[animators.length] = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
		return allAnimators;
	}

	private long calculateAnimationDelay() {
		long delay;
		int numberOfItems = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition();
		if (numberOfItems + 1 < mLastAnimatedPosition) {
			delay = getAnimationDelayMillis();
		} else {
			long delaySinceStart = (mLastAnimatedPosition + 1) * getAnimationDelayMillis();
			delay = mAnimationStartMillis + INITIALDELAYMILLIS + delaySinceStart - System.currentTimeMillis();
		}
		return Math.max(0, delay);
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 * 
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set. You can either create a View manually or inflate it from an XML
	 * layout file. When the View is inflated, the parent View (GridView,
	 * ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	protected abstract View getItemView(int position, View convertView, ViewGroup parent);

	/**
	 * Get the delay in milliseconds before an animation of a view should start.
	 */
	protected abstract long getAnimationDelayMillis();

	/**
	 * Get the Animators to apply to the views. In addition to the returned
	 * Animators, an alpha transition will be applied to the view.
	 * 
	 * @param parent
	 *            The parent of the view
	 * @param view
	 *            The view that will be animated, as retrieved by getView()
	 */
	protected abstract Animator[] getAnimators(ViewGroup parent, View view);

}