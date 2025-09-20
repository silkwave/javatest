import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
    public static void main(String[] args) {
        // Gson 객체 생성 (pretty printing)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // 한 줄로 DTO 초기화 + JSON 출력
        System.out.println("=== DtoInitializerSafe ===");
        UserDto user1 = DtoInitializerSafe.init(UserDto.class);
        System.out.println(gson.toJson(user1));

        System.out.println("\n=== DtoInitializerGson ===");
        UserDto user2 = DtoInitializerGson.init(UserDto.class);
        System.out.println(gson.toJson(user2));

    }
}
