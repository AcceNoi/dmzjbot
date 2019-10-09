package org.accen.dmzj.core.handler.cmd;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.accen.dmzj.core.task.GeneralTask;
import org.accen.dmzj.util.CQUtil;
import org.accen.dmzj.web.dao.CfgQuickReplyMapper;
import org.accen.dmzj.web.vo.CfgQuickReply;
import org.accen.dmzj.web.vo.Qmessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FuzzyMsgAddCmd implements CmdAdapter {
	@Autowired
	private CfgQuickReplyMapper cfgQuickReplyMapper;

	@Override
	public String describe() {
		return "新增一条消息匹配回复";
	}

	@Override
	public String example() {
		return "添加[精确]问什么番？答[回复]吐鲁番";
	}

	@Override
	public GeneralTask cmdAdapt(Qmessage qmessage,String selfQnum) {
		//1.基本信息
		String message = qmessage.getMessage().trim();
		GeneralTask task = new GeneralTask();
		task.setSelfQnum(selfQnum);
		task.setType(qmessage.getMessageType());
		task.setTargetId(qmessage.getGroupId());
		//2.匹配
		//	2.1匹配结果
		boolean isPrecise = false;//是否是精确的
		boolean isNeedReply = false;//是否是需要回复的
		//	2.2开始匹配
		Pattern pattern = Pattern.compile("^添加(精确)?问(.*?)答(回复)?(.*)");
		Matcher matcher = pattern.matcher(message);
		if(matcher.matches()) {
			isPrecise |= matcher.group(1)==null;
			isNeedReply |= matcher.group(3)==null;
			
			String ask = matcher.group(2);
			String reply = matcher.group(4);
			//3.1处理
			
			if(StringUtils.isEmpty(ask)||StringUtils.isEmpty(reply)) {
				task.setMessage(CQUtil.at(qmessage.getUserId())+"添加失败，示例："+example());
			}else {
				CfgQuickReply cfgReply = new CfgQuickReply();
				cfgReply.setMatchType(isPrecise?1:2);
				cfgReply.setPattern(ask);
				cfgReply.setReply(reply);
				
				switch (task.getType()) {
				case "private":
					cfgReply.setApplyType(1);
					break;
				case "group":
					cfgReply.setApplyType(2);
					break;
				case "discuss":
					cfgReply.setApplyType(3);
					break;
				default:
					break;
				}
				
				cfgReply.setApplyTarget(task.getTargetId());
				cfgReply.setNeedAt(isNeedReply?1:2);
				cfgReply.setCreateTime(new Date());
				cfgReply.setCreatUserId(qmessage.getUserId());
				cfgReply.setStatus(1);
				long replyId = cfgQuickReplyMapper.insert(cfgReply);
				
				task.setMessage(CQUtil.at(qmessage.getUserId())+"添加成功！词条编号："+replyId);
			}
			
		}else {
			//3.2未匹配到
			task.setMessage(CQUtil.at(qmessage.getUserId())+"添加失败，示例："+example());
		}
		return task;
	}
}