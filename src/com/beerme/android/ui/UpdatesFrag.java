package com.beerme.android.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.database.TableDefs;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.UpdatesItem;
import com.beerme.android.utils.Utils;

public class UpdatesFrag extends Fragment {
	private final static int LOAD_START = 1;
	private final static int LOAD_END = 2;
	private ArrayList<UpdatesItem> mList = null;
	private ListView mListView = null;
	private UpdatesAdapter mAdapter;
	private static UpdatesHandler mHandler = null;
	private Thread mLoadThread = null;

	public static UpdatesFrag getInstance() {
		UpdatesFrag frag = new UpdatesFrag();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new UpdatesHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_frag, container, false);

		mListView = (ListView) view;
		mListView.setOnItemClickListener(itemClickListener);

		if (mList == null) {
			mHandler.sendEmptyMessage(LOAD_START);
		} else {
			mHandler.sendEmptyMessage(LOAD_END);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mList != null) {
			mHandler.sendEmptyMessage(LOAD_START);
		}
	}

	@Override
	public void onDestroy() {
		if (mLoadThread != null) {
			if (mLoadThread.isAlive()) {
				mLoadThread.interrupt();
			}
		}

		super.onDestroy();
	}

	private Runnable load = new Runnable() {
		private static final String TABLE = TableDefs.TABLE_BREWERY;
		private final String[] COLUMNS = { "_id", "name", "address" };
		private final static String ORDERBY = "updated desc";
		private final static String LIMIT = "100";

		@Override
		public void run() {
			boolean interrupted = false;

			mList = new ArrayList<UpdatesItem>();

			SQLiteDatabase db = null;
			Cursor cursor = null;

			try {
				db = DbOpenHelper.getInstance(getActivity())
						.getReadableDatabase();
				cursor = db.query(TABLE, COLUMNS, null, null, null, null,
						ORDERBY, LIMIT);

				while (cursor.moveToNext()) {
					if (Thread.interrupted()) {
						interrupted = true;
						break;
					}
					mList.add(new UpdatesItem(cursor.getLong(0), cursor
							.getString(1), cursor.getString(2)));
				}
			} catch (SQLiteException e) {
				ErrLog.log(getActivity(), "UpdatesFrag.load.run", e,
						R.string.Database_is_busy);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				if (db != null) {
					db.close();
				}

				if (!interrupted) {
					mHandler.sendEmptyMessage(LOAD_END);
				}
			}
		}
	};

	public class UpdatesAdapter extends ArrayAdapter<UpdatesItem> {
		private Context context;
		private int textViewResourceId;
		private ArrayList<UpdatesItem> items;

		public UpdatesAdapter(Context context, int textViewResourceId,
				ArrayList<UpdatesItem> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.textViewResourceId = textViewResourceId;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
			}

			UpdatesItem item = items.get(position);
			if (item != null) {
				TextView name = (TextView) v
						.findViewById(R.id.update_item_name);
				name.setText(item.getName());
				TextView addr = (TextView) v
						.findViewById(R.id.update_item_addr);
				addr.setText(item.getAddr());
			}

			return v;
		}
	}

	public OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(), BreweryActivity.class);
			intent.putExtra("id", mList.get(position).getBreweryid());
			startActivity(intent);
		}
	};

	static class UpdatesHandler extends Handler {
		WeakReference<UpdatesFrag> mRef;

		UpdatesHandler(UpdatesFrag frag) {
			mRef = new WeakReference<UpdatesFrag>(frag);
		}

		@Override
		public void handleMessage(Message msg) {
			UpdatesFrag frag = mRef.get();
			Activity activity = frag.getActivity();
			ArrayList<UpdatesItem> list = frag.mList;
			UpdatesAdapter adapter = frag.mAdapter;
			ListView listView = frag.mListView;
			switch (msg.what) {
			case LOAD_START:
				frag.mLoadThread = new Thread(frag.load, "LoadUpdates");
				frag.mLoadThread.start();
				break;
			case LOAD_END:
				adapter = frag.new UpdatesAdapter(activity,
						R.layout.updates_item, list);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
}