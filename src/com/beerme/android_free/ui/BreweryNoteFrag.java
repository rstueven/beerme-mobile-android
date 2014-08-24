package com.beerme.android_free.ui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.beerme.android_free.R;
import com.beerme.android_free.database.BreweryNote;
import com.beerme.android_free.database.DbOpenHelper;
import com.beerme.android_free.database.TableDefs;
import com.beerme.android_free.utils.ErrLog;
import com.beerme.android_free.utils.Utils;

public class BreweryNoteFrag extends Fragment {
	private static final String TAG_NOTE = "note";
	protected static final int CONTEXT_MENU_ID = R.menu.note_context_menu;
	protected BreweryNotesFrag mBreweryNotesFrag = null;
	protected BreweryNote mNote = null;
	protected ArrayList<BreweryNoteCallbacks> mCallbacks = new ArrayList<BreweryNoteCallbacks>();

	public interface BreweryNoteCallbacks {
		public void onBreweryNoteDeleted();
	}

	// MEDIUM: AND0115: Add picture(s) to notes

	public static BreweryNoteFrag getInstance(
			BreweryNotesFrag breweryNotesFrag, BreweryNote note) {
		if (note == null) {
			throw new IllegalArgumentException("null note");
		}

		BreweryNoteFrag frag = null;

		if (Utils.SUPPORTS_HONEYCOMB) {
			frag = new BreweryNoteHC_Frag();
		} else {
			frag = new BreweryNoteFrag();
		}

		Bundle args = new Bundle();
		args.putSerializable(TAG_NOTE, note);
		frag.setArguments(args);
		return frag;
	}

	public void registerListener(BreweryNoteCallbacks callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void unregisterListener(BreweryNoteCallbacks callback) {
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}

	protected void setupContextMenu(ViewGroup layoutView) {
		registerForContextMenu(layoutView);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mNote = (BreweryNote) getArguments().getSerializable(TAG_NOTE);

		View view = inflater.inflate(R.layout.brewerynote_frag, container,
				false);

		TableLayout layoutView = (TableLayout) view
				.findViewById(R.id.brewerynote_layout);
		setupContextMenu(layoutView);

		TextView visitedView = (TextView) view
				.findViewById(R.id.brewerynote_visited);
		TextView ratingView = (TextView) view
				.findViewById(R.id.brewerynote_rating);
		TextView notesView = (TextView) view
				.findViewById(R.id.brewerynote_notes);

		visitedView.setText(mNote.getDate());
		ratingView.setText(getActivity().getString(R.string.N_out_of_5_points,
				Utils.toFrac(mNote.getRating())));
		notesView.setText(mNote.getNotes());

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(CONTEXT_MENU_ID, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			share();
			return true;
		case R.id.edit:
			edit();
			return true;
		case R.id.delete:
			delete();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	// MEDIUM: Share note
	protected void share() {
		// See BeerNote.java
	}

	protected void edit() {
		Intent intent = new Intent(getActivity(), EditableBreweryNote.class);
		intent.putExtra("note", mNote);
		startActivity(intent);
	}

	protected void delete() {
		DeleteDialog dialog = DeleteDialog.newInstance(mCallbacks,
				mNote.getId());
		dialog.show(getChildFragmentManager(), "deletebrewerynote");
	}

	public static class DeleteDialog extends DialogFragment {
		private ArrayList<BreweryNoteCallbacks> mCallbacks;
		private long mId = -1;

		public static DeleteDialog newInstance(
				ArrayList<BreweryNoteCallbacks> callbacks, long id) {
			DeleteDialog frag = new DeleteDialog();
			frag.mCallbacks = callbacks;
			frag.mId = id;
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog dialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle(R.string.Delete_note)
					.setCancelable(false)
					.setPositiveButton(R.string.Yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SQLiteDatabase db = null;
									try {
										DbOpenHelper helper = DbOpenHelper
												.getInstance(getActivity());
										db = helper.getWritableDatabase();
										db.delete(TableDefs.TABLE_BREWERYNOTES,
												"_id=?", new String[] { mId
														+ "" });
										for (BreweryNoteCallbacks callback : mCallbacks) {
											callback.onBreweryNoteDeleted();
										}
									} catch (SQLException e) {
										ErrLog.log(
												getActivity(),
												"BeerNoteFrag.onCreateDialog.onClick",
												e, R.string.Database_problem);
									} finally {
										if (db != null) {
											db.close();
										}
									}
								}
							})
					.setNegativeButton(R.string.No,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});

			dialog = builder.create();

			return dialog;
		}
	}
}