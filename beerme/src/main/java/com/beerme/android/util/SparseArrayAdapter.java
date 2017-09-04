package com.beerme.android.util;

import android.util.SparseArray;
import android.widget.BaseAdapter;

/**
 * Created by rstueven on 9/4/17.
 * <p>Adapter based on a SparseArray</p>
 * <p>https://stackoverflow.com/questions/21677866/how-to-use-sparsearray-as-a-source-for-adapter</p>
 */

public abstract class SparseArrayAdapter<E> extends BaseAdapter {
    private SparseArray<E> mData;

    public void setData(final SparseArray<E> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public E getItem(final int position) {
        return mData.valueAt(position);
    }

    @Override
    public long getItemId(final int position) {
        return mData.keyAt(position);
    }
}