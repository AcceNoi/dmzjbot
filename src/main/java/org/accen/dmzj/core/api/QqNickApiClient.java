package org.accen.dmzj.core.api;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 获取qq昵称的api，返回值如{
    "ret": 0,
    "nick": "%E3%82%AF%E3%83%AD%E3%83%8E%E3%82%B9",
    "provide_uin": "1339633536"
}，注意nick为urlencode后的值
 * @author <a href="1339liu@gmail.com">Accen</a>
 *
 */
@FeignClient(
		name="qq-api",
		url="https://api.unipay.qq.com")
public interface QqNickApiClient {
	@GetMapping("/v1/r/1450000186/wechat_query?cmd=1&pf=mds_storeopen_qb-__mds_qqclub_tab_-html5&pfkey=pfkey&from_h5=1&from_https=1&openid=openid&openkey=openkey&session_id=hy_gameid&session_type=st_dummy&qq_appid=&offerId=1450000186&sandbox=")
	public Map<String,Object> qqNick(@RequestParam("provide_uin")String qq);
}
