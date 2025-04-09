package io.watch.basedata.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataResults<T> {
    private List<T> listOfDataObjects;
    private String recordsTotal;
    private String recordsFiltered;
    private String start;
}
