package com.ecommerce.project.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String category,
                                                        @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                        @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                        @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                        @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder){
        ProductResponse productResponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder, keyword, category);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PostMapping("/seller/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long categoryId){
        ProductDTO savedProductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(savedProductDTO,HttpStatus.CREATED);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder){
        ProductResponse productResponse = productService.searchByCategory(categoryId, pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder){
        ProductResponse productResponse=  productService.searchProductByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrder);
         return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/seller/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @PathVariable Long productId) throws IOException {
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("/seller/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        ProductDTO deletedProductDTO =  productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProductDTO, HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam MultipartFile image) throws IOException {
        ProductDTO updatedProductDTO = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @GetMapping("/seller/products")
    public ResponseEntity<ProductResponse> getSellerProducts(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String category,
                                                             @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                             @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder){
        ProductResponse productResponse = productService.getSellerProducts(keyword,category,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/seller/products/{productId}/availability")
    public ResponseEntity<ProductDTO> changeAvailability(@PathVariable long productId, @RequestBody boolean isAvailable){
        ProductDTO productDTO = productService.changeAvailability(productId,isAvailable);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
