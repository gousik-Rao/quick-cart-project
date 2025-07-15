package com.ecommerce.project.service;

import java.io.IOException;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.util.AuthUtil;

@Service
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final AuthUtil authUtil;
    @SuppressWarnings("unused")
	private final UserRepository userRepository;
    @Value("${project.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public ProductServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, ModelMapper modelMapper, FileService fileService, CartRepository cartRepository, CartService cartService, AuthUtil authUtil, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.authUtil = authUtil;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        User seller = authUtil.loggedInUser();

        if(seller.getRoles().stream().noneMatch(role->role.getRoleName().equals(AppRole.ROLE_SELLER)|| role.getRoleName().equals(AppRole.ROLE_ADMIN))){
            throw new APIException("Only sellers and admins are allowed to add products.");
        }

        List<Product> products = category.getProducts();

        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                throw new APIException("Product already exists.");
            }
        }

        if (productDTO.getDiscount() < 0 || productDTO.getDiscount() > 100) {
            throw new APIException("Discount must be between 0 and 100.");
        }

        Product product = modelMapper.map(productDTO, Product.class);
        product.setImage("default.png");
        product.setUser(seller);
        product.setCategory(category);
        double discountRate = product.getDiscount() / 100.0;
        double specialPrice = Math.round(product.getPrice() * (1 - discountRate) * 100.0) / 100.0;
        product.setSpecialPrice(specialPrice);
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        seller.getProducts().add(savedProduct);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,String keyword, String category) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Specification<Product> specification = Specification.where((Specification<Product>) (root, query, cb) ->
                cb.isTrue(root.get("isAvailable"))
        );
        if (keyword != null && !keyword.isEmpty()){
            specification = specification
                    .and(((root, query, criteriaBuilder) ->
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),"%"+keyword.toLowerCase()+"%")));
        }
        if (category != null && !category.isEmpty()){
            specification = specification
                    .and(((root, query, criteriaBuilder) ->
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("category").get("categoryName")),category.toLowerCase())));
        }
        Page<Product> page = productRepository.findAll(specification, pageDetails);
        List<Product> products = page.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map((product) -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(constructImageUrl(product.getImage()));
                    return productDTO;

                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getSellerProducts(String keyword, String category, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        User currUser = authUtil.loggedInUser();
        Long currUserId = currUser.getUserId();

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Specification<Product> specification = Specification
                .<Product>where((root, query, cb) ->
                        cb.equal(root.get("user").get("userId"), currUserId)
                )
                .and((Specification<Product>) (root, query, cb) ->
                        cb.isTrue(root.get("isAvailable"))
                );

        if (keyword != null && !keyword.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
        }

        if (category != null && !category.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("category").get("categoryName")), category.toLowerCase()));
        }

        Page<Product> page = productRepository.findAll(specification, pageDetails);
        List<Product> products = page.getContent();


        List<ProductDTO> productDTOS = products.stream()
                .map((product) -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(constructImageUrl(product.getImage()));
                    return productDTO;

                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO changeAvailability(long productId, boolean isAvailable) {
        User seller = authUtil.loggedInUser();
        if(seller.getRoles().stream().noneMatch(role->role.getRoleName().equals(AppRole.ROLE_SELLER)|| role.getRoleName().equals(AppRole.ROLE_ADMIN))){
            throw new APIException("Only sellers and admins are allowed to update products.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        product.setIsAvailable(isAvailable);
        productRepository.save(product);
        return modelMapper.map(product, ProductDTO.class);

    }

    private String constructImageUrl(String imageName){
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> page = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> products = page.getContent();
        List<ProductDTO> productDTOS = products.stream().filter((Product::getIsAvailable)).map((product) -> modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> page = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageDetails);
        List<Product> products = page.getContent();

        List<ProductDTO> productDTOS = products.stream().filter((Product::getIsAvailable)).map((product) -> modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(page.getNumber());
        productResponse.setPageSize(page.getSize());
        productResponse.setTotalElements(page.getTotalElements());
        productResponse.setTotalPages(page.getTotalPages());
        productResponse.setLastPage(page.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) throws IOException {
        Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        User currUser = authUtil.loggedInUser();

        boolean isOwner = currUser.getProducts().stream().anyMatch((product) -> product.getProductId().equals(productId));
        boolean isAdmin = currUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(AppRole.ROLE_ADMIN));

        if(!isOwner && !isAdmin){
                throw new APIException("Only owner or admin can update this product.");
        }

        if(productDTO.getDescription().length() < 6 || productDTO.getDescription().length() > 255){
            throw new APIException("Product description length must be between 6-255 characters.");
        }

        Product product = modelMapper.map(productDTO, Product.class);
        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        double specialPrice = existingProduct.getPrice() - (existingProduct.getDiscount() * 0.01) * existingProduct.getPrice();
        existingProduct.setSpecialPrice(specialPrice);

        Product updatedProduct = productRepository.save(existingProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

        cartDTOs.forEach(cart-> cartService.updateProductInCarts(cart.getCartId(), productId));


        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Transactional
    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productToDelete = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        User currUser = authUtil.loggedInUser();

        boolean isOwner = currUser.getProducts().stream().anyMatch((product) -> product.getProductId().equals(productId));
        boolean isAdmin = currUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(AppRole.ROLE_ADMIN));

        if(!isOwner && !isAdmin){
            throw new APIException("Only owner or admin can delete this product.");
        }

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart->cartService.deleteProductFromCart(cart.getCartId(), productId));

        currUser.getProducts().remove(productToDelete);
        productRepository.deleteById(productId);
        return modelMapper.map(productToDelete, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image)  {
        Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        User currUser = authUtil.loggedInUser();

        boolean isOwner = currUser.getProducts().stream().anyMatch((product) -> product.getProductId().equals(productId));
        boolean isAdmin = currUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(AppRole.ROLE_ADMIN));

        if(!isOwner && !isAdmin){
            throw new APIException("Only owner or admin can update image of this product.");
        }

        String fileName = "";
        try {
            fileName = fileService.uploadImage(path, image);
        } catch (IOException e) {
            throw new APIException("Please provide valid multipart file.");
        }
        existingProduct.setImage(fileName);
        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

}
