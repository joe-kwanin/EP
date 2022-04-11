package com.example.vvuexampermitapp;

public class StudentClass {
    private String firstname;
    private String lastname;
    private String studentid;
    private String dob;
    private String gender;
    private String degree;
    private String major;
    private String feeBalance;
    private String nationality;
    private String seatnumber;
    private boolean signin;
    private String keyid;

    public StudentClass() {
    }

    public StudentClass(String firstname, String lastname, String studentid, String dob, String gender, String degree, String major, String feeBalance, String nationality, String seatnumber, boolean signin, String keyid) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.studentid = studentid;
        this.dob = dob;
        this.gender = gender;
        this.degree = degree;
        this.major = major;
        this.feeBalance = feeBalance;
        this.nationality = nationality;
        this.seatnumber = seatnumber;
        this.signin = signin;
        this.keyid = keyid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getFeeBalance() {
        return feeBalance;
    }

    public void setFeeBalance(String feeBalance) {
        this.feeBalance = feeBalance;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getSeatnumber() {
        return seatnumber;
    }

    public void setSeatnumber(String seatnumber) {
        this.seatnumber = seatnumber;
    }

    public boolean isSignin() {
        return signin;
    }

    public void setSignin(boolean signin) {
        this.signin = signin;
    }

    public String getKeyid() {
        return keyid;
    }

    public void setKeyid(String keyid) {
        this.keyid = keyid;
    }
}
