package com.product_service.entities;

import com.product_service.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "category_db")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Category extends BaseEntity {

    private String name;

    private String description;

    @Column(nullable = false)
    private String userId;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> product;
}
