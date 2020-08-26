package com.beerme.android.ui.tripplanner;

import androidx.fragment.app.Fragment;

public class DirectionsDisplayFragment extends Fragment {
//    private Activity mActivity;
//    private ExpandableListView mListView;
//    private Directions directions;
//    List<Route> routes;
//    List<Leg> legs;
//
//    public static DirectionsDisplayFragment newInstance(String savedDirections) {
//        DirectionsDisplayFragment f = new DirectionsDisplayFragment();
//
//        Bundle args = new Bundle();
//        args.putString(TripPlannerFrag.DIRECTIONS_FILE_TAG, savedDirections);
//        f.setArguments(args);
//
//        return f;
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        mActivity = activity;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setRetainInstance(true);
//
//        Bundle args = getArguments();
//        if (args != null) {
//            String dirFile = args.getString(TripPlannerFrag.DIRECTIONS_FILE_TAG);
//            directions = TripPlannerFrag.loadDirections(mActivity, dirFile);
//        }
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Utils.trackFragment(this);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        routes = directions.getRoutes();
//        Route route = routes.get(0);
//
//        legs = route.getLegs();
//
//        mListView.setAdapter(new DirectionsDisplayAdapter(mActivity, legs));
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.directionsdisplayfragment, container, false);
//        setHasOptionsMenu(true);
//
//        mListView = view.findViewById(R.id.directionslist);
//
//        return view;
//    }
//
//    // http://stackoverflow.com/questions/18594744/how-to-navigate-of-a-map-android-maps-api-v2
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.directions_actions, menu);
//        if (!Utils.isNavigationAvailable(getActivity())) {
//            menu.findItem(R.id.action_navigation).setVisible(false);
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_navigation:
//                Leg lastLeg = legs.get(legs.size() - 1);
//                LatLng endLoc = lastLeg.getEndLocation();
//                double lat = endLoc.latitude;
//                double lng = endLoc.longitude;
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + lat + "," + lng));
//                startActivity(intent);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    // http://about-android.blogspot.ca/2010/04/steps-to-implement-expandablelistview.html
//    public class DirectionsDisplayAdapter extends BaseExpandableListAdapter {
//        private Context mContext;
//        private List<Leg> mLegs;
//        private List<List<Step>> mSteps = new ArrayList<>();
//
//        DirectionsDisplayAdapter(Context context, List<Leg> legs) {
//            this.mContext = context;
//            this.mLegs = legs;
//
//            for (Leg leg : mLegs) {
//                mSteps.add(loadSteps(leg));
//            }
//        }
//
//        private List<Step> loadSteps(Leg leg) {
//            return new ArrayList<>(leg.getSteps());
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            return mSteps.get(groupPosition).get(childPosition);
//        }
//
//        @Override
//        public long getChildId(int groupPosition, int childPosition) {
//            return childPosition;
//        }
//
//        public TextView getGenericView() {
//            // Layout parameters for the ExpandableListView
//            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64);
//
//            TextView textView = new TextView(mContext);
//            textView.setLayoutParams(lp);
//            // Center the text vertically
//            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
//            textView.setTextColor(0xffff0000);
//            // Set the text starting position
//            textView.setPadding(36, 0, 0, 0);
//            return textView;
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//            // TextView textView = getGenericView();
//            // textView.setText(getGroup(groupPosition).toString());
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View groupView = inflater.inflate(R.layout.directionsdisplaygroup, parent, false);
//
//            Leg leg = mLegs.get(groupPosition);
//
//            TextView originView = groupView.findViewById(R.id.origin);
//            TextView destinationView = groupView.findViewById(R.id.destination);
//            TextView distanceView = groupView.findViewById(R.id.distance);
//            TextView durationView = groupView.findViewById(R.id.duration);
//
//            originView.setText(String.format(Locale.getDefault(), getString(R.string.From), leg.getStartAddress()));
//            destinationView.setText(String.format(Locale.getDefault(), getString(R.string.To), leg.getEndAddress()));
//            distanceView.setText(leg.getDistanceText());
//            durationView.setText(leg.getDurationText());
//
//            return groupView;
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View groupView = inflater.inflate(R.layout.directionsdisplayitem, parent, false);
//
//            Step step = mSteps.get(groupPosition).get(childPosition);
//
//            ImageView imageView = groupView.findViewById(R.id.direction_image);
//            TextView textView = groupView.findViewById(R.id.direction_text);
//            TextView distanceView = groupView.findViewById(R.id.direction_distance);
//            TextView durationView = groupView.findViewById(R.id.direction_duration);
//
//            textView.setText(Html.fromHtml(step.getHtmlInstructions()));
//            distanceView.setText(step.getDistanceText());
//            durationView.setText(step.getDurationText());
//
//            int maneuverImage = R.drawable.ic_blank;
//            String maneuver = step.getManeuver();
//
//            if (maneuver != null) {
//                switch (maneuver) {
//                    case "turn-left":
//                        maneuverImage = R.drawable.ic_turn_left;
//                        break;
//                    case "turn-right":
//                        maneuverImage = R.drawable.ic_turn_right;
//                        break;
//                    case "fork-left":
//                        maneuverImage = R.drawable.ic_fork_left;
//                        break;
//                    case "fork-right":
//                        maneuverImage = R.drawable.ic_fork_right;
//                        break;
//                    case "turn-slight-left":
//                        maneuverImage = R.drawable.ic_turn_slight_left;
//                        break;
//                    case "turn-slight-right":
//                        maneuverImage = R.drawable.ic_turn_slight_right;
//                        break;
//                    case "merge":
//                        maneuverImage = R.drawable.ic_merge;
//                        break;
//                    case "straight":
//                        maneuverImage = R.drawable.ic_straight;
//                        break;
//                    case "ramp":  /* *** */
//                        break;
//                    case "ramp-left":
//                        maneuverImage = R.drawable.ic_ramp_left;
//                        break;
//                    case "ramp-right":
//                        maneuverImage = R.drawable.ic_ramp_right;
//                        break;
//                    case "keep-left":  /* *** */
//                        break;
//                    case "keep-right":  /* *** */
//                        break;
//                }
//            }
//
//            imageView.setImageResource(maneuverImage);
//
//            return groupView;
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            int i = 0;
//            try {
//                i = mSteps.get(groupPosition).size();
//            } catch (IndexOutOfBoundsException e) {
//                // Log.i(Utils.APPTAG, e.getLocalizedMessage());
//                // Ignore
//            }
//
//            return i;
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            return mLegs.get(groupPosition);
//        }
//
//        @Override
//        public int getGroupCount() {
//            return mLegs.size();
//        }
//
//        @Override
//        public long getGroupId(int groupPosition) {
//            return groupPosition;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return true;
//        }
//
//        @Override
//        public boolean isChildSelectable(int groupPosition, int childPosition) {
//            return true;
//        }
//    }
}