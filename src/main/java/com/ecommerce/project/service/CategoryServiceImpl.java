package com.ecommerce.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.OrderItemRepository;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final OrderItemRepository orderItemRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper, OrderItemRepository orderItemRepository) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable  pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> page = categoryRepository.findAll(pageDetails);
        List<Category> categories = page.getContent();
        if(categories.isEmpty()){
            throw new APIException("No categories found");
        }
        List<CategoryDTO> categoryDTOS = categories.stream().map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
        return new CategoryResponse(categoryDTOS, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category categoryToSave = modelMapper.map(categoryDTO, Category.class);
        Category existingCategory = categoryRepository.findByCategoryName(categoryToSave.getCategoryName());
        if(existingCategory != null){
            throw new APIException("Category with name " + categoryDTO.getCategoryName() + " already exists");
        }
        Category savedCategory = categoryRepository.save(categoryToSave);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("Category","categoryId",categoryId));
        for (Product product : category.getProducts()) {
            orderItemRepository.deleteAllByProductId(product.getProductId());
        }
        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category categoryToUpdate = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("Category","categoryId",categoryId));

        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryToUpdate.setCategoryName(category.getCategoryName());
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }
}
