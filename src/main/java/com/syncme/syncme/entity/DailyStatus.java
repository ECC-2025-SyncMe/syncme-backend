package com.syncme.syncme.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class DailyStatus {

    /**
     * PK = USER#{email}
     * SK = DATE#{YYYY-MM-DD}
     */
    
    private String pk;
    private String sk;

    private Integer energy;
    private Integer burden;
    private Integer passion;

    private String createdAt; // ISO string
    private String updatedAt; // ISO string

    @DynamoDbPartitionKey
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }

    @DynamoDbSortKey
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }

    public Integer getEnergy() { return energy; }
    public void setEnergy(Integer energy) { this.energy = energy; }

    public Integer getBurden() { return burden; }
    public void setBurden(Integer burden) { this.burden = burden; }

    public Integer getPassion() { return passion; }
    public void setPassion(Integer passion) { this.passion = passion; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
