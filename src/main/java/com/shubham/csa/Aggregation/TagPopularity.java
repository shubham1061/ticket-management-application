package com.shubham.csa.Aggregation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagPopularity {
 private String tag;
    private long count;
}
