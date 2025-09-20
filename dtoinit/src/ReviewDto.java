import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
public class ReviewDto {
    private String reviewer;
    private String comment;
    private int rating;

    private UserDto userDto;
}
