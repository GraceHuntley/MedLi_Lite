package com.moorango.medli;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Comparator;
import java.util.List;

public class Home extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ListView routineList;
    private ListView prnList;
    private MedLiDataSource dataSource;

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

        List<Medication> meds = dataSource.getAllRoutineMeds();
        List<Medication> prnMeds = dataSource.getAllPrnMeds();
        ArrayAdapter<Medication> adapter = new ArrayAdapter<Medication>(getActivity(),
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

        routineList.setAdapter(adapter);
        prnList.setAdapter(prnAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
