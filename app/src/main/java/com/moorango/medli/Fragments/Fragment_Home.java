package com.moorango.medli.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moorango.medli.CustomObjects.SparseBooleanArrayParcelable;
import com.moorango.medli.CustomViews.HomeCustomAdapter;
import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Helpers.AlarmHelpers;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.Medication;
import com.moorango.medli.NotifyService;
import com.moorango.medli.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Fragment_Home extends Fragment {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "Home.java";
    private static ArrayList<Medication> chosenList;
    private static MyAsyncTask updateLists;
    public OnFragmentInteractionListener mListener;
    private ListView routineList;
    private MedLiDataSource dataSource;
    private HomeCustomAdapter adapter;
    private SparseBooleanArray routineChoicesFromInstance = null;
    private int imageNumber = 1; //int to check which image is displayed


    public Fragment_Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null) {
            //routineChoicesFromInstance = (SparseBooleanArray) savedInstanceState.getParcelable("routine_array");

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataSource.getPreferenceBool("show_home_welcome")) {
            introDialogBuilder();
        }

        if (updateLists == null || updateLists.getStatus() != AsyncTask.Status.RUNNING) {
            updateLists = new MyAsyncTask(this);
            updateLists.execute();
        } else {
            updateLists.cancel(true);
            updateLists = null;
            updateLists = new MyAsyncTask(this);
            updateLists.execute();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        routineList = (ListView) getActivity().findViewById(R.id.routine_listview);
        Button clearChoices = (Button) getActivity().findViewById(R.id.clear_button);
        Button submitMed = (Button) getActivity().findViewById(R.id.submit_button);
        chosenList = new ArrayList<Medication>();

        dataSource = MedLiDataSource.getHelper(getActivity());


        if (updateLists == null || updateLists.getStatus() != AsyncTask.Status.RUNNING) {
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
                final int pos = position;

                if (!adapter.getItem(pos).isSubHeading()) {
                    Medication med = adapter.getItem(pos);
                    if ((med.getActualDoseCount() >= med.getDoseCount()) && adapter.notChecked(pos)) {

                        AlertDialog.Builder adB = new AlertDialog.Builder(getActivity())
                                .setTitle("Maximum Doses Reached")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setMessage("You have reached the maximum set dose count for " + DataCheck.capitalizeTitles(med.getMedName()) + ". "
                                        + "Click Proceed if you still want to enter it. Otherwise, click Cancel.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        adapter.toggleChecked(pos);
                                    }
                                });
                        adB.show();
                    } else if (med.getNextDue().compareTo(DateTime.getIncrementedTimestamp(DateTime.getCurrentTimestamp(false, null), 0, 0, 20)) > 0 && adapter.notChecked(pos)) {

                        AlertDialog.Builder adB = new AlertDialog.Builder(getActivity())
                                .setTitle("Early Dose")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setMessage(DateTime.getTimeDifference(med.getNextDue())
                                        + " early for " + DataCheck.capitalizeTitles(med.getMedName())
                                        + ". Click Proceed if you still want to enter it. Otherwise, click Cancel.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        adapter.toggleChecked(pos);
                                    }
                                });
                        adB.show();
                    } else {
                        adapter.toggleChecked(position);

                    }
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
                            for (Medication aChosenList : chosenList) {
                                ready += DataCheck.capitalizeTitles(aChosenList.getMedName()) + " " + aChosenList.getDoseForm() + "\n";
                            }

                            return ready;
                        }
                    }.getReady();

                    showDialog("Double check the following medication(s)\nbefore pressing submit.\n\n" + for_display);

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

        AlarmHelpers ah = new AlarmHelpers(getActivity());

        for (Medication aChosenList : chosenList) {

            dataSource.submitMedicationAdmin(aChosenList, null);

            ah.clearAlarm(aChosenList.getIdUnique());

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

        if (updateLists != null && updateLists.getStatus() == AsyncTask.Status.RUNNING) {
            updateLists.cancel(true);
            updateLists = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (updateLists != null && updateLists.getStatus() == AsyncTask.Status.RUNNING) {
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

    private void introDialogBuilder() {
        AlertDialog.Builder adB = new AlertDialog.Builder(getActivity());
        adB.setIcon(android.R.drawable.ic_dialog_info);
        adB.setTitle("A few words..");
        adB.setInverseBackgroundForced(true); // fixed bug in older versions of android.

        View view = getActivity().getLayoutInflater().inflate(R.layout.info_dialog, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.no_show_checkbox);

        String home_welcome_info = "<html><body>" +
                getResources().getString(R.string.home_welcome_info) +
                "<img src=\"history.png\"/>.<br/>" +
                getResources().getString(R.string.home_welcome_info_2) +
                "<img src=\"edit.png\"/>." +
                " <br/> " +
                getResources().getString(R.string.home_welcome_info_3) +
                "</body></html>";
        TextView tv1 = (TextView) view.findViewById(R.id.main_text);
        tv1.setText(Html.fromHtml(home_welcome_info, imgGetter, null));


        ((TextView) view.findViewById(R.id.dont_show_message)).setText(getResources().getString(R.string.do_not_show));

        adB.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (checkBox.isChecked()) {
                            dataSource.addOrUpdatePreference("show_home_welcome", false);
                        }
                    }
                }).show();

    }

    private final Html.ImageGetter imgGetter = new Html.ImageGetter() {

        public Drawable getDrawable(String source) {
            Drawable drawable;
            if (imageNumber == 1) {
                drawable = getResources().getDrawable(R.drawable.ic_action_view_as_list);
                ++imageNumber;
            } else drawable = getResources().getDrawable(R.drawable.ic_action_edit);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };


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
            List<Medication> meds;
            //if (getActivity().getIntent().hasExtra(NotifyService.INTENT_FROM_NOTIFICATION)) {
                meds = dataSource.getAllMeds(getActivity());
            //} else {
            //    meds = dataSource.getAllMeds(getActivity());
            //}

            adapter = new HomeCustomAdapter(getActivity(), meds, fragmentHome);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isCancelled()) {
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

}

