package com.example.buyerfinder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Buyer {
    private String name;
    private String company;
    private String email;
    private String location;
    private String website;
}
