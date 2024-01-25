package com.solace.ep.muleflow.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * Intermediate mapper model class representing Solace configuration
 * for Mule Flows using the Solace extension
 */
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
