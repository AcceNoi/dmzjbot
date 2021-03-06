package org.accen.dmzj.core.api.pixivc;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.Response;
/**
 * pixivc的配置
 * @author <a href="1339liu@gmail.com">Accen</a>
 *
 */
import feign.codec.ErrorDecoder;
@Configuration
@EnableConfigurationProperties(PixivcAuthConfigurationProperties.class)
public class PixivcAuthConfiguration {
	private PixivcAuthConfigurationProperties prop;
	public PixivcAuthConfiguration(PixivcAuthConfigurationProperties prop) {
		this.prop = prop;
	}
	
	@Bean
	public RequestInterceptor pixivcAuthFeignRequestInterceptor() {
		return new PixivcAuthFeignRequestInterceptor(this.prop);
	}
//	/**
//	 * 处理401
//	 * @return
//	 */
//	@Bean
//	public ErrorDecoder UnauthorizedErrorDecoder() {
//		return (methodKey, response)->{
//			
//			return null;
//			
//		};
//	}
}
