import lombok.*;

@Data
@NoArgsConstructor
public class ProductDto {
    private String name;
    private double price;
    private String category;
}