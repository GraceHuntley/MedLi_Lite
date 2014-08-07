package com.moorango.medli;

import android.app.Activity;
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

    private static ArrayList<Medication> chosenList;
    private static MyAsyncTask updateLists;
    private OnFragmentInteractionListener mListener;
    private ListView routineList;
    private ListView prnList;
    private MedLiDataSource dataSource;
    private Button submitMed;
    private ArrayAdapter<Medication> adapter;
    private ArrayAdapter<Medication> prnAdapter;
    private MedLiDataSource dbHelper;


    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = new MedLiDataSource(getActivity());

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

        dbHelper = MedLiDataSource.getHelper(getActivity());


        //fillLists();
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

                    showDialog("Submit:\n" + for_display + "?");

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

                    chosenList.add(adapter.getItem(SBA_routine_choices.keyAt(index)));

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

    void showDialog(String medList) {
        Dialog_Fragment newFragment = Dialog_Fragment.newInstance(
                R.string.med_dose_dialog_title, medList); // TODO look further into this was a string.
        newFragment.setChoiceList(chosenList);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");

    }

    public void doPositiveClick(ArrayList<Medication> choicesList) {

        for (int index = 0; index < chosenList.size(); index++) {
            dbHelper.submitMedicationAdmin(choicesList.get(index), null);
        }
        mListener.onFragmentInteraction(1);
        submitMed.setEnabled(false);
        Toast.makeText(getActivity(),
                "Submitted", Toast.LENGTH_LONG)
                .show();

        fillLists();
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

        if (updateLists != null && updateLists.getStatus().equals(AsyncTask.Status.RUNNING)) {
            updateLists.cancel(true);
        }
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

    private void fillLists() {

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

        routineList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        routineList.setAdapter(adapter);
        prnList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        prnList.setAdapter(prnAdapter);
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
            routineList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            routineList.setAdapter(adapter);
            prnList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            prnList.setAdapter(prnAdapter);
        }
    }



}
