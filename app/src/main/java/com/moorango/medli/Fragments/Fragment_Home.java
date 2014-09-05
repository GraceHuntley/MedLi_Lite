package com.moorango.medli.Fragments;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.moorango.medli.CustomObjects.SparseBooleanArrayParcelable;
import com.moorango.medli.CustomViews.HomeCustomAdapter;
import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Models.Object_Medication;
import com.moorango.medli.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Fragment_Home extends Fragment {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "Home.java";
    private static ArrayList<Object_Medication> chosenList;
    private static MyAsyncTask updateLists;
    public OnFragmentInteractionListener mListener;
    private ListView routineList;
    private MedLiDataSource dataSource;
    private HomeCustomAdapter adapter;
    private SparseBooleanArray routineChoicesFromInstance = null;


    public Fragment_Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        Button submitMed = (Button) getActivity().findViewById(R.id.submit_button);
        chosenList = new ArrayList<Object_Medication>();

        dataSource = MedLiDataSource.getHelper(getActivity());

        if (updateLists == null || !updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists = new MyAsyncTask(this);
            updateLists.execute();
        } else {
            updateLists.cancel(true);
            updateLists = null;
            updateLists = new MyAsyncTask(this);
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
                medList);

        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick() {

        for (Object_Medication aChosenList : chosenList) {

            dataSource.submitMedicationAdmin(aChosenList, null);
        }
        mListener.onFragmentInteraction(1, null, 0);

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
        public void onFragmentInteraction(int tag, String name, int id);
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        final Fragment_Home fragmentHome;

        public MyAsyncTask(Fragment_Home context) {
            this.fragmentHome = context;
        }

        @Override
        protected String doInBackground(Void... voids) {

            List<Object_Medication> meds = dataSource.getAllMeds();

            adapter = new HomeCustomAdapter(getActivity(), meds, fragmentHome);

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

