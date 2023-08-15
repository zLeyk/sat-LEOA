package com.sat.domain;

import lombok.Data;

@Data
public class Edge implements Comparable<Edge>{
    private Integer id;
    private String u;
    private String v;
    private Integer w;

    @Override
    public int compareTo(Edge o) {
        return this.w-o.getW();
    }
}
