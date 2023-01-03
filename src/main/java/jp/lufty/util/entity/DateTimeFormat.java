package jp.lufty.util.entity;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Target;

@Target(FIELD)
public @interface DateTimeFormat {
	String value();
}
