package com.moorango.medli;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Fragment_Home extends Fragment {

    private final String TAG = "Home.java";
    private static ArrayList<Object_Medication> chosenList;
    private static MyAsyncTask updateLists;
    private OnFragmentInteractionListener mListener;
    private ListView routineList;
    private MedLiDataSource dataSource;
    private Button submitMed;
    private HomeCustomAdapter adapter;
    private SparseBooleanArray routineChoicesFromInstance = null;


    public Fragment_Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (savedInstanceState != null) {
            routineChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("routine_array");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        routineList = (ListView) getActivity().findViewById(R.id.routine_listview);
        Button clearChoices = (Button) getActivity().findViewById(R.id.clear_button);
        submitMed = (Button) getActivity().findViewById(R.id.submit_button);
        chosenList = new ArrayList<Object_Medication>();

        dataSource = MedLiDataSource.getHelper(getActivity());

        if (updateLists == null || !updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists = new MyAsyncTask();
            updateLists.execute();
        } else {
            updateLists.cancel(true);
            updateLists = null;
            updateLists = new MyAsyncTask();
            updateLists.execute();
        }

        routineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (!adapter.getItem(position).isSubHeading()) {
                    adapter.toggleChecked(position);
                }

            }
        });

        clearChoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int index = 0; index < routineList.getCount(); index++) {
                    routineList.setItemChecked(index, false);
                    adapter.clearChoices();
                }

                chosenList.clear();
            }
        });

        submitMed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                grabChoices();

                if (chosenList.size() > 0) {

                    String for_display = new Object() {
                        String getReady() {

                            String ready = "";
                            for (Object_Medication aChosenList : chosenList) {
                                ready += aChosenList.getMedName() + "\n";
                            }

                            return ready;
                        }
                    }.getReady();

                    showDialog("Submit:\n" + for_display + "?");

                }
            }
        });
    }

    private void grabChoices() {
        chosenList.clear();
        try {

            SparseBooleanArray SBA_routine_choices = adapter.getCheckedItemPositions();

            for (int index = 0; index < SBA_routine_choices.size(); index++) {
                if (SBA_routine_choices.valueAt(index)) {

                    chosenList.add(adapter.getItem(SBA_routine_choices.keyAt(index)));

                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "error" + e,
                    Toast.LENGTH_LONG).show();
        }

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

    void showDialog(String medList) {
        Fragment_HomeDialog newFragment = Fragment_HomeDialog.newInstance(
                R.string.med_dose_dialog_title, medList);
        newFragment.setChoiceList(chosenList);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick() {

        for (int index = 0; index < chosenList.size(); index++) {
            dataSource.submitMedicationAdmin(chosenList.get(index), null);
        }
        mListener.onFragmentInteraction(1);
        submitMed.setEnabled(false);
        Toast.makeText(getActivity(),
                "Submitted", Toast.LENGTH_LONG)
                .show();

        routineList.clearChoices();
        chosenList.clear();
        adapter.clearChoices();
        adapter.notifyDataSetChanged();
    }

    public void doNegativeClick() {

        routineList.clearChoices();
        chosenList.clear();
        adapter.clearChoices();

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onPause() {

        if (dataSource != null) {
            dataSource.close();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (updateLists != null && updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists.cancel(true);
        }

        if (dataSource != null) {

            dataSource.close();
        }
        super.onDestroy();

    }

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        if (getActivity().getSupportFragmentManager().findFragmentByTag("home").isVisible()) {
            // Note: getValues() is a method in your ArrayAdaptor subclass
            SparseBooleanArray spRoutine = (adapter != null) ? adapter.getCheckedItemPositions() : null;

            if (spRoutine != null) {
                savedState.putParcelable("routine_array", new SparseBooleanArrayParcelable(spRoutine));

            }
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag);
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            List<Object_Medication> meds = dataSource.getAllMeds("routine");

            adapter = new HomeCustomAdapter(getActivity(), R.layout.home_list_item, R.id.title, meds);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            routineList.setAdapter(adapter);

            if (routineChoicesFromInstance != null) {

                int routine = routineChoicesFromInstance.size();

                for (int index = 0; index < routine; index++) {
                    //adapter.setItemChecked(index, routineChoicesFromInstance.valueAt(index));
                    adapter.setItemChecked(index, routineChoicesFromInstance.valueAt(index));

                }

                grabChoices();
            }
        }
    }
}

class HomeCustomAdapter extends ArrayAdapter<Object_Medication> {

    private final String TAG = "Home/HomeCustomAdapter";
    Context context;
    List<Object_Medication> data;
    SparseBooleanArray sparseBooleanArray;

    public HomeCustomAdapter(Context context, int resource,
                             int textViewResourceId, List<Object_Medication> rowItem) {
        super(context, resource, textViewResourceId, rowItem);
        this.context = context;
        this.data = rowItem;
        sparseBooleanArray = new SparseBooleanArray();

        for (int index = 0; index < rowItem.size(); index++) {
            sparseBooleanArray.put(index, false);
        }
    }


    public SparseBooleanArray getCheckedItemPositions() {

        return sparseBooleanArray;
    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Object_Medication getItem(int position) {

        return data.get(position);

    }

    public void clearChoices() {
        for (int index = 0; index < sparseBooleanArray.size(); index++) {
            sparseBooleanArray.put(index, false);
            notifyDataSetChanged();
        }
    }

    public void setItemChecked(int position, boolean value) {
        sparseBooleanArray.put(position, value);
    }

    public void toggleChecked(int position) {
        if (sparseBooleanArray.get(position)) {
            sparseBooleanArray.put(position, false);
        } else {
            sparseBooleanArray.put(position, true);
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {

        return data.indexOf(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.home_list_item, null);
        }

        final CheckedTextView txtTitle = (CheckedTextView) convertView.findViewById(R.id.title);
        final TextView headerText = (TextView) convertView.findViewById(R.id.header_title);
        final TextView nextDueTime = (TextView) convertView.findViewById(R.id.next_dose_time);
        final TextView doseMeasure = (TextView) convertView.findViewById(R.id.dose_measure);
        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        final Object_Medication dataItem = data.get(position);

        Boolean checked = sparseBooleanArray.get(position);
        if (checked != null) {
            txtTitle.setChecked(checked);
        }

        if (dataItem.isSubHeading()) {
            headerText.setText(dataItem.getMedName());
            txtTitle.setText(dataItem.getMedName());
            txtTitle.setChecked(false);
            txtTitle.setVisibility(View.INVISIBLE);
            nextDueTime.setVisibility(View.GONE);
            doseMeasure.setVisibility(View.GONE);
            headerText.setVisibility(View.VISIBLE);
            boxWrapper.setBackgroundResource(android.R.color.background_light);
        } else {
            txtTitle.setText(Helper_DataCheck.capitalizeTitles(dataItem.getMedName()));

            /***
             * Fill in missed doses for a new medication.
             */

            if (dataItem.getStatus().equalsIgnoreCase("new")) {
                dataItem.setNextDue(Helper_DataCheck.findNextDoseNewMed(context, dataItem));

            }
            String doseVerbage = (dataItem.getAdminType().equalsIgnoreCase("routine")) ? "Next Due: " : "Next Earliest Dose: ";
            nextDueTime.setText((dataItem.getNextDue().equalsIgnoreCase("complete")) ? dataItem.getNextDue() : doseVerbage + dataItem.getNextDue());
            boxWrapper.setBackgroundResource(android.R.color.white);


            if (!dataItem.getNextDue().equalsIgnoreCase("complete") &&
                    !dataItem.getNextDue().equalsIgnoreCase("prn") &&
                    !dataItem.getAdminType().equalsIgnoreCase("prn") &&
                    dataItem.getStatus().equalsIgnoreCase("active")) {
                //boxWrapper.setBackgroundResource((Helper_DataCheck.isDoseLate(dataItem.getNextDue()))? R.color.red : android.R.color.white);

                if (Helper_DataCheck.isDoseLate(dataItem.getNextDue())) {
                    boxWrapper.setBackgroundResource(R.drawable.late_dose_background);
                }
            }

            doseMeasure.setText(dataItem.getDoseMeasure() + " " + dataItem.getDoseMeasureType());
            doseMeasure.setVisibility(View.VISIBLE);
            txtTitle.setVisibility(View.VISIBLE);
            nextDueTime.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);

        }

        return convertView;

    }

}
