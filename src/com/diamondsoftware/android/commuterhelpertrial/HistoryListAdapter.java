package com.diamondsoftware.android.commuterhelpertrial;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;

public class HistoryListAdapter extends ArrayAdapter<HistoryListItem> {
	private ArrayList<HistoryListItem> mItems;
	public HistoryListAdapter(Context context, ArrayList<HistoryListItem> items) {
		super(context,  android.R.layout.activity_list_item, android.R.id.text1,items);
		mItems=items;
	}
	
	@Override
	public HistoryListItem getItem(int position) {
		return mItems.get(position);
	}
	public void addItem(HistoryListItem item) {
		mItems.add(item);
	}
	@Override
	public int getCount() {
		return mItems.size();
	}
	@Override
	public long getItemId(int position) {
		return mItems.get(position).getmRowId();
	}
}
