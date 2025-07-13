package com.islandhop.tripinit.model.postgresql;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "guide_fees")
public class GuideFee {

    @Id
    private String city;
    private double pricePerDay;

    public GuideFee() {
    }

    public GuideFee(String city, double pricePerDay) {
        this.city = city;
        this.pricePerDay = pricePerDay;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }
}