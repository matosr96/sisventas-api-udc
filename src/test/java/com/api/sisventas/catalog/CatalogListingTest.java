package com.api.sisventas.catalog;

import com.api.sisventas.businessLogic.category.CreateCategory;
import com.api.sisventas.businessLogic.category.DeleteCategory;
import com.api.sisventas.businessLogic.category.ListCategories;
import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.businessLogic.product.DeleteProduct;
import com.api.sisventas.businessLogic.product.GetProduct;
import com.api.sisventas.businessLogic.product.ListProducts;
import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.businessLogic.supplier.CreateSupplier;
import com.api.sisventas.businessLogic.supplier.ListSuppliers;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.SupplierStatus;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.category.CategoryResponse;
import com.api.sisventas.models.dtos.category.CreateCategoryRequest;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import com.api.sisventas.models.dtos.supplier.CreateSupplierRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Los listados filtran y ordenan en la base tal como promete la API, los uniques del catálogo se
 * traducen a códigos y lo que tiene histórico se desactiva en vez de borrarse.
 */
@SpringBootTest
class CatalogListingTest {

    private static final String TESTER = "catalog-tester";
    private static final BigDecimal PRICE = new BigDecimal("10.00");

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CreateCategory createCategory;
    @Autowired private DeleteCategory deleteCategory;
    @Autowired private ListCategories listCategories;
    @Autowired private CreateProduct createProduct;
    @Autowired private DeleteProduct deleteProduct;
    @Autowired private GetProduct getProduct;
    @Autowired private ListProducts listProducts;
    @Autowired private CreateSupplier createSupplier;
    @Autowired private ListSuppliers listSuppliers;
    @Autowired private CreateSale createSale;

    private String tag;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername(TESTER).isEmpty()) {
            Role role = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
            User user = new User();
            user.setFirstName("Catalog");
            user.setLastName("Tester");
            user.setUsername(TESTER);
            user.setPassword("not-used");
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TESTER, null, List.of()));
        tag = "T" + System.nanoTime();
    }

    private ProductResponse product(String name, int stock, Integer low, Long categoryId, ProductStatus status) {
        return createProduct.execute(new CreateProductRequest(
                tag + "-" + name, name + " " + tag, null, PRICE, stock, status, null, low, categoryId));
    }

    @Test
    void productsFilterBySearchStatusCategoryAndLowStockAndSortByStock() {
        CategoryResponse drinks = createCategory.execute(new CreateCategoryRequest("Drinks " + tag, null));
        product("agua", 2, 5, drinks.id(), ProductStatus.ACTIVE);
        product("jugo", 40, 5, drinks.id(), ProductStatus.ACTIVE);
        product("jabon", 1, 5, null, ProductStatus.INACTIVE);

        var byText = listProducts.execute(tag + "-ju", null, null, null, null, null, 1, 10);
        assertEquals(1, byText.count());
        assertEquals("jugo " + tag, byText.items().get(0).name());

        var lowActive = listProducts.execute(tag, ProductStatus.ACTIVE, null, true, null, null, 1, 10);
        assertEquals(List.of("agua " + tag), lowActive.items().stream().map(ProductResponse::name).toList(),
                "el jabón está bajo pero retirado; el jugo está activo pero no bajo");

        var inCategory = listProducts.execute(tag, null, drinks.id(), null, "stock", "desc", 1, 10);
        assertEquals(List.of("jugo " + tag, "agua " + tag), inCategory.items().stream().map(ProductResponse::name).toList());
        assertEquals(2, inCategory.count());
    }

    @Test
    void duplicateSkuCategoryNameAndSupplierNameAreDomainErrors() {
        product("uno", 1, null, null, ProductStatus.ACTIVE);
        DomainError sku = assertThrows(DomainError.class, () -> product("uno", 1, null, null, ProductStatus.ACTIVE));
        assertEquals(ErrorCodes.SKU_ALREADY_EXISTS, sku.code());

        createCategory.execute(new CreateCategoryRequest("Cat " + tag, null));
        DomainError category = assertThrows(DomainError.class,
                () -> createCategory.execute(new CreateCategoryRequest("Cat " + tag, null)));
        assertEquals(ErrorCodes.CATEGORY_NAME_ALREADY_EXISTS, category.code());

        createSupplier.execute(new CreateSupplierRequest("Sup " + tag, null, null, null));
        DomainError supplier = assertThrows(DomainError.class,
                () -> createSupplier.execute(new CreateSupplierRequest("Sup " + tag, null, null, null)));
        assertEquals(ErrorCodes.SUPPLIER_NAME_ALREADY_EXISTS, supplier.code());
        assertEquals(1, listSuppliers.execute("Sup " + tag, SupplierStatus.ACTIVE, null, null, 1, 10).count());
        assertEquals(1, listCategories.execute("Cat " + tag, null, null, 1, 10).count());
    }

    @Test
    void deletingKeepsHistoryAndRefusesCategoriesInUse() {
        CategoryResponse used = createCategory.execute(new CreateCategoryRequest("Used " + tag, null));
        ProductResponse sold = product("vendido", 3, null, used.id(), ProductStatus.ACTIVE);
        ProductResponse untouched = product("intacto", 0, null, null, ProductStatus.ACTIVE);
        createSale.execute(new CreateSaleRequest(null, List.of(new SaleItemRequest(sold.id(), 1)), null, null, null, null));

        DomainError inUse = assertThrows(DomainError.class, () -> deleteCategory.execute(used.id()));
        assertEquals(ErrorCodes.CATEGORY_IN_USE, inUse.code());

        deleteProduct.execute(sold.id());
        assertEquals(ProductStatus.INACTIVE, getProduct.execute(sold.id()).status(), "con movimientos se retira, no se borra");

        deleteProduct.execute(untouched.id());
        DomainError gone = assertThrows(DomainError.class, () -> getProduct.execute(untouched.id()));
        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, gone.code());
        assertTrue(listProducts.execute(tag + "-intacto", null, null, null, null, null, 1, 10).items().isEmpty());
    }
}
