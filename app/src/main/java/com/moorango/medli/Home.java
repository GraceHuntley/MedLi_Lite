package com.moorango.medli;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Home extends Fragment {

    private final String TAG = "Home.java";
    private static ArrayList<Medication> chosenList;
    private static MyAsyncTask updateLists;
    private OnFragmentInteractionListener mListener;
    private ListView routineList, prnList;
    private MedLiDataSource dataSource;
    private Button submitMed;
    private ArrayAdapter<Medication> adapter, prnAdapter;
    private SparseBooleanArray routineChoicesFromInstance = null;
    private SparseBooleanArray prnChoicesFromInstance = null;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            routineChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("routine_array");

            prnChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("prn_array");

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
        prnList = (ListView) getActivity().findViewById(R.id.prn_listview);
        Button clearChoices = (Button) getActivity().findViewById(R.id.clear_button);
        submitMed = (Button) getActivity().findViewById(R.id.submit_button);
        chosenList = new ArrayList<Medication>();

        if (savedInstanceState != null) {
            routineChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("routine_array");
            prnChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("prn_array");
        }

        dataSource = MedLiDataSource.getHelper(getActivity());
        //if (savedInstanceState == null) {
        if (updateLists == null || !updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists = new MyAsyncTask();
            updateLists.execute();
        } else {
            updateLists.cancel(true);
            updateLists = null;
            updateLists = new MyAsyncTask();
            updateLists.execute();
        }
        // }

        routineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                grabChoices();
            }
        });


        prnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                grabChoices();
            }
        });

        clearChoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int index = 0; index < routineList.getCount(); index++) {
                    routineList.setItemChecked(index, false);
                }

                for (int index = 0; index < prnList.getCount(); index++) {
                    prnList.setItemChecked(index, false);
                }
                chosenList.clear();
            }
        });

        submitMed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (chosenList.size() > 0) {

                    String for_display = new Object() {
                        String getReady() {

                            String ready = "";
                            for (Medication aChosenList : chosenList) {
                                ready += aChosenList.getMedName() + "\n";
                            }

                            return ready;
                        }
                    }.getReady();

                    showDialog(view.getContext(), "Submit:\n" + for_display + "?");

                }
            }
        });
    }

    private void grabChoices() {
        chosenList.clear();
        try {
            SparseBooleanArray SBA_prn_choices = prnList.getCheckedItemPositions();
            SparseBooleanArray SBA_routine_choices = routineList.getCheckedItemPositions();

            for (int index = 0; index < SBA_routine_choices.size(); index++) {
                if (SBA_routine_choices.valueAt(index)) {
                    if (!adapter.getItem(SBA_routine_choices.keyAt(index)).getNextDue().equalsIgnoreCase("complete")) {
                        chosenList.add(adapter.getItem(SBA_routine_choices.keyAt(index)));
                    } else {
                        routineList.setItemChecked(index, false);

                    }

                }
            }

            for (int index = 0; index < SBA_prn_choices.size(); index++) {
                if (SBA_prn_choices.valueAt(index)) {
                    chosenList.add(prnAdapter.getItem(SBA_prn_choices.keyAt(index)));
                }
            }
            if (chosenList.size() > 0) {
                submitMed.setEnabled(true);
            } else {
                submitMed.setEnabled(false);
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

    void showDialog(Context con, String medList) {
        Dialog_Fragment newFragment = Dialog_Fragment.newInstance(
                R.string.med_dose_dialog_title, medList);
        newFragment.setChoiceList(chosenList);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick(ArrayList<Medication> choicesList) {


        for (int index = 0; index < chosenList.size(); index++) {
            dataSource.submitMedicationAdmin(chosenList.get(index), null);
        }
        mListener.onFragmentInteraction(1);
        submitMed.setEnabled(false);
        Toast.makeText(getActivity(),
                "Submitted", Toast.LENGTH_LONG)
                .show();


        adapter.notifyDataSetChanged();
        routineList.clearChoices();
        prnList.clearChoices();
        chosenList.clear();
    }

    public void doNegativeClick() {

        routineList.clearChoices();
        prnList.clearChoices();

        for (int i = 0; i < routineList.getCount(); i++) {
            routineList.setItemChecked(i, false);
        }

        for (int i = 0; i < prnList.getCount(); i++) {
            prnList.setItemChecked(i, false);
        }
        submitMed.setEnabled(false);
    }
    /***
     * End dialog test above.
     */

    @Override
    public void onDetach() {
        super.onDetach();


        mListener = null;
    }

    @Override
    public void onPause() {

        /*if (updateLists != null && updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists.cancel(true);
        } */

        dataSource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (updateLists != null && updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists.cancel(true);
        }
        dataSource.close();
        super.onDestroy();

    }

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        SparseBooleanArray spRoutine = routineList.getCheckedItemPositions();
        SparseBooleanArray spPRN = prnList.getCheckedItemPositions();

        savedState.putParcelable("routine_array", new SparseBooleanArrayParcelable(spRoutine));
        savedState.putParcelable("prn_array", new SparseBooleanArrayParcelable(spPRN));

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
        public void onFragmentInteraction(int tag);
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            List<Medication> meds = dataSource.getAllMeds("routine");
            List<Medication> prnMeds = dataSource.getAllMeds("prn");
            adapter = new ArrayAdapter<Medication>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice, meds);

            adapter.sort(new Comparator<Medication>() {

                @Override
                public int compare(Medication lhs, Medication rhs) {

                    return lhs.compareNextDue(rhs);
                }
            });

            prnAdapter = new ArrayAdapter<Medication>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice, prnMeds);


            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            routineList.setAdapter(adapter);
            prnList.setAdapter(prnAdapter);


            if (routineChoicesFromInstance != null && prnChoicesFromInstance != null) {

                int routine = routineChoicesFromInstance.size();
                int prn = prnChoicesFromInstance.size();

                for (int index = 0; index < routine; index++) {
                    routineList.setItemChecked(index, routineChoicesFromInstance.valueAt(index));

                }
                for (int index = 0; index < prn; index++) {

                    prnList.setItemChecked(index, prnChoicesFromInstance.valueAt(index));
                }
                grabChoices();
            }
        }
    }
}


