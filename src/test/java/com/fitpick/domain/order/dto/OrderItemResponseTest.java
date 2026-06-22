package com.fitpick.domain.order.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.order.entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemResponseTest {

    @Test
    void 주문항목_응답에_상품_썸네일_URL을_포함한다() {
        Clothes clothes = mock(Clothes.class);
        when(clothes.getId()).thenReturn(1L);
        when(clothes.getTitle()).thenReturn("오버핏 코튼 셔츠");
        when(clothes.getThumbnailImageUrl()).thenReturn("/images/shirt_thumb.jpg");

        ClothesOption option = mock(ClothesOption.class);
        when(option.getId()).thenReturn(2L);
        when(option.getClothes()).thenReturn(clothes);
        when(option.getSize()).thenReturn("M");
        when(option.getColor()).thenReturn("화이트");

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(20L);
        when(item.getClothesOption()).thenReturn(option);
        when(item.getQuantity()).thenReturn(2);
        when(item.getPrice()).thenReturn(39000);

        OrderItemResponse response = OrderItemResponse.from(item);

        assertThat(response.thumbnailImageUrl()).isEqualTo("/images/shirt_thumb.jpg");
    }
}
