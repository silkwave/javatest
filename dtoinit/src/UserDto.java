import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class UserDto {
    private String name;
    private int age;
    private List<OrderDto> orders; // UserDto 안에 List<DTO>


}