/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
/**
 * DELL INC. PROPRIETARY INFORMATION: This software is supplied under the terms of a

 * license agreement or nondisclosure agreement with Dell Inc. and may not be copied
 * or disclosed except in accordance with the terms of that agreement.
 * Copyright (c) 2010-2015 Dell Inc. All Rights Reserved.
 */

package com.dell.isg.smi.virtualnetwork.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.commons.elm.model.CreatedResponse;
import com.dell.isg.smi.commons.utilities.model.PagedResult;
import com.dell.isg.smi.virtualnetwork.model.AssignIpPoolAddresses;
import com.dell.isg.smi.virtualnetwork.model.IpRange;
import com.dell.isg.smi.virtualnetwork.model.Network;
import com.dell.isg.smi.virtualnetwork.model.ReserveIpPoolAddressesRequest;

/**
 * Virtual Network Configuration and IPAM Rest API
 * 
 * @author michael.hepfer
 *
 */
@RestController
@Api(value = "/api/1.0/networks", description = "/api/1.0/networks")
@RequestMapping("/api/1.0/networks")
public interface NetworkController {

    String PARAMETER_NETWORK_ID = "networkId";
    String PARAMETER_RANGE_ID = "rangeId";
    String PARAMETER_USAGE_ID = "usageId";
    String PARAMETER_PAGINATION_OFFSET = "offset";
    String PARAMETER_PAGINATION_LIMIT = "limit";
    String PARAMETER_STATE = "state";
    String PARAMETER_IPADDRESS = "ipAddress";
    String PARAMETER_NAME = "name";

    String PATH_URI_ALL_NETWORKS = "";
    String PATH_URI_NETWORK_ID = "/{networkId}";
    String PATH_URI_ADD_IPV4_RANGE = PATH_URI_NETWORK_ID + "/ipv4Ranges";
    String PATH_URI_UPDATE_IPV4_RANGE = PATH_URI_ADD_IPV4_RANGE + "/{rangeId}";
    String PATH_URI_IPADDRESSPOOLS = PATH_URI_NETWORK_ID + "/ipAddressPools";
    String PATH_URI_IPADDRESSPOOLS_IPADDRESS = PATH_URI_IPADDRESSPOOLS + "/{ipAddress:.+}";
    String PATH_URI_RESERVE_ASSIGN_RELEASE_IPADDRESSPOOLS = PATH_URI_IPADDRESSPOOLS + "/{usageId}";

    String PATH_URI_EXPORT_IPADDRESS_POOL = PATH_URI_IPADDRESSPOOLS + "/export";

    String DEFAULT_OFFSET = "0";
    String DEFAULT_LIMIT = "10";
    String DEFAULT_STATE = "ALL";
    String DEFAULT_CONTENT_TYPE = "application/json";

    String ROLE_READ = "ROLE_READ";
    String ROLE_WRITE = "ROLE_WRITE";
    String ROLE_CONFIGURE_NETWORK = "ROLE_CONFIGURE_NETWORK";


