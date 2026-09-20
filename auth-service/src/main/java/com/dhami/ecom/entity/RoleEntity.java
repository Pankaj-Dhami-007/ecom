package com.dhami.ecom.entity;

import com.dhami.ecom.enums.RoleName;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        callSuper = false
)
@Getter
@Setter
public class RoleEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;
    private String description;

}
