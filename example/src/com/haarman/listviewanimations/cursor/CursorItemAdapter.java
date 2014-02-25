package com.haarman.listviewanimations.cursor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.DatabaseHelper;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.ExpandableCursorListAdapter;


public class CursorItemAdapter extends ExpandableCursorListAdapter {


    private Context mContext;

    public CursorItemAdapter(Context context, Cursor c) {
        super(context, c, R.id.button, R.layout.single_row_item, R.id.title, R.id.content);
        mContext = context;
    }


    public View getContentView(View convertView, ViewGroup parent, Cursor cursor) {
        TextView tv = (TextView) convertView;
        if (tv == null) {
            tv = new TextView(mContext);
        }
        tv.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PERSON_TABLE_COLUMN_ID)));
        return tv;
    }

    public View getTitleView(Context context, View convertView, ViewGroup parent, Cursor cursor) {
        View newView = LayoutInflater.from(context).inflate(R.layout.cursor_item, parent, false);
        TextView tv = (TextView)newView.findViewById(R.id.textView);
        tv.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PERSON_TABLE_COLUMN_NAME)));
        return newView;

    }


}
