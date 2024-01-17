package com.solace.ep.muleflow.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapConfig {
    
    protected String configName;

    protected String connectBrokerHost;

    protected String connectMsgVpn;

    protected String connectClientUserName;

    protected String connectPassword;

    protected String epCloudApiToken;

}
