package jp.lufty.util.entity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * エンティティの変換クラス。
 * 
 * @author izumi
 */
@Slf4j
public final class EntityMapper {

    // 時刻フォーマッタ（変更可能）
    private final DateTimeFormatter dateTimeFormatter;
    private final boolean isAllBigDecimal;
    private final ObjectMapper mapper;

    protected EntityMapper(String dateTimeFormat, Locale locale, boolean isAllBigDecimal) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat, locale);
        this.isAllBigDecimal = isAllBigDecimal;

        this.mapper = new ObjectMapper();
        if (this.isAllBigDecimal) {
            this.mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        }
    }

    /**
     * エンティティの生成
     * 
     * @param jsonStr     エンティティの元となるJson文字列
     * @param entityClass 生成されるエンティティの実装クラス
     * @return 生成されたエンティティ
     * @throws ResponseParseException エンティティのパースに失敗した場合
     */
    public final <T extends Entity, J> T convert(String jsonStr, Class<T> entityClass)
            throws EntityParseException {

        if (jsonStr.isEmpty()) {
            return null;
        }

        Object resultMap;
        try {
            resultMap = this.mapper.readValue(jsonStr, new TypeReference<J>() {
            });
        } catch (Exception e) {
            throw new EntityParseException(e);
        }

        if (resultMap instanceof Map) {
            return convert((Map<String, Object>) resultMap, entityClass);
        } else {
            return (T) resultMap;
        }

    }

    /**
     * Listからエンティティに変換する。
     * 
     * @param <T>         エンティティの型
     * @param list        変換元のリスト
     * @param entityClass 変換後のエンティティタイプ
     * @return
     * @throws EntityParseException
     */
    @SuppressWarnings("unchecked")
    public final <T extends Entity> List<T> convert(List<Object> list, Class<T> entityClass)
            throws EntityParseException {

        if (list == null) {
            return null; // NOSONAR nullに意味がある
        }

        List<T> result = new ArrayList<>();

        for (Object map : list) {
            result.add(convert((LinkedHashMap<String, Object>) map, entityClass));
        }

        return result;
    }

    /**
     * JsonからパースされたMapからエンティティをパースする。
     * 
     * @param jsonMap     JsonからパースされたMap
     * @param entityClass エンティティのクラス
     * @return エンティティ
     * @throws ResponseParseException エンティティのパースに失敗した場合
     */
    @SuppressWarnings({ "unchecked", "java:S3011" }) // SonarLintのリフレクションに対する警告を抑制する。
    public final <T extends Entity> T convert(Map<String, Object> jsonMap, Class<T> entityClass)
            throws EntityParseException {

        if (jsonMap == null) {
            return null;
        }

        T entity = null;

        try {
            // 生成
            entity = ReflectUtils.newInstance(entityClass);

            // フィールドに値を設定
            Set<Field> fields = ReflectUtils.getAllFields(entityClass, "invalidFields_",
                    "undefEntries_");

            // 未定義フィールドチェック用
            Set<String> undefFields = new HashSet<>(jsonMap.keySet());

            // 無効フィールドチェック用
            Set<String> invalidFields = new HashSet<>();

            for (Field field : fields) {
                field.setAccessible(true);

                String entityFieldName = field.getName();

                // 予約語対応のため、サフィックスに'_'を付けているフィールド対応
                if (entityFieldName.endsWith("_")) {
                    entityFieldName = entityFieldName.substring(0, entityFieldName.length() - 1);
                }

                String jsonFieldName = null;

                // JSONのフィールドではキャメルのものも存在することを考慮
                if (jsonMap.containsKey(entityFieldName)) {
                    jsonFieldName = entityFieldName;
                } else {
                    jsonFieldName = EntityUtils.camelToSnake(entityFieldName);
                }

                if (jsonMap.containsKey(jsonFieldName)) {
                    undefFields.remove(jsonFieldName);

                    Object value = jsonMap.get(jsonFieldName);

                    Class<?> type = field.getType();

                    try {
                        // エンティティかどうかのチェック
                        if (Entity.class.isAssignableFrom(type)) {

                            field.set(entity, convert((LinkedHashMap<String, Object>) value,
                                    (Class<T>) field.getType()));
                        } else {

                            if (type.equals(Long.class)) {
                                value = ParseUtils.parseLong(value);
                            } else if (type.equals(Integer.class)) {
                                value = ParseUtils.parseInteger(value);
                            } else if (type.equals(String.class)) {

                                if (value instanceof Collection || value instanceof Map) {
                                    log.warn(
                                            "[{}] was converted String. entity={}, name={}, value={}",
                                            value.getClass().getName(), entityClass.getName(),
                                            entityFieldName, value);
                                }

                                value = String.valueOf(value);

                            } else if (type.equals(Boolean.class)) {
                                value = ParseUtils.parseBoolean(value);
                            } else if (type.equals(LocalDateTime.class)) {
                                value = parseDateTime(value);
                            } else if (type.equals(BigDecimal.class)) {
                                value = ParseUtils.parseBigDecimal(value);
                            } else if (List.class.isAssignableFrom(type)) {

                                // ジェネリクスのクラスを取得
                                Class<?> genericClass = ReflectUtils.getGenericClass(field);

                                // リストのジェネリックタイプがEntityだった場合
                                if (Entity.class.isAssignableFrom(genericClass)) {
                                    value = convert((List<Object>) value, (Class<T>) genericClass);
                                }
                            }

                            field.set(entity, value);
                        }
                    } catch (ClassCastException e) {
                        log.warn(
                                "Entityのパースで例外が発生しました。 name={}, expectedType={}, actualType={} actualValue={}, message={}",
                                entityFieldName, type.getName(), value.getClass().getName(), value,
                                e.getMessage());
                    }
                } else {
                    invalidFields.add(jsonFieldName);
                }
            }

            // 取得漏れの値を別枠に設定
            Map<String, Object> undefEntries = new LinkedHashMap<>();
            if (!undefFields.isEmpty()) {
                undefFields.forEach(name -> {
                    Object value = jsonMap.get(name);
                    undefEntries.put(name, value);
                });
            }
            ((Entity) entity).setUndefEntries_(undefEntries);

            ((Entity) entity).setInvalidFields_(invalidFields);

        } catch (Exception e) {
            throw new EntityParseException(e);
        }
        return entity;
    }

    /**
     * 日付時刻のパース
     * 
     * @param data 日付時刻を表すデータ
     * @return パースされた日付時刻
     */
    public LocalDateTime parseDateTime(Object data) {

        LocalDateTime result = null;

        if (data instanceof String) {
            result = LocalDateTime.from(dateTimeFormatter.parse((String) data));
        } else if (data instanceof Long) {
            result = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) data),
                    ZoneId.systemDefault());
        } else if (data instanceof Date) {
            result = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) data).getTime()),
                    ZoneId.systemDefault());
        }

        return result;
    }

    public static class Builder {

        private String dateTimeFormat = "EEE MMM dd HH:mm:ss Z yyyy";
        private Locale locale = Locale.ENGLISH;
        private boolean isAllBigDecimal = true;

        public Builder setAllBigDecimal(boolean isAllBigDecimal) {
            this.isAllBigDecimal = isAllBigDecimal;
            return this;
        }

        public Builder setDateTimeFroamt(String dateTimeFormat, Locale locale) {
            this.dateTimeFormat = dateTimeFormat;
            this.locale = locale;
            return this;
        }

        public EntityMapper build() {
            return new EntityMapper(dateTimeFormat, locale, isAllBigDecimal);
        }
    }

}
