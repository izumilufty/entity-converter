package jp.lufty.util.entity;

import java.util.Map;
import java.util.Set;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API実行結果のエンティティ抽象クラス
 * 
 * @author izumi
 */
@EqualsAndHashCode
@ToString
public abstract class Entity {

    /**
     * 未定義エントリ<br>
     * ソースデータに存在しているが、エンティティに定義されていないエントリ
     * <p>
     * このフィールドにデータが存在する場合は、エンティティの定義を拡張する必要がある。
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Map<String, Object> undefEntries_ = null; // NOSONAR エンティティのプロパティとは明確に区別するため_を使用している。

    /**
     * 無効フィールド<br>
     * エンティティに存在しているが、ソースデータに存在しなかったフィールド名
     * <p>
     * WEBAPIは取得時の条件によってフィールドがあったりなかったりするので、このフィールドにデータが存在するからと言って問題があるわけではない 。
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Set<String> invalidFields_ = null; // NOSONAR エンティティのプロパティとは明確に区別するため_を使用している。
}
