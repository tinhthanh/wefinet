package com.citi.winner21.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Getter
@Setter
public class ProviderAccount {
    private String providerName = "";
    private String accountName = "";
    private String password = "";
    private String authKey = "";
    private String pingStatus = "INACTIVE";
    private String proxyHost = "";
    private Integer proxyPort = 0;

    public ProviderAccount(ResultSet rs) throws SQLException {
        this.providerName = rs.getString("provider_name");
        this.accountName = rs.getString("account_name");
        this.password = rs.getString("password");
        this.authKey = rs.getString("auth_key");
        this.pingStatus = rs.getString("ping_status");
        this.proxyHost = rs.getString("proxy_host");
        this.proxyPort = rs.getInt("proxy_port");

    }

    public String getProxyUrlWithSchema() {
        return "http://" + this.proxyHost.trim() + ":" + this.proxyPort;
    }
}

