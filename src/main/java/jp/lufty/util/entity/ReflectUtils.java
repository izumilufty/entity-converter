package jp.lufty.util.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("java:S3011") // SonarLintのリフレクションに対する警告を抑制する。
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String fieldName) {

        Objects.requireNonNull(target);
        Objects.requireNonNull(fieldName);

        Object result = null;
        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = field.get(target);

        } catch (Exception e) {
            return null;
        }
        return (T) result;
    }

    public static void setFieldValue(Object target, String fieldName, Object value) {

        Objects.requireNonNull(target);
        Objects.requireNonNull(fieldName);

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
        }
    }

    public static <T> T newInstance(Class<T> clazz)
            throws NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Objects.requireNonNull(clazz);

        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public static Class<?> getGenericClass(Field field) {

        Type genericType = getGenericType(field);
        Class<?> genericClass;
        try {
            genericClass = Class.forName(genericType.getTypeName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        return genericClass;
    }

    private static Type getGenericType(Field field) {

        ParameterizedType paramType = (ParameterizedType) field.getGenericType();

        return paramType.getActualTypeArguments()[0];
//		for (Type type : paramType.getActualTypeArguments()) {
//			result = type;
//			break;
//		}
//
//		return result;
    }

    public static Set<Field> getAllFields(Class<?> clazz, String... excludeNames) {

        Set<Field> fields = new HashSet<>();
        Set<String> excludeNameSet = Sets.newHashSet(excludeNames); // TODO おためし

//		for (String excludeName : excludeNames) {
//			excludeNameSet.add(excludeName);
//		}

        for (Field field : clazz.getDeclaredFields()) {
            if (!excludeNameSet.contains(field.getName())) {
                fields.add(field);
            }
        }

        Class<?> superClass = clazz.getSuperclass();

        if (superClass != null) {
            fields.addAll(getAllFields(superClass, excludeNames));
        }

        return fields;
    }
}
