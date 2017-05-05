/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.virtualnetwork.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.dell.isg.smi.commons.elm.model.CreatedResponse;
import com.dell.isg.smi.commons.elm.model.PagedResult;
import com.dell.isg.smi.virtualnetwork.entity.NetworkConfiguration;
import com.dell.isg.smi.virtualnetwork.exception.BadRequestException;
import com.dell.isg.smi.virtualnetwork.exception.ErrorCodeEnum;
import com.dell.isg.smi.virtualnetwork.exception.NotFoundException;
import com.dell.isg.smi.virtualnetwork.model.AssignIpPoolAddresses;
import com.dell.isg.smi.virtualnetwork.model.ExportIpPoolData;
import com.dell.isg.smi.virtualnetwork.model.IpRange;
import com.dell.isg.smi.virtualnetwork.model.Network;
import com.dell.isg.smi.virtualnetwork.model.ReserveIpPoolAddressesRequest;
import com.dell.isg.smi.virtualnetwork.service.IpAddressPoolManager;
import com.dell.isg.smi.virtualnetwork.service.NetworkConfigurationManager;

@Component
public class NetworkControllerImpl implements NetworkController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkControllerImpl.class.getName());

    public static final String EXPORT_FILE_HEADER = "id,ipAddress,ipAddressState,expiryDate,targetId,targetName,lastModifiedBy,lastModifiedDate";
    public static final String NEW_LINE = "\n";
    public static final String CSV_SEPERATOR = ",";
    public static final String CSV_FILE_NAME_WITH_EXTENSION = "ip_addess_pool_data.csv";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    @Autowired
    private NetworkConfigurationManager networkConfigurationManager;

    @Autowired
    private IpAddressPoolManager ipAddressPoolManager;


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#createNetwork(com.dell.isg.smi.virtualnetwork.model.Network)
     */
    @Override
    public CreatedResponse createNetwork(@RequestBody Network network) {
        logger.trace("entered createNetwork(Network network)");
        if (null != network && null != network.getVlanId()) {
            int vlanId = network.getVlanId();
            if (networkConfigurationManager.isVlandIdGreaterThan4000(vlanId)) {
                BadRequestException badRequestException = new BadRequestException();
                badRequestException.setErrorCode(ErrorCodeEnum.NETWORKCONF_VLAN_ID_CREATE_OR_UPDATE_NOT_ALLOWED);
                badRequestException.addAttribute("vlanId");
                throw badRequestException;
            }
        }
        long networkId = networkConfigurationManager.createNetwork(network);

        CreatedResponse createdResponse = new CreatedResponse();
        createdResponse.setId(networkId);
        logger.trace("exiting createNetwork(Network network) with networkId: {} ", networkId);
        return createdResponse;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#getNetwork(long)
     */
    @Override
    public Network getNetwork(@PathVariable(PARAMETER_NETWORK_ID) long networkId) {
        logger.trace("Entered getNetwork() with networkId: {}", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        Network network = networkConfigurationManager.getNetwork(networkId);
        logger.trace("Exiting getNetwork() with networkId: {}", networkId);
        return network;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#updateNetwork(com.dell.isg.smi.virtualnetwork.model.Network, long)
     */
    @Override
    public void updateNetwork(@RequestBody Network network, @PathVariable(PARAMETER_NETWORK_ID) long networkId) {
        logger.trace("Entered updateNetwork(Network network) with networkId: {}", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (null != network && null != network.getVlanId()) {
            int vlanId = network.getVlanId();
            if (networkConfigurationManager.isVlandIdGreaterThan4000(vlanId)) {
                BadRequestException badRequestException = new BadRequestException();
                badRequestException.setErrorCode(ErrorCodeEnum.NETWORKCONF_VLAN_ID_CREATE_OR_UPDATE_NOT_ALLOWED);
                badRequestException.addAttribute("vlanId");
                throw badRequestException;
            }
        }
        networkConfigurationManager.updateNetwork(network, networkId);
        logger.trace("Exiting updateNetwork(Network network) with networkId: {}", networkId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#deleteNetwork(long)
     */
    @Override
    public void deleteNetwork(@PathVariable(PARAMETER_NETWORK_ID) long networkId) {
        logger.trace("Entered deleteNetwork() with networkId: {}", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        NetworkConfiguration networkConfiguration = networkConfigurationManager.getNetworkConfiguration(networkId);
        int vlanId = networkConfiguration.getVlanId();
        if (networkConfigurationManager.isVlandIdGreaterThan4000(vlanId)) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(ErrorCodeEnum.NETWORKCONF_VLAN_ID_DELETE_NOT_ALLOWED);
            badRequestException.addAttribute("vlanId");
            throw badRequestException;
        }

        networkConfigurationManager.deleteNetwork(networkConfiguration, networkId);
        logger.trace("Exiting deleteNetwork() with networkId: {}", networkId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#getAllNetworks(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public PagedResult getAllNetworks(@RequestParam(name = PARAMETER_NAME, required = false) String name, @RequestParam(name = PARAMETER_PAGINATION_OFFSET, defaultValue = DEFAULT_OFFSET) Integer offset, @RequestParam(name = PARAMETER_PAGINATION_LIMIT, defaultValue = DEFAULT_LIMIT) Integer limit) {
        logger.trace("entered getAllNetworks()");
        PagedResult networks;
        if (!StringUtils.isEmpty(name)) {
            networks = networkConfigurationManager.getNetworkByNamePaged(name);
        } else {
            networks = networkConfigurationManager.getAllNetworks(offset, limit);
        }
        logger.trace("exiting getAllNetworks()");
        return networks;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#addIpv4Range(long, com.dell.isg.smi.virtualnetwork.model.IpRange)
     */
    @Override
    public CreatedResponse addIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody IpRange ipRange) {
        logger.trace("Entered addIPV4Range() method with networkId:{}", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        long ipRangeId = networkConfigurationManager.addIpv4Range(networkId, ipRange);
        CreatedResponse createdResponse = new CreatedResponse();
        createdResponse.setId(ipRangeId);
        logger.trace("Exiting addIPV4Range() method with ipRangeId:{}", ipRangeId);
        return createdResponse;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#updateIpv4Range(long, long, com.dell.isg.smi.virtualnetwork.model.IpRange)
     */
    @Override
    public void updateIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_RANGE_ID) long rangeId, @RequestBody IpRange ipRange) {
        logger.trace("Entered updateIPV4Range(networkId{}, rangeId{})", networkId, rangeId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        networkConfigurationManager.isIpRangeIdValid(rangeId);
        networkConfigurationManager.updateIpv4Range(networkId, rangeId, ipRange);
        logger.trace("Exiting updateIPV4Range(networkId{}, rangeId{})", networkId, rangeId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#deleteIpv4Range(long, long)
     */
    @Override
    public void deleteIpv4Range(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_RANGE_ID) long rangeId) {
        logger.trace("Entered deleteIPV4Range(networkId{}, rangeId{})", networkId, rangeId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        networkConfigurationManager.isIpRangeIdValid(rangeId);
        networkConfigurationManager.deleteIpv4Range(networkId, rangeId);
        logger.trace("Exiting deleteIPV4Range(networkId{}, rangeId{})", networkId, rangeId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#getIpv4AddressPoolEntries(long, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public PagedResult getIpv4AddressPoolEntries(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestParam(name = PARAMETER_USAGE_ID, required = false) String usageId, @RequestParam(name = PARAMETER_STATE, defaultValue = DEFAULT_STATE) String state, @RequestParam(name = PARAMETER_PAGINATION_OFFSET, defaultValue = DEFAULT_OFFSET) int offset, @RequestParam(name = PARAMETER_PAGINATION_LIMIT, defaultValue = DEFAULT_LIMIT) int limit) {
        logger.trace("Entered getIpv4AddressPoolEntries(networkId{})", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        if (!"ALL".equalsIgnoreCase(state)) {
            if (!ipAddressPoolManager.isIpAddressStateValid(state)) {
                BadRequestException badRequestException = new BadRequestException();
                badRequestException.setErrorCode(ErrorCodeEnum.ENUM_INVALID_DATA);
                badRequestException.addAttribute("ipAddressState");
                throw badRequestException;
            }
        }

        PagedResult ipv4AddressPoolEntries = ipAddressPoolManager.getIpv4AddressPoolEntries(networkId, state, usageId, offset, limit);
        if (ipv4AddressPoolEntries == null) {
            throw new NotFoundException();
        }
        logger.trace("Exiting getIpv4AddressPoolEntries(networkId{})", networkId);
        return ipv4AddressPoolEntries;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#reserveIpv4AddressPoolAddresses(long, com.dell.isg.smi.virtualnetwork.model.ReserveIpPoolAddressesRequest)
     */
    @Override
    public Set<String> reserveIpv4AddressPoolAddresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody ReserveIpPoolAddressesRequest reserveIpPoolAddressesRequest) {
        logger.trace("Entered reserveIpv4AddressPoolAddresses(networkId{})", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);

        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        if (!ipAddressPoolManager.isUsageIdValid(reserveIpPoolAddressesRequest.getUsageId())) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(ErrorCodeEnum.ENUM_INVALID_DATA);
            badRequestException.addAttribute("usageId");
            throw badRequestException;
        }

        if (!ipAddressPoolManager.isReserveQuantityRequestedIsValid(reserveIpPoolAddressesRequest)) {
            BadRequestException badRequestException = new BadRequestException();
            badRequestException.setErrorCode(ErrorCodeEnum.ENUM_INVALID_DATA);
            badRequestException.addAttribute("reserveIpPoolAddressesRequest.quantityRequested");
            throw badRequestException;
        }

        // Setting the expiration date of reserved ip address as 2 Days.
        int reservationCalendarUnit = Calendar.HOUR;
        int reservationNumberOfUnits = 48;
        Set<String> reserveIpPoolAddressesResponse = ipAddressPoolManager.reserveIpv4AddressPoolAddresses(networkId, reserveIpPoolAddressesRequest, reservationCalendarUnit, reservationNumberOfUnits);

        logger.trace("Exiting reserveIpv4AddressPoolAddresses(networkId{})", networkId);
        return reserveIpPoolAddressesResponse;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#assignIpv4AddressPoolAddresses(long, com.dell.isg.smi.virtualnetwork.model.AssignIpPoolAddresses)
     */
    @Override
    public void assignIpv4AddressPoolAddresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestBody AssignIpPoolAddresses assignIpPoolAddresses) {
        logger.trace("Entered assignIpv4AddressPoolAddresses(networkId{})", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        ipAddressPoolManager.isUsageIdValid(assignIpPoolAddresses.getUsageId());
        ipAddressPoolManager.assignIpv4AddressPoolAddresses(networkId, assignIpPoolAddresses);
        logger.trace("Exiting assignIpv4AddressPoolAddresses(networkId{})", networkId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#releaseAllIpv4Addresses(long, java.lang.String)
     */
    @Override
    public void releaseAllIpv4Addresses(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @RequestParam(PARAMETER_USAGE_ID) String usageId) {
        logger.trace("Entered releaseAllIpv4Addresses(networkId{}, usageId{})", networkId, usageId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        ipAddressPoolManager.isUsageIdValid(usageId);
        ipAddressPoolManager.releaseAllIpv4Addresses(networkId, usageId);
        logger.trace("Exiting releaseAllIpv4Addresses(networkId{}, usageId{})", networkId, usageId);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#releaseSpecificIpv4Address(long, java.lang.String)
     */
    @Override
    public void releaseSpecificIpv4Address(@PathVariable(PARAMETER_NETWORK_ID) long networkId, @PathVariable(PARAMETER_IPADDRESS) String ipAddress) {
        logger.trace("Entered releaseSpecificIpv4Address(networkId{}, ipAddress{})", networkId, ipAddress);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        ipAddressPoolManager.isIpAddressValid(ipAddress);
        ipAddressPoolManager.releaseSpecificIpv4Address(networkId, ipAddress);
        logger.trace("Exiting releaseSpecificIpv4Address(networkId{}, ipAddress{})", networkId, ipAddress);
        return;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.dell.isg.smi.virtualnetwork.controller.NetworkController#exportIpAddressPoolData(long)
     */
    @Override
    public ResponseEntity<InputStreamResource> exportIpAddressPoolData(@PathVariable(PARAMETER_NETWORK_ID) long networkId) {
        logger.trace("Entering exportIpAddressPoolData() with networkId: {}", networkId);
        networkConfigurationManager.isNetworkIdValid(networkId);
        if (!ipAddressPoolManager.doesNetworkRecordExist(networkId)) {
            NotFoundException notFoundException = new NotFoundException();
            notFoundException.setErrorCode(ErrorCodeEnum.ENUM_NOT_FOUND_ERROR);
            notFoundException.addAttribute("network");
            throw notFoundException;
        }
        List<ExportIpPoolData> ipPoolAddressesInUse = ipAddressPoolManager.exportIpPoolsInUse(networkId);
        String ipPoolData = EXPORT_FILE_HEADER + NEW_LINE;

        for (ExportIpPoolData poolData : ipPoolAddressesInUse) {
            ipPoolData += String.format("%s,%s,%s,%s,%s,%s,%s,%s%n", poolData.getId(), poolData.getIpAddress(), poolData.getIpAddressState(), poolData.getExpiryDate(), poolData.getUsageId(), poolData.getTargetName(), poolData.getLastModifiedBy(), poolData.getLastModifiedDate());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add(CONTENT_DISPOSITION_HEADER, "attachment; filename=\"" + CSV_FILE_NAME_WITH_EXTENSION + "\"");

        logger.trace("Exiting exportIpAddressPoolData()");

        InputStream is = new ByteArrayInputStream(ipPoolData.getBytes());
        ResponseEntity<InputStreamResource> streamResource = ResponseEntity.ok().headers(headers).contentLength(ipPoolData.length()).contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream")).body(new InputStreamResource(is));
        logger.trace("Exiting exportIpAddressPoolData()");
        return streamResource;
    }

}