    /**
     * Create Network.
     *
     * @param network (Network) the request body
     * @return (CreatedResponse) containing the serial ID of the created network
     */
    @ApiOperation(value = "Create Network", nickname = "Create Network", notes = "Creates a network", response = CreatedResponse.class)
    // @ApiImplicitParams({
    // @ApiImplicitParam(name="network", dataType="com.dell.isg.smi.virtualnetwork.model.Network", paramType="body", required=true),
    // })
    @RequestMapping(method = RequestMethod.POST)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.CREATED)
    public abstract CreatedResponse createNetwork(@RequestBody Network network);


    /**
     * Gets the network.
     *
     * @param networkId the network id
     * @return the network
     */
    @ApiOperation(value = "Get Network (by ID)", nickname = "Get Network (by ID)", notes = "Returns the network information for the given " + PARAMETER_NETWORK_ID + " path variable", response = Network.class)
    @RequestMapping(method = RequestMethod.GET, path = PATH_URI_NETWORK_ID)
    @RolesAllowed({ ROLE_READ })
    public abstract Network getNetwork(@PathVariable(PARAMETER_NETWORK_ID) long networkId);


    /**
     * Update Network.
     *
     * @param network (Network) the partial values of the network to update
     * @param networkId (long) the ID of the network to update
     */
    @ApiOperation(value = "Update Network", nickname = "Update Network", notes = "Updates the network for the given " + PARAMETER_NETWORK_ID + " path variable")
    @RequestMapping(method = RequestMethod.PUT, path = PATH_URI_NETWORK_ID)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void updateNetwork(@RequestBody Network network, @PathVariable(PARAMETER_NETWORK_ID) long networkId);


    /**
     * Delete Network.
     *
     * @param networkId (long) the ID of the network to delete
     */
    @ApiOperation(value = "Delete Network", nickname = "Delete Network", notes = "Deletes the network for the given " + PARAMETER_NETWORK_ID + " path variable")
    @RequestMapping(method = RequestMethod.DELETE, path = PATH_URI_NETWORK_ID)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void deleteNetwork(@PathVariable(PARAMETER_NETWORK_ID) long networkId);


    /**
     * Get all Networks.
     *
     * @param name the name
     * @param offset (int) the starting offset of records to return, default value 0
     * @param limit (int) the limit of records to return, default value 10
     * @return (PagedResult) Paged list of networks
     */
    @ApiOperation(value = "Get Networks", nickname = "Get Networks", notes = "Gets a paged result of networks using the optional pagination offset and limit provided. " + "The default limit is " + DEFAULT_LIMIT + " records. An optional querystring parameter of '" + PARAMETER_NAME + "' is available to find a network by name.", response = PagedResult.class)
    @RequestMapping(method = RequestMethod.GET, value = PATH_URI_ALL_NETWORKS, produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({ ROLE_READ })
    public abstract PagedResult getAllNetworks(@RequestParam(name = PARAMETER_NAME, defaultValue = "") String name, @RequestParam(name = PARAMETER_PAGINATION_OFFSET, defaultValue = DEFAULT_OFFSET) Integer offset, @RequestParam(name = PARAMETER_PAGINATION_LIMIT, defaultValue = DEFAULT_LIMIT) Integer limit);


    /**
     * Add IPv4 Range.
     *
     * @param networkId (long) the ID of the network to add the range to
     * @param ipRange (IpRange) the request body
     * @return (CreatedResponse) containing the serial ID for the range
     */
    @ApiOperation(value = "Add IPv4 Range", nickname = "Add IPv4 Range", notes = "Adds an IPv4 Range to an existing network that is specified by the " + PARAMETER_NETWORK_ID + " path variable", response = CreatedResponse.class)
    @RequestMapping(method = RequestMethod.POST, value = PATH_URI_ADD_IPV4_RANGE)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.CREATED)
    public abstract CreatedResponse addIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody IpRange ipRange);


    /**
     * Update IPv4 Range.
     *
     * @param networkId (long) the serial identifier for the network
     * @param rangeId (long) the serial identifier for the static range
     * @param ipRange (IpRange) The request body
     */
    @ApiOperation(value = "Update IPv4 Range", nickname = "Update IPv4 Range", notes = "Updates an IPv4 Range for the given " + PARAMETER_NETWORK_ID + " and " + PARAMETER_RANGE_ID + " path variables")
    @RequestMapping(method = RequestMethod.PUT, value = PATH_URI_UPDATE_IPV4_RANGE)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void updateIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_RANGE_ID) long rangeId, @RequestBody IpRange ipRange);


    /**
     * Delete IPv4 Range.
     *
     * @param networkId (long) the ID of the network the range belongs to
     * @param rangeId (long) the ID of the range to delete
     */
    @ApiOperation(value = "Delete IPv4 Range", nickname = "Delete IPv4 Range", notes = "Deletes an IPv4 Range for the given " + PARAMETER_NETWORK_ID + " and " + PARAMETER_RANGE_ID + " path variables")
    @RequestMapping(method = RequestMethod.DELETE, value = PATH_URI_UPDATE_IPV4_RANGE)
    @RolesAllowed({ ROLE_CONFIGURE_NETWORK })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void deleteIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_RANGE_ID) long rangeId);


    // IPV4 Address Pool APIs

    /**
     * Get IPv4 Address Pool Entries.
     *
     * @param networkId (long) the network id
     * @param state (String) One of these states: {ALL, AVAILABLE, RESERVED, ASSIGNED}
     * @param usageId (String) An identifier for the process or entity using the IP address
     * @param offset (int) for pagination, the starting record to return
     * @param limit (int) for pagination, the number of records to return
     * @return (PagedResult) object containing a list of IpAddressPoolEntry objects and pagination information
     */
    @ApiOperation(value = "Get IPv4 Address Pool Entries", nickname = "Get IPv4 Address Pool Entries", notes = "Gets IPv4 Address Pool entries for the given" + PARAMETER_NETWORK_ID + " path variable.  Optionally the data can be filtered by the '" + PARAMETER_STATE + "'. Pagination is provided via the offset and limit query paramters.  The default limit is" + DEFAULT_LIMIT + " records", response = PagedResult.class)
    @ApiImplicitParams(@ApiImplicitParam(paramType = "query", name = "state", dataType = "string", value = "ALL", allowableValues = "ALL,AVAILABLE,RESERVED,ASSIGNED"))
    @RequestMapping(method = RequestMethod.GET, value = PATH_URI_IPADDRESSPOOLS, produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({ ROLE_READ })
    public abstract PagedResult getIpv4AddressPoolEntries(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestParam(name = PARAMETER_STATE, defaultValue = DEFAULT_STATE) String state, @RequestParam(name = PARAMETER_USAGE_ID, required = false) String usageId, @RequestParam(name = PARAMETER_PAGINATION_OFFSET, defaultValue = DEFAULT_OFFSET) int offset, @RequestParam(name = PARAMETER_PAGINATION_LIMIT, defaultValue = DEFAULT_LIMIT) int limit);


    /**
     * Reserve IPv4 Address Pool Entries.
     *
     * @param networkId (long) the network ID
     * @param reserveIpPoolAddressesRequest (ReserveIpPoolAddressesRequest) object containing the number of IP addresses to request, and the UsageId (string identifier) for the
     * process or entity reserving them.
     * @return the sets a set of string IP addresses
     */
    @ApiOperation(value = "Reserve IPv4 Address Pool Entries", nickname = "Reserve IPv4 Address Pool Entries", notes = "Reserves IPv4 address pool entries for the given" + PARAMETER_NETWORK_ID + " path variable. The body is required to contain the number of IP's requested, and a UsageId (string identifier) for the process or entity reserving them.", response = Set.class)
    @RequestMapping(method = RequestMethod.POST, value = PATH_URI_IPADDRESSPOOLS)
    @RolesAllowed({ ROLE_WRITE })
    @ResponseStatus(HttpStatus.CREATED)
    public abstract Set<String> reserveIpv4AddressPoolAddresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody ReserveIpPoolAddressesRequest reserveIpPoolAddressesRequest);


    /**
     * Assign IPv4 Address Pool Entries.
     *
     * @param networkId (long) the network ID
     * @param assignIpPoolAddresses (AssignIpPoolAddresses) object containing an array of the IP addresses to assign, and the UsageId (string identifier) for the process or entity
     * reserving them.
     */
    @ApiOperation(value = "Assign IPv4 Address Pool Entries", nickname = "Assign IPv4 Address Pool Entries", notes = "Assigns IPv4 address pool entries for the given" + PARAMETER_NETWORK_ID + " path variable. The body is required to contain an array with the specific IP addresses to assign, and a UsageId (string identifier) for the process or entity reserving them.")
    @RequestMapping(method = RequestMethod.PUT, value = PATH_URI_IPADDRESSPOOLS)
    @RolesAllowed({ ROLE_WRITE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void assignIpv4AddressPoolAddresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody AssignIpPoolAddresses assignIpPoolAddresses);


    /**
     * Release All IPv4 Address Pool Entries (for a Usage ID on a given Network).
     *
     * @param networkId (long) the network ID
     * @param usageId (string) a string identifier for the process or entity for which they are reserved or assigned.
     */
    @ApiOperation(value = "Release All IPv4 Address Pool Entries (for a Usage ID on a given network)", nickname = "Release All IPv4 Address Pool Entries", notes = "Releases all IPv4 address pool entries for the given" + PARAMETER_NETWORK_ID + " path variable. The body is required to contain a UsageId (string identifier).")
    @RequestMapping(method = RequestMethod.DELETE, value = PATH_URI_IPADDRESSPOOLS)
    @RolesAllowed({ ROLE_WRITE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void releaseAllIpv4Addresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestParam(PARAMETER_USAGE_ID) String usageId);


    /**
     * Release IPv4 Address Pool Entry.
     *
     * @param networkId (long) the network ID
     * @param ipAddress (string) the IP address to release
     */
    @ApiOperation(value = "Release IPv4 Address Pool Entry", nickname = "Release IPv4 Address Pool Entry", notes = "Releases a specific IP address pool entry for the given" + PARAMETER_NETWORK_ID + " path variable. The body is required to contain a IP address to release.")
    @RequestMapping(method = RequestMethod.DELETE, value = PATH_URI_IPADDRESSPOOLS_IPADDRESS)
    @RolesAllowed({ ROLE_WRITE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public abstract void releaseSpecificIpv4Address(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_IPADDRESS) String ipAddress);


    /**
     * Export IP Address Pool Data as Stream.
     *
     * @param networkId (long) the network ID
     * @return Stream containing exported data
     */
    @ApiOperation(value = "Export IP Address Pool Data as Stream", nickname = "Export IP Address Pool Data", notes = "Export IP Address Pool Data for the given" + PARAMETER_NETWORK_ID + " path variable", response = InputStreamResource.class)
    @RequestMapping(method = RequestMethod.GET, value = PATH_URI_EXPORT_IPADDRESS_POOL, produces = "application/octet-stream")
    @RolesAllowed({ ROLE_READ })
    public abstract ResponseEntity<InputStreamResource> exportIpAddressPoolData(@PathVariable(PARAMETER_NETWORK_ID) long networkId);
}
