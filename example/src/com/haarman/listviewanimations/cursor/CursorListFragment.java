package com.haarman.listviewanimations.cursor;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.DatabaseHelper;
import com.haarman.listviewanimations.R;

public class CursorListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private CursorItemAdapter adapter;
    private DatabaseHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cursor_list, container, false );
        helper = new DatabaseHelper(getActivity().getApplicationContext());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        getLoaderManager().restartLoader( 1, null, this );
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader( getActivity().getApplicationContext(), Uri.parse("content://" + DatabaseHelper.DATABASE_NAME+"/"+ DatabaseHelper.TABLE_NAME), null, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter = new CursorItemAdapter( getActivity(), cursor );
        setListAdapter( adapter );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
