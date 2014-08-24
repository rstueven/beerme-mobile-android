package com.beerme.android_free.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.beerme.android_free.R;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.NewsItem;
import com.beerme.android_free.utils.Utils;

public class NewsFrag extends Fragment {
	private final static int LOAD_START = 1;
	private final static int LOAD_END = 2;
	private ArrayList<NewsItem> mList = null;
	private ListView mListView = null;
	private NewsAdapter mAdapter;
	private static NewsHandler mHandler = null;
	private Thread mLoadThread = null;

	public static NewsFrag getInstance() {
		NewsFrag frag = new NewsFrag();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new NewsHandler(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
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
		if (mLoadThread != null && mLoadThread.isAlive()) {
			mLoadThread.interrupt();
		}

		super.onDestroy();
	}

	private Runnable load = new Runnable() {
		private int DEFAULT_BUFFER_SIZE = 128 * 1024;

		@Override
		public void run() {
			String[] fields;
			String line;
			BufferedReader reader = null;
			boolean interrupted = false;

			mList = new ArrayList<NewsItem>();

			try {
				URLConnection conn = new URL(Utils.NEWS_URL).openConnection();
				InputStream is = conn.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				reader = new BufferedReader(isr, DEFAULT_BUFFER_SIZE);

				while ((line = reader.readLine()) != null) {
					if (Thread.interrupted()) {
						interrupted = true;
						break;
					}
					fields = line.split("\\|", -1);
					mList.add(new NewsItem(fields));
				}
			} catch (MalformedURLException e) {
				ErrLog.log(getActivity(), "load.run()", e,
						R.string.Network_problem);
			} catch (IOException e) {
				ErrLog.log(getActivity(), "load.run()", e,
						R.string.Network_problem);
			} catch (ParseException e) {
				ErrLog.log(getActivity(), "load.run()", e,
						R.string.Data_problem);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					// Ignore
				}

				if (!interrupted) {
					mHandler.sendEmptyMessage(LOAD_END);
				}
			}
		}
	};

	public class NewsAdapter extends ArrayAdapter<NewsItem> {
		private Context context;
		private int textViewResourceId;
		private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

		public NewsAdapter(Context context, int textViewResourceId,
				ArrayList<NewsItem> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
			}

			NewsItem item = getItem(position);
			if (item != null) {
				TextView title = (TextView) v
						.findViewById(R.id.news_item_title);
				title.setText(item.getTitle());
				TextView source = (TextView) v
						.findViewById(R.id.news_item_source);
				source.setText(item.getSource() + " — "
						+ df.format(item.getDate()));
			}

			return v;
		}
	}

	public OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mList.get(
					position).getUrl())));
		}
	};

	static class NewsHandler extends Handler {
		WeakReference<NewsFrag> mRef;

		NewsHandler(NewsFrag fragment) {
			mRef = new WeakReference<NewsFrag>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			NewsFrag frag = mRef.get();
			Activity activity = frag.getActivity();
			ArrayList<NewsItem> list = frag.mList;
			NewsAdapter adapter = frag.mAdapter;
			ListView listView = frag.mListView;
			switch (msg.what) {
			case LOAD_START:
				frag.mLoadThread = new Thread(frag.load, "LoadNews");
				frag.mLoadThread.start();
				break;
			case LOAD_END:
				adapter = frag.new NewsAdapter(activity, R.layout.news_item,
						list);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
}