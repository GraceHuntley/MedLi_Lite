package com.moorango.medli.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Helpers.Helper_DataCheck;
import com.moorango.medli.Helpers.Helper_DateTime;
import com.moorango.medli.Models.Object_MedLog;
import com.moorango.medli.R;

import java.util.List;

public class Fragment_History extends Fragment implements AbsListView.OnItemClickListener {

    private final String TAG = "LogFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView historyHeader;

    private MedLiDataSource dbHelper;
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

    // TODO: Rename and change types of parameters
    public static Fragment_History newInstance() {

        return new Fragment_History();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbHelper = MedLiDataSource.getHelper(getActivity());

        List<Object_MedLog> loggedMedsList = dbHelper.getMedHistory();

        for (Object_MedLog log : loggedMedsList) { // mark if any meds were missed.
            boolean areMedsMissed = false;
            if (areMedsMissed = log.isWasMissed()) break;
        }


        if (loggedMedsList.size() == 0) {
            flashTheScreen = true;
        }

        mAdapter = new CustomAdapterHistory(getActivity(), loggedMedsList, this);

    }

    public void editMedAdmin(final Object_MedLog medLog) {

        final LinearLayout editMedBox = (LinearLayout) getActivity().findViewById(R.id.edit_med_admin);
        final TextView historyHeader = (TextView) getActivity().findViewById(R.id.history_header);
        final TimePicker timePicker = (TimePicker) getActivity().findViewById(R.id.time_picker_edit_med);
        final EditText editDoseBox = (EditText) getActivity().findViewById(R.id.edit_dose_box);
        Button submitChanges = (Button) getActivity().findViewById(R.id.submit_changes);
        Button cancelChanges = (Button) getActivity().findViewById(R.id.cancel_edit);

        historyHeader.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        editMedBox.setVisibility(View.VISIBLE);


        String splitTime[] = medLog.getTimeOnly().split(":");
        timePicker.setCurrentHour(Integer.valueOf(splitTime[0]));
        timePicker.setCurrentMinute(Integer.valueOf(splitTime[1]));

        editDoseBox.setText(medLog.getDose());

        submitChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Object_MedLog editLog = medLog;
                historyHeader.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                editMedBox.setVisibility(View.GONE);

                /*** TODO need to validate data from inputs later. ***/
                editLog.setDose(editDoseBox.getText().toString());

                String formattedHour = String.format("%02d", timePicker.getCurrentHour());
                String formattedMinute = String.format("%02d", timePicker.getCurrentMinute());
                editLog.setTimestamp(editLog.getDateOnly() + " " + formattedHour + ":" + formattedMinute + ":" + "00");

                if (dbHelper.updateMedicationAdmin(editLog) > -1) {
                    mListener.onFragmentInteraction(4, null, 0);
                    Toast.makeText(getActivity(), "Changes submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "There was an Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                historyHeader.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                editMedBox.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Set the adapter

        mListView = (ListView) view.findViewById(android.R.id.list);
        LinearLayout flashScreen = (LinearLayout) view.findViewById(R.id.flash_screen);
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
                    Log.i(TAG, "stopped scrolling");

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
        public void onFragmentInteraction(int id, String name, int holder);
    }

}

class CustomAdapterHistory extends BaseAdapter {

    private final Context context;
    private final List<Object_MedLog> data;
    private final MedLiDataSource dbHelper;
    private final Fragment_History caller;

    CustomAdapterHistory(Context context, List<Object_MedLog> rowItem, Fragment_History caller) {
        this.context = context;
        this.data = rowItem;
        this.caller = caller;
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

        //noinspection SuspiciousMethodCalls
        return data.indexOf(getItem(position));
    }


    public void removeItem(int itemPosition) {

        data.remove(itemPosition);
        notifyDataSetChanged();
    }

    @SuppressLint("InflateParams")
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

                caller.editMedAdmin(dataItem);

                return true;
            }
        });

        delButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                dbHelper.deleteMedEntry(dataItem.getUniqueID());

                removeItem(position);

                Toast.makeText(context, "Object Deleted", Toast.LENGTH_LONG).show();

                return true;
            }
        });

        if (dataItem.isSubHeading()) {

            medName.setText(dt.getReadableDate(dataItem.getDateOnly()));
            boxWrapper.setBackgroundResource(android.R.color.background_light);
            editButton.setVisibility(View.GONE);
            delButton.setVisibility(View.GONE);

        } else {

            String messageNote = dataItem.isWasMissed() ? "MISSED" : dataItem.isWasManual() ? "MANUAL ENTRY" : dataItem.isLate() ? "LATE" : "ON-TIME";
            medName.setText(Helper_DataCheck.capitalizeTitles(dataItem.getName()));
            doseTime.setText(Helper_DateTime.convertToTime12(dataItem.getTimeOnly()));
            dose.setText(dataItem.getDose());
            message.setText(messageNote);
            boxWrapper.setBackgroundResource(android.R.color.white);
            editButton.setVisibility(View.VISIBLE);
            delButton.setVisibility(View.VISIBLE);

        }
        return convertView;

    }
}
