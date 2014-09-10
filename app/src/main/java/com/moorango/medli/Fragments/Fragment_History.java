package com.moorango.medli.Fragments;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.R;

import java.util.List;

public class Fragment_History extends Fragment {

    private final String TAG = "LogFragment";

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

        List<MedLog> loggedMedsList = dbHelper.getMedHistory();

        if (loggedMedsList.size() == 0) {
            flashTheScreen = true;
        }

        mAdapter = new com.moorango.medli.CustomViews.CustomAdapterHistory(getActivity(), loggedMedsList, this);

    }

    public void editMedAdmin(final MedLog medLog) {

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

                historyHeader.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                editMedBox.setVisibility(View.GONE);

                /*** TODO need to validate data from inputs later. ***/
                medLog.setDose(editDoseBox.getText().toString());

                String formattedHour = String.format("%02d", timePicker.getCurrentHour());
                String formattedMinute = String.format("%02d", timePicker.getCurrentMinute());
                medLog.setTimestamp(medLog.getDateOnly() + " " + formattedHour + ":" + formattedMinute + ":" + "00");

                if (dbHelper.updateMedicationAdmin(medLog) > -1) {
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {


                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mAdapter.getCount() > 0) { // do not perform this on an empty list.
                    MedLog mLog = (MedLog) mAdapter.getItem(firstVisibleItem);

                    historyHeader.setText(DateTime.getReadableDate(mLog.getDateOnly()));

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int id, String name, int holder);
    }

}

