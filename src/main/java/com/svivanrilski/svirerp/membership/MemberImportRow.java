package com.svivanrilski.svirerp.membership;

import java.time.LocalDate;

/**
 * One parsed row from the member import CSV. Date parsing and required-field
 * presence checks happen in MemberImportService while reading the raw CSV
 * (a system boundary); this record only ever holds already-well-formed data.
 */
public record MemberImportRow(
        String firstName,
        String lastName,
        String email,
        String phone,
        String addressLine1,
        String city,
        String state,
        String zip,
        LocalDate dateOfBirth,
        String membershipTypeName,
        String memberNumber,
        LocalDate joinDate,
        LocalDate expiryDate,
        String status,
        Boolean emailOptIn) {
}
