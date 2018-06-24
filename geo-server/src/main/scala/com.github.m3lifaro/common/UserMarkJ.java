package com.github.m3lifaro.common;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

public class UserMarkJ implements Serializable {

    @QuerySqlField(index = true)
    public Long id;

    @QuerySqlField(index = true)
    public Double lat;
    @QuerySqlField(index = true)
    public Double lon;

    public UserMarkJ(Double lat, Double lon, Long id) {
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }
}
