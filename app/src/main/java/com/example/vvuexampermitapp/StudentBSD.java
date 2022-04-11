package com.example.vvuexampermitapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class StudentBSD extends BottomSheetDialogFragment {
    private View view;
    private TextView fname,lname,id,dob,nation,gender,degree,major,fees,seat;
    private Button seatnumber,signout;
    private StudentClass studentClass;
    private String keyid;
    private DatabaseReference reference;

    static StudentBSD newInstance(){
        return new StudentBSD();
    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.student_details_layout,container,false);

        reference = FirebaseDatabase.getInstance().getReference("student");
        //getting bundle passed
        Bundle bundle = getArguments();

        fname = view.findViewById(R.id.fname);
        lname = view.findViewById(R.id.lname);
        id = view.findViewById(R.id.id);
        dob = view.findViewById(R.id.dob);
        nation = view.findViewById(R.id.nation);
        gender = view.findViewById(R.id.gender);
        degree = view.findViewById(R.id.degree);
        major = view.findViewById(R.id.major);
        fees = view.findViewById(R.id.balance);
        seat = view.findViewById(R.id.seat);
        seatnumber = view.findViewById(R.id.seatnumber);
        signout = view.findViewById(R.id.signout);

        fname.setText(bundle.getString("fname"));
        lname.setText(bundle.getString("lname"));
        id.setText(bundle.getString("id"));
        dob.setText(bundle.getString("dob"));
        nation.setText(bundle.getString("nationality"));
        gender.setText(bundle.getString("gender"));
        degree.setText(bundle.getString("degree"));
        major.setText(bundle.getString("major"));
        fees.setText(bundle.getString("fee"));
        seat.setText(bundle.getString("seatnum"));
        keyid = bundle.getString("keyid");

        if (bundle.getString("fee").equals("0.00")){
            fees.setTextColor(R.color.green);
            if (bundle.getString("seatnum") != null){
                seatnumber.setVisibility(View.GONE);
                signout.setVisibility(View.VISIBLE);
            }else {
                seatnumber.setVisibility(View.VISIBLE);
                signout.setVisibility(View.GONE);
            }
        }else {
            seatnumber.setVisibility(View.GONE);
            signout.setVisibility(View.GONE);
            fees.setTextColor(R.color.red);
        }




        
        seatnumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generaterandomnumber();
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),Dashboard.class);
                startActivity(intent);
            }
        });



        return  view;
    }


    private void generaterandomnumber() {
        int min = 1, max=400;
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);

        //check if number already assigned to someone
        Query query = FirebaseDatabase.getInstance().getReference("student").orderByChild("seatnumber").equalTo(String.valueOf(randomNum));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    studentClass = ds.getValue(StudentClass.class);
                }
                if (studentClass != null){
                    generaterandomnumber();
                }else {
                    //updating student seatnumber field
                    if (keyid!=null){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seatnumber", String.valueOf(randomNum));
                        reference.child(keyid).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //show a dialog with the student name and seat number
                            }
                        });
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
