package org.accen.dmzj.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.accen.dmzj.core.handler.group.Default;

/**
 * 配置在Cmd上面，标识其对应的功能点
 * @author <a href="1339liu@gmail.com">Accen</a>
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FuncSwitch {
	/**
	 * 功能key,默认cmd_类名
	 * @return
	 */
	String name() default "";
	/**
	 * 是否要在菜单中展示出来
	 * @return
	 */
	boolean showMenu() default false;
	/**
	 * 功能名
	 * @return
	 */
	String title();
	/**
	 * 排序
	 * @return
	 */
	int order() default 99;
	/**
	 * 格式
	 * @return
	 */
	String format() default "";
	/**
	 * 所属分组
	 * @return
	 */
	Class<?> groupClass() default Default.class;
}
