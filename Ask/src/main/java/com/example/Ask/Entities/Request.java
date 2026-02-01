package com.example.Ask.Entities;

import jakarta.persistence.*;
@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;


    @Column
    private int age;
    @Enumerated(EnumType.STRING)
    private Gender Gender;
    @Column
    private String type;

    @Column
    private String name;

    @Column
    private int AdminApproved;

    @Column
    private int DocApproved;

    @Column
    private Integer userId;

    @Column
    private String imageUrl;

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return Gender;
    }

    public String getType() {
        return type;
    }

    public int getAdminApproved() {
        return AdminApproved;
    }

    public void setAdminApproved(int adminApproved) {
        AdminApproved = adminApproved;
    }

    public int getDocApproved() {
        return DocApproved;
    }

    public void setDocApproved(int docApproved) {
        DocApproved = docApproved;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGender(Gender gender) {
        this.Gender = gender;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Request(Integer id, int age, Gender gender, String type, String name) {
        this.id = id;
        this.age = age;
        this.Gender = gender;
        this.type = type;
        this.name = name;
    }
    public Request() {

    }


}
