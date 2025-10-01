package com.order.service.order_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillingAddressRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String company;

    @NotBlank(message = "Address line 1 is required")
    @Size(min = 5, max = 255, message = "Address line 1 must be between 5 and 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "City can only contain letters, spaces, hyphens, and apostrophes")
    private String city;

    @NotBlank(message = "State/Province is required")
    @Size(min = 2, max = 100, message = "State/Province must be between 2 and 100 characters")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(min = 3, max = 20, message = "Postal code must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "Invalid postal code format")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\s()-]{10,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    // Business validation methods
    public boolean isValidBillingAddress() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                addressLine1 != null && !addressLine1.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty() &&
                country != null && !country.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getShortAddress() {
        return (addressLine1 != null ? addressLine1 : "") + ", " +
                (city != null ? city : "") + ", " +
                (state != null ? state : "");
    }

    public boolean isInternational(String domesticCountry) {
        return country != null && !country.equalsIgnoreCase(domesticCountry);
    }

    public boolean hasCompany() {
        return company != null && !company.trim().isEmpty();
    }

    public boolean hasSecondAddressLine() {
        return addressLine2 != null && !addressLine2.trim().isEmpty();
    }

    // Check if billing address matches shipping address
    public boolean matches(ShippingAddressRequest shippingAddress) {
        if (shippingAddress == null)
            return false;

        return java.util.Objects.equals(firstName, shippingAddress.getFirstName()) &&
                java.util.Objects.equals(lastName, shippingAddress.getLastName()) &&
                java.util.Objects.equals(addressLine1, shippingAddress.getAddressLine1()) &&
                java.util.Objects.equals(addressLine2, shippingAddress.getAddressLine2()) &&
                java.util.Objects.equals(city, shippingAddress.getCity()) &&
                java.util.Objects.equals(state, shippingAddress.getState()) &&
                java.util.Objects.equals(postalCode, shippingAddress.getPostalCode()) &&
                java.util.Objects.equals(country, shippingAddress.getCountry()) &&
                java.util.Objects.equals(phoneNumber, shippingAddress.getPhoneNumber());
    }
}
