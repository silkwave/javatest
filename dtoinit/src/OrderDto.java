import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
public class OrderDto {
    private String orderId;
    private ProductDto product;    // OrderDto 안에 DTO
    private List<ReviewDto> reviews; // OrderDto 안에 List<DTO>
}

