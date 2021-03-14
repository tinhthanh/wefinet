package com.wefinet.auto.entitys;

import lombok.Data;

import java.util.Date;

@Data
public class WefinetModel {
     private String type;
     private String price ;
     private String time ;
     private String action;
     private String id ;
     private Date lastUpdate;
}
