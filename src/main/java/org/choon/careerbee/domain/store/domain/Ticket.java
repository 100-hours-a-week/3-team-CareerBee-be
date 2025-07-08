package org.choon.careerbee.domain.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;
import org.choon.careerbee.common.enums.CustomResponseStatus;
import org.choon.careerbee.common.exception.CustomException;
import org.choon.careerbee.domain.store.domain.enums.TicketType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price")
    private Integer price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(length = 500, nullable = false)
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TicketType type;

    private Ticket(Integer price, Integer quantity, String imgUrl, TicketType type) {
        this.price = price;
        this.quantity = quantity;
        this.imgUrl = imgUrl;
        this.type = type;
    }

    @Builder
    public static Ticket of(
        Integer price, Integer quantity, String imgUrl, TicketType type
    ) {
        return new Ticket(
            price, quantity, imgUrl, type
        );
    }


    public void use() {
        if (this.quantity == null || this.quantity <= 0) {
            throw new CustomException(CustomResponseStatus.TICKET_OUT_OF_STOCK);
        }
        this.quantity -= 1;
    }
}
