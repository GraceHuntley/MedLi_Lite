package com.moorango.medli;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Fragment_History extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private final String TAG = "LogFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView historyHeader;
    private int selectionForView = 0;

    private boolean isScrolling = false;
    private boolean hideHeader = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MedLiDataSource dbHelper;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Fragment_History() {
    }

    // TODO: Rename and change types of parameters
    public static Fragment_History newInstance(String param1, String param2) {
        Fragment_History fragment = new Fragment_History();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dbHelper = MedLiDataSource.getHelper(getActivity());

        List<Object_MedLog> loggedMedsList = dbHelper.getMedHistory(1);

        mAdapter = new CustomAdapterHistory(getActivity(), loggedMedsList);
        // TODO: Change Adapter to display your content
        /*mAdapter = new ArrayAdapter<MedLog>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, loggedMedsList); */


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Set the adapter

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        Toast toast = Toast.makeText(getActivity(), "Long press Entry to Edit or Delete", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView tv = new TextView(getActivity());
        tv.setText("Long press Entry to Edit or Delete!");
        tv.setPadding(5, 5, 5, 5);
        tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        tv.setBackgroundResource(android.R.color.white);

        toast.setView(tv);
        toast.show();
        historyHeader = (TextView) getActivity().findViewById(R.id.history_header);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //Log.d(TAG, "stopped scrolling");


                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mAdapter.getCount() > 0) { // do not perform this on an empty list.
                    Object_MedLog mLog = (Object_MedLog) mAdapter.getItem(firstVisibleItem);

                    Helper_DateTime dt = new Helper_DateTime();

                    historyHeader.setText(dt.getReadableDate(mLog.getDateOnly()));

                    selectionForView = firstVisibleItem;


                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!((Object_MedLog) mListView.getItemAtPosition(position)).isSubHeading()) {
                    dbHelper.deleteMedEntry(((Object_MedLog) mListView.getItemAtPosition(position)).getUniqueID());
                    mListener.onFragmentInteraction(1);
                }
                return false;
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            if (!((Object_MedLog) mListView.getItemAtPosition(position)).isSubHeading()) {
                //mListener.onFragmentInteraction(1);
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int id);
    }

}

class CustomAdapterHistory extends BaseAdapter {

    Context context;
    List<Object_MedLog> data;

    CustomAdapterHistory(Context context, List<Object_MedLog> rowItem) {
        this.context = context;
        this.data = rowItem;

    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Object getItem(int position) {

        return data.get(position);
    }

    @Override
    public long getItemId(int position) {

        return data.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item, null);
        }
        Helper_DateTime dt = new Helper_DateTime();

        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        Object_MedLog dataItem = data.get(position);

        if (dataItem.isSubHeading()) {

            txtTitle.setText(dt.getReadableDate(dataItem.getDateOnly()));
            boxWrapper.setBackgroundResource(android.R.color.background_light);

        } else {
            String wasMissed = dataItem.isWasMissed() ? "Missed" : "";
            txtTitle.setText(Helper_DataCheck.capitalizeTitles(dataItem.getName()) + " " + dt.convertToTime12(dataItem.getTimeOnly()) + " " + wasMissed);
            boxWrapper.setBackgroundResource(android.R.color.white);

        }
        return convertView;

    }

}

