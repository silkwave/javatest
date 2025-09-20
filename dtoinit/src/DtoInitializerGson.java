import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO를 기본값으로 초기화하고 JsonObject를 생성하는 유틸리티 클래스
 */
public class DtoInitializerGson {

    private DtoInitializerGson() {
        throw new IllegalStateException("Utility class");
    }

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * DTO 클래스를 기본값으로 초기화하여 객체로 반환
     *
     * @param clazz 초기화할 DTO 클래스
     * @param <T> DTO 타입
     * @return 기본값으로 초기화된 DTO 객체
     */
    public static <T> T init(Class<T> clazz) {
        JsonObject defaultJson = createDefaultJson(clazz, new HashSet<>());
        return GSON.fromJson(defaultJson, clazz);
    }

    /**
     * DTO 클래스의 필드를 기반으로 기본 JsonObject 생성
     * 순환 참조를 방지하기 위해 callStack 사용
     *
     * @param clazz 대상 DTO 클래스
     * @param callStack 현재 탐색 중인 클래스 집합 (순환 참조 방지)
     * @return 기본값이 채워진 JsonObject
     */
    public static JsonObject createDefaultJson(Class<?> clazz, Set<Class<?>> callStack) {
        if (callStack.contains(clazz)) {
            System.out.println("⚠️ 순환 참조 감지: " + clazz.getSimpleName());
            return null;
        }

        callStack.add(clazz);
        JsonObject jsonObject = new JsonObject();

        for (var field : clazz.getDeclaredFields()) {
            String name = field.getName();
            Class<?> type = field.getType();

            if (isPrimitiveType(type)) {
                // 기본형 또는 Wrapper 타입 처리
                addPrimitive(jsonObject, name, type);
            } else if (java.util.List.class.isAssignableFrom(type)) {
                // List 타입 처리
                addList(jsonObject, name, field, callStack);
            } else {
                // 객체 타입 처리 (재귀 호출)
                addNestedObject(jsonObject, name, type, callStack);
            }
        }

        callStack.remove(clazz);
        return jsonObject;
    }

    /**
     * 문자열, 숫자, 불리언 등 기본 타입인지 확인
     */
    private static boolean isPrimitiveType(Class<?> type) {
        return type == String.class ||
                type == Integer.class || type == int.class ||
                type == Double.class || type == double.class ||
                type == Boolean.class || type == boolean.class;
    }

    /**
     * 기본 타입 필드에 기본값 설정
     */
    private static void addPrimitive(JsonObject jsonObject, String name, Class<?> type) {
        if (type == String.class) jsonObject.addProperty(name, "");
        else if (type == Integer.class || type == int.class)     jsonObject.addProperty(name, 0);
        else if (type == Double.class  || type == double.class)  jsonObject.addProperty(name, 0.0);
        else if (type == Boolean.class || type == boolean.class) jsonObject.addProperty(name, false);
    }

    /**
     * List 필드에 기본값 객체 추가
     */
    private static void addList(JsonObject jsonObject, String name, java.lang.reflect.Field field, Set<Class<?>> callStack) {
        var genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType pt)) return;

        var itemType = pt.getActualTypeArguments()[0];
        if (!(itemType instanceof Class<?> itemClass)) return;

        var jsonArray = new com.google.gson.JsonArray();
        var nestedObject = createDefaultJson(itemClass, callStack);
        if (nestedObject != null) jsonArray.add(nestedObject);

        jsonObject.add(name, jsonArray);
    }

    /**
     * 객체 타입 필드에 기본값 객체 생성 후 추가
     */
    private static void addNestedObject(JsonObject jsonObject, String name, Class<?> type, Set<Class<?>> callStack) {
        var nestedObject = createDefaultJson(type, callStack);
        if (nestedObject != null) jsonObject.add(name, nestedObject);
    }
}
