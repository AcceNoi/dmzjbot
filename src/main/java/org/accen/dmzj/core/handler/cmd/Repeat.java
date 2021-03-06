package org.accen.dmzj.core.handler.cmd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.accen.dmzj.core.annotation.FuncSwitch;
import org.accen.dmzj.core.task.GeneralTask;
import org.accen.dmzj.util.CQUtil;
import org.accen.dmzj.web.vo.Qmessage;
import org.springframework.stereotype.Component;
@FuncSwitch(title = "复读")
@Component
public class Repeat implements CmdAdapter {

	private final static Pattern pattern = Pattern.compile("^老婆说(/|\\$|%)?(.+)");
	
	@Override
	public GeneralTask cmdAdapt(Qmessage qmessage, String selfQnum) {
		String message = qmessage.getMessage().trim();
		Matcher matcher = pattern.matcher(message);
		if(matcher.matches()) {
			GeneralTask task =  new GeneralTask();
			
			task.setSelfQnum(selfQnum);
			task.setType(qmessage.getMessageType());
			task.setTargetId(qmessage.getGroupId());
			String sign = matcher.group(1);
			if("/".equals(sign)) {
				//如果为/，则识别为一个语音网络地址
				task.setMessage(CQUtil.recordUrl(matcher.group(2)));
			}else {
				task.setMessage(matcher.group(2).replaceAll("我", "##").replaceAll("你", "我").replaceAll("##", "你"));
			}
			
			return task;
		}
		return null;
	}

}
