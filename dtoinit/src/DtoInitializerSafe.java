import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * BeanInfo를 활용하여 DTO 객체를 안전하게 기본값으로 초기화하는 유틸리티 클래스
 *
 * 특징:
 * 1. setAccessible 사용하지 않음 → Fortify 안전
 * 2. 순환 참조 방지 (HashSet 인자로 전달)
 * 3. 기본 타입 및 리스트 필드 자동 초기화 (항상 1개 생성)
 */
public class DtoInitializerSafe {

    /** DTO 객체를 기본값으로 초기화 */
    public static <T> T init(Class<T> clazz) {
        return init(clazz, new HashSet<>());
    }

    /** 재귀적으로 DTO를 초기화하면서 순환 참조 방지 */
    private static <T> T init(Class<T> clazz, Set<Class<?>> callStack) {
        if (callStack.contains(clazz)) {
            System.out.println("⚠️ 순환 참조 감지: " + clazz.getSimpleName());
            return null;
        }

        try {
            callStack.add(clazz);
            T instance = clazz.getConstructor().newInstance();

            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method setter = pd.getWriteMethod();
                Method getter = pd.getReadMethod();
                if (setter == null) continue;

                Class<?> type = pd.getPropertyType();

                // 기본 타입
                if (type == String.class) setter.invoke(instance, "");
                else if (type == Integer.class || type == int.class) setter.invoke(instance, 0);
                else if (type == Double.class || type == double.class) setter.invoke(instance, 0.0);
                else if (type == Boolean.class || type == boolean.class) setter.invoke(instance, false);

                    // 리스트 타입
                else if (List.class.isAssignableFrom(type)) {
                    setter.invoke(instance, initList(getter, callStack));
                }

                // 중첩 DTO
                else {
                    Object child = init(type, callStack);
                    if (child != null) setter.invoke(instance, child);
                }
            }
            return instance;

        } catch (Exception e) {
            throw new RuntimeException("DTO 초기화 실패: " + clazz.getSimpleName(), e);
        } finally {
            callStack.remove(clazz);
        }
    }

    /** 리스트 타입 필드를 기본값으로 초기화 (항상 1개만 생성) */
    @SuppressWarnings("unchecked")
    private static <T> List<T> initList(Method getter, Set<Class<?>> callStack) throws Exception {
        Type returnType = getter.getGenericReturnType();

        if (!(returnType instanceof ParameterizedType pt)) {
            return Collections.emptyList();
        }

        Type itemType = pt.getActualTypeArguments()[0];
        if (!(itemType instanceof Class<?> itemClass)) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>();
        T child = (T) init(itemClass, callStack);
        if (child != null) list.add(child);

        return list;
    }
}
