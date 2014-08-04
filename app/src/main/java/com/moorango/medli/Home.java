package com.moorango.medli;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Home extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ListView routineList;
    private ListView prnList;
    private MedLiDataSource dataSource;
    private SparseBooleanArray SBA_medChoices;
    private String ready_med;
    private Button submitMed;
    private ArrayAdapter<Medication> adapter;
    private ArrayList<Medication> chosenList;
    private AlertDialog.Builder ad;

    private MedLiDataSource dbHelper;

    /*public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    } */
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

        submitMed = (Button) getActivity().findViewById(R.id.submit_button);
        SBA_medChoices = null;
        ad = new AlertDialog.Builder(getActivity());
        chosenList = new ArrayList<Medication>();

        dbHelper = MedLiDataSource.getHelper(getActivity());

        List<Medication> meds = dataSource.getAllRoutineMeds();
        List<Medication> prnMeds = dataSource.getAllPrnMeds();
        adapter = new ArrayAdapter<Medication>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, meds);

        adapter.sort(new Comparator<Medication>() {

            @Override
            public int compare(Medication lhs, Medication rhs) {
                //return lhs.compareTo(rhs);   //or whatever your sorting algorithm

                return lhs.compareNextDue(rhs);
            }
        });

        ArrayAdapter<Medication> prnAdapter = new ArrayAdapter<Medication>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, prnMeds);

        routineList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        routineList.setAdapter(adapter);
        prnList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        prnList.setAdapter(prnAdapter);

        routineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                chosenList.clear();
                try {

                    SBA_medChoices = routineList.getCheckedItemPositions();

                    for (int index = 0; index < SBA_medChoices.size(); index++) {
                        if (SBA_medChoices.valueAt(index)) {

                            chosenList.add(adapter.getItem(SBA_medChoices.keyAt(index)));

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
        });

        /***
         * start dialog test here.
         */



        submitMed.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                if (chosenList.size() > 0) {



                    String for_display = new Object() {
                        String getReady() {

                            String ready = "";
                            for (int index = 0; index < chosenList.size(); index++) {
                                ready += chosenList.get(index).getMedName() + "\n";
                            }

                            return ready;
                        }
                    }.getReady();

                    showDialog("Submit:\n" + for_display + "?");
                    String forToast = "Submit Meds?\n" + for_display;
                    ad.setTitle("Confirm");
                    ad.setMessage(forToast);
                    ad.setIcon(android.R.drawable.ic_dialog_alert);
                    ad.setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    for (int index = 0; index < chosenList.size(); index++) {
                                        dbHelper.submitMedicationAdmin(chosenList.get(index), null);
                                    }
                                    submitMed.setEnabled(false);
                                    Toast.makeText(getActivity(),
                                            "Submitted", Toast.LENGTH_LONG)
                                            .show();

                                    adapter.notifyDataSetChanged();

                                }
                            }
                    )
                            .setNegativeButton(android.R.string.no,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {

                                            SBA_medChoices = null;

                                            submitMed.setEnabled(false);
                                        }
                                    }
                            );
                    // ad.show();

                    int count = routineList.getAdapter().getCount();
                    for (int index = 0; index < count; index++) {
                        routineList.setItemChecked(index, false);
                        SBA_medChoices.put(index, false);
                    }
                    routineList.clearChoices();
                    chosenList.clear();
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

    void showDialog(String medList) {
        Dialog_Fragment newFragment = Dialog_Fragment.newInstance(
                R.string.med_dose_dialog_title); // TODO look further into this was a string.
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");



    }

    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");

        for (int index = 0; index < chosenList.size(); index++) {
            dbHelper.submitMedicationAdmin(chosenList.get(index), null);
        }
        submitMed.setEnabled(false);
        Toast.makeText(getActivity(),
                "Submitted", Toast.LENGTH_LONG)
                .show();

        adapter.notifyDataSetChanged();
    }

    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
        SBA_medChoices = null;

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag);
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();

    }

}
