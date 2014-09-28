package com.beerme.android.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beerme.android.R;
import com.beerme.android.database.BeerNote;
import com.beerme.android.database.BeerNote.Source;
import com.beerme.android.database.DbOpenHelper;
import com.beerme.android.database.TableDefs;
import com.beerme.android.utils.ErrLog;
import com.beerme.android.utils.URIDispatcher;
import com.beerme.android.utils.Utils;

public class BeerNoteFrag extends Fragment {
	private static final String TAG_NOTE = "note";
	protected static final int CONTEXT_MENU_ID = R.menu.note_context_menu;
	protected BeerNote mNote = null;
	protected ArrayList<BeerNoteCallbacks> mCallbacks = new ArrayList<BeerNoteCallbacks>();

	public interface BeerNoteCallbacks {
		public void onBeerNoteDeleted();
	}

	// MEDIUM: AND0115: Add picture(s) to notes

	public static BeerNoteFrag getInstance(BeerNote note) {
		if (note == null) {
			throw new IllegalArgumentException("null note");
		}

		BeerNoteFrag frag = null;

		if (Utils.SUPPORTS_HONEYCOMB) {
			frag = new BeerNoteHC_Frag();
		} else {
			frag = new BeerNoteFrag();
		}

		Bundle args = new Bundle();
		args.putSerializable(TAG_NOTE, note);
		frag.setArguments(args);
		return frag;
	}

	public void registerListener(BeerNoteCallbacks callback) {
		if (!mCallbacks.contains(callback)) {
			mCallbacks.add(callback);
		}
	}

	public void unregisterListener(BeerNoteCallbacks callback) {
		if (mCallbacks.contains(callback)) {
			mCallbacks.remove(callback);
		}
	}

	protected void setupContextMenu(ViewGroup layoutView) {
		registerForContextMenu(layoutView);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Bundle args = getArguments();
		if (args != null) {
			mNote = (BeerNote) args.getSerializable(TAG_NOTE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.trackFragment(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.beernote_frag, container, false);

		if (mNote.getSource() == Source.MY) {
			RelativeLayout layoutView = (RelativeLayout) view
					.findViewById(R.id.beernote_layout);
			setupContextMenu(layoutView);
		}

		TextView starsView = (TextView) view.findViewById(R.id.beernote_score);
		TextView sampledView = (TextView) view
				.findViewById(R.id.beernote_sampled);
		TextView pkgView = (TextView) view.findViewById(R.id.beernote_pkg);
		TextView placeView = (TextView) view.findViewById(R.id.beernote_place);
		TextView appRatingView = (TextView) view
				.findViewById(R.id.beernote_appRating);
		TextView appearanceView = (TextView) view
				.findViewById(R.id.beernote_appearance);
		TextView aroRatingView = (TextView) view
				.findViewById(R.id.beernote_aroRating);
		TextView aromaView = (TextView) view.findViewById(R.id.beernote_aroma);
		TextView mouRatingView = (TextView) view
				.findViewById(R.id.beernote_mouRating);
		TextView mouthfeelView = (TextView) view
				.findViewById(R.id.beernote_mouthfeel);
		TextView ovrRatingView = (TextView) view
				.findViewById(R.id.beernote_ovrRating);
		TextView notesView = (TextView) view.findViewById(R.id.beernote_notes);

		starsView.setText(getActivity().getString(R.string.N_out_of_20_points,
				Utils.toFrac(mNote.getScore())));
		sampledView.setText(mNote.getSampled());
		pkgView.setText(mNote.getPkg());
		placeView.setText(mNote.getPlace());
		appRatingView.setText(getActivity().getString(R.string.N_outOfThree,
				Utils.toFrac(mNote.getAppscore())));
		Utils.setTextOrGone(appearanceView,
				URIDispatcher.rewriteUri(mNote.getAppearance()));
		appearanceView.setLinksClickable(true);
		appearanceView.setMovementMethod(LinkMovementMethod.getInstance());
		aroRatingView.setText(getActivity().getString(R.string.N_outOfFour,
				Utils.toFrac(mNote.getAroscore())));
		Utils.setTextOrGone(aromaView,
				URIDispatcher.rewriteUri(mNote.getAroma()));
		aromaView.setLinksClickable(true);
		aromaView.setMovementMethod(LinkMovementMethod.getInstance());
		mouRatingView.setText(getActivity().getString(R.string.N_outOfTen,
				Utils.toFrac(mNote.getMouscore())));
		Utils.setTextOrGone(mouthfeelView,
				URIDispatcher.rewriteUri(mNote.getMouthfeel()));
		mouthfeelView.setLinksClickable(true);
		mouthfeelView.setMovementMethod(LinkMovementMethod.getInstance());
		ovrRatingView.setText(getActivity().getString(R.string.N_outOfThree,
				Utils.toFrac(mNote.getOvrscore())));
		Utils.setTextOrGone(notesView,
				URIDispatcher.rewriteUri(mNote.getNotes()));
		notesView.setLinksClickable(true);
		notesView.setMovementMethod(LinkMovementMethod.getInstance());

		// http://code.google.com/p/android/issues/detail?id=3414#c27
		((ViewGroup) view)
				.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

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

	// MEDIUM: Option to share via text
	protected void share() {
		ShareCompat.IntentBuilder.from(getActivity()).setType("text/html")
				.setSubject("Beer Me!™ Beer Note")
				.setText(mNote.toHtml(getActivity())).startChooser();
	}

	protected void edit() {
		Intent intent = new Intent(getActivity(), EditableBeerNote.class);
		intent.putExtra("note", mNote);
		startActivity(intent);
	}

	protected void delete() {
		DeleteDialog dialog = DeleteDialog.newInstance(mCallbacks,
				mNote.getId());
		dialog.show(getChildFragmentManager(), "deletebeernote");
	}

	public static class DeleteDialog extends DialogFragment {
		private ArrayList<BeerNoteCallbacks> mCallbacks;
		private long mId = -1;

		public static DeleteDialog newInstance(
				ArrayList<BeerNoteCallbacks> callbacks, long id) {
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
										db.delete(TableDefs.TABLE_BEERNOTES,
												"_id=?", new String[] { mId
														+ "" });
										for (BeerNoteCallbacks callback : mCallbacks) {
											callback.onBeerNoteDeleted();
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