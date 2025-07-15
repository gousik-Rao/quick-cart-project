package com.ecommerce.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.util.AuthUtil;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class AddressServiceImpl implements AddressService{

    private final AuthUtil authUtil;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AddressServiceImpl(AuthUtil authUtil, AddressRepository addressRepository, ModelMapper modelMapper, UserRepository userRepository, OrderRepository orderRepository) {
        this.authUtil = authUtil;
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public AddressDTO createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User currUser = authUtil.loggedInUser();
        Address addressToSave = modelMapper.map(addressDTO, Address.class);

        List<Address> userAddresses = currUser.getAddresses();
        userAddresses.add(addressToSave);
        currUser.setAddresses(userAddresses);

        addressToSave.setUser(currUser);

        Address savedAddress = addressRepository.save(addressToSave);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> allAddresses = addressRepository.findAll();
        return allAddresses.stream()
                .map((address) -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        User currUser = authUtil.loggedInUser();

        return currUser.getAddresses().stream()
                .map((addr)->modelMapper.map(addr, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address addressToUpdate = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        addressToUpdate.setStreet(addressDTO.getStreet());
        addressToUpdate.setBuildingName(addressDTO.getBuildingName());
        addressToUpdate.setCity(addressDTO.getCity());
        addressToUpdate.setState(addressDTO.getState());
        addressToUpdate.setCountry(addressDTO.getCountry());
        addressToUpdate.setZipcode(addressDTO.getZipcode());

        Address updatedAddress = addressRepository.save(addressToUpdate);

        User currUser = authUtil.loggedInUser();
        currUser.getAddresses().removeIf((address -> address.getAddressId().equals(addressId)));
        currUser.getAddresses().add(updatedAddress);
        userRepository.save(currUser);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        User currUser = authUtil.loggedInUser();
        List<Order> orders = orderRepository.findByAddress(address);

        if(!orders.isEmpty()){
            throw new APIException("Unable to delete address associated with order under process.");
        }
        currUser.getAddresses().removeIf((addr)->addr.getAddressId().equals(addressId));
        userRepository.save(currUser);


        addressRepository.delete(address);

        return "Address deleted successfully with addressId: "+addressId;
    }


}
