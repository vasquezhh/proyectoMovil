package com.example.vasquezh.stuff;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListStudentAssigment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListStudentAssigment extends Fragment implements StudentAdapter.RecyclerClickListener{

    private RecyclerView myRecyclerView;
    private StudentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Firebase m_fb,mFbStudent;
    private ArrayList<Home.User> members;
    private ProgressDialog pDialog;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ID_KEY = "keyAssigment";
    private static final String ID_NAME = "nameAssigment";

    // TODO: Rename and change types of parameters
    private String keyAssigment;
    private String nameAssigment;


    public ListStudentAssigment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param keyAssigment Parameter 1.
     * @param nameAssigment parameter 2.
     * @return A new instance of fragment ListStudentAssigment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListStudentAssigment newInstance(String keyAssigment, String nameAssigment) {
        ListStudentAssigment fragment = new ListStudentAssigment();
        Bundle args = new Bundle();
        args.putString(ID_KEY,keyAssigment);
        args.putString(ID_NAME,nameAssigment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyAssigment = getArguments().getString(ID_KEY);
            nameAssigment = getArguments().getString(ID_NAME);
        }
        setHasOptionsMenu(true);
        m_fb = new Firebase("https://vasquezhproyectofinal.firebaseio.com/members");
        Log.i("onCreate","ListStudentAssigment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_list_student_assigment, container, false);
        myRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycle_view_list_student);
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        myRecyclerView.setLayoutManager(mLayoutManager);
        myRecyclerView.setItemAnimator(new DefaultItemAnimator());
        members = new ArrayList<Home.User>();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_list_student);
        AppCompatActivity appca = ((AppCompatActivity)getActivity());
        appca.setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar ab=appca.getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.drawable.pin2);
        ab.setTitle(nameAssigment);

        Log.i("onCreateView","ListStudentAssigment");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        new GetData().execute();
        Log.i("onResume", "ListStudentAssigment");
        members.clear();
    }

    /*
    *
    * Metodo para comunicarse con la interface StudentAdapter.MyViewHolder
    * al momento de tocar un item del recyclerview, hace algun evento.
    * */

    @Override
    public void itemClick(Home.User user) {

    }

    private class GetData extends AsyncTask<Void,Void,Void> {
        private ArrayList<String> keys;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog=new ProgressDialog(getContext());
            pDialog.setTitle("App Stuff");
            pDialog.setMessage("Checking...");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            m_fb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    keys= new ArrayList<String>();
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if(postSnapshot.getKey().compareTo(keyAssigment)==0) {
                            HashMap<String,String> m= (HashMap<String, String>) postSnapshot.getValue();
                            for(String key: m.keySet()){
                                System.out.println(key);
                                keys.add(key);
                            }
                        }
                    }

                    searchStudent();
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            pDialog.dismiss();
        }

        public void searchStudent(){
            mFbStudent = new Firebase("https://vasquezhproyectofinal.firebaseio.com/users");
            mFbStudent.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> student= (HashMap<String, String>)dataSnapshot.getValue();
                    for (String key: keys){
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Home.User a = postSnapshot.getValue(Home.User.class);
                            System.out.println(keys.size()+" - "+postSnapshot.getKey()+" - "+postSnapshot.getValue());
                            if(postSnapshot.getKey().compareTo(key)==0){
                                a.setKeyFireBase(dataSnapshot.getKey());
                                members.add(a);
                            }
                        }
                    }
                    System.out.println("members size "+ members.size());
                    if(!members.isEmpty()) {
                        for (Home.User a : members) {
                            System.out.println("Member: " + a.getKeyFireBase() + " " + a.getName() + " " + a.getEmail() + " " + a.getPassword());
                        }
                    }
                    mAdapter = new StudentAdapter(members);
                    mAdapter.setRecyclerClickListener(ListStudentAssigment.this);
                    myRecyclerView.setAdapter(mAdapter);
                }


                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

}
