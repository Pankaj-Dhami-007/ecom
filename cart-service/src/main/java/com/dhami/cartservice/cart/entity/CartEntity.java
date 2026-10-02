package com.dhami.cartservice.cart.entity;

import com.dhami.cartservice.cart.enums.CartStatusEnum;
import com.dhami.cartservice.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "cart",
        indexes = {
                @Index(name = "idx_cart_user_id_status", columnList = "user_id,status")
        }
)
public class CartEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatusEnum status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "active_marker")
    private Integer activeMarker;

    @Column(name = "order_id")
    private Long orderId;

    @Version
    @Column(nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }

    public void changeStatus(CartStatusEnum status) {
        this.status = status;
        this.activeMarker = (status == CartStatusEnum.ACTIVE) ? 1 : null;
    }

    public void removeItem(CartItemEntity item) {
        items.remove(item);
        item.setCart(null);
    }
}