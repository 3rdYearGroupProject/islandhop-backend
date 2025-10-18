package com.islandhop.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Payment request DTO for creating PayHere payments
 */
public class PaymentRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(LKR|USD|EUR|GBP)$", message = "Currency must be LKR, USD, EUR, or GBP")
    @JsonProperty("currency")
    private String currency;
    
    @NotBlank(message = "Order ID is required")
    @Size(max = 50, message = "Order ID must not exceed 50 characters")
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("tripId")
    private String tripId; // Optional: Trip ID for trip-related payments
    
    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must not exceed 100 characters")
    @JsonProperty("itemName")
    private String itemName;
    
    @Valid
    @NotNull(message = "Customer details are required")
    @JsonProperty("customerDetails")
    private CustomerDetails customerDetails;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(BigDecimal amount, String currency, String orderId, String itemName, CustomerDetails customerDetails) {
        this.amount = amount;
        this.currency = currency;
        this.orderId = orderId;
        this.itemName = itemName;
        this.customerDetails = customerDetails;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getTripId() {
        return tripId;
    }
    
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public CustomerDetails getCustomerDetails() {
        return customerDetails;
    }
    
    public void setCustomerDetails(CustomerDetails customerDetails) {
        this.customerDetails = customerDetails;
    }
    
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", orderId='" + orderId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", customerDetails=" + customerDetails +
                '}';
    }
    
    /**
     * Customer details nested class
     */
    public static class CustomerDetails {
        
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        @JsonProperty("firstName")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        @JsonProperty("lastName")
        private String lastName;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @JsonProperty("email")
        private String email;
        
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        @JsonProperty("phone")
        private String phone;
        
        @NotBlank(message = "Address is required")
        @Size(max = 200, message = "Address must not exceed 200 characters")
        @JsonProperty("address")
        private String address;
        
        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City must not exceed 50 characters")
        @JsonProperty("city")
        private String city;
        
        @NotBlank(message = "Country is required")
        @Size(max = 50, message = "Country must not exceed 50 characters")
        @JsonProperty("country")
        private String country;
        
        // Constructors
        public CustomerDetails() {}
        
        public CustomerDetails(String firstName, String lastName, String email, String phone, String address, String city, String country) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.city = city;
            this.country = country;
        }
        
        // Getters and Setters
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        @Override
        public String toString() {
            return "CustomerDetails{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", address='" + address + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    '}';
        }
    }
}
