package com.diamondsoftware.android.commuterhelper;

import java.util.ArrayList;

import com.diamondsoftware.android.commuterhelper.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryListAdapterII extends BaseAdapter {
	private ArrayList<HistoryListItem> mItems;
	private Activity mActivity;

	public HistoryListAdapterII(Activity activity,ArrayList<HistoryListItem> items) {
		mActivity=activity;
		mItems=items;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).getmRowId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view=null;
		HistoryListItem hli=(HistoryListItem)getItem(position);
		ViewHolder vh=null;
		if(convertView!=null && convertView.getTag()!=null) {
			vh=(ViewHolder)convertView.getTag();
			view=convertView;
			vh.historyHame.setText(hli.getmName());
		} else {
			LayoutInflater inflater=(LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view=inflater.inflate(R.layout.history_listitem, parent, false);
			TextView name=(TextView)view.findViewById(R.id.historyname);
			ImageView image=(ImageView)view.findViewById(R.id.historylist_image);
			TextView countUsed=(TextView)view.findViewById(R.id.historydescription);
			vh=new ViewHolder();
			vh.historyHame=name;
			vh.imageView=image;
			vh.countUsed=countUsed;
			view.setTag(vh);
		}
		vh.historyHame.setText(hli.getmName());
		vh.imageView.setImageResource(hli.ismIsStation()?R.drawable.train4_transparent:R.drawable.emptymapicon);
		vh.countUsed.setText("Usage count: "+ String.valueOf(hli.getmCount()));
		return view;
	}
	static class ViewHolder {
		  TextView historyHame;
		  int position;
		  ImageView imageView;
		  TextView countUsed;
		}
}
