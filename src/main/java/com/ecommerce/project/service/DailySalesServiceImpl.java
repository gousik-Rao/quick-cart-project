package com.ecommerce.project.service;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.DailySales;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.DailySalesDTO;
import com.ecommerce.project.payload.SalesResponse;
import com.ecommerce.project.repository.DailySalesRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.util.AuthUtil;

@Service
public class DailySalesServiceImpl implements DailySalesService{

    @SuppressWarnings("unused")
	private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final DailySalesRepository dailySalesRepository;
    private final ModelMapper modelMapper;

    public DailySalesServiceImpl(UserRepository userRepository, AuthUtil authUtil, DailySalesRepository dailySalesRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.authUtil = authUtil;
        this.dailySalesRepository = dailySalesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void updateCreateSales(int quantity, double totalEarned, User seller) {
        System.out.println("qty->"+quantity);
        System.out.println("Total earned ->"+totalEarned);
        int currYear = LocalDate.now().getYear();
        int currMonth = LocalDate.now().getMonthValue();
        int currDay = LocalDate.now().getDayOfMonth();
        LocalDate today = LocalDate.of(currYear, currMonth, currDay);
        DailySales dailySales = dailySalesRepository.findByDateAndSeller(today, seller)
                .stream().findFirst()
                .orElseGet(() -> new DailySales(seller, today));
        dailySales.setSoldCount(dailySales.getSoldCount()+quantity);
        dailySales.setTotalEarned(dailySales.getTotalEarned()+totalEarned);
        dailySalesRepository.save(dailySales);
    }

    @Override
    public SalesResponse getSales(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        User currUser = authUtil.loggedInUser();

        if(currUser.getRoles().stream().noneMatch(role->role.getRoleName().equals(AppRole.ROLE_SELLER)|| role.getRoleName().equals(AppRole.ROLE_ADMIN))){
            throw new APIException("Only sellers can view sales raport.");
        }

        Page<DailySales> page = dailySalesRepository.findAllBySeller(currUser, pageDetails);
        List<DailySales> sales = page.getContent();
        List<DailySalesDTO> salesDTOS = sales.stream().map((sale) -> modelMapper.map(sale, DailySalesDTO.class)).toList();

        SalesResponse salesResponse = new SalesResponse();
        salesResponse.setContent(salesDTOS);
        salesResponse.setPageNumber(page.getNumber());
        salesResponse.setPageSize(page.getSize());
        salesResponse.setTotalElements(page.getTotalElements());
        salesResponse.setTotalPages(page.getTotalPages());
        salesResponse.setLastPage(page.isLast());
        return salesResponse;
    }


}
