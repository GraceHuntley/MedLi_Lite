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
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private MedLiDataSource dbHelper;
    private LinearLayout flashScreen;
    private OnFragmentInteractionListener mListener;
    private boolean flashTheScreen = false;

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

    public void onEditMedListener(Object_MedLog medLog) {

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


        dbHelper = MedLiDataSource.getHelper(getActivity());

        List<Object_MedLog> loggedMedsList = dbHelper.getMedHistory(1);

        if (loggedMedsList.size() == 0) {
            flashTheScreen = true;
        }

        mAdapter = new CustomAdapterHistory(getActivity(), loggedMedsList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Set the adapter

        mListView = (ListView) view.findViewById(android.R.id.list);
        flashScreen = (LinearLayout) view.findViewById(R.id.flash_screen);
        historyHeader = (TextView) view.findViewById(R.id.history_header);


        if (!flashTheScreen) {
            flashScreen.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            historyHeader.setVisibility(View.VISIBLE);
            mListView.setAdapter(mAdapter);
        } else {
            mListView.setVisibility(View.GONE);
            historyHeader.setVisibility(View.GONE);
            flashScreen.setVisibility(View.VISIBLE);
        }

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

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

                }
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int id);
    }


}

class CustomAdapterHistory extends BaseAdapter {

    Context context;
    List<Object_MedLog> data;
    private MedLiDataSource dbHelper;

    CustomAdapterHistory(Context context, List<Object_MedLog> rowItem) {
        this.context = context;
        this.data = rowItem;
        dbHelper = MedLiDataSource.getHelper(this.context);
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        if (data.size() > 0)
            return data.get(position);
        else
            return null;

    }

    @Override
    public long getItemId(int position) {

        return data.indexOf(getItem(position));
    }


    public void removeItem(int itemPosition) {

        data.remove(itemPosition);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.history_list_item, null);
        }
        Helper_DateTime dt = new Helper_DateTime();

        TextView medName = (TextView) convertView.findViewById(R.id.name);
        final TextView doseTime = (TextView) convertView.findViewById(R.id.dose_time);
        final TextView dose = (TextView) convertView.findViewById(R.id.dose);
        final TextView message = (TextView) convertView.findViewById(R.id.message);

        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        ImageView delButton = (ImageView) convertView.findViewById(R.id.button_delete_med_admin);
        ImageView editButton = (ImageView) convertView.findViewById(R.id.button_edit_med_admin);

        final Object_MedLog dataItem = data.get(position);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(context, "Long press button to Edit Record", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(context, "Long Press button to delete Record", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        editButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {


                return false;
            }
        });

        delButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                dbHelper.deleteMedEntry(dataItem.getUniqueID());

                removeItem(position);

                Toast.makeText(context, "Object Deleted", Toast.LENGTH_LONG).show();

                return false;
            }
        });

        if (dataItem.isSubHeading()) {

            medName.setText(dt.getReadableDate(dataItem.getDateOnly()));
            boxWrapper.setBackgroundResource(android.R.color.background_light);

        } else {
            String wasMissed = dataItem.isWasMissed() ? "Missed" : "";

            String messageNote = dataItem.isWasMissed() ? "MISSED" : dataItem.isLate() ? "LATE" : "ON-TIME";
            medName.setText(Helper_DataCheck.capitalizeTitles(dataItem.getName()));
            doseTime.setText(dt.convertToTime12(dataItem.getTimeOnly()));
            dose.setText(dataItem.getDose());
            message.setText(messageNote);
            boxWrapper.setBackgroundResource(android.R.color.white);

        }
        return convertView;

    }

    public interface onEditMedListener {
        public void onEditMed(Object_MedLog medLog);
    }

}

